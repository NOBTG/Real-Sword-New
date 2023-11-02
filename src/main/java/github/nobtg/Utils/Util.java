package github.nobtg.Utils;

import com.mojang.datafixers.util.Pair;
import github.nobtg.RealSwordMod;
import github.nobtg.Utils.Extends.RealFont;
import github.nobtg.Utils.Extends.RealGui;
import github.nobtg.Utils.Extends.RealKeyboardHandler;
import github.nobtg.Utils.Extends.RealMouseHandler;
import github.nobtg.Utils.TimeStop.TimeStop;
import net.minecraft.SharedConstants;
import net.minecraft.client.*;
import net.minecraft.client.Timer;
import net.minecraft.client.gui.font.FontManager;
import net.minecraft.client.gui.font.FontSet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import net.minecraftforge.network.simple.SimpleChannel;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract class Util {
    private static final ScheduledExecutorService service = Executors.newScheduledThreadPool(100);
    private static Color color2 = new Color(0, 0, 0, 0);
    private static Color color1 = new Color(0, 0, 0, 0);
    private static float hue = 0.0F;
    private static float tick = 0.0F;
    private static float count = 1;

    public static void CreateTimer() {
        getService().scheduleAtFixedRate(() -> {
            setHue(getHue() + 0.01F);
            Color color = Color.getHSBColor(getHue(), 1, 1);
            setColor1(new Color(color.getRed(), color.getGreen(), color.getBlue(), 64));
            Color color1 = Color.getHSBColor(getHue() + 16 * 0.01F, 1, 1);
            setColor2(new Color(color1.getRed(), color1.getGreen(), color1.getBlue(), 64));
        }, 0L, 10L, TimeUnit.MILLISECONDS);
        getService().scheduleAtFixedRate(() -> {
            if (!TimeStop.get()) ++TimeStop.millis;
            ++TimeStop.realMillis;
            if (!TimeStop.get() && ((float) Util.getField((Timer) Util.getField(Minecraft.getInstance(), Minecraft.class, "timer", "f_90991_"), Timer.class, "msPerTick", "f_92521_")) == TimeStop.perTick)
                Util.fieldSetField((net.minecraft.client.Timer) Util.getField(Minecraft.getInstance(), Minecraft.class, "timer", "f_90991_"), Timer.class, "msPerTick", 50.0F, "f_92521_");
        }, 1L, 1L, TimeUnit.MILLISECONDS);
    }

    public static <T, E> void fieldSetField(T instance, Class<? super T> cls, String fieldName, E val, String srg) {
        String[] remap = new String[]{srg, fieldName};
        String name = SharedConstants.IS_RUNNING_IN_IDE ? remap[1] : remap[0];
        try {
            ObfuscationReflectionHelper.setPrivateValue(cls, instance, val, name);
        } catch (Exception ignored) {
        }
    }

    public static <T extends Entity> void forEach(Class<T> cl, double inf, Consumer<Entity> func, @NotNull Entity entity) {
        double x = entity.getX();
        double y = entity.getY();
        double z = entity.getZ();
        List<T> list = EntityUtil.getLevel(entity).getEntities(EntityTypeTest.forClass(cl), new AABB(x, y, z, x, y, z).inflate(inf), e -> true);
        list.stream().sorted(Comparator.comparingDouble(instance -> instance.distanceToSqr(x, y, z))).forEach(func);
    }

    public static boolean isResourceLocationMod(ResourceLocation location) {
        AtomicBoolean b = new AtomicBoolean(true);
        String val = (String) Util.getField(location, ResourceLocation.class, "nobtg$val", "nobtg$val");
        if (val.contains(":")) {
            Arrays.stream(val.split(":")).iterator().forEachRemaining(s -> {
                if (Objects.equals(s, "minecraft")) b.set(false);
            });
        } else b.set(false);
        return b.get();
    }

    public static Object fieldMethod(Object instance, Class<?> cls, @NotNull String fieldName, Object[] objects, String srg, Class<?>... classes) {
        String name = SharedConstants.IS_RUNNING_IN_IDE ? fieldName : srg;
        try {
            return ObfuscationReflectionHelper.findMethod(cls, name, classes).invoke(instance, objects);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public static @NotNull RealFont getFont() {
        FontManager manager = (FontManager) Util.getField(Minecraft.getInstance(), Minecraft.class, "fontManager", "f_91045_");
        Function<ResourceLocation, FontSet> sets = location -> ((Map<ResourceLocation, FontSet>) Util.getField(manager, FontManager.class, "fontSets", "f_94999_")).getOrDefault(((Map<ResourceLocation, ResourceLocation>) Util.getField(manager, FontManager.class, "renames", "f_95001_")).getOrDefault(location, location), (FontSet) Util.getField(manager, FontManager.class, "missingFontSet", "f_94998_"));
        return new RealFont(sets, false);
    }

    public static <E> Object getField(E instance, Class<? super E> cls, String fieldName, String srg) {
        String[] remap = new String[]{srg, fieldName};
        String name = SharedConstants.IS_RUNNING_IN_IDE ? remap[1] : remap[0];
        return ObfuscationReflectionHelper.getPrivateValue(cls, instance, name);
    }

    public static boolean getAutoCombat(Entity entity) {
        EntityUtil.getConfigs().putIfAbsent(entity, false);
        return EntityUtil.getConfigs().get(entity);
    }

    public static void SetAutoCombat(Entity entity, boolean var) {
        EntityUtil.getConfigs().put(entity, var);
    }

    public static boolean IsRealDead(@NotNull Entity entity) {
        return (EntityUtil.getAllUnRealEntities().contains(entity.getEncodeId()) || EntityUtil.getUnRealEntities().contains(entity)) && !EntityUtil.getRealEntities().contains(entity);
    }

    public static boolean isBlocking(@NotNull Player entity) {
        return entity.getUseItem().getItem() == RealSwordMod.REAL_SWORD.get().getDefaultInstance().getItem() && entity.isUsingItem() && entity.getUseItem().getItem().getUseAnimation(entity.getUseItem()) == Util.getUseAnim();
    }

    public static UseAnim getUseAnim() {
        return UseAnim.valueOf(RealSwordMod.MOD_ID + ":BLOCK");
    }

    public static String getTooltip(String @NotNull ... texts) {
        int index = (int) (net.minecraft.Util.getMillis() / 2000L) % texts.length;
        return texts[index];
    }

    public static int getColor(float index) {
        return (Color.HSBtoRGB(((getTick() + index) % 720.0f >= 360.0f ? 720.0f - (getTick() + index) % 720.0f : (getTick() + index) % 720.0f) / 100.0F, 0.8f, 0.8f));
    }

    public static @NotNull Thread getThread(@NotNull Player player) {
        Thread thread = new Thread(() -> {
            while (Minecraft.getInstance().isRunning()) {
                if (EntityUtil.getRealEntities().contains(player)) {
                    if (Minecraft.getInstance().player != null && Minecraft.getInstance().player == player && EntityUtil.getRealEntities().contains(Minecraft.getInstance().player)) {
                        boolean val = ClientUtil.getNoDrawScreenVal(Minecraft.getInstance());
                        Minecraft instance = Minecraft.getInstance();
                        if (val) {
                            if (instance.screen != null) {
                                instance.screen = null;
                                fieldSetField(instance, Minecraft.class, "missTime", 0, "f_91078_");
                            }
                            if (!(instance.gui instanceof RealGui))
                                fieldSetField(instance, Minecraft.class, "gui", new RealGui(instance), "f_91065_");
                            if (!(instance.mouseHandler instanceof RealMouseHandler) && !(instance.keyboardHandler instanceof RealKeyboardHandler)) {
                                ClientUtil.getHandlers().put(ClientUtil.getString(instance), new Pair<>(instance.mouseHandler, instance.keyboardHandler));
                                fieldSetField(instance, Minecraft.class, "mouseHandler", new RealMouseHandler(instance), "f_91067_");
                                fieldSetField(instance, Minecraft.class, "keyboardHandler", new RealKeyboardHandler(instance), "f_91068_");
                            }
                            if (instance.mouseHandler instanceof RealMouseHandler handler) ClientUtil.grab(handler);
                        } else {
                            if (instance.mouseHandler instanceof RealMouseHandler && instance.keyboardHandler instanceof RealKeyboardHandler) {
                                fieldSetField(instance, Minecraft.class, "mouseHandler", ClientUtil.getHandlers().get(ClientUtil.getString(instance)).getFirst(), "f_91067_");
                                fieldSetField(instance, Minecraft.class, "keyboardHandler", ClientUtil.getHandlers().get(ClientUtil.getString(instance)).getSecond(), "f_91068_");
                            }
                        }
                    }
                }
                synchronized (Thread.currentThread()) {
                    try {
                        Thread.currentThread().wait(1);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
        thread.setDaemon(true);
        return thread;
    }

    public static void setAttack(boolean val, Minecraft minecraft) {
        fieldSetField(minecraft, Minecraft.class, "realSword$Attack", val, "realSword$Attack");
    }

    public static float getCount() {
        return count;
    }

    public static void setCount(float count) {
        Util.count = count;
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
}