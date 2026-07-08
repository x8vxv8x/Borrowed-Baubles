package com.smd.borrowedbaubles.util;

import com.smd.borrowedbaubles.config.ConfigHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EntityDamageSourceIndirect;

public class TranslatorMagicDamageSource extends EntityDamageSourceIndirect {

    public static final String DAMAGE_TYPE = "borrowedbaubles_translator_magic";
    public static final String ARC_DAMAGE_TYPE = "borrowedbaubles_arc_magic";

    public TranslatorMagicDamageSource(Entity immediateSource, EntityLivingBase trueSource) {
        this(DAMAGE_TYPE, immediateSource, trueSource);
    }

    public TranslatorMagicDamageSource(String damageType, Entity immediateSource, EntityLivingBase trueSource) {
        super(damageType, immediateSource, trueSource);
        applyDamageMode();
        setProjectile();
    }

    private void applyDamageMode() {
        switch (ConfigHandler.surprise_damage_mode) {
            case 1:
                setDamageBypassesArmor();
                break;
            case 2:
                setMagicDamage();
                break;
            case 3:
                setMagicDamage();
                setDamageIsAbsolute();
                break;
            case 0:
            default:
                break;
        }
    }
}
