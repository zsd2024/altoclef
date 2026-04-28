package adris.altoclef.multiversion.versionedfields;

import net.minecraft.block.Block;

/**
 * 版本化方块字段
 * 此辅助类实现了在某些 Minecraft 版本中尚未支持的方块
 * 在不支持的版本中使用这些方块可能会导致奇怪的错误或崩溃...
 * 请参阅 {@link VersionedFieldHelper#isSupported(Object)} 方法检查支持情况
 */
public abstract class Blocks extends net.minecraft.block.Blocks {

    /**
     * 表示不受支持的方块占位符
     */
    public static final Block UNSUPPORTED = VersionedFieldHelper.createUnsafeUnsupportedBlock();

    //#if MC <= 11802
    //$$ /**
    //$$  * 红树林胚轴方块 - 在 Minecraft 1.18.2 及以下版本中不受支持
    //$$  */
    //$$ public static final Block MANGROVE_PROPAGULE = UNSUPPORTED;
    //$$ /**
    //$$  * 樱花树叶方块 - 在 Minecraft 1.18.2 及以下版本中不受支持
    //$$  */
    //$$ public static final Block CHERRY_LEAVES = UNSUPPORTED;
    //$$ /**
    //$$  * 红树林告示牌方块 - 在 Minecraft 1.18.2 及以下版本中不受支持
    //$$  */
    //$$ public static final Block MANGROVE_SIGN = UNSUPPORTED;
    //$$ /**
    //$$  * 红树林墙上的告示牌方块 - 在 Minecraft 1.18.2 及以下版本中不受支持
    //$$  */
    //$$ public static final Block MANGROVE_WALL_SIGN = UNSUPPORTED;
    //$$ /**
    //$$  * 竹子告示牌方块 - 在 Minecraft 1.18.2 及以下版本中不受支持
    //$$  */
    //$$ public static final Block BAMBOO_SIGN = UNSUPPORTED;
    //$$ /**
    //$$  * 竹子墙上的告示牌方块 - 在 Minecraft 1.18.2 及以下版本中不受支持
    //$$  */
    //$$ public static final Block BAMBOO_WALL_SIGN = UNSUPPORTED;
    //$$ /**
    //$$  * 樱花告示牌方块 - 在 Minecraft 1.18.2 及以下版本中不受支持
    //$$  */
    //$$ public static final Block CHERRY_SIGN = UNSUPPORTED;
    //$$ /**
    //$$  * 樱花墙上的告示牌方块 - 在 Minecraft 1.18.2 及以下版本中不受支持
    //$$  */
    //$$ public static final Block CHERRY_WALL_SIGN = UNSUPPORTED;
    //$$ /**
    //$$  * 幽匿块 - 在 Minecraft 1.18.2 及以下版本中不受支持
    //$$  */
    //$$ public static final Block SCULK = UNSUPPORTED;
    //$$ /**
    //$$  * 幽匿脉络 - 在 Minecraft 1.18.2 及以下版本中不受支持
    //$$  */
    //$$ public static final Block SCULK_VEIN = UNSUPPORTED;
    //$$ /**
    //$$  * 幽匿尖啸体 - 在 Minecraft 1.18.2 及以下版本中不受支持
    //$$  */
    //$$ public static final Block SCULK_SHRIEKER = UNSUPPORTED;
    //#endif

