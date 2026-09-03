/*
 * This file is modified based on
 * https://github.com/DimensionalDevelopment/VanillaFix/blob/99cb47cc05b4790e8ef02bbcac932b21dafa107f/src/main/java/org/
 * dimdev/vanillafix/crashes/CrashUtils.java The source file uses the MIT License.
 */

package vfyjxf.bettercrashes.utils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import net.minecraft.client.Minecraft;
import net.minecraft.crash.CrashReport;

import org.lwjgl.Sys;

import vfyjxf.bettercrashes.BetterCrashes;

/**
 * @author Runemoro
 */
public class CrashUtils {

    /**
     * @param report
     * @author Runemoro
     */
    public static void outputReport(CrashReport report) {
        try {
            if (report.getFile() == null) {
                String reportName = "crash-";
                reportName += new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss").format(new Date());
                reportName += Minecraft.getMinecraft().func_152345_ab() ? "-client" : "-server";
                reportName += ".txt";

                File reportsDir = isClient() ? new File(Minecraft.getMinecraft().mcDataDir, "crash-reports")
                        : new File("crash-reports");
                File reportFile = new File(reportsDir, reportName);

                report.saveToFile(reportFile);
            }
        } catch (Throwable e) {
            BetterCrashes.logger.fatal("Failed saving report", e);
        }

        BetterCrashes.logger.fatal(
                "Minecraft ran into a problem! " + (report.getFile() != null ? "Report saved to: " + report.getFile()
                        : "Crash report could not be saved.") + "\n" + report.getCompleteReport());
    }

    /**
     * @author Runemoro
     */
    private static boolean isClient() {
        try {
            return Minecraft.getMinecraft() != null;
        } catch (NoClassDefFoundError e) {
            return false;
        }
    }

    /**
     * @author vfyjxf
     */
    public static boolean openCrashReport(CrashReport crashReport) {
        File report = crashReport.getFile();
        if (report == null || !report.exists()) {
            BetterCrashes.logger.error("Crash report was not generated");
            return false;
        }

        boolean opened = Sys.openURL(report.toURI().toString());
        if (!opened) {
            BetterCrashes.logger.error("Failed to open crash report: {}", report);
        }
        return opened;
    }

    public static boolean openBrowser(String url) {
        boolean opened = Sys.openURL(url);
        if (!opened) {
            BetterCrashes.logger.error("Failed to open URL: {}", url);
        }
        return opened;
    }
}
