/*
 *This file is modified based on
 *https://github.com/DimensionalDevelopment/VanillaFix/blob/99cb47cc05b4790e8ef02bbcac932b21dafa107f/src/main/java/org/dimdev/vanillafix/crashes/GuiCrashScreen.java
 *The source file uses the MIT License.
 */

package vfyjxf.bettercrashes.utils;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiOptionButton;
import net.minecraft.client.resources.I18n;
import net.minecraft.crash.CrashReport;

@SideOnly(Side.CLIENT)
public class GuiCrashScreen extends GuiProblemScreen {

    public GuiCrashScreen(CrashReport report) {
        super(report);
    }

    @Override
    public void initGui() {
        super.initGui();
        GuiOptionButton mainMenuButton = new GuiOptionButton(
                0,
                width / 2 - 50 - 115,
                height / 4 + 120 + 12,
                110,
                20,
                I18n.format("bettercrashes.gui.crash.toTitle"));
        buttonList.add(mainMenuButton);
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        super.actionPerformed(button);
        if (button.id == 0) {
            mc.displayGuiScreen(new GuiMainMenu());
        }
    }

    @Override
    protected String getScreenTitle() {
        return I18n.format("bettercrashes.gui.crash.title");
    }

    @Override
    protected String getScreenSummary() {
        return I18n.format("bettercrashes.gui.crash.summary");
    }
}
