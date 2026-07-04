package com.smd.borrowedbaubles.init;

import com.smd.borrowedbaubles.Tags;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(modid = Tags.MOD_ID, value = Side.CLIENT)
public final class ClientModelRegister {

    private ClientModelRegister() {
    }

    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event) {
        ModelLoader.setCustomModelResourceLocation(
                ModItems.DIVINE_INTERPRETER,
                0,
                new ModelResourceLocation(ModItems.DIVINE_INTERPRETER.getRegistryName(), "inventory")
        );
    }
}
