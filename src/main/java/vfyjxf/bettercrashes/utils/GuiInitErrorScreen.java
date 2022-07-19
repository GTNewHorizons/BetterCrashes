/*
 *This file is modified based on
 *https://github.com/DimensionalDevelopment/VanillaFix/blob/99cb47cc05b4790e8ef02bbcac932b21dafa107f/src/main/java/org/dimdev/vanillafix/crashes/GuiInitErrorScreen.java
 *The source file uses the MIT License.
 */

package vfyjxf.bettercrashes.utils;

import static vfyjxf.bettercrashes.BetterCrashesConfig.isGTNH;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.I18n;
import net.minecraft.crash.CrashReport;
import org.apache.commons.lang3.StringUtils;

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
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if (detectedUnsupportedModNames == null) {
            detectedUnsupportedModNames = getUnsupportedMods();
        }
        boolean hasUnsupportedMods = !detectedUnsupportedModNames.isEmpty();

        drawDefaultBackground();
        drawCenteredString(
                fontRendererObj,
                I18n.format("bettercrashes.gui.init_error.title"),
                width / 2,
                height / 4 - 40 - (hasUnsupportedMods ? 16 : 0),
                0xFFFFFF);

        int textColor = 0xD0D0D0;
        int x = width / 2 - 155;
        int y = height / 4;
        if (hasUnsupportedMods) {
            y -= 32;
        }

        drawString(fontRendererObj, I18n.format("bettercrashes.gui.init_error.summary"), x, y, textColor);
        drawString(fontRendererObj, I18n.format("bettercrashes.gui.common.paragraph1"), x, y += 18, textColor);

        drawCenteredString(fontRendererObj, getModListString(), width / 2, y += 11, 0xE0E000);

        drawString(fontRendererObj, I18n.format("bettercrashes.gui.common.paragraph2"), x, y += 11, textColor);

        drawCenteredString(
                fontRendererObj,
                report.getFile() != null
                        ? "\u00A7n" + report.getFile().getName()
                        : I18n.format("bettercrashes.gui.common.reportSaveFailed"),
                width / 2,
                y += 11,
                0x00FF00);

        y += 12;
        y += drawLongString(
                fontRendererObj,
                I18n.format("bettercrashes.gui.common.paragraph3" + (isGTNH ? "_gtnh" : "")),
                x,
                y,
                340,
                textColor);

        if (hasUnsupportedMods) {
            drawString(fontRendererObj, I18n.format("bettercrashes.gui.common.paragraph4_gtnh"), x, y += 10, textColor);
            drawCenteredString(
                    fontRendererObj, StringUtils.join(detectedUnsupportedModNames, ", "), width / 2, y += 11, 0xE0E000);
            drawString(fontRendererObj, I18n.format("bettercrashes.gui.common.paragraph5_gtnh"), x, y += 12, textColor);
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}
