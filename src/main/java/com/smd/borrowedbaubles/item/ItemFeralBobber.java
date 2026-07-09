package com.smd.borrowedbaubles.item;

import baubles.api.BaubleType;
import baubles.api.IBauble;
import com.smd.borrowedbaubles.BorrowedBaubles;
import com.smd.borrowedbaubles.Tags;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class ItemFeralBobber extends Item implements IBauble {

    public ItemFeralBobber() {
        setRegistryName(Tags.MOD_ID, "feral_bobber");
        setTranslationKey(Tags.MOD_ID + ".feral_bobber");
        setCreativeTab(BorrowedBaubles.TAB);
        setMaxStackSize(1);
    }

    @Override
    public BaubleType getBaubleType(ItemStack stack) {
        return BaubleType.TRINKET;
    }
}
