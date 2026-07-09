package com.smd.borrowedbaubles.util;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EntityDamageSourceIndirect;

public class FeralBobberSurpriseDamageSource extends EntityDamageSourceIndirect {

    public FeralBobberSurpriseDamageSource(Entity immediateSource, EntityLivingBase trueSource) {
        this(TranslatorMagicDamageSource.DAMAGE_TYPE, immediateSource, trueSource);
    }

    public FeralBobberSurpriseDamageSource(String damageType, Entity immediateSource, EntityLivingBase trueSource) {
        super(damageType, immediateSource, trueSource);
        setMagicDamage();
        setProjectile();
    }
}
