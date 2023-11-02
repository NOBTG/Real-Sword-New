package github.nobtg.Utils;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Axis;
import github.nobtg.Utils.Other.Inject;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.system.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Mod.EventBusSubscriber
public abstract class ClientUtil {
    private static final Map<String, Pair<MouseHandler, KeyboardHandler>> hands = new HashMap<>();

    public static void ClientAttack(Minecraft minecraft) {
        Util.setAttack(true, minecraft);
    }

    @SuppressWarnings("unchecked")
    public static void grab(MouseHandler handler) {
        if (!Minecraft.ON_OSX) {
            KeyMapping.setAll();
            for (KeyMapping keymapping : ((Map<String, KeyMapping>) Util.getField(null, KeyMapping.class, "ALL", "f_90809_")).values())
                if (keymapping.getKey().getType() == InputConstants.Type.KEYSYM && keymapping.getKey().getValue() != InputConstants.UNKNOWN.getValue())
                    Util.fieldSetField(keymapping, KeyMapping.class, "isDown", GLFW.glfwGetKey(Minecraft.getInstance().getWindow().getWindow(), keymapping.getKey().getValue()) == 1, "f_90817_");
        }
        try (SharedLibrary library = Library.loadNative(GLFW.class, "org.lwjgl.glfw", Configuration.GLFW_LIBRARY_NAME.get(Platform.mapLibraryNameBundled("glfw")), true)) {
            long v = ((Minecraft) Util.getField(handler, MouseHandler.class, "minecraft", "f_91503_")).getWindow().getWindow();
            if (Checks.CHECKS && v == MemoryUtil.NULL) throw new NullPointerException();
            long a = library.getFunctionAddress("glfwSetInputMode");
            if (a == MemoryUtil.NULL && !Configuration.DISABLE_FUNCTION_CHECKS.get(false))
                throw new NullPointerException("A required function is missing: glfwSetInputMode");
            JNI.invokePV(v, 208897, 212995, a);
        }
    }

    @Inject
    public static void font_tick() {
        Util.setTick(Util.getTick() + 1.6F);
        if (Util.getTick() >= 720.0f) Util.setTick(0.0F);
    }

    public static boolean getNoDrawScreenVal(Minecraft minecraft) {
        return (boolean) Util.getField(minecraft, Minecraft.class, "nobtg$NoDrawScreen", "nobtg$NoDrawScreen");
    }

    public static boolean getAttack(Minecraft minecraft) {
        return (boolean) Util.getField(minecraft, Minecraft.class, "realSword$Attack", "realSword$Attack");
    }

    public static @NotNull String getString(@NotNull Minecraft instance) {
        return instance.getUser().getUuid() + instance.getWindow().getWindow();
    }

    public static boolean isAttack(Minecraft minecraft) {
        return getAttack(minecraft);
    }

    public static Map<String, Pair<MouseHandler, KeyboardHandler>> getHandlers() {
        return hands;
    }

    @SubscribeEvent
    public static void RenderGuiEvent(RenderGuiEvent event) {
        if (!isAttack(Minecraft.getInstance())) return;
        Minecraft mc = Minecraft.getInstance();
        event.getGuiGraphics().pose().pushPose();
        event.getGuiGraphics().pose().scale(2.0F, 2.0F, 2.0F);
        drawString(event.getGuiGraphics(), mc.getWindow().getGuiScaledWidth() / 2 / 2, Util.getColor1().getRGB());
        event.getGuiGraphics().pose().popPose();
        for (int i = 0; i < Util.getCount(); i++) {
            float rotationAngle = (float) (i / Math.random());
            RenderRainbowTexture(event.getGuiGraphics(), mc, rotationAngle);
        }
        Util.setCount((Util.getCount() % 16.0F) + 0.05F);
    }

    public static void RenderRainbowTexture(@NotNull GuiGraphics graphics, @NotNull Minecraft mc, float rotationAngle) {
        graphics.pose().pushPose();
        graphics.pose().mulPose(chooseDirection().rotationDegrees(rotationAngle));
        graphics.fillGradient(0, 0, mc.getWindow().getGuiScaledWidth(), mc.getWindow().getGuiScaledHeight(), Util.getColor1().getRGB(), Util.getColor2().getRGB());
        graphics.pose().popPose();
    }

    public static Axis chooseDirection() {
        int randomIndex = ThreadLocalRandom.current().nextInt(6) + 1;
        return switch (randomIndex) {
            case 1 -> Axis.XP;
            case 2 -> Axis.YN;
            case 3 -> Axis.YP;
            case 4 -> Axis.ZN;
            case 5 -> Axis.ZP;
            default -> Axis.XN;
        };
    }

    public static void drawString(@NotNull GuiGraphics guiGraphics, int p_281586_, int p_281743_) {
        Util.getFont().SetGuiOnly(true).drawInBatch(Component.translatable("deathScreen.title"), (float) p_281586_ - (float) Util.getFont().width(Component.translatable("deathScreen.title").getVisualOrderText()) / 2, 30.0F, p_281743_, true, guiGraphics.pose().last().pose(), guiGraphics.bufferSource(), Font.DisplayMode.NORMAL, 0, 15728880);
        flushIfUnmanaged(guiGraphics);
    }

    private static void flushIfUnmanaged(GuiGraphics guiGraphics) {
        if (!(boolean) Util.getField(guiGraphics, GuiGraphics.class, "managed", "f_285610_"))
            guiGraphics.flush();
    }
}
