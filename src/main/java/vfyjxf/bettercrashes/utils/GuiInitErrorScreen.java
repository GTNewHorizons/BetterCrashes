/*
 *This file is modified based on
 *https://github.com/DimensionalDevelopment/VanillaFix/blob/99cb47cc05b4790e8ef02bbcac932b21dafa107f/src/main/java/org/dimdev/vanillafix/crashes/GuiInitErrorScreen.java
 *The source file uses the MIT License.
 */

package vfyjxf.bettercrashes.utils;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.I18n;
import net.minecraft.crash.CrashReport;

@SideOnly(Side.CLIENT)
public class GuiInitErrorScreen extends GuiProblemScreen {

    public GuiInitErrorScreen(CrashReport report) {
        super(report);
    }

    @Override
    public void initGui() {
        mc.setIngameNotInFocus();
        buttonList.clear();
        buttonList.add(new GuiButton(
                1,
                width / 2 - 155,
                height / 4 + 120 + 12,
                150,
                20,
                I18n.format("bettercrashes.gui.common.openCrashReport")));
        buttonList.add(new GuiButton(
                2,
                width / 2 - 155 + 160,
                height / 4 + 120 + 12,
                150,
                20,
                I18n.format("bettercrashes.gui.common.uploadReportAndCopyLink")));
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        super.actionPerformed(button);
    }

    @Override
    protected String getScreenTitle() {
        return I18n.format("bettercrashes.gui.init_error.title");
    }

    @Override
    protected String getScreenSummary() {
        return I18n.format("bettercrashes.gui.init_error.summary");
    }
}
