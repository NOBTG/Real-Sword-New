package github.nobtg.Utils.TimeStop;

import github.nobtg.Utils.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Timer;

public class TimeStop {
    public static float perTick = 1.2443632E2F;
    public static long millis = 0L;
    public static long realMillis = 0L;
    public static Timer timer = new Timer(20.0F, 0L);
    private static boolean isTimeStop = false;

    public static void setIsTimeStop(boolean isTimeStop) {
        TimeStop.isTimeStop = isTimeStop;
        Util.fieldSetField(Minecraft.getInstance(), Minecraft.class, "pause", isTimeStop, "f_91012_");
    }

    public static boolean get() {
        return isTimeStop;
    }

    public static float getPartialTick(float val) {
        return TimeStop.get() ? TimeStop.timer.partialTick : val;
    }
}