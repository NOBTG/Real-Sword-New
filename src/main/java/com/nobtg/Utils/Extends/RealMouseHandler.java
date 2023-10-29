package com.nobtg.Utils.Extends;

import com.mojang.blaze3d.Blaze3D;
import com.mojang.blaze3d.platform.InputConstants;
import com.nobtg.Utils.Util;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.util.Mth;
import net.minecraft.util.SmoothDouble;
import net.minecraftforge.client.ForgeHooksClient;

import java.util.Objects;

public class RealMouseHandler extends MouseHandler {
    public RealMouseHandler(Minecraft p_91522_) {
        super(p_91522_);
    }

    public boolean isMouseGrabbed() {
        Util.grab(this);
        return true;
    }

    public void onPress(long p_91531_, int p_91532_, int p_91533_, int p_91534_) {
        if (p_91531_ == ((Minecraft) Util.getField(this, MouseHandler.class, "minecraft", "f_91503_")).getWindow().getWindow()) {
            boolean flag = p_91533_ == 1;
            if (Minecraft.ON_OSX && p_91532_ == 0) {
                if (flag) {
                    if ((p_91534_ & 2) == 2) {
                        p_91532_ = 1;
                        Util.fieldSetField(this, MouseHandler.class, "fakeRightMouse", (((int) Util.getField(this, MouseHandler.class, "fakeRightMouse", "f_91509_")) + 1), "f_91509_");
                    }
                } else if ((int) Util.getField(this, MouseHandler.class, "fakeRightMouse", "f_91509_") > 0) {
                    p_91532_ = 1;
                    Util.fieldSetField(this, MouseHandler.class, "fakeRightMouse", (((int) Util.getField(this, MouseHandler.class, "fakeRightMouse", "f_91509_")) - 1), "f_91509_");
                }
            }
            int i = p_91532_;
            if (flag) {
                if (((Minecraft) Util.getField(this, MouseHandler.class, "minecraft", "f_91503_")).options.touchscreen().get() && ((int) Util.getField(this, MouseHandler.class, "clickDepth", "f_91512_")) > 0) {
                    Util.fieldSetField(this, MouseHandler.class, "clickDepth", (((int) Util.getField(this, MouseHandler.class, "clickDepth", "f_91512_")) + 1), "f_91512_");
                    return;
                }
                Util.fieldSetField(this, MouseHandler.class, "activeButton", i, "f_91510_");
                Util.fieldSetField(this, MouseHandler.class, "mousePressedTime", Blaze3D.getTime(), "f_91513_");
            } else if ((int) Util.getField(this, MouseHandler.class, "activeButton", "f_91510_") != -1) {
                Util.fieldSetField(this, MouseHandler.class, "clickDepth", (((int) Util.getField(this, MouseHandler.class, "clickDepth", "f_91512_")) - 1), "f_91512_");
                if (((Minecraft) Util.getField(this, MouseHandler.class, "minecraft", "f_91503_")).options.touchscreen().get() && ((int) Util.getField(this, MouseHandler.class, "clickDepth", "f_91512_")) > 0)
                    return;
                Util.fieldSetField(this, MouseHandler.class, "activeButton", -1, "f_91510_");
            }
            if (ForgeHooksClient.onMouseButtonPre(p_91532_, p_91533_, p_91534_)) return;
            if (!((boolean) Util.getField(this, MouseHandler.class, "mouseGrabbed", "f_91520_")) && flag)
                this.grabMouse();
            if (i == 0)
                Util.fieldSetField(this, MouseHandler.class, "isLeftPressed", flag, "f_91504_");
            else if (i == 2)
                Util.fieldSetField(this, MouseHandler.class, "isMiddlePressed", flag, "f_91505_");
            else if (i == 1)
                Util.fieldSetField(this, MouseHandler.class, "isRightPressed", flag, "f_91506_");
            KeyMapping.set(InputConstants.Type.MOUSE.getOrCreate(i), flag);
            if (flag)
                if (((Minecraft) Util.getField(this, MouseHandler.class, "minecraft", "f_91503_")).player != null && Objects.requireNonNull(((Minecraft) Util.getField(this, MouseHandler.class, "minecraft", "f_91503_")).player).isSpectator() && i == 2)
                    ((Minecraft) Util.getField(this, MouseHandler.class, "minecraft", "f_91503_")).gui.getSpectatorGui().onMouseMiddleClick();
                else KeyMapping.click(InputConstants.Type.MOUSE.getOrCreate(i));
            ForgeHooksClient.onMouseButtonPost(p_91532_, p_91533_, p_91534_);
        }
    }

