package com.smd.borrowedbaubles.event;

import baubles.api.BaublesApi;
import com.smd.borrowedbaubles.Tags;
import com.smd.borrowedbaubles.config.ConfigHandler;
import com.smd.borrowedbaubles.init.ModItems;
import com.smd.borrowedbaubles.util.FeralBobberPullDamageSource;
import com.smd.tcongreedyaddon.tools.fishingrod.FishingRodHookedEntityTickEvent;
import com.smd.tcongreedyaddon.util.ToolAttackHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import slimeknights.tconstruct.library.tools.ToolCore;
import slimeknights.tconstruct.library.utils.ToolHelper;

@Mod.EventBusSubscriber(modid = Tags.MOD_ID)
public final class FeralBobberHandler {

    private static final int DAMAGE_INTERVAL_TICKS = 4;
    private static final float FLAT_HEALTH_COST = 4.0F;
    private static final float MAX_HEALTH_COST_RATE = 0.02F;
    private static final float MISSING_HEALTH_DIVISOR = 100.0F;

    private FeralBobberHandler() {}

    @SubscribeEvent
    public static void onFishingRodHookedEntityTick(FishingRodHookedEntityTickEvent event) {

        EntityPlayer player = event.getEntityPlayer();
        Entity target = event.getTarget();
        ItemStack rod = event.getFishingRod();

        if (player == null || player.world.isRemote
                || target == null || target.isDead
                || !(target instanceof EntityLivingBase)
                || !(rod.getItem() instanceof ToolCore)
                || ToolHelper.isBroken(rod)
                || BaublesApi.isBaubleEquipped(player, ModItems.FERAL_BOBBER) < 0
                || !event.shouldExecute(DAMAGE_INTERVAL_TICKS)) {
            return;
        }

        float maxHealth = player.getMaxHealth();
        float healthCost = Math.max(FLAT_HEALTH_COST, maxHealth * MAX_HEALTH_COST_RATE);
        float safeHealth = Math.max(0, player.getHealth() - healthCost);
        player.setHealth(safeHealth);

        float damageMultiplier = calculateDamageMultiplier(player);
        EntityFishHook hook = event.getHook();

        FeralBobberPullDamageSource damageSource = new FeralBobberPullDamageSource(hook, player);

        float healthPercent = player.getHealth() / player.getMaxHealth();

        if (healthPercent <= 0.1F) {
            damageSource.setMagicDamage().setDamageIsAbsolute();
        } else if (healthPercent <= 0.3F) {
            damageSource.setMagicDamage();
        }
        ToolAttackHelper.attackEntityRight(rod, (ToolCore) rod.getItem(), player, target, damageMultiplier, damageSource);
    }

    private static float calculateDamageMultiplier(EntityPlayer player) {
        float missingHealth = player.getMaxHealth() - player.getHealth();
        float missingRate = missingHealth / MISSING_HEALTH_DIVISOR;
        float missingMult = 1.0F + missingRate * ConfigHandler.feral_bobber_pull_missing_health_scaling;
        return ConfigHandler.feral_bobber_pull_base_multiplier * missingMult;
    }
}