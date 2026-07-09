package com.smd.borrowedbaubles.event;

import baubles.api.BaublesApi;
import com.smd.borrowedbaubles.Tags;
import com.smd.borrowedbaubles.config.ConfigHandler;
import com.smd.borrowedbaubles.init.ModItems;
import com.smd.borrowedbaubles.util.FeralBobberSurpriseDamageSource;
import com.smd.borrowedbaubles.util.TinkerDamageHelper;
import com.smd.borrowedbaubles.util.TranslatorMagicDamageSource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import slimeknights.tconstruct.library.entity.EntityProjectileBase;
import slimeknights.tconstruct.library.tools.ToolCore;
import slimeknights.tconstruct.library.tools.ranged.BowCore;
import slimeknights.tconstruct.library.tools.ranged.ProjectileCore;
import slimeknights.tconstruct.library.utils.ToolHelper;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = Tags.MOD_ID)
public final class DivineInterpreterHandler {

    private static final List<PendingAction> PENDING_STRIKES = new ArrayList<>();
    private static final float FERAL_SURPRISE_FLAT_HEALTH_COST = 3.0F;
    private static final float FERAL_SURPRISE_MAX_HEALTH_COST_RATE = 0.03F;

    private DivineInterpreterHandler() {
    }

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent event) {
        if (event.getEntityLiving().getEntityWorld().isRemote || event.getAmount() <= 0.0F) {
            return;
        }
        if (event.getSource() instanceof TranslatorMagicDamageSource) {
            return;
        }
        if (!(event.getEntityLiving() instanceof IMob)) {
            return;
        }
        if (!(event.getSource().getTrueSource() instanceof EntityPlayer)) {
            return;
        }

        EntityPlayer player = (EntityPlayer) event.getSource().getTrueSource();
        if (BaublesApi.isBaubleEquipped(player, ModItems.DIVINE_INTERPRETER) < 0) {
            return;
        }
        if (tryQueueFeralBobberStrike(event, player)) {
            return;
        }
        if (!(event.getSource().getImmediateSource() instanceof EntityProjectileBase)) {
            return;
        }

        EntityProjectileBase projectile = (EntityProjectileBase) event.getSource().getImmediateSource();
        ItemStack mainhand = player.getHeldItemMainhand();
        ItemStack offhand = player.getHeldItemOffhand();

        if (!(mainhand.getItem() instanceof BowCore) || ToolHelper.isBroken(mainhand)) {
            return;
        }
        if (!(offhand.getItem() instanceof ToolCore) || ToolHelper.isBroken(offhand)) {
            return;
        }
        if (!(projectile.tinkerProjectile.getLaunchingStack().getItem() instanceof BowCore)) {
            return;
        }
        if (!(projectile.tinkerProjectile.getItemStack().getItem() instanceof ProjectileCore)) {
            return;
        }
        if (player.getRNG().nextFloat() >= ConfigHandler.proc_chance) {
            return;
        }

        PENDING_STRIKES.add(new PendingStrike(player, event.getEntityLiving(), projectile, offhand.copy()));
    }

    private static boolean tryQueueFeralBobberStrike(LivingDamageEvent event, EntityPlayer player) {
        Entity immediateSource = event.getSource().getImmediateSource();
        if (!event.getSource().isProjectile()
                || !(immediateSource instanceof EntityFishHook)
                || BaublesApi.isBaubleEquipped(player, ModItems.FERAL_BOBBER) < 0) {
            return false;
        }

        ItemStack offhand = player.getHeldItemOffhand();
        if (!(offhand.getItem() instanceof ToolCore) || ToolHelper.isBroken(offhand)) {
            return false;
        }
        if (player.getRNG().nextFloat() >= ConfigHandler.proc_chance) {
            return true;
        }

        PENDING_STRIKES.add(new PendingFeralBobberStrike(player, event.getEntityLiving(), immediateSource, offhand.copy()));
        return true;
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || PENDING_STRIKES.isEmpty()) {
            return;
        }

        List<PendingAction> strikes = new ArrayList<>(PENDING_STRIKES);
        PENDING_STRIKES.clear();
        for (PendingAction strike : strikes) {
            strike.execute();
        }
    }

    private interface PendingAction {
        void execute();
    }

    private static final class PendingStrike implements PendingAction {

        private final EntityPlayer player;
        private final net.minecraft.entity.EntityLivingBase target;
        private final EntityProjectileBase projectile;
        private final ItemStack offhandTool;

        private PendingStrike(EntityPlayer player, net.minecraft.entity.EntityLivingBase target, EntityProjectileBase projectile, ItemStack offhandTool) {
            this.player = player;
            this.target = target;
            this.projectile = projectile;
            this.offhandTool = offhandTool;
        }

        @Override
        public void execute() {
            if (player == null || target == null || projectile == null) {
                return;
            }
            if (player.isDead || target.isDead || player.getEntityWorld().isRemote || player.getEntityWorld() != target.getEntityWorld()) {
                return;
            }
            if (!(offhandTool.getItem() instanceof ToolCore) || ToolHelper.isBroken(offhandTool)) {
                return;
            }

            ItemStack projectileStack = projectile.tinkerProjectile.getItemStack();
            ItemStack launcherStack = projectile.tinkerProjectile.getLaunchingStack();
            if (!(projectileStack.getItem() instanceof ProjectileCore) || ToolHelper.isBroken(projectileStack)) {
                return;
            }
            if (!(launcherStack.getItem() instanceof BowCore) || ToolHelper.isBroken(launcherStack)) {
                return;
            }

            float offhandDamage = TinkerDamageHelper.simulateOffhandMeleeDamage(offhandTool, player, target);
            if (offhandDamage <= 0.0F) {
                return;
            }

            float projectileBaseDamage = TinkerDamageHelper.applyProjectileDamageModifiers(
                    offhandDamage,
                    launcherStack,
                    projectileStack,
                    projectile.tinkerProjectile.getPower()
            );
            if (projectileBaseDamage <= 0.0F) {
                return;
            }

            float finalDamage = projectileBaseDamage * ConfigHandler.damagemultiplier;

            TinkerDamageHelper.AttackResult result = TinkerDamageHelper.attackEntityWithCustomDamageResult(
                    projectileStack,
                    player,
                    target,
                    projectile,
                    finalDamage,
                    new TranslatorMagicDamageSource(projectile, player)
            );
            if (result.hit && player.getEntityWorld() instanceof WorldServer) {
                finishTranslatorHit((WorldServer) player.getEntityWorld(), player, target, projectile, result.attemptedDamage, false);
            }
        }
    }

    private static final class PendingFeralBobberStrike implements PendingAction {

        private final EntityPlayer player;
        private final EntityLivingBase target;
        private final Entity immediateSource;
        private final ItemStack offhandTool;

        private PendingFeralBobberStrike(EntityPlayer player, EntityLivingBase target, Entity immediateSource, ItemStack offhandTool) {
            this.player = player;
            this.target = target;
            this.immediateSource = immediateSource;
            this.offhandTool = offhandTool;
        }

        @Override
        public void execute() {
            if (player == null || target == null || immediateSource == null) {
                return;
            }
            if (player.isDead || target.isDead || player.getEntityWorld().isRemote || player.getEntityWorld() != target.getEntityWorld()) {
                return;
            }
            if (!(offhandTool.getItem() instanceof ToolCore) || ToolHelper.isBroken(offhandTool)) {
                return;
            }

            float offhandDamage = TinkerDamageHelper.simulateOffhandMeleeDamage(offhandTool, player, target);
            if (offhandDamage <= 0.0F) {
                return;
            }

            float maxHealth = (float) player.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).getAttributeValue();
            float healthCost = Math.max(FERAL_SURPRISE_FLAT_HEALTH_COST, maxHealth * FERAL_SURPRISE_MAX_HEALTH_COST_RATE);
            player.setHealth(player.getHealth() - healthCost);

            float finalDamage = offhandDamage
                    * ConfigHandler.damagemultiplier
                    * ConfigHandler.feral_bobber_surprise_multiplier;
            target.hurtResistantTime = 0;
            boolean hit = target.attackEntityFrom(new FeralBobberSurpriseDamageSource(immediateSource, player), finalDamage);
            if (hit && player.getEntityWorld() instanceof WorldServer) {
                finishTranslatorHit((WorldServer) player.getEntityWorld(), player, target, immediateSource, finalDamage, true);
            }
        }
    }

    private static void finishTranslatorHit(WorldServer world, EntityPlayer player, EntityLivingBase target,
                                            Entity immediateSource, float damageAmount, boolean feralBobberDamage) {
        world.spawnParticle(
                EnumParticleTypes.SPELL_MOB,
                target.posX,
                target.posY + target.height * 0.5D,
                target.posZ,
                16,
                0.45D,
                0.00D,
                0.60D,
                1.0D
        );
        if (!target.isEntityAlive()) {
            player.sendMessage(new TextComponentTranslation(
                    "chat." + Tags.MOD_ID + ".translator_kill",
                    target.getDisplayName(),
                    player.getDisplayName()
            ));
        }
        applyArcDamage(world, player, target, immediateSource, damageAmount, feralBobberDamage);
    }

    private static void applyArcDamage(WorldServer world, EntityPlayer player, EntityLivingBase target,
                                       Entity immediateSource, float damageAmount, boolean feralBobberDamage) {
        if (damageAmount <= 0.0F) {
            return;
        }

        AxisAlignedBB area = new AxisAlignedBB(
                target.posX - ConfigHandler.arc_radius,
                target.posY - ConfigHandler.arc_radius,
                target.posZ - ConfigHandler.arc_radius,
                target.posX + ConfigHandler.arc_radius,
                target.posY + target.height + ConfigHandler.arc_radius,
                target.posZ + ConfigHandler.arc_radius
        );

        List<EntityLivingBase> nearbyTargets = world.getEntitiesWithinAABB(EntityLivingBase.class, area);
        for (EntityLivingBase nearby : nearbyTargets) {
            if (nearby == null || nearby == target || nearby == player || nearby.isDead) {
                continue;
            }

            boolean hit = nearby.attackEntityFrom(
                    feralBobberDamage
                            ? new FeralBobberSurpriseDamageSource(TranslatorMagicDamageSource.ARC_DAMAGE_TYPE, immediateSource, player)
                            : new TranslatorMagicDamageSource(TranslatorMagicDamageSource.ARC_DAMAGE_TYPE, immediateSource, player),
                    damageAmount);
            if (!hit) {
                continue;
            }

            world.spawnParticle(
                    EnumParticleTypes.SPELL_MOB,
                    nearby.posX,
                    nearby.posY + nearby.height * 0.5D,
                    nearby.posZ,
                    10,
                    0.35D,
                    0.00D,
                    0.50D,
                    1.0D
            );

            if (!nearby.isEntityAlive()) {
                player.sendMessage(new TextComponentTranslation(
                        "chat." + Tags.MOD_ID + ".arc_kill",
                        nearby.getDisplayName(),
                        player.getDisplayName()
                ));
            }
        }
    }
}
