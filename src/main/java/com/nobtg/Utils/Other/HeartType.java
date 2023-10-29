package com.nobtg.Utils.Other;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public enum HeartType {
    CONTAINER(0, false),
    NORMAL(2, true);
    private final int index;
    private final boolean canBlink;

    HeartType(int p_168729_, boolean p_168730_) {
        this.index = p_168729_;
        this.canBlink = p_168730_;
    }

    public int getX(boolean p_168735_, boolean p_168736_) {
        return 16 + (this.index * 2 + (this == CONTAINER ? (p_168736_ ? 1 : 0) : ((p_168735_ ? 1 : 0) + (this.canBlink && p_168736_ ? 2 : 0)))) * 9;
    }
}