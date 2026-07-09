package com.smd.borrowedbaubles.init;

import com.smd.borrowedbaubles.Tags;
import com.smd.borrowedbaubles.item.ItemDivineInterpreter;
import com.smd.borrowedbaubles.item.ItemFeralBobber;
import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = Tags.MOD_ID)
public final class ModItems {

    public static final Item DIVINE_INTERPRETER = new ItemDivineInterpreter();
    public static final Item FERAL_BOBBER = new ItemFeralBobber();

    private ModItems() {
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        event.getRegistry().register(DIVINE_INTERPRETER);
        event.getRegistry().register(FERAL_BOBBER);
    }
}
