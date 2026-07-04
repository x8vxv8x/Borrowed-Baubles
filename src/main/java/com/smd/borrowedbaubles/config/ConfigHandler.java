package com.smd.borrowedbaubles.config;

import com.smd.borrowedbaubles.Tags;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.Config.Name;
import net.minecraftforge.common.config.Config.Comment;

@Config(modid = Tags.MOD_ID)
public class ConfigHandler {

    @Name("PROC_CHANCE")
    @Comment("神言翻译器的触发几率")
    public static double proc_chance = 1.0;

    @Name("DAMAGE_MULTIPLIER")
    @Comment("奇袭攻击的倍率")
    public static float damagemultiplier = 1.0F;

    @Name("ARC_RADIUS")
    @Comment("弧光的索敌范围")
    public static double arc_radius = 2;

}