    public void onScroll(long p_91527_, double p_91528_, double p_91529_) {
        if (p_91527_ == Minecraft.getInstance().getWindow().getWindow()) {
            double offset = p_91529_;
            if (Minecraft.ON_OSX && p_91529_ == 0)
                offset = p_91528_;
            double d0 = (((Minecraft) Util.getField(this, MouseHandler.class, "minecraft", "f_91503_")).options.discreteMouseScroll().get() ? Math.signum(offset) : offset) * ((Minecraft) Util.getField(this, MouseHandler.class, "minecraft", "f_91503_")).options.mouseWheelSensitivity().get();
            if (((Minecraft) Util.getField(this, MouseHandler.class, "minecraft", "f_91503_")).player != null) {
                if ((double) Util.getField(this, MouseHandler.class, "accumulatedScroll", "f_91518_") != 0.0D && Math.signum(d0) != Math.signum((double) Util.getField(this, MouseHandler.class, "accumulatedScroll", "f_91518_")))
                    Util.fieldSetField(this, MouseHandler.class, "accumulatedScroll", 0.0D, "f_91518_");
                Util.fieldSetField(this, MouseHandler.class, "accumulatedScroll", ((double) Util.getField(this, MouseHandler.class, "accumulatedScroll", "f_91518_")) + d0, "f_91518_");
                int i = (int) ((double) Util.getField(this, MouseHandler.class, "accumulatedScroll", "f_91518_"));
                if (i == 0) return;
                Util.fieldSetField(this, MouseHandler.class, "accumulatedScroll", ((double) Util.getField(this, MouseHandler.class, "accumulatedScroll", "f_91518_")) - i, "f_91518_");
                if (ForgeHooksClient.onMouseScroll(this, d0)) return;
                if (Objects.requireNonNull(((Minecraft) Util.getField(this, MouseHandler.class, "minecraft", "f_91503_")).player).isSpectator()) {
                    if (((Minecraft) Util.getField(this, MouseHandler.class, "minecraft", "f_91503_")).gui.getSpectatorGui().isMenuActive()) {
                        ((Minecraft) Util.getField(this, MouseHandler.class, "minecraft", "f_91503_")).gui.getSpectatorGui().onMouseScrolled(-i);
                    } else {
                        float f = Mth.clamp(Objects.requireNonNull(((Minecraft) Util.getField(this, MouseHandler.class, "minecraft", "f_91503_")).player).getAbilities().getFlyingSpeed() + (float) i * 0.005F, 0.0F, 0.2F);
                        Objects.requireNonNull(((Minecraft) Util.getField(this, MouseHandler.class, "minecraft", "f_91503_")).player).getAbilities().setFlyingSpeed(f);
                    }
                } else if (((Minecraft) Util.getField(this, MouseHandler.class, "minecraft", "f_91503_")).player != null) Objects.requireNonNull(((Minecraft) Util.getField(this, MouseHandler.class, "minecraft", "f_91503_")).player).getInventory().swapPaint(i);
            }
        }
    }

