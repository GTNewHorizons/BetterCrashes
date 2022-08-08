/*
 *This file is modified based on
 *https://github.com/DimensionalDevelopment/VanillaFix/blob/99cb47cc05b4790e8ef02bbcac932b21dafa107f/src/main/java/org/dimdev/vanillafix/crashes/GuiProblemScreen.java
 *The source file uses the MIT License.
 */

package vfyjxf.bettercrashes.utils;

import static vfyjxf.bettercrashes.BetterCrashesConfig.isGTNH;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.crash.CrashReport;
import org.apache.commons.lang3.StringUtils;
import vfyjxf.bettercrashes.BetterCrashesConfig;

@SideOnly(Side.CLIENT)
public abstract class GuiProblemScreen extends GuiScreen {

    private static Field fieldClientCrashCount = null;
    private static Field fieldServerCrashCount = null;

    static {
        try {
            // these are actually reachable, see MinecraftMixin
            fieldClientCrashCount = Minecraft.class.getDeclaredField("clientCrashCount");
            fieldClientCrashCount.setAccessible(true);
            fieldServerCrashCount = Minecraft.class.getDeclaredField("serverCrashCount");
            fieldServerCrashCount.setAccessible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected final CrashReport report;
    private volatile String hasteLink = null;
    private String modListString;
    protected static final List<String> UNSUPPORTED_MOD_IDS = Arrays.asList();
    protected List<String> detectedUnsupportedModNames;
    private static final String GTNH_ISSUE_TRACKER =
            "https://github.com/GTNewHorizons/GT-New-Horizons-Modpack/issues?q=label%3A%22Type%3A+Crash%22";

    public GuiProblemScreen(CrashReport report) {
        this.report = report;
    }

    @Override
    public void initGui() {
        mc.setIngameNotInFocus();
        buttonList.clear();
        buttonList.add(new GuiButton(
                1,
                width / 2 - 50,
                height / 4 + 120 + 12,
                110,
                20,
                I18n.format("bettercrashes.gui.common.openCrashReport")));
        buttonList.add(new GuiButton(
                2,
                width / 2 - 50 + 115,
                height / 4 + 120 + 12,
                110,
                20,
                I18n.format("bettercrashes.gui.common.uploadReportAndCopyLink")));
        if (BetterCrashesConfig.isGTNH) {
            buttonList.add(new GuiButton(
                    3,
                    width / 2 - 50 - 15,
                    height / 4 + 120 + 12 + 25,
                    140,
                    20,
                    I18n.format("bettercrashes.gui.common.gtnhIssueTracker")));
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == 1) {
            try {
                CrashUtils.openCrashReport(report);
            } catch (IOException e) {
                button.displayString = I18n.format("bettercrashes.gui.common.failed");
                button.enabled = false;
                e.printStackTrace();
            }
        }
        if (button.id == 2) {
            if (hasteLink == null) {
                button.enabled = false;
                button.displayString = I18n.format("bettercrashes.gui.common.uploading");
                Thread thread = new Thread("BetterCrashes report uploading") {
                    @Override
                    public void run() {
                        try {
                            hasteLink = CrashReportUpload.uploadToUbuntuPastebin(
                                    "https://paste.ubuntu.com", report.getCompleteReport());
                            synchronized (button) {
                                button.enabled = true;
                                button.displayString = I18n.format("bettercrashes.gui.common.success");
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                            synchronized (button) {
                                button.enabled = false;
                                button.displayString = I18n.format("bettercrashes.gui.common.failed");
                            }
                        }
                    }
                };
                thread.start();
            } else {
                CrashUtils.openBrowser(hasteLink);
            }

            if (hasteLink != null) {
                setClipboardString(hasteLink);
            }
        }
        if (button.id == 3) {
            CrashUtils.openBrowser(GTNH_ISSUE_TRACKER);
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

        drawCenteredString(fontRendererObj, getModListString(), width / 2, y += 11, 0xE0E000);

        if (isCrashLogExpectedToBeGenerated()) {
            drawString(fontRendererObj, I18n.format("bettercrashes.gui.common.paragraph2"), x, y += 11, textColor);

            drawCenteredString(
                    fontRendererObj,
                    report.getFile() != null
                            ? "\u00A7n" + report.getFile().getName()
                            : I18n.format("bettercrashes.gui.common.reportSaveFailed"),
                    width / 2,
                    y += 11,
                    0x00FF00);
        } else {
            drawString(fontRendererObj, I18n.format("bettercrashes.gui.common.paragraph6"), x, y += 11, textColor);
            drawString(fontRendererObj, I18n.format("bettercrashes.gui.common.paragraph7"), x, y += 11, textColor);
        }

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

    protected String getModListString() {
        if (modListString == null) {
            final Set<ModContainer> suspectedMods = ((IPatchedCrashReport) report).getSuspectedMods();
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

    protected int drawLongString(FontRenderer fontRenderer, String text, int x, int y, int width, int color) {
        int yOffset = 0;
        for (Object line : Minecraft.getMinecraft().fontRenderer.listFormattedStringToWidth(text, width)) {
            drawString(fontRenderer, (String) line, x, y + yOffset, color);
            yOffset += 9;
        }
        return yOffset;
    }

    protected List<String> getUnsupportedMods() {
        if (!BetterCrashesConfig.isGTNH) {
            return Collections.emptyList();
        }
        List<String> list = new ArrayList<>();
        for (ModContainer mod : Loader.instance().getModList()) {
            if (UNSUPPORTED_MOD_IDS.contains(mod.getModId())) {
                list.add(mod.getName());
            }
        }
        if (FMLClientHandler.instance().hasOptifine()) {
            list.add("Optifine");
        }
        return list;
    }

    private int getClientCrashCount() {
        if (fieldClientCrashCount != null) {
            try {
                return (int) fieldClientCrashCount.get(Minecraft.getMinecraft());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    private int getServerCrashCount() {
        if (fieldServerCrashCount != null) {
            try {
                return (int) fieldServerCrashCount.get(Minecraft.getMinecraft());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    protected boolean isCrashLogExpectedToBeGenerated() {
        return getClientCrashCount() <= BetterCrashesConfig.crashLogLimitClient
                && getServerCrashCount() <= BetterCrashesConfig.crashLogLimitServer;
    }
}
