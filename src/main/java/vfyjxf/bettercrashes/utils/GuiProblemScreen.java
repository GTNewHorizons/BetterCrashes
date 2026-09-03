/*
 * This file is modified based on
 * https://github.com/DimensionalDevelopment/VanillaFix/blob/99cb47cc05b4790e8ef02bbcac932b21dafa107f/src/main/java/org/
 * dimdev/vanillafix/crashes/GuiProblemScreen.java The source file uses the MIT License.
 */

package vfyjxf.bettercrashes.utils;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.resources.I18n;
import net.minecraft.crash.CrashReport;

import org.apache.commons.lang3.StringUtils;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import vfyjxf.bettercrashes.BetterCrashes;
import vfyjxf.bettercrashes.BetterCrashesConfig;
import vfyjxf.bettercrashes.mixins.interfaces.CrashReportExt;
import vfyjxf.bettercrashes.mixins.interfaces.MinecraftExt;
import vfyjxf.bettercrashes.upload.CrashReportUpload;

@SideOnly(Side.CLIENT)
public abstract class GuiProblemScreen extends GuiScreen {

    private static final int MAX_MOD_LIST_LINES = 3;
    private static final int MOD_LIST_WIDTH = 310;

    private static final int OPEN_CRASH_REPORT_BUTTON_ID = 1;
    private static final int UPLOAD_REPORT_AND_COPY_LINT_BUTTON_ID = 2;
    private static final int OPEN_ISSUE_TRACKER_BUTTON_ID = 3;

    protected final CrashReport report;
    private volatile URL pasteLink = null;
    private String modListString;
    private List<String> modListLines;
    protected List<String> detectedUnsupportedModNames;

    public GuiProblemScreen(CrashReport report) {
        this.report = report;
    }