    public void turnPlayer() {
        double d0 = Blaze3D.getTime();
        double d1 = d0 - ((double) Util.getField(this, MouseHandler.class, "lastMouseEventTime", "f_91519_"));
        Util.fieldSetField(this, MouseHandler.class, "lastMouseEventTime", d0, "f_91519_");
        if (this.isMouseGrabbed() && ((Minecraft) Util.getField(this, MouseHandler.class, "minecraft", "f_91503_")).isWindowActive()) {
            double d4 = ((Minecraft) Util.getField(this, MouseHandler.class, "minecraft", "f_91503_")).options.sensitivity().get() * (double)0.6F + (double)0.2F;
            double d5 = d4 * d4 * d4;
            double d6 = d5 * 8.0D;
            double d2;
            double d3;
            if (((Minecraft) Util.getField(this, MouseHandler.class, "minecraft", "f_91503_")).options.smoothCamera) {
                double d7 = ((SmoothDouble) Util.getField(this, MouseHandler.class, "smoothTurnX", "f_91514_")).getNewDeltaValue(((double) Util.getField(this, MouseHandler.class, "accumulatedDX", "f_91516_")) * d6, d1 * d6);
                double d8 = ((SmoothDouble) Util.getField(this, MouseHandler.class, "smoothTurnY", "f_91515_")).getNewDeltaValue(((double) Util.getField(this, MouseHandler.class, "accumulatedDY", "f_91517_")) * d6, d1 * d6);
                d2 = d7;
                d3 = d8;
            } else if (((Minecraft) Util.getField(this, MouseHandler.class, "minecraft", "f_91503_")).options.getCameraType().isFirstPerson() && ((Minecraft) Util.getField(this, MouseHandler.class, "minecraft", "f_91503_")).player != null && Objects.requireNonNull(((Minecraft) Util.getField(this, MouseHandler.class, "minecraft", "f_91503_")).player).isScoping()) {
                ((SmoothDouble) Util.getField(this, MouseHandler.class, "smoothTurnX", "f_91514_")).reset();
                ((SmoothDouble) Util.getField(this, MouseHandler.class, "smoothTurnY", "f_91515_")).reset();
                d2 = ((double) Util.getField(this, MouseHandler.class, "accumulatedDX", "f_91516_")) * d5;
                d3 = ((double) Util.getField(this, MouseHandler.class, "accumulatedDY", "f_91517_")) * d5;
            } else {
                ((SmoothDouble) Util.getField(this, MouseHandler.class, "smoothTurnX", "f_91514_")).reset();
                ((SmoothDouble) Util.getField(this, MouseHandler.class, "smoothTurnY", "f_91515_")).reset();
                d2 = ((double) Util.getField(this, MouseHandler.class, "accumulatedDX", "f_91516_")) * d6;
                d3 = ((double) Util.getField(this, MouseHandler.class, "accumulatedDY", "f_91517_")) * d6;
            }
            Util.fieldSetField(this, MouseHandler.class, "accumulatedDX", 0.0D, "f_91516_");
            Util.fieldSetField(this, MouseHandler.class, "accumulatedDY", 0.0D, "f_91517_");
            int i = 1;
            if (((Minecraft) Util.getField(this, MouseHandler.class, "minecraft", "f_91503_")).options.invertYMouse().get())
                i = -1;
            ((Minecraft) Util.getField(this, MouseHandler.class, "minecraft", "f_91503_")).getTutorial().onMouse(d2, d3);
            if (((Minecraft) Util.getField(this, MouseHandler.class, "minecraft", "f_91503_")).player != null)
                Objects.requireNonNull(((Minecraft) Util.getField(this, MouseHandler.class, "minecraft", "f_91503_")).player).turn(d2, d3 * (double)i);
        } else {
            Util.fieldSetField(this, MouseHandler.class, "accumulatedDX", 0.0D, "f_91516_");
            Util.fieldSetField(this, MouseHandler.class, "accumulatedDY", 0.0D, "f_91517_");
        }
    }

    public void releaseMouse() {
        Util.grab(this);
    }
}
