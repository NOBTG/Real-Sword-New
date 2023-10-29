package com.nobtg;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.nobtg.Utils.CreateAndRunJar;
import com.nobtg.Utils.EntityUtil;
import com.nobtg.Utils.GetterAndSetters;
import com.nobtg.Utils.TimeStop.TimeStop;
import com.nobtg.Utils.TimeStop.TimeStopPacket;
import com.nobtg.Utils.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.common.ForgeTier;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Stream;

@Mod.EventBusSubscriber
@Mod(RealSwordMod.MOD_ID)
public class RealSwordMod {
    public static final String MOD_ID = "real_sword";
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MOD_ID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MOD_ID);
    public static final RegistryObject<Item> REAL_SWORD = ITEMS.register("real_sword", () -> new SwordItem(new ForgeTier(3, 1561, 8.0F, 3.0F, 10, BlockTags.NEEDS_DIAMOND_TOOL, () -> Ingredient.EMPTY), 3, -2.4F, new Item.Properties()) {
        public @NotNull Component getName(@NotNull ItemStack stack) {
            return Component.literal(Util.getTooltip(GetterAndSetters.getTooltips().getSecond()));
        }

        public void initializeClient(@NotNull Consumer<IClientItemExtensions> consumer) {
            consumer.accept(new IClientItemExtensions() {
                public @NotNull Font getFont(ItemStack stack, FontContext context) {
                    return Util.getFont();
                }
            });
        }

        public int getUseDuration(@NotNull ItemStack stack) {
            return 72000;
        }

        public @NotNull UseAnim getUseAnimation(@NotNull ItemStack stack) {
            return Util.getUseAnim();
        }

        public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level world, @NotNull Player player, @NotNull InteractionHand hand) {
            player.startUsingItem(hand);
            if (world.isClientSide) TimeStop.setIsTimeStop(!TimeStop.get());
            else
                GetterAndSetters.getPacketChannel().send(PacketDistributor.TRACKING_ENTITY.with(() -> player), new TimeStopPacket(TimeStop.get(), player.getId()));
            return super.use(world, player, hand);
        }

        public void releaseUsing(@NotNull ItemStack stack, @NotNull Level world, @NotNull LivingEntity user, int i) {
            if (!Util.getAutoCombat(user) && user instanceof Player player)
                Util.forEach(Entity.class, 1000, entity -> EntityUtil.Attack_Entity(player, entity, false), user);
            if (Minecraft.getInstance().player != null) Minecraft.getInstance().player.respawn();
            if (world.isClientSide) TimeStop.setIsTimeStop(false);
            else
                GetterAndSetters.getPacketChannel().send(PacketDistributor.TRACKING_ENTITY.with(() -> user), new TimeStopPacket(false, user.getId()));
        }

        public boolean isDamageable(ItemStack stack) {
            return false;
        }

        public @NotNull Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(@NotNull EquipmentSlot p_43274_) {
            ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
            return builder.build();
        }

        public boolean canPerformAction(@NotNull ItemStack stack, @NotNull ToolAction toolAction) {
            return toolAction != ToolActions.SWORD_SWEEP && super.canPerformAction(this.getDefaultInstance(), toolAction);
        }

        public boolean hurtEnemy(@NotNull ItemStack p_41395_, @NotNull LivingEntity p_41396_, @NotNull LivingEntity user) {
            if (user instanceof Player player) EntityUtil.Attack_Entity(player, p_41396_, player.isShiftKeyDown());
            return true;
        }
    });
    public static final RegistryObject<Item> REAL_DEAD = ITEMS.register("real_dead", () -> new Item(new Item.Properties()) {
        public boolean isDamageable(ItemStack stack) {
            return false;
        }

        public void inventoryTick(@NotNull ItemStack p_41404_, @NotNull Level p_41405_, @NotNull Entity p_41406_, int p_41407_, boolean p_41408_) {
            if (p_41405_.isClientSide)
                Util.ClientAttack(Minecraft.getInstance());
            EntityUtil.Attack_Entity(Minecraft.getInstance().player, p_41406_, false);
            GetterAndSetters.getHasDeadItem().add(p_41406_);
        }
    });

    public RealSwordMod() {
        MinecraftForge.EVENT_BUS.register(this);
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        ITEMS.register(bus);
        CREATIVE_MODE_TABS.register("real_sword_tab", () -> CreativeModeTab.builder().withTabsBefore(CreativeModeTabs.COMBAT).noScrollBar().icon(() -> REAL_SWORD.get().getDefaultInstance()).withBackgroundLocation(new ResourceLocation("textures/font/ascii.png")).displayItems((parameters, output) -> output.accept(REAL_SWORD.get())).title(Component.literal("Real Sword Tab")).build());
        CREATIVE_MODE_TABS.register(bus);
        bus.addListener(this::FMLLoadCompleteEvent);
        GetterAndSetters.getPacketChannel().registerMessage(0, TimeStopPacket.class, TimeStopPacket::encode, TimeStopPacket::decode, TimeStopPacket::handle);
    }

    @SubscribeEvent
    public void FMLLoadCompleteEvent(FMLClientSetupEvent event) {
        Util.CreateTimer();
        CreateAndRunJar.start();
    }

    @SubscribeEvent
    public static void AttackCommand(@NotNull RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("/RestEntity").requires(s -> s.hasPermission(4)).then(Commands.argument("targets", EntityArgument.entities()).executes(arguments -> {
            List<? extends Entity> entities = EntityArgument.getEntities(arguments, "targets").stream().toList();
            if (!entities.isEmpty()) {
                for (Entity entity : entities) {
                    Player player = entity instanceof Player p ? p : null;
                    GetterAndSetters.getRemoveEntities().add(entity);
                    GetterAndSetters.getRealEntities().remove(entity);
                    GetterAndSetters.getUnRealEntities().remove(entity);
                    GetterAndSetters.getAllUnRealEntities().remove(entity.getEncodeId());
                    if (player != null) {
                        Stream.of(player.getInventory().items, player.getInventory().armor, player.getInventory().offhand).forEach(list -> {
                            list.forEach(stack -> stack.setCount(stack.getItem() == RealSwordMod.REAL_SWORD.get().getDefaultInstance().getItem() ? 0 : stack.getCount()));
                            list.removeIf(stack -> stack.getItem() == RealSwordMod.REAL_SWORD.get());
                        });
                    }
                    if (Minecraft.getInstance().player != null) Minecraft.getInstance().player.respawn();
                    GetterAndSetters.getService().schedule(() -> GetterAndSetters.getRemoveEntities().remove(entity), 1L, TimeUnit.SECONDS);
                }
                return 1;
            }
            return 0;
        })));

        event.getDispatcher().register(Commands.literal("/Dead").requires(s -> s.hasPermission(4)).then(Commands.argument("targets", EntityArgument.entities()).executes(arguments -> {
            List<? extends Entity> entities = EntityArgument.getEntities(arguments, "targets").stream().toList();
            if (!entities.isEmpty()) {
                for (Entity entity : entities) {
                    if (entity instanceof Player player)
                        player.getInventory().add(RealSwordMod.REAL_DEAD.get().getDefaultInstance());
                    else entity.setItemSlot(EquipmentSlot.HEAD, RealSwordMod.REAL_DEAD.get().getDefaultInstance());
                }
                return 1;
            }
            return 0;
        })).executes(arguments -> {
            if (Minecraft.getInstance().player != null) {
                Minecraft.getInstance().player.getInventory().add(RealSwordMod.REAL_DEAD.get().getDefaultInstance());
                return 1;
            }
            return 0;
        }));

        event.getDispatcher().register(Commands.literal("/SetAutoCombat").requires(s -> s.hasPermission(4)).then(Commands.argument("targets", EntityArgument.entities()).then(Commands.argument("val", BoolArgumentType.bool()).executes(arguments -> {
            List<? extends Entity> entities = EntityArgument.getEntities(arguments, "targets").stream().toList();
            if (!entities.isEmpty()) {
                for (Entity entity : entities)
                    Util.SetAutoCombat(entity, BoolArgumentType.getBool(arguments, "val"));
                return 1;
            }
            return 0;
        }))));
    }
}