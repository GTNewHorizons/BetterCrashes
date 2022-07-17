package vfyjxf.bettercrashes.mixinplugin;

import static vfyjxf.bettercrashes.mixinplugin.TargetedMod.VANILLA;

import cpw.mods.fml.relauncher.FMLLaunchHandler;
import java.util.Arrays;
import java.util.List;

public enum Mixin {

    //
    // IMPORTANT: Do not make any references to any mod from this file. This file is loaded quite early on and if
    // you refer to other mods you load them as well. The consequence is: You can't inject any previously loaded
    // classes!
    // Exception: Tags.java, as long as it is used for Strings only!
    //

    MinecraftMixin("minecraft.MinecraftMixin", Side.BOTH, VANILLA),
    CrashReportMixin("minecraft.CrashReportMixin", Side.BOTH, VANILLA);

    public final String mixinClass;
    public final List<TargetedMod> targetedMods;
    private final Side side;

    Mixin(String mixinClass, Side side, TargetedMod... targetedMods) {
        this.mixinClass = mixinClass;
        this.targetedMods = Arrays.asList(targetedMods);
        this.side = side;
    }

    Mixin(String mixinClass, TargetedMod... targetedMods) {
        this.mixinClass = mixinClass;
        this.targetedMods = Arrays.asList(targetedMods);
        this.side = Side.BOTH;
    }

    public boolean shouldLoad(List<TargetedMod> loadedMods) {
        return (side == Side.BOTH
                        || side == Side.SERVER && FMLLaunchHandler.side().isServer()
                        || side == Side.CLIENT && FMLLaunchHandler.side().isClient())
                && loadedMods.containsAll(targetedMods);
    }
}

enum Side {
    BOTH,
    CLIENT,
    SERVER;
}
