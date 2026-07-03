package com.smd.borrowedbaubles.util;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SPacketEntityVelocity;
import net.minecraft.stats.StatList;
import net.minecraft.util.DamageSource;
import slimeknights.tconstruct.library.tools.ToolCore;
import slimeknights.tconstruct.library.tools.ranged.ILauncher;
import slimeknights.tconstruct.library.tools.ranged.IProjectile;
import slimeknights.tconstruct.library.traits.ITrait;
import slimeknights.tconstruct.library.utils.TinkerUtil;
import slimeknights.tconstruct.library.utils.ToolHelper;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public final class TinkerDamageHelper {

    private static final UUID PROJECTILE_POWER_MODIFIER = UUID.fromString("5c46cff4-a970-4974-bf31-6f8dbf80eb0d");

    private TinkerDamageHelper() {
    }

    public static float simulateOffhandMeleeDamage(ItemStack offhandTool, EntityPlayer player, EntityLivingBase target) {
        if (offhandTool.isEmpty() || !(offhandTool.getItem() instanceof ToolCore) || ToolHelper.isBroken(offhandTool)) {
            return 0.0F;
        }

        ToolCore tool = (ToolCore) offhandTool.getItem();
        unequip(player, EntityEquipmentSlot.MAINHAND);
        unequip(player, EntityEquipmentSlot.OFFHAND);
        player.getAttributeMap().applyAttributeModifiers(offhandTool.getAttributeModifiers(EntityEquipmentSlot.MAINHAND));

        try {
            float baseDamage = (float) player.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
            return calculateToolDamage(offhandTool, tool, player, target, baseDamage);
        } finally {
            player.getAttributeMap().removeAttributeModifiers(offhandTool.getAttributeModifiers(EntityEquipmentSlot.MAINHAND));
            equip(player, EntityEquipmentSlot.MAINHAND);
            equip(player, EntityEquipmentSlot.OFFHAND);
        }
    }

    public static float applyProjectileDamageModifiers(float baseDamage, ItemStack launcherStack, ItemStack projectileStack, float power) {
        Multimap<String, AttributeModifier> projectileAttributes = HashMultimap.create();
        if (projectileStack.getItem() instanceof IProjectile) {
            projectileAttributes.putAll(((IProjectile) projectileStack.getItem()).getProjectileAttributeModifier(projectileStack));
        }
        if (launcherStack.getItem() instanceof ILauncher) {
            ((ILauncher) launcherStack.getItem()).modifyProjectileAttributes(projectileAttributes, launcherStack, projectileStack, power);
        }

        projectileAttributes.put(
                SharedMonsterAttributes.ATTACK_DAMAGE.getName(),
                new AttributeModifier(PROJECTILE_POWER_MODIFIER, "Borrowed Baubles power modifier", power - 1.0F, 2)
        );
        return applyAttackDamageModifiers(baseDamage, projectileAttributes.get(SharedMonsterAttributes.ATTACK_DAMAGE.getName()));
    }

    public static boolean attackEntityWithCustomDamage(ItemStack projectileStack, EntityPlayer player, EntityLivingBase target, Entity projectileEntity, float baseDamage, DamageSource damageSource) {
        return attackEntityWithCustomDamageResult(projectileStack, player, target, projectileEntity, baseDamage, damageSource).hit;
    }

    public static AttackResult attackEntityWithCustomDamageResult(ItemStack projectileStack, EntityPlayer player, EntityLivingBase target, Entity projectileEntity, float baseDamage, DamageSource damageSource) {
        if (projectileStack.isEmpty() || !(projectileStack.getItem() instanceof ToolCore) || ToolHelper.isBroken(projectileStack)) {
            return AttackResult.miss();
        }

        ToolCore tool = (ToolCore) projectileStack.getItem();
        List<ITrait> traits = TinkerUtil.getTraitsOrdered(projectileStack);
        boolean isCritical = isCriticalHit(player, projectileStack, target, traits);
        float damage = calculateToolDamage(projectileStack, tool, player, target, baseDamage, traits, isCritical);

        float baseKnockback = player.isSprinting() ? 1.0F : 0.0F;
        float knockback = baseKnockback;
        for (ITrait trait : traits) {
            knockback = trait.knockBack(projectileStack, player, target, damage, baseKnockback, knockback, isCritical);
        }

        float oldHealth = target.getHealth();
        double oldVelX = target.motionX;
        double oldVelY = target.motionY;
        double oldVelZ = target.motionZ;
        int originalHurtResistantTime = target.hurtResistantTime;

        for (ITrait trait : traits) {
            trait.onHit(projectileStack, player, target, damage, isCritical);
            target.hurtResistantTime = originalHurtResistantTime;
        }

        target.hurtResistantTime = 0;
        boolean hit = target.attackEntityFrom(damageSource, damage);
        if (!hit) {
            target.hurtResistantTime = originalHurtResistantTime;
            return AttackResult.miss();
        }

        float damageDealt = oldHealth - target.getHealth();
        oldVelX = target.motionX = oldVelX + (target.motionX - oldVelX) * (double) tool.knockback();
        oldVelY = target.motionY = oldVelY + (target.motionY - oldVelY) * (double) tool.knockback() / 3.0D;
        oldVelZ = target.motionZ = oldVelZ + (target.motionZ - oldVelZ) * (double) tool.knockback();

        if (knockback > 0.0F) {
            double velX = -Math.sin(player.rotationYaw * (float) Math.PI / 180.0F) * knockback * 0.5F;
            double velZ = Math.cos(player.rotationYaw * (float) Math.PI / 180.0F) * knockback * 0.5F;
            target.addVelocity(velX, 0.1D, velZ);
            player.motionX *= 0.6D;
            player.motionZ *= 0.6D;
            player.setSprinting(false);
        }

        if (target instanceof EntityPlayerMP && target.velocityChanged) {
            ((EntityPlayerMP) target).connection.sendPacket(new SPacketEntityVelocity(target));
            target.velocityChanged = false;
            target.motionX = oldVelX;
            target.motionY = oldVelY;
            target.motionZ = oldVelZ;
        }

        if (isCritical) {
            player.onCriticalHit(target);
        }
        if (damage > baseDamage) {
            player.onEnchantmentCritical(target);
        }

        player.setLastAttackedEntity(target);

        for (ITrait trait : traits) {
            trait.afterHit(projectileStack, player, target, damageDealt, isCritical, true);
        }

        projectileStack.hitEntity(target, player);
        player.addStat(StatList.DAMAGE_DEALT, Math.round(damageDealt * 10.0F));
        player.addExhaustion(0.3F);
        player.world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_CRIT, player.getSoundCategory(), 0.7F, 1.0F);
        return new AttackResult(true, damage, damageDealt);
    }

    private static float calculateToolDamage(ItemStack stack, ToolCore tool, EntityLivingBase attacker, EntityLivingBase target, float baseDamage) {
        List<ITrait> traits = TinkerUtil.getTraitsOrdered(stack);
        boolean isCritical = isCriticalHit(attacker, stack, target, traits);
        return calculateToolDamage(stack, tool, attacker, target, baseDamage, traits, isCritical);
    }

    private static float calculateToolDamage(ItemStack stack, ToolCore tool, EntityLivingBase attacker, EntityLivingBase target, float baseDamage, List<ITrait> traits, boolean isCritical) {
        float damage = baseDamage;
        for (ITrait trait : traits) {
            damage = trait.damage(stack, attacker, target, baseDamage, damage, isCritical);
        }

        if (isCritical) {
            damage *= 1.5F;
        }

        return ToolHelper.calcCutoffDamage(damage, tool.damageCutoff());
    }

    private static boolean isCriticalHit(EntityLivingBase attacker, ItemStack stack, EntityLivingBase target, List<ITrait> traits) {
        boolean isCritical = attacker.fallDistance > 0.0F
                && !attacker.onGround
                && !attacker.isOnLadder()
                && !attacker.isInWater()
                && !attacker.isPotionActive(MobEffects.BLINDNESS)
                && !attacker.isRiding();

        for (ITrait trait : traits) {
            if (trait.isCriticalHit(stack, attacker, target)) {
                isCritical = true;
            }
        }
        return isCritical;
    }

    private static float applyAttackDamageModifiers(float baseDamage, Collection<AttributeModifier> modifiers) {
        double value = baseDamage;
        for (AttributeModifier modifier : modifiers) {
            if (modifier.getOperation() == 0) {
                value += modifier.getAmount();
            }
        }

        double withMultipliers = value;
        for (AttributeModifier modifier : modifiers) {
            if (modifier.getOperation() == 1) {
                withMultipliers += value * modifier.getAmount();
            }
        }

        double result = withMultipliers;
        for (AttributeModifier modifier : modifiers) {
            if (modifier.getOperation() == 2) {
                result *= 1.0D + modifier.getAmount();
            }
        }
        return (float) result;
    }

    private static void unequip(EntityLivingBase entity, EntityEquipmentSlot slot) {
        ItemStack stack = entity.getItemStackFromSlot(slot);
        if (!stack.isEmpty()) {
            entity.getAttributeMap().removeAttributeModifiers(stack.getAttributeModifiers(slot));
        }
    }

    private static void equip(EntityLivingBase entity, EntityEquipmentSlot slot) {
        ItemStack stack = entity.getItemStackFromSlot(slot);
        if (!stack.isEmpty()) {
            entity.getAttributeMap().applyAttributeModifiers(stack.getAttributeModifiers(slot));
        }
    }

    public static final class AttackResult {

        public final boolean hit;
        public final float attemptedDamage;
        public final float dealtDamage;

        private AttackResult(boolean hit, float attemptedDamage, float dealtDamage) {
            this.hit = hit;
            this.attemptedDamage = attemptedDamage;
            this.dealtDamage = dealtDamage;
        }

        private static AttackResult miss() {
            return new AttackResult(false, 0.0F, 0.0F);
        }
    }
}
