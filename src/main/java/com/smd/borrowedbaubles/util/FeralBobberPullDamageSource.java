package com.smd.borrowedbaubles.util;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EntityDamageSourceIndirect;

public class FeralBobberPullDamageSource extends EntityDamageSourceIndirect {

    public static final String DAMAGE_TYPE = "borrowedbaubles_feral_bobber_pull";

    public FeralBobberPullDamageSource(Entity immediateSource, EntityLivingBase trueSource) {
        super(DAMAGE_TYPE, immediateSource, trueSource);
        setProjectile();
    }
}
