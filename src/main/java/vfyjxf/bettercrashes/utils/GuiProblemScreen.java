/*
 *This file is modified based on
 *https://github.com/DimensionalDevelopment/VanillaFix/blob/99cb47cc05b4790e8ef02bbcac932b21dafa107f/src/main/java/org/dimdev/vanillafix/crashes/GuiProblemScreen.java
 *The source file uses the MIT License.
 */

package vfyjxf.bettercrashes.utils;

import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.crash.CrashReport;
import org.apache.commons.lang3.StringUtils;

@SideOnly(Side.CLIENT)
public abstract class GuiProblemScreen extends GuiScreen {

    protected final CrashReport report;
    private String hasteLink = null;
    private String modListString;

    public GuiProblemScreen(CrashReport report) {
        this.report = report;
    }

    @Override
    public void initGui() {
        mc.setIngameNotInFocus();
        buttonList.clear();
        buttonList.add(new GuiButton(
                1, width / 2 - 50, height / 4 + 120 + 12, 110, 20, I18n.format("bettercrashes.gui.openCrashReport")));
        buttonList.add(new GuiButton(
                2,
                width / 2 - 50 + 115,
                height / 4 + 120 + 12,
                110,
                20,
                I18n.format("bettercrashes.gui.uploadReportAndCopyLink")));
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == 1) {
            try {
                CrashUtils.openCrashReport(report);
            } catch (IOException e) {
                button.displayString = I18n.format("bettercrashes.gui.failed");
                button.enabled = false;
                e.printStackTrace();
            }
        }
        if (button.id == 2) {
            if (hasteLink == null) {
                try {
                    hasteLink = CrashReportUpload.uploadToUbuntuPastebin(
                            "https://paste.ubuntu.com", report.getCompleteReport());
                } catch (IOException e) {
                    button.displayString = I18n.format("bettercrashes.gui.failed");
                    button.enabled = false;
                    e.printStackTrace();
                }
            }
            setClipboardString(hasteLink);
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {}

    protected String getModListString() {
        if (modListString == null) {
            final Set<ModContainer> suspectedMods = ((IPatchedCrashReport) report).getSuspectedMods();
            if (suspectedMods == null) {
                return modListString = I18n.format("bettercrashes.crashscreen.identificationErrored");
            }
            List<String> modNames = new ArrayList<>();
            for (ModContainer mod : suspectedMods) {
                modNames.add(mod.getName());
            }
            if (modNames.isEmpty()) {
                modListString = I18n.format("bettercrashes.crashscreen.unknownCause");
            } else {
                modListString = StringUtils.join(modNames, ", ");
            }
        }
        return modListString;
    }
}
