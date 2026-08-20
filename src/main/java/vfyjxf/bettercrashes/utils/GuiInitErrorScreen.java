/*
 * This file is modified based on
 * https://github.com/DimensionalDevelopment/VanillaFix/blob/99cb47cc05b4790e8ef02bbcac932b21dafa107f/src/main/java/org/
 * dimdev/vanillafix/crashes/GuiInitErrorScreen.java The source file uses the MIT License.
 */

package vfyjxf.bettercrashes.utils;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.I18n;
import net.minecraft.crash.CrashReport;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiInitErrorScreen extends GuiProblemScreen {

    public GuiInitErrorScreen(CrashReport report) {
        super(report);
    }

    @Override
    public void initGui() {
        mc.setIngameNotInFocus();
        buttonList.clear();
        int buttonY = getButtonY();
        buttonList.add(
                new GuiButton(
                        1,
                        width / 2 - 155,
                        buttonY,
                        150,
                        20,
                        I18n.format("bettercrashes.gui.common.openCrashReport")));
        buttonList.add(
                new GuiButton(
                        2,
                        width / 2 - 155 + 160,
                        buttonY,
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
