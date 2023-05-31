package vfyjxf.bettercrashes;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import net.minecraftforge.common.config.Configuration;

public class BetterCrashesConfig {

    public static Configuration config;

    public static final String GENERAL = "General";

    public static int crashLogLimitClient = 30;
    public static int crashLogLimitServer = 30;
    public static String crashLogPasteService;
    public static String issueTrackerURL;
    public static List<String> unsupportedMods;

    public static void init(File file) {
        config = new Configuration(file);
        syncConfig();
    }

    public static void syncConfig() {
        config.setCategoryComment(GENERAL, "General config");

        crashLogLimitClient = config.getInt(
                "crashLogLimitClient",
                GENERAL,
                30,
                0,
                Integer.MAX_VALUE,
                "Maximum number of crash logs generated per restart for client. Suppresses too many logs generated by continuous crashes.");
        crashLogLimitServer = config.getInt(
                "crashLogLimitServer",
                GENERAL,
                30,
                0,
                Integer.MAX_VALUE,
                "Maximum number of crash logs generated per restart for server. Suppresses too many logs generated by continuous crashes.");
        crashLogPasteService = config.getString(
                "crashLogPasteService",
                GENERAL,
                "mclo.gs",
                "Service to use for uploading crashlogs. Currently, only mclo.gs is currently supported.");
        issueTrackerURL = config.getString("issueTrackerURL", GENERAL, "", "Link to a bug tracker.");
        unsupportedMods = Arrays.asList(
                config.getStringList(
                        "unsupportedMods",
                        GENERAL,
                        new String[] { "Optifine" },
                        "List of modids of mods that are not supported by the modpack. BetterCrashes will encourage the player to mention those specifically in their bug report."));

        if (config.hasChanged()) {
            config.save();
        }
    }
}
