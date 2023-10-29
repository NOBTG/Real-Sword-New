package com.nobtg.Utils.Extends;

import com.mojang.blaze3d.platform.InputConstants;
import com.nobtg.Utils.Util;
import net.minecraft.client.*;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.SimpleOptionsSubScreen;
import net.minecraft.client.gui.screens.controls.KeyBindsScreen;
import net.minecraftforge.client.ForgeHooksClient;

import java.util.Objects;

public class RealKeyboardHandler extends KeyboardHandler {
    public RealKeyboardHandler(Minecraft p_90875_) {
        super(p_90875_);
    }

    public void keyPress(long p_90894_, int p_90895_, int p_90896_, int p_90897_, int p_90898_) {
        if (p_90894_ == ((Minecraft) Util.getField(this, KeyboardHandler.class, "minecraft", "f_90867_")).getWindow().getWindow()) {
            if (((long) Util.getField(this, KeyboardHandler.class, "debugCrashKeyTime", "f_90870_")) > 0L) {
                if (!InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 67) || !InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 292))
                    Util.fieldSetField(this, KeyboardHandler.class, "debugCrashKeyTime", -1L, "f_90870_");
            } else if (InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 67) && InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 292)) {
                Util.fieldSetField(this, KeyboardHandler.class, "handledDebugKey", true, "f_90873_");
                Util.fieldSetField(this, KeyboardHandler.class, "debugCrashKeyTime", net.minecraft.Util.getMillis(), "f_90870_");
                Util.fieldSetField(this, KeyboardHandler.class, "debugCrashKeyReportedTime", net.minecraft.Util.getMillis(), "f_90871_");
                Util.fieldSetField(this, KeyboardHandler.class, "debugCrashKeyReportedCount", 0L, "f_90872_");
            }
            Screen screen = ((Minecraft) Util.getField(this, KeyboardHandler.class, "minecraft", "f_90867_")).screen;
            if (screen != null && (!(((Minecraft) Util.getField(this, KeyboardHandler.class, "minecraft", "f_90867_")).screen instanceof KeyBindsScreen) || ((KeyBindsScreen)screen).lastKeySelection <= net.minecraft.Util.getMillis() - 20L)) {
                if (p_90897_ == 1) {
                    if (((Minecraft) Util.getField(this, KeyboardHandler.class, "minecraft", "f_90867_")).options.keyFullscreen.matches(p_90895_, p_90896_)) {
                        ((Minecraft) Util.getField(this, KeyboardHandler.class, "minecraft", "f_90867_")).getWindow().toggleFullScreen();
                        ((Minecraft) Util.getField(this, KeyboardHandler.class, "minecraft", "f_90867_")).options.fullscreen().set(((Minecraft) Util.getField(this, KeyboardHandler.class, "minecraft", "f_90867_")).getWindow().isFullscreen());
                        return;
                    }
                    if (((Minecraft) Util.getField(this, KeyboardHandler.class, "minecraft", "f_90867_")).options.keyScreenshot.matches(p_90895_, p_90896_))
                        if (Screen.hasControlDown()) {
                            Screenshot.grab(((Minecraft) Util.getField(this, KeyboardHandler.class, "minecraft", "f_90867_")).gameDirectory, ((Minecraft) Util.getField(this, KeyboardHandler.class, "minecraft", "f_90867_")).getMainRenderTarget(), (p_90917_) -> ((Minecraft) Util.getField(this, KeyboardHandler.class, "minecraft", "f_90867_")).execute(() -> ((Minecraft) Util.getField(this, KeyboardHandler.class, "minecraft", "f_90867_")).gui.getChat().addMessage(p_90917_)));
                            return;
                        }
                } else if (p_90897_ == 0 && ((Minecraft) Util.getField(this, KeyboardHandler.class, "minecraft", "f_90867_")).screen instanceof KeyBindsScreen)
                    ((KeyBindsScreen) Objects.requireNonNull(((Minecraft) Util.getField(this, KeyboardHandler.class, "minecraft", "f_90867_")).screen)).selectedKey = null;
            }
            if (((Minecraft) Util.getField(this, KeyboardHandler.class, "minecraft", "f_90867_")).getNarrator().isActive()) {
                boolean flag = screen == null || !(screen.getFocused() instanceof EditBox) || !((EditBox)screen.getFocused()).canConsumeInput();
                if (p_90897_ != 0 && p_90895_ == 66 && Screen.hasControlDown() && flag) {
                    boolean flag1 = ((Minecraft) Util.getField(this, KeyboardHandler.class, "minecraft", "f_90867_")).options.narrator().get() == NarratorStatus.OFF;
                    ((Minecraft) Util.getField(this, KeyboardHandler.class, "minecraft", "f_90867_")).options.narrator().set(NarratorStatus.byId(((Minecraft) Util.getField(this, KeyboardHandler.class, "minecraft", "f_90867_")).options.narrator().get().getId() + 1));
                    if (screen instanceof SimpleOptionsSubScreen)
                        ((SimpleOptionsSubScreen)screen).updateNarratorButton();
                    if (flag1 && screen != null)
                        screen.narrationEnabled();
                }
            }
            InputConstants.Key inputconstants$key = InputConstants.getKey(p_90895_, p_90896_);
            if (p_90897_ == 0) {
                KeyMapping.set(inputconstants$key, false);
                if (p_90895_ == 292) {
                    if ((boolean) Util.getField(this, KeyboardHandler.class, "handledDebugKey", "f_90873_")) {
                        Util.fieldSetField(this, KeyboardHandler.class, "handledDebugKey", false, "f_90873_");
                    } else {
                        ((Minecraft) Util.getField(this, KeyboardHandler.class, "minecraft", "f_90867_")).options.renderDebug = !((Minecraft) Util.getField(this, KeyboardHandler.class, "minecraft", "f_90867_")).options.renderDebug;
                        ((Minecraft) Util.getField(this, KeyboardHandler.class, "minecraft", "f_90867_")).options.renderDebugCharts = ((Minecraft) Util.getField(this, KeyboardHandler.class, "minecraft", "f_90867_")).options.renderDebug && Screen.hasShiftDown();
                        ((Minecraft) Util.getField(this, KeyboardHandler.class, "minecraft", "f_90867_")).options.renderFpsChart = ((Minecraft) Util.getField(this, KeyboardHandler.class, "minecraft", "f_90867_")).options.renderDebug && Screen.hasAltDown();
                    }
                }
            } else {
                if (p_90895_ == 293)
                    ((Minecraft) Util.getField(this, KeyboardHandler.class, "minecraft", "f_90867_")).gameRenderer.togglePostEffect();
                boolean flag3;
                if (p_90895_ == 256) {
                    boolean flag2 = InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 292);
                    ((Minecraft) Util.getField(this, KeyboardHandler.class, "minecraft", "f_90867_")).pauseGame(flag2);
                }
                flag3 = InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 292) && ((boolean) Util.fieldMethod(this, KeyboardHandler.class, "handleDebugKeys", new Object[]{p_90895_}, "m_90932_", int.class));
                boolean newBoolean = (boolean) Util.getField(this, KeyboardHandler.class, "handledDebugKey", "f_90873_");
                newBoolean |= flag3;
                Util.fieldSetField(this, KeyboardHandler.class, "handledDebugKey", newBoolean, "f_90873_");
                if (p_90895_ == 290)
                    ((Minecraft) Util.getField(this, KeyboardHandler.class, "minecraft", "f_90867_")).options.hideGui = !((Minecraft) Util.getField(this, KeyboardHandler.class, "minecraft", "f_90867_")).options.hideGui;
                if (flag3)
                    KeyMapping.set(inputconstants$key, false);
                else {
                    KeyMapping.set(inputconstants$key, true);
                    KeyMapping.click(inputconstants$key);
                }
                if (((Minecraft) Util.getField(this, KeyboardHandler.class, "minecraft", "f_90867_")).options.renderDebugCharts && p_90895_ >= 48 && p_90895_ <= 57)
                    ((Minecraft) Util.getField(this, KeyboardHandler.class, "minecraft", "f_90867_")).debugFpsMeterKeyPress(p_90895_ - 48);
            }
            ForgeHooksClient.onKeyInput(p_90895_, p_90896_, p_90897_, p_90898_);
        }
    }
}