    @Override
    public void initGui() {
        mc.setIngameNotInFocus();
        buttonList.clear();
        int buttonY = getButtonY();
        buttonList.add(
                new GuiButton(
                        OPEN_CRASH_REPORT_BUTTON_ID,
                        width / 2 - 50,
                        buttonY,
                        110,
                        20,
                        I18n.format("bettercrashes.gui.common.openCrashReport")));
        buttonList.add(
                new GuiButton(
                        UPLOAD_REPORT_AND_COPY_LINT_BUTTON_ID,
                        width / 2 - 50 + 115,
                        buttonY,
                        110,
                        20,
                        I18n.format("bettercrashes.gui.common.uploadReportAndCopyLink")));
        if (StringUtils.isNotEmpty(BetterCrashesConfig.issueTrackerURL)) {
            buttonList.add(
                    new GuiButton(
                            OPEN_ISSUE_TRACKER_BUTTON_ID,
                            width / 2 - 50 - 15,
                            buttonY + 25,
                            140,
                            20,
                            I18n.format("bettercrashes.gui.common.issueTracker")));
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        switch (button.id) {
            case OPEN_CRASH_REPORT_BUTTON_ID -> {
                boolean opened = CrashUtils.openCrashReport(report);
                if (!opened) {
                    button.displayString = I18n.format("bettercrashes.gui.common.failed");
                }
            }

            case UPLOAD_REPORT_AND_COPY_LINT_BUTTON_ID -> {
                if (pasteLink != null) {
                    boolean opened = CrashUtils.openBrowser(pasteLink.toString());
                    if (!opened) {
                        button.displayString = I18n.format("bettercrashes.gui.common.failed");
                    }
                    break;
                }

                button.enabled = false;
                button.displayString = I18n.format("bettercrashes.gui.common.uploading");

                Thread thread = new Thread("BetterCrashes report uploading") {

                    @Override
                    public void run() {
                        try {
                            pasteLink = CrashReportUpload.uploadCrashReport(report.getCompleteReport());
                            if (pasteLink != null) {
                                setClipboardString(pasteLink.toString());
                            }
                            synchronized (button) {
                                button.enabled = true;
                                button.displayString = I18n.format("bettercrashes.gui.common.openUploadedCrashReport");
                            }
                        } catch (IOException e) {
                            BetterCrashes.logger.error("Failed to upload crash report");
                            synchronized (button) {
                                button.enabled = true;
                                button.displayString = I18n.format("bettercrashes.gui.common.failed");
                            }
                        }
                    }
                };
                thread.start();
            }

            case OPEN_ISSUE_TRACKER_BUTTON_ID -> {
                boolean opened = CrashUtils.openBrowser(BetterCrashesConfig.issueTrackerURL);
                if (!opened) {
                    button.displayString = I18n.format("bettercrashes.gui.common.failed");
                }
            }
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {}

    protected abstract String getScreenTitle();

    protected abstract String getScreenSummary();

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if (detectedUnsupportedModNames == null) {
            detectedUnsupportedModNames = getUnsupportedMods();
        }
        boolean hasUnsupportedMods = !detectedUnsupportedModNames.isEmpty();

        try {
            Tessellator.instance.draw();
        } catch (IllegalStateException ignored) {}
        drawDefaultBackground();
        drawCenteredString(
                fontRendererObj,
                getScreenTitle(),
                width / 2,
                height / 4 - 40 - (hasUnsupportedMods ? 16 : 0),
                0xFFFFFF);

        int textColor = 0xD0D0D0;
        int x = width / 2 - 155;
        int y = height / 4;
        if (hasUnsupportedMods) {
            y -= 32;
        }

        drawString(fontRendererObj, getScreenSummary(), x, y, textColor);
        drawString(fontRendererObj, I18n.format("bettercrashes.gui.common.paragraph1"), x, y += 18, textColor);

        y += 11;
        y += drawCenteredLines(fontRendererObj, getModListLines(), y, 0xE0E000);

        if (isCrashLogExpectedToBeGenerated()) {
            drawString(fontRendererObj, I18n.format("bettercrashes.gui.common.paragraph2"), x, y += 11, textColor);

            drawCenteredString(
                    fontRendererObj,
                    report.getFile() != null ? "§n" + report.getFile().getName()
                            : I18n.format("bettercrashes.gui.common.reportSaveFailed"),
                    width / 2,
                    y += 11,
                    0x00FF00);
        } else {
            drawString(fontRendererObj, I18n.format("bettercrashes.gui.common.paragraph6"), x, y += 11, textColor);
            drawString(fontRendererObj, I18n.format("bettercrashes.gui.common.paragraph7"), x, y += 11, textColor);
        }

        y += 12;
        y += drawLongString(fontRendererObj, I18n.format("bettercrashes.gui.common.paragraph3"), x, y, 340, textColor);

        if (hasUnsupportedMods) {
            drawString(fontRendererObj, I18n.format("bettercrashes.gui.common.paragraph4"), x, y += 10, textColor);
            y += 11;
            y += drawCenteredLongString(
                    fontRendererObj,
                    StringUtils.join(detectedUnsupportedModNames, ", "),
                    y,
                    MOD_LIST_WIDTH,
                    0xE0E000);
            drawString(fontRendererObj, I18n.format("bettercrashes.gui.common.paragraph5"), x, y += 12, textColor);
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    protected String getModListString() {
        if (modListString == null) {
            final Set<ModContainer> suspectedMods = ((CrashReportExt) report).betterCrashes$getSuspectedMods();
            if (suspectedMods == null) {
                return modListString = I18n.format("bettercrashes.gui.common.identificationErrored");
            }
            List<String> modNames = new ArrayList<>();
            for (ModContainer mod : suspectedMods) {
                modNames.add(mod.getName());
            }
            if (modNames.isEmpty()) {
                modListString = I18n.format("bettercrashes.gui.common.unknownCause");
            } else {
                modListString = StringUtils.join(modNames, ", ");
            }
        }
        return modListString;
    }

    protected List<String> getModListLines() {
        if (modListLines == null) {
            List<String> lines = fontRendererObj.listFormattedStringToWidth(getModListString(), MOD_LIST_WIDTH);
            if (lines.size() > MAX_MOD_LIST_LINES) {
                lines = new ArrayList<>(lines.subList(0, MAX_MOD_LIST_LINES));
                lines.set(MAX_MOD_LIST_LINES - 1, lines.get(MAX_MOD_LIST_LINES - 1) + "...");
            }
            modListLines = lines;
        }
        return modListLines;
    }

    protected int getButtonY() {
        int y = height / 4 + 120 + 12 + (getModListLines().size() - 1) * fontRendererObj.FONT_HEIGHT;
        int lastRowOffset = StringUtils.isNotEmpty(BetterCrashesConfig.issueTrackerURL) ? 25 : 0;
        return Math.min(y, height - 20 - 4 - lastRowOffset);
    }

    protected int drawCenteredLongString(FontRenderer fontRenderer, String text, int y, int maxWidth, int color) {
        return drawCenteredLines(fontRenderer, fontRenderer.listFormattedStringToWidth(text, maxWidth), y, color);
    }

    protected int drawCenteredLines(FontRenderer fontRenderer, List<String> lines, int y, int color) {
        int yOffset = 0;
        for (String line : lines) {
            drawCenteredString(fontRenderer, line, width / 2, y + yOffset, color);
            yOffset += fontRenderer.FONT_HEIGHT;
        }
        return yOffset;
    }

    protected int drawLongString(FontRenderer fontRenderer, String text, int x, int y, int width, int color) {
        int yOffset = 0;
        for (String line : fontRenderer.listFormattedStringToWidth(text, width)) {
            drawString(fontRenderer, line, x, y + yOffset, color);
            yOffset += fontRenderer.FONT_HEIGHT;
        }
        return yOffset;
    }

    protected List<String> getUnsupportedMods() {
        List<String> installedUnsupportedMods = new ArrayList<>();
        for (ModContainer mod : Loader.instance().getModList()) {
            if (BetterCrashesConfig.unsupportedMods.contains(mod.getModId())) {
                installedUnsupportedMods.add(mod.getName());
            }
        }

        // Due to the nature of Optifine, it will very often need special
        // consideration in bug reports.
        if (FMLClientHandler.instance().hasOptifine()) {
            installedUnsupportedMods.add("Optifine");
        }

        return installedUnsupportedMods;
    }

    protected boolean isCrashLogExpectedToBeGenerated() {
        return getClientCrashCount() <= BetterCrashesConfig.crashLogLimitClient
                && getServerCrashCount() <= BetterCrashesConfig.crashLogLimitServer;
    }

    private int getClientCrashCount() {
        return ((MinecraftExt) Minecraft.getMinecraft()).betterCrashes$getClientCrashCount();
    }

    private int getServerCrashCount() {
        return ((MinecraftExt) Minecraft.getMinecraft()).betterCrashes$getServerCrashCount();
    }
}
