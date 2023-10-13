/*
 * This file is from
 * https://github.com/DimensionalDevelopment/VanillaFix/blob/master/src/main/java/org/dimdev/vanillafix/crashes/mixins/
 * MixinCrashReport.java The source file uses the MIT License.
 */

package vfyjxf.bettercrashes.mixins.early;

import static vfyjxf.bettercrashes.BetterCrashes.MODID;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import cpw.mods.fml.common.ModContainer;
import vfyjxf.bettercrashes.BetterCrashesConfig;
import vfyjxf.bettercrashes.utils.IPatchedCrashReport;
import vfyjxf.bettercrashes.utils.ModIdentifier;
import vfyjxf.bettercrashes.utils.StacktraceDeobfuscator;

@Mixin(value = CrashReport.class, priority = 500)
public class CrashReportMixin implements IPatchedCrashReport {

    @Shadow
    @Final
    private CrashReportCategory theReportCategory;

    @Shadow
    @Final
    private Throwable cause;

    @Shadow
    @Final
    private List<CrashReportCategory> crashReportSections;

    @Shadow
    @Final
    private String description;

    @Shadow
    private static String getWittyComment() {
        return null;
    }

    @Unique
    private Set<ModContainer> betterCrashes$suspectedMods;

    @Override
    public Set<ModContainer> betterCrashes$getSuspectedMods() {
        return betterCrashes$suspectedMods;
    }

    /** @reason Deobfuscates the stacktrace using MCP mappings */
    @Inject(method = "populateEnvironment", at = @At("HEAD"))
    private void betterCrashes$beforePopulateEnvironment(CallbackInfo ci) {
        if (BetterCrashesConfig.stacktraceDeobfuscation) {
            StacktraceDeobfuscator.init(new File(String.format("%s-stackdeobfuscator-methods.csv", MODID)));
        }
        StacktraceDeobfuscator.deobfuscateThrowable(cause);
    }

    /** @reason Adds a list of mods which may have caused the crash to the report. */
    @Inject(method = "populateEnvironment", at = @At("TAIL"))
    private void betterCrashes$afterPopulateEnvironment(CallbackInfo ci) {
        theReportCategory.addCrashSectionCallable("Suspected Mods", () -> {
            try {
                betterCrashes$suspectedMods = ModIdentifier.identifyFromStacktrace(cause);

                String modListString = "Unknown";
                List<String> modNames = new ArrayList<>();
                for (ModContainer mod : betterCrashes$suspectedMods) {
                    modNames.add(mod.getName() + " (" + mod.getModId() + ")");
                }

                if (!modNames.isEmpty()) {
                    modListString = StringUtils.join(modNames, ", ");
                }
                return modListString;
            } catch (Throwable e) {
                return ExceptionUtils.getStackTrace(e).replace("\t", "    ");
            }
        });
    }

    /**
     * @reason Improve report formatting
     * @author Runemoro
     */
    @Overwrite
    public String getCompleteReport() {
        StringBuilder builder = new StringBuilder();
        builder.append("---- Minecraft Crash Report ----\n").append("// ").append(betterCrashes$getVanillaFixComment())
                .append("\n\n").append("Time: ")
                .append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z").format(new Date())).append("\n")
                .append("Description: ").append(description).append("\n\n")
                .append(betterCrashes$stacktraceToString(cause).replace("\t", "    ")) // Vanilla's
                                                                                       // getCauseStackTraceOrString
                                                                                       // doesn't
                // print causes and suppressed
                // exceptions
                .append(
                        "\n\nA detailed walkthrough of the error, its code path and all known details is as follows:\n");

        for (int i = 0; i < 87; i++) {
            builder.append("-");
        }

        builder.append("\n\n");
        getSectionsInStringBuilder(builder);
        return builder.toString().replace("\t", "    ");
    }

    @Unique
    private static String betterCrashes$stacktraceToString(Throwable cause) {
        StringWriter writer = new StringWriter();
        cause.printStackTrace(new PrintWriter(writer));
        return writer.toString();
    }

    /**
     * @reason Improve report formatting, add VanillaFix comment
     * @author Runemoro
     */
    @Overwrite
    public void getSectionsInStringBuilder(StringBuilder builder) {
        for (CrashReportCategory crashreportcategory : crashReportSections) {
            crashreportcategory.appendToStringBuilder(builder);
            builder.append("\n");
        }
        theReportCategory.appendToStringBuilder(builder);
    }

    @Unique
    private String betterCrashes$getVanillaFixComment() {
        try {
            if (Math.random() < 0.01 && !betterCrashes$suspectedMods.isEmpty()) {
                ModContainer mod = betterCrashes$suspectedMods.iterator().next();
                String author = mod.getMetadata().authorList.get(0);
                return "I blame " + author + ".";
            }
        } catch (Throwable ignored) {}
        return getWittyComment();
    }
}
