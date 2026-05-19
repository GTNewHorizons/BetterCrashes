package vfyjxf.bettercrashes;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.relauncher.Side;

@Mod(modid = BetterCrashes.MODID, version = Tags.VERSION, name = BetterCrashes.NAME)
public class BetterCrashes {

    public static final String MODID = "bettercrashes";
    public static final String NAME = "BetterCrashes";
    public static final Logger logger = LogManager.getLogger(NAME);

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        if (event.getSide() == Side.CLIENT) {
            BetterCrashesConfig.init(event.getSuggestedConfigurationFile());
        } else {
            FMLLog.info(NAME + " has been installed on a server, it will do nothing here, it can be removed!");
        }
    }
}
