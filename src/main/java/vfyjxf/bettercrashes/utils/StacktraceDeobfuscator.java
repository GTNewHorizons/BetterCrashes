/*
 * This file is from
 * https://github.com/DimensionalDevelopment/VanillaFix/blob/master/src/main/java/org/dimdev/vanillafix/crashes/
 * StacktraceDeobfuscator.java The source file uses the MIT License.
 */

package vfyjxf.bettercrashes.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;

import vfyjxf.bettercrashes.BetterCrashes;

/**
 * @author Runemoro
 */
public final class StacktraceDeobfuscator {

    private static final String MAPPING_URL = "https://maven.minecraftforge.net/de/oceanlabs/mcp/mcp_stable/12-1.7.10/mcp_stable-12-1.7.10.zip";
    private static final String MAPPING_METHODS_CSV_HASH = "ba91c6bcd9a76ba4400729e0b3232daf0d4c47abed708c758318927540a33121"; // sha256

    private static final boolean DEBUG_IN_DEV = false; // Makes this MCP -> SRG for testing in dev. Don't forget to set
                                                       // to false when done!
    private static HashMap<String, String> srgMcpMethodMap = null;

    /**
     * If the file does not exist, downloads method mappings and saves them to it. Initializes a HashMap between
     * obfuscated and deobfuscated names from that file.
     */
    public static void init(File mappings) {
        if (srgMcpMethodMap != null) return;

        // Download the file if necessary
        if (!mappings.exists()) {
            BetterCrashes.logger.info("Downloading MCP method mappings to deobfuscate stacktrace");
            HttpURLConnection connection = null;
            try {
                URL mappingsURL = new URL(MAPPING_URL);
                connection = HttpUtils.createConnection(mappingsURL);
                connection.setDoInput(true);
                connection.connect();
                try (InputStream inputStream = connection.getInputStream()) {
                    ZipInputStream zipInputStream = new ZipInputStream(inputStream);
                    ZipEntry entry;
                    while ((entry = zipInputStream.getNextEntry()) != null) {
                        if (entry.getName().equals("methods.csv")) {
                            boolean downloadInvalid = false;

                            try (FileOutputStream out = new FileOutputStream(mappings)) {
                                Hasher hasher = Hashing.sha256().newHasher();
                                byte[] buffer = new byte[2048];
                                int len;
                                while ((len = zipInputStream.read(buffer)) > 0) {
                                    out.write(buffer, 0, len);
                                    hasher.putBytes(buffer, 0, len);
                                }

                                if (!hasher.hash().toString().equals(MAPPING_METHODS_CSV_HASH)) {
                                    downloadInvalid = true;
                                    BetterCrashes.logger.warn(
                                            "Downloaded MCP mapping method.csv does not match expected hash. Skipping deobfuscation...");
                                }
                            }

                            if (downloadInvalid) {
                                mappings.delete();
                                return;
                            }
                            break;
                        }
                    }
                    if (entry == null) {
                        BetterCrashes.logger.warn(
                                "Downloaded MCP mappings zip did not contain methods.csv. Skipping deobfuscation...");
                        return;
                    }
                }
            } catch (IOException e) {
                BetterCrashes.logger.warn("Failed downloading MCP mappings. Skipping deobfuscation...");
                return;
            } finally {
                if (connection != null) connection.disconnect();
            }
        }

        // Read the mapping
        HashMap<String, String> srgMcpMethodMap = new HashMap<>();
        try (Scanner scanner = new Scanner(mappings)) {
            scanner.nextLine(); // Skip CSV header
            while (scanner.hasNext()) {
                String mappingLine = scanner.nextLine();
                int commaIndex = mappingLine.indexOf(',');
                String srgName = mappingLine.substring(0, commaIndex);
                String mcpName = mappingLine
                        .substring(commaIndex + 1, commaIndex + 1 + mappingLine.substring(commaIndex + 1).indexOf(','));

                // System.out.println(srgName + " <=> " + mcpName);
                if (!DEBUG_IN_DEV) {
                    srgMcpMethodMap.put(srgName, mcpName);
                } else {
                    srgMcpMethodMap.put(mcpName, srgName);
                }
            }
        } catch (IOException | IndexOutOfBoundsException e) {
            BetterCrashes.logger.warn(
                    "Failed to parse {}. Possible corruption. Please delete to trigger redownload.",
                    mappings.getName());
            return;
        }

        // Set the map only if it's successful, to make sure that it's complete
        StacktraceDeobfuscator.srgMcpMethodMap = srgMcpMethodMap;
    }

    public static void deobfuscateThrowable(Throwable t) {
        Deque<Throwable> queue = new ArrayDeque<>();
        queue.add(t);
        while (!queue.isEmpty()) {
            t = queue.remove();
            t.setStackTrace(deobfuscateStacktrace(t.getStackTrace()));
            if (t.getCause() != null) queue.add(t.getCause());
            Collections.addAll(queue, t.getSuppressed());
        }
    }

    public static StackTraceElement[] deobfuscateStacktrace(StackTraceElement[] stackTrace) {
        int index = 0;
        for (StackTraceElement el : stackTrace) {
            stackTrace[index++] = new StackTraceElement(
                    el.getClassName(),
                    deobfuscateMethodName(el.getMethodName()),
                    el.getFileName(),
                    el.getLineNumber());
        }
        return stackTrace;
    }

    public static String deobfuscateMethodName(String srgName) {
        if (srgMcpMethodMap == null) {
            return srgName; // Not initialized
        }

        String mcpName = srgMcpMethodMap.get(srgName);
        // log.debug(srgName + " <=> " + mcpName != null ? mcpName : "?"); // Can't do this, it would be a recursive
        // call to log appender
        return mcpName != null ? mcpName : srgName;
    }

    public static void main(String[] args) {
        init(new File("methods.csv"));
        for (Map.Entry<String, String> entry : srgMcpMethodMap.entrySet()) {
            System.out.println(entry.getKey() + " <=> " + entry.getValue());
        }
    }
}
