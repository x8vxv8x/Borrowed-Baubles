package com.smd.borrowedbaubles.init;

import com.smd.borrowedbaubles.Tags;
import com.smd.borrowedbaubles.item.ItemDivineInterpreter;
import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = Tags.MOD_ID)
public final class ModItems {

    public static final Item DIVINE_INTERPRETER = new ItemDivineInterpreter();

    private ModItems() {
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        event.getRegistry().register(DIVINE_INTERPRETER);
    }
}
