package com.smd.borrowedbaubles;

import com.smd.borrowedbaubles.init.ModItems;
import com.smd.borrowedbaubles.proxy.CommonProxy;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = Tags.MOD_ID,
     name = Tags.MOD_NAME,
     version = Tags.VERSION,
     dependencies = "required-after:tconstruct;required-after:baubles;required-after:tcongreedyaddon")
public class BorrowedBaubles {

    public static final Logger LOGGER = LogManager.getLogger(Tags.MOD_NAME);

    @SidedProxy(serverSide = "com.smd.borrowedbaubles.proxy.CommonProxy",
                clientSide = "com.smd.borrowedbaubles.proxy.ClientProxy")

    public static CommonProxy proxy;

    public static final CreativeTabs TAB = new CreativeTabs(Tags.MOD_ID)
    {
        @Override
        public ItemStack createIcon()
        {
            return new ItemStack(ModItems.DIVINE_INTERPRETER);
        }
    };
}
