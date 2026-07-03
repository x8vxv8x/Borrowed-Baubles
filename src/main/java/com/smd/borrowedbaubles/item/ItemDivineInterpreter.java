package com.smd.borrowedbaubles.item;

import baubles.api.BaubleType;
import baubles.api.IBauble;
import com.smd.borrowedbaubles.Tags;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class ItemDivineInterpreter extends Item implements IBauble {

    public ItemDivineInterpreter() {
        setRegistryName(Tags.MOD_ID, "divine_interpreter");
        setTranslationKey(Tags.MOD_ID + ".divine_interpreter");
        setCreativeTab(CreativeTabs.COMBAT);
        setMaxStackSize(1);
    }

    @Override
    public BaubleType getBaubleType(ItemStack stack) {
        return BaubleType.CHARM;
    }
}
