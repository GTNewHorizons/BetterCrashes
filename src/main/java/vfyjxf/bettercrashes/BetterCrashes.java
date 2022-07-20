package vfyjxf.bettercrashes;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(
        modid = BetterCrashes.MODID,
        version = BetterCrashes.VERSION,
        name = BetterCrashes.NAME,
        dependencies = BetterCrashes.DEPENDENCIES)
public class BetterCrashes {

    public static final String MODID = "GRADLETOKEN_MODID";
    public static final String NAME = "GRADLETOKEN_MODNAME";
    public static final String VERSION = "GRADLETOKEN_VERSION";
    public static final String DEPENDENCIES = "";
    public static final Logger logger = LogManager.getLogger(NAME);

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        BetterCrashesConfig.init(event.getSuggestedConfigurationFile());
    }
}
