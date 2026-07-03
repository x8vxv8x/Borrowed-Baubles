package com.smd.borrowedbaubles.init;

import com.smd.borrowedbaubles.Tags;
import com.smd.borrowedbaubles.item.ItemDivineInterpreter;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Mod.EventBusSubscriber(modid = Tags.MOD_ID)
public final class ModItems {

    public static final Item DIVINE_INTERPRETER = new ItemDivineInterpreter();

    private ModItems() {
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        event.getRegistry().register(DIVINE_INTERPRETER);
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event) {
        ModelLoader.setCustomModelResourceLocation(
                DIVINE_INTERPRETER,
                0,
                new ModelResourceLocation(DIVINE_INTERPRETER.getRegistryName(), "inventory")
        );
    }
}
