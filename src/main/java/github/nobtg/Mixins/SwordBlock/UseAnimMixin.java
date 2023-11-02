package github.nobtg.Mixins.SwordBlock;

import github.nobtg.RealSwordMod;
import net.minecraft.world.item.UseAnim;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Arrays;
import java.util.stream.Stream;

@Mixin(value = UseAnim.class, priority = 0x7fffffff)
public abstract class UseAnimMixin {
    @Shadow
    @Final
    @Mutable
    private static UseAnim[] $VALUES;

    @Invoker(value = "<init>")
    private static UseAnim useAnim$invokeInit(String name, int id) {
        throw new RuntimeException();
    }

    static {
        $VALUES = Stream.concat(Arrays.stream($VALUES), Stream.of(UseAnimMixin.useAnim$invokeInit(RealSwordMod.MOD_ID + ":BLOCK", $VALUES[$VALUES.length - 1].ordinal() + 1)))
            .toArray(UseAnim[]::new);
    }
}