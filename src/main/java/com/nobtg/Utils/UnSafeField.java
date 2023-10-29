package com.nobtg.Utils;

import com.mojang.datafixers.util.Pair;
import com.nobtg.RealSwordMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

abstract class UnSafeField {
    protected static final Pair<String[], String[]> Tooltips = new Pair<>(new String[]{"In the vast expanse of reality, the cosmos and actuality converge, exuding a formidable essence devoid of any contrived intricacies.", "現実の広がりの中で、宇宙と実在が交わり、作り事の入り組んだ複雑さの一切がない、堂々たるエッセンスを放っているんだ。", "在这无边的宇宙和现实的交汇点，散发出一种强大而不经意的本质，没有任何刻意设计的复杂性。", "宇宙的微妙之处难以捉摸，或许即是如此。", "宇宙の微妙なところは捉えどころがありません。あるいは、実際にそうなのかもしれません。", "The subtleties of the universe are elusive, or so they may be."}, new String[]{"Real Sword", "真実の剣", "真实之剑"});
    protected static final SimpleChannel SIMPLE_CHANNEL = NetworkRegistry.newSimpleChannel(new ResourceLocation(RealSwordMod.MOD_ID, "main"), () -> "1", "1"::equals, "1"::equals);
}