    //#if MC <= 11605
    //$$ /**
    //$$  * 开花的杜鹃花丛 - 在 Minecraft 1.16.5 及以下版本中不受支持
    //$$  */
    //$$ public static Block FLOWERING_AZALEA = UNSUPPORTED;
    //$$ /**
    //$$  * 杜鹃花丛 - 在 Minecraft 1.16.5 及以下版本中不受支持
    //$$  */
    //$$ public static Block AZALEA = UNSUPPORTED;
    //$$ /**
    //$$  * 细雪 - 在 Minecraft 1.16.5 及以下版本中不受支持
    //$$  */
    //$$ public static Block POWDER_SNOW = UNSUPPORTED;
    //$$ /**
    //$$  * 大垂滴叶 - 在 Minecraft 1.16.5 及以下版本中不受支持
    //$$  */
    //$$ public static Block BIG_DRIPLEAF = UNSUPPORTED;
    //$$ /**
    //$$  * 大垂滴叶茎 - 在 Minecraft 1.16.5 及以下版本中不受支持
    //$$  */
    //$$ public static Block BIG_DRIPLEAF_STEM = UNSUPPORTED;
    //$$ /**
    //$$  * 洞穴藤蔓植株 - 在 Minecraft 1.16.5 及以下版本中不受支持
    //$$  */
    //$$ public static Block CAVE_VINES_PLANT = UNSUPPORTED;
    //$$ /**
    //$$  * 洞穴藤蔓 - 在 Minecraft 1.16.5 及以下版本中不受支持
    //$$  */
    //$$ public static Block CAVE_VINES = UNSUPPORTED;
    //$$ /**
    //$$  * 小紫水晶芽 - 在 Minecraft 1.16.5 及以下版本中不受支持
    //$$  */
    //$$ public static Block SMALL_AMETHYST_BUD = UNSUPPORTED;
    //$$ /**
    //$$  * 中紫水晶芽 - 在 Minecraft 1.16.5 及以下版本中不受支持
    //$$  */
    //$$ public static Block MEDIUM_AMETHYST_BUD = UNSUPPORTED;
    //$$ /**
    //$$  * 大紫水晶芽 - 在 Minecraft 1.16.5 及以下版本中不受支持
    //$$  */
    //$$ public static Block LARGE_AMETHYST_BUD = UNSUPPORTED;
    //$$ /**
    //$$  * 紫水晶簇 - 在 Minecraft 1.16.5 及以下版本中不受支持
    //$$  */
    //$$ public static Block AMETHYST_CLUSTER = UNSUPPORTED;
    //$$ /**
    //$$  * 方解石 - 在 Minecraft 1.16.5 及以下版本中不受支持
    //$$  */
    //$$ public static Block CALCITE = UNSUPPORTED;
    //$$ /**
    //$$  * 凝灰岩 - 在 Minecraft 1.16.5 及以下版本中不受支持
    //$$  */
    //$$ public static Block TUFF = UNSUPPORTED;
    //$$ /**
    //$$  * 深板岩煤矿石 - 在 Minecraft 1.16.5 及以下版本中不受支持
    //$$  */
    //$$ public static Block DEEPSLATE_COAL_ORE = UNSUPPORTED;
    //$$ /**
    //$$  * 深板岩铁矿石 - 在 Minecraft 1.16.5 及以下版本中不受支持
    //$$  */
    //$$ public static Block DEEPSLATE_IRON_ORE = UNSUPPORTED;
    //$$ /**
    //$$  * 深板岩金矿石 - 在 Minecraft 1.16.5 及以下版本中不受支持
    //$$  */
    //$$ public static Block DEEPSLATE_GOLD_ORE = UNSUPPORTED;
    //$$ /**
    //$$  * 深板岩铜矿石 - 在 Minecraft 1.16.5 及以下版本中不受支持
    //$$  */
    //$$ public static Block DEEPSLATE_COPPER_ORE = UNSUPPORTED;
    //$$ /**
    //$$  * 深板岩钻石矿石 - 在 Minecraft 1.16.5 及以下版本中不受支持
    //$$  */
    //$$ public static Block DEEPSLATE_DIAMOND_ORE = UNSUPPORTED;
    //$$ /**
    //$$  * 深板岩绿宝石矿石 - 在 Minecraft 1.16.5 及以下版本中不受支持
    //$$  */
    //$$ public static Block DEEPSLATE_EMERALD_ORE = UNSUPPORTED;
    //$$ /**
    //$$  * 深板岩红石矿石 - 在 Minecraft 1.16.5 及以下版本中不受支持
    //$$  */
    //$$ public static Block DEEPSLATE_REDSTONE_ORE = UNSUPPORTED;
    //$$ /**
    //$$  * 深板岩青金石矿石 - 在 Minecraft 1.16.5 及以下版本中不受支持
    //$$  */
    //$$ public static Block DEEPSLATE_LAPIS_ORE = UNSUPPORTED;
    //$$ /**
    //$$  * 滴水石锥 - 在 Minecraft 1.16.5 及以下版本中不受支持
    //$$  */
    //$$ public static Block POINTED_DRIPSTONE = UNSUPPORTED;
    //$$ /**
    //$$  * 小垂滴叶 - 在 Minecraft 1.16.5 及以下版本中不受支持
    //$$  */
    //$$ public static Block SMALL_DRIPLEAF = UNSUPPORTED;
    //$$ /**
    //$$  * 铜矿石 - 在 Minecraft 1.16.5 及以下版本中不受支持
    //$$  */
    //$$ public static Block COPPER_ORE = UNSUPPORTED;
    //$$ /**
    //$$  * 发光地衣 - 在 Minecraft 1.16.5 及以下版本中不受支持
    //$$  */
    //$$ public static Block GLOW_LICHEN = UNSUPPORTED;
    //$$ /**
    //$$  * 深板岩砖 - 在 Minecraft 1.16.5 及以下版本中不受支持
    //$$  */
    //$$ public static Block DEEPSLATE_BRICKS = UNSUPPORTED;
    //$$ /**
    //$$  * 幽匿感测器 - 在 Minecraft 1.16.5 及以下版本中不受支持
    //$$  */
    //$$ public static Block SCULK_SENSOR = UNSUPPORTED;
    //$$ /**
    //$$  * 深板岩瓦楼梯 - 在 Minecraft 1.16.5 及以下版本中不受支持
    //$$  */
    //$$ public static Block DEEPSLATE_TILE_STAIRS = UNSUPPORTED;
    //$$ /**
    //$$  * 裂纹深板岩砖 - 在 Minecraft 1.16.5 及以下版本中不受支持
    //$$  */
    //$$ public static Block CRACKED_DEEPSLATE_BRICKS = UNSUPPORTED;
    //$$ /**
    //$$  * 深板岩瓦 - 在 Minecraft 1.16.5 及以下版本中不受支持
    //$$  */
    //$$ public static Block DEEPSLATE_TILES = UNSUPPORTED;
    //$$ /**
    //$$  * 抛光深板岩 - 在 Minecraft 1.16.5 及以下版本中不受支持
    //$$  */
    //$$ public static Block POLISHED_DEEPSLATE = UNSUPPORTED;
    //$$ /**
    //$$  * 紫水晶块 - 在 Minecraft 1.16.5 及以下版本中不受支持
    //$$  */
    //$$ public static Block AMETHYST_BLOCK = UNSUPPORTED;
    //$$ /**
    //$$  * 萌芽紫水晶 - 在 Minecraft 1.16.5 及以下版本中不受支持
    //$$  */
    //$$ public static Block BUDDING_AMETHYST = UNSUPPORTED;
    //$$ /**
    //$$  * 深板岩 - 在 Minecraft 1.16.5 及以下版本中不受支持
    //$$  */
    //$$ public static Block DEEPSLATE = UNSUPPORTED;
    //$$ /**
    //$$  * 粗深板岩 - 在 Minecraft 1.16.5 及以下版本中不受支持
    //$$  */
    //$$ public static Block COBBLED_DEEPSLATE = UNSUPPORTED;
    //$$ /**
    //$$  * 滴水石块 - 在 Minecraft 1.16.5 及以下版本中不受支持
    //$$  */
    //$$ public static Block DRIPSTONE_BLOCK = UNSUPPORTED;
    //#endif

}
