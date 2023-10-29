package com.nobtg.Utils;

import com.mojang.datafixers.util.Pair;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.MouseHandler;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.simple.SimpleChannel;

import java.awt.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public abstract class GetterAndSetters {
    private static final ScheduledExecutorService service = Executors.newScheduledThreadPool(100);
    private static final Map<String, Pair<MouseHandler, KeyboardHandler>> hands = new HashMap<>();
    private static final Set<String> ALL_UN_REAL_ENTITIES = new HashSet<>();
    private static final Set<Entity> UN_REAL_ENTITIES = new HashSet<>();
    private static final Map<Entity, Boolean> configs = new HashMap<>();
    private static final Set<Entity> REMOVE_ENTITIES = new HashSet<>();
    private static final Set<Entity> HAS_DEAD_ITEM = new HashSet<>();
    private static final Set<Entity> REAL_ENTITIES = new HashSet<>();
    private static Color color2 = new Color(0, 0, 0, 0);
    private static Color color1 = new Color(0, 0, 0, 0);
    private static float hue = 0.0F;
    private static float tick = 0.0F;
    private static float count = 1;

    public static float getCount() {
        return count;
    }

    public static void setCount(float count) {
        GetterAndSetters.count = count;
    }

    public static Map<String, Pair<MouseHandler, KeyboardHandler>> getHandlers() {
        return hands;
    }

    public static Map<Entity, Boolean> getConfigs() {
        return configs;
    }

    public static float getTick() {
        return tick;
    }

    public static void setTick(float val) {
        tick = val;
    }

    public static float getHue() {
        return hue;
    }

    public static void setHue(float val) {
        hue = val;
    }

    public static Color getColor1() {
        return color1;
    }

    public static void setColor1(Color color) {
        color1 = color;
    }

    public static Color getColor2() {
        return color2;
    }

    public static void setColor2(Color color) {
        color2 = color;
    }

    public static SimpleChannel getPacketChannel() {
        return UnSafeField.SIMPLE_CHANNEL;
    }

    public static Pair<String[], String[]> getTooltips() {
        return UnSafeField.Tooltips;
    }

    public static ScheduledExecutorService getService() {
        return service;
    }

    public static Set<String> getAllUnRealEntities() {
        return ALL_UN_REAL_ENTITIES;
    }

    public static Set<Entity> getRealEntities() {
        return REAL_ENTITIES;
    }

    public static Set<Entity> getUnRealEntities() {
        return UN_REAL_ENTITIES;
    }

    public static Set<Entity> getRemoveEntities() {
        return REMOVE_ENTITIES;
    }

    public static Set<Entity> getHasDeadItem() {
        return HAS_DEAD_ITEM;
    }
}
