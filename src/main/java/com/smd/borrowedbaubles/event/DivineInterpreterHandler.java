package com.smd.borrowedbaubles.event;

import baubles.api.BaublesApi;
import com.smd.borrowedbaubles.Tags;
import com.smd.borrowedbaubles.init.ModItems;
import com.smd.borrowedbaubles.util.TinkerDamageHelper;
import com.smd.borrowedbaubles.util.TranslatorMagicDamageSource;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
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

    private static final float PROC_CHANCE = 1.0F;
    private static final double ARC_RADIUS = 1.0D;
    private static final List<PendingStrike> PENDING_STRIKES = new ArrayList<>();

    private DivineInterpreterHandler() {
    }

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent event) {
        if (event.getEntityLiving().world.isRemote || event.getAmount() <= 0.0F) {
            return;
        }
        if (event.getSource() instanceof TranslatorMagicDamageSource) {
            return;
        }
        if (!(event.getEntityLiving() instanceof IMob)) {
            return;
        }
        if (!(event.getSource().getImmediateSource() instanceof EntityProjectileBase)) {
            return;
        }
        if (!(event.getSource().getTrueSource() instanceof EntityPlayer)) {
            return;
        }

        EntityPlayer player = (EntityPlayer) event.getSource().getTrueSource();
        EntityProjectileBase projectile = (EntityProjectileBase) event.getSource().getImmediateSource();
        ItemStack mainhand = player.getHeldItemMainhand();
        ItemStack offhand = player.getHeldItemOffhand();

        if (!(mainhand.getItem() instanceof BowCore) || ToolHelper.isBroken(mainhand)) {
            return;
        }
        if (!(offhand.getItem() instanceof ToolCore) || ToolHelper.isBroken(offhand)) {
            return;
        }
        if (BaublesApi.isBaubleEquipped(player, ModItems.DIVINE_INTERPRETER) < 0) {
            return;
        }
        if (!(projectile.tinkerProjectile.getLaunchingStack().getItem() instanceof BowCore)) {
            return;
        }
        if (!(projectile.tinkerProjectile.getItemStack().getItem() instanceof ProjectileCore)) {
            return;
        }
        if (player.getRNG().nextFloat() >= PROC_CHANCE) {
            return;
        }

        PENDING_STRIKES.add(new PendingStrike(player, event.getEntityLiving(), projectile, offhand.copy()));
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || PENDING_STRIKES.isEmpty()) {
            return;
        }

        List<PendingStrike> strikes = new ArrayList<>(PENDING_STRIKES);
        PENDING_STRIKES.clear();
        for (PendingStrike strike : strikes) {
            strike.execute();
        }
    }

    private static final class PendingStrike {

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

        private void execute() {
            if (player == null || target == null || projectile == null) {
                return;
            }
            if (player.isDead || target.isDead || player.world.isRemote || player.world != target.world) {
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

            TinkerDamageHelper.AttackResult result = TinkerDamageHelper.attackEntityWithCustomDamageResult(
                    projectileStack,
                    player,
                    target,
                    projectile,
                    projectileBaseDamage,
                    new TranslatorMagicDamageSource(projectile, player)
            );
            if (result.hit && player.world instanceof WorldServer) {
                WorldServer world = (WorldServer) player.world;
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
                applyArcDamage(world, projectile, result.attemptedDamage);
            }
        }

        private void applyArcDamage(WorldServer world, EntityProjectileBase projectile, float damageAmount) {
            if (damageAmount <= 0.0F) {
                return;
            }

            AxisAlignedBB area = new AxisAlignedBB(
                    target.posX - ARC_RADIUS,
                    target.posY - ARC_RADIUS,
                    target.posZ - ARC_RADIUS,
                    target.posX + ARC_RADIUS,
                    target.posY + target.height + ARC_RADIUS,
                    target.posZ + ARC_RADIUS
            );

            List<EntityLivingBase> nearbyTargets = world.getEntitiesWithinAABB(EntityLivingBase.class, area);
            for (EntityLivingBase nearby : nearbyTargets) {
                if (nearby == null || nearby == target || nearby == player || nearby.isDead) {
                    continue;
                }

                boolean hit = nearby.attackEntityFrom(
                        new TranslatorMagicDamageSource(TranslatorMagicDamageSource.ARC_DAMAGE_TYPE, projectile, player),
                        damageAmount
                );
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
}
