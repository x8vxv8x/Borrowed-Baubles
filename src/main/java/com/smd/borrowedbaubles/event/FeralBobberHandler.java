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
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import slimeknights.tconstruct.library.tools.ToolCore;
import slimeknights.tconstruct.library.utils.ToolHelper;

@Mod.EventBusSubscriber(modid = Tags.MOD_ID)
public final class FeralBobberHandler {

    private static final int DAMAGE_INTERVAL_TICKS = 2;
    private static final float FLAT_HEALTH_COST = 2.0F;
    private static final float MAX_HEALTH_COST_RATE = 0.01F;

    private FeralBobberHandler() {
    }

    @SubscribeEvent
    public static void onFishingRodHookedEntityTick(FishingRodHookedEntityTickEvent event) {
        EntityPlayer player = event.getEntityPlayer();
        Entity target = event.getTarget();
        ItemStack rod = event.getFishingRod();

        if (player == null
                || player.world.isRemote
                || target == null
                || target.isDead
                || !(target instanceof EntityLivingBase)
                || !(rod.getItem() instanceof ToolCore)
                || ToolHelper.isBroken(rod)
                || BaublesApi.isBaubleEquipped(player, ModItems.FERAL_BOBBER) < 0
                || !event.shouldExecute(DAMAGE_INTERVAL_TICKS)) {
            return;
        }

        float maxHealth = (float) player.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).getAttributeValue();
        float healthCost = Math.max(FLAT_HEALTH_COST, maxHealth * MAX_HEALTH_COST_RATE);
        player.setHealth(player.getHealth() - healthCost);

        float missingHealthRate = maxHealth <= 0.0F ? 0.0F : (maxHealth - player.getHealth()) / maxHealth;
        float damageMultiplier = ConfigHandler.feral_bobber_pull_multiplier * Math.max(1.0F, 1.0F + missingHealthRate);

        EntityFishHook hook = event.getHook();
        FeralBobberPullDamageSource damageSource = new FeralBobberPullDamageSource(hook, player);
        ToolAttackHelper.attackEntityRight(rod, (ToolCore) rod.getItem(), player, target, damageMultiplier, damageSource);
    }
}
