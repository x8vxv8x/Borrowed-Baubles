package com.smd.borrowedbaubles.config;

import com.smd.borrowedbaubles.Tags;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.Config.Name;
import net.minecraftforge.common.config.Config.Comment;
import net.minecraftforge.common.config.Config.RangeInt;

@Config(modid = Tags.MOD_ID)
public class ConfigHandler {

    @Name("PROC_CHANCE")
    @Comment("神言翻译器的触发几率")
    public static double proc_chance = 1.0;

    @Name("DAMAGE_MULTIPLIER")
    @Comment("奇袭攻击的倍率")
    public static float damagemultiplier = 1.0F;

    @Name("SURPRISE_DAMAGE_MODE")
    @Comment("奇袭攻击伤害模式：0=普通投射物伤害，1=仅穿甲，2=魔法伤害，3=魔法伤害且穿透保护")
    @RangeInt(min = 0, max = 3)
    public static int surprise_damage_mode = 1;

    @Name("ARC_RADIUS")
    @Comment("弧光的索敌范围")
    public static double arc_radius = 2;

    @Name("FERAL_BOBBER_PULL_BASE_MULTIPLIER")
    @Comment("野性浮漂拉回伤害的基础倍率")
    public static float feral_bobber_pull_base_multiplier = 2.0F;

    @Name("FERAL_BOBBER_PULL_MISSING_HEALTH_SCALING")
    @Comment("野性浮漂拉回伤害的已损生命值倍率斜率。倍率 = 基础倍率 * (1 + 已损生命值 * 该值)")
    public static float feral_bobber_pull_missing_health_scaling = 2.0F;

}
