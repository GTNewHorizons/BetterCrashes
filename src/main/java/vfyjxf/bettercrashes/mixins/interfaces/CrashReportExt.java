/*
 * This file is from
 * https://github.com/DimensionalDevelopment/VanillaFix/blob/99cb47cc05b4790e8ef02bbcac932b21dafa107f/src/main/java/org/
 * dimdev/vanillafix/crashes/IPatchedCrashReport.java The source file uses the MIT License.
 */

package vfyjxf.bettercrashes.mixins.interfaces;

import java.util.Set;

import cpw.mods.fml.common.ModContainer;

/**
 * @author Runemoro
 */
public interface CrashReportExt {

    Set<ModContainer> betterCrashes$getSuspectedMods();
}
