package vfyjxf.bettercrashes;

import java.io.File;
import net.minecraftforge.common.config.Configuration;

public class BetterCrashesConfig {

    public static Configuration config;

    public static final String GENERAL = "General";

    public static boolean isGTNH = true;

    public static void init(File file) {
        config = new Configuration(file);
        syncConfig();
    }

    public static void syncConfig() {
        config.setCategoryComment(GENERAL, "General config");

        isGTNH = config.get(GENERAL, "isGTNH", true, "Set to false if you're playing outside of GTNH")
                .getBoolean();

        if (config.hasChanged()) {
            config.save();
        }
    }
}
