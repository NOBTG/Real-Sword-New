package com.nobtg.Utils.TimeStop;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class TimeStopPacket {
    private final boolean paused;

    private final int id;

    public TimeStopPacket(boolean pressed, int id) {
        this.paused = pressed;
        this.id = id;
    }

    public static @NotNull TimeStopPacket decode(@NotNull FriendlyByteBuf buf) {
        return new TimeStopPacket(buf.readBoolean(), buf.readInt());
    }

    public static void encode(@NotNull TimeStopPacket msg, @NotNull FriendlyByteBuf buf) {
        buf.writeBoolean(msg.paused);
        buf.writeInt(msg.id);
    }

    public boolean isPaused() {
        return this.paused;
    }

    public static void handle(TimeStopPacket msg, @NotNull Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            if (msg != null)
                context.get().enqueueWork(() -> TimeStop.setIsTimeStop(msg.isPaused()));
        });
        context.get().setPacketHandled(true);
    }
}