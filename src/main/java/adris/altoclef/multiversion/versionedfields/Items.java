package adris.altoclef.multiversion.versionedfields;


import net.minecraft.item.Item;

/**
 * 版本化物品字段
 * 此辅助类实现了在某些 Minecraft 版本中尚未支持的物品
 * 在不支持的版本中使用这些物品可能会导致奇怪的错误或崩溃...
 * 请参阅 {@link VersionedFieldHelper#isSupported(Object)} 方法检查支持情况
 */
public class Items extends net.minecraft.item.Items {


    /**
     * 表示不受支持的物品占位符
     */
    public static final Item UNSUPPORTED = VersionedFieldHelper.createUnsafeUnsupportedItem();

    //#if MC <= 11802
    //$$ /**
    //$$  * 红树林胚轴物品 - 在 Minecraft 1.18.2 及以下版本中不受支持
    //$$  */
    //$$ public static final Item MANGROVE_PROPAGULE = UNSUPPORTED;
    //$$ /**
    //$$  * 樱花树苗物品 - 在 Minecraft 1.18.2 及以下版本中不受支持
    //$$  */
    //$$ public static final Item CHERRY_SAPLING = UNSUPPORTED;
    //$$ /**
    //$$  * 幽匿催发体物品 - 在 Minecraft 1.18.2 及以下版本中不受支持
    //$$  */
    //$$ public static final Item SCULK_CATALYST = UNSUPPORTED;
    //$$ /**
    //$$  * 红树林木板物品 - 在 Minecraft 1.18.2 及以下版本中不受支持
    //$$  */
    //$$ public static final Item MANGROVE_PLANKS = UNSUPPORTED;
    //$$ /**
    //$$  * 樱花木板物品 - 在 Minecraft 1.18.2 及以下版本中不受支持
    //$$  */
    //$$ public static final Item CHERRY_PLANKS = UNSUPPORTED;
    //$$ /**
    //$$  * 竹木板物品 - 在 Minecraft 1.18.2 及以下版本中不受支持
    //$$  */
    //$$ public static final Item BAMBOO_PLANKS = UNSUPPORTED;
    //$$ /**
    //$$  * 红树林树叶物品 - 在 Minecraft 1.18.2 及以下版本中不受支持
    //$$  */
    //$$ public static final Item MANGROVE_LEAVES = UNSUPPORTED;
    //$$ /**
    //$$  * 樱花树叶物品 - 在 Minecraft 1.18.2 及以下版本中不受支持
    //$$  */
    //$$ public static final Item CHERRY_LEAVES = UNSUPPORTED;
    //$$ /**
    //$$  * 红树林原木物品 - 在 Minecraft 1.18.2 及以下版本中不受支持
    //$$  */
    //$$ public static final Item MANGROVE_WOOD = UNSUPPORTED;
    //$$ /**
    //$$  * 红树林按钮物品 - 在 Minecraft 1.18.2 及以下版本中不受支持
    //$$  */
    //$$ public static final Item MANGROVE_BUTTON = UNSUPPORTED;
    //$$ /**
    //$$  * 竹按钮物品 - 在 Minecraft 1.18.2 及以下版本中不受支持
    //$$  */
    //$$ public static final Item BAMBOO_BUTTON = UNSUPPORTED;
    //$$ /**
    //$$  * 樱花按钮物品 - 在 Minecraft 1.18.2 及以下版本中不受支持
    //$$  */
    //$$ public static final Item CHERRY_BUTTON = UNSUPPORTED;
    //$$ /**
    //$$  * 红树林告示牌物品 - 在 Minecraft 1.18.2 及以下版本中不受支持
    //$$  */
    //$$ public static final Item MANGROVE_SIGN = UNSUPPORTED;
    //$$ /**
    //$$  * 竹告示牌物品 - 在 Minecraft 1.18.2 及以下版本中不受支持
    //$$  */
    //$$ public static final Item BAMBOO_SIGN = UNSUPPORTED;
    //$$ /**
    //$$  * 樱花告示牌物品 - 在 Minecraft 1.18.2 及以下版本中不受支持
    //$$  */
    //$$ public static final Item CHERRY_SIGN = UNSUPPORTED;
    //$$ /**
    //$$  * 金合欢悬挂告示牌物品 - 在 Minecraft 1.18.2 及以下版本中不受支持
    //$$  */
    //$$ public static final Item ACACIA_HANGING_SIGN = UNSUPPORTED;
    //$$ /**
    //$$  * 白桦悬挂告示牌物品 - 在 Minecraft 1.18.2 及以下版本中不受支持
    //$$  */
    //$$ public static final Item BIRCH_HANGING_SIGN = UNSUPPORTED;
    //$$ /**
    //$$  * 绯红悬挂告示牌物品 - 在 Minecraft 1.18.2 及以下版本中不受支持
    //$$  */
    //$$ public static final Item CRIMSON_HANGING_SIGN = UNSUPPORTED;
    //$$ /**
    //$$  * 深色橡木悬挂告示牌物品 - 在 Minecraft 1.18.2 及以下版本中不受支持
    //$$  */
    //$$ public static final Item DARK_OAK_HANGING_SIGN = UNSUPPORTED;
    //$$ /**
    //$$  * 橡木悬挂告示牌物品 - 在 Minecraft 1.18.2 及以下版本中不受支持
    //$$  */
    //$$ public static final Item OAK_HANGING_SIGN = UNSUPPORTED;
    //$$ /**
    //$$  * 丛林悬挂告示牌物品 - 在 Minecraft 1.18.2 及以下版本中不受支持
    //$$  */
    //$$ public static final Item JUNGLE_HANGING_SIGN = UNSUPPORTED;
    //$$ /**
    //$$  * 云杉悬挂告示牌物品 - 在 Minecraft 1.18.2 及以下版本中不受支持
    //$$  */
    //$$ public static final Item SPRUCE_HANGING_SIGN = UNSUPPORTED;
    //$$ /**
    //$$  * 诡异悬挂告示牌物品 - 在 Minecraft 1.18.2 及以下版本中不受支持
    //$$  */
    //$$ public static final Item WARPED_HANGING_SIGN = UNSUPPORTED;
    //$$ /**
    //$$  * 红树林悬挂告示牌物品 - 在 Minecraft 1.18.2 及以下版本中不受支持
    //$$  */
    //$$ public static final Item MANGROVE_HANGING_SIGN = UNSUPPORTED;
    //$$ /**
    //$$  * 竹悬挂告示牌物品 - 在 Minecraft 1.18.2 及以下版本中不受支持
    //$$  */
    //$$ public static final Item BAMBOO_HANGING_SIGN = UNSUPPORTED;
    //$$ /**
    //$$  * 樱花悬挂告示牌物品 - 在 Minecraft 1.18.2 及以下版本中不受支持
    //$$  */
    //$$ public static final Item CHERRY_HANGING_SIGN = UNSUPPORTED;
    //$$ /**
    //$$  * 红树林压力板物品 - 在 Minecraft 1.18.2 及以下版本中不受支持
    //$$  */
    //$$ public static final Item MANGROVE_PRESSURE_PLATE = UNSUPPORTED;
    //$$ /**
    //$$  * 竹压力板物品 - 在 Minecraft 1.18.2 及以下版本中不受支持
    //$$  */
    //$$ public static final Item BAMBOO_PRESSURE_PLATE = UNSUPPORTED;
    //$$ /**
    //$$  * 樱花压力板物品 - 在 Minecraft 1.18.2 及以下版本中不受支持
    //$$  */
    //$$ public static final Item CHERRY_PRESSURE_PLATE = UNSUPPORTED;
    //$$ /**
    //$$  * 红树林栅栏物品 - 在 Minecraft 1.18.2 及以下版本中不受支持
    //$$  */
    //$$ public static final Item MANGROVE_FENCE = UNSUPPORTED;
    //$$ /**
    //$$  * 竹栅栏物品 - 在 Minecraft 1.18.2 及以下版本中不受支持
    //$$  */
    //$$ public static final Item BAMBOO_FENCE = UNSUPPORTED;
    //$$ /**
    //$$  * 樱花栅栏物品 - 在 Minecraft 1.18.2 及以下版本中不受支持
    //$$  */
    //$$ public static final Item CHERRY_FENCE = UNSUPPORTED;
    //$$ /**
    //$$  * 红树林栅栏门物品 - 在 Minecraft 1.18.2 及以下版本中不受支持
    //$$  */
    //$$ public static final Item MANGROVE_FENCE_GATE = UNSUPPORTED;
    //$$ /**
    //$$  * 竹栅栏门物品 - 在 Minecraft 1.18.2 及以下版本中不受支持
    //$$  */
    //$$ public static final Item BAMBOO_FENCE_GATE = UNSUPPORTED;
    //$$ /**
    //$$  * 樱花栅栏门物品 - 在 Minecraft 1.18.2 及以下版本中不受支持
    //$$  */
    //$$ public static final Item CHERRY_FENCE_GATE = UNSUPPORTED;
    //$$ /**
    //$$  * 红树林船物品 - 在 Minecraft 1.18.2 及以下版本中不受支持
    //$$  */
    //$$ public static final Item MANGROVE_BOAT = UNSUPPORTED;
    //$$ /**
    //$$  * 樱花船物品 - 在 Minecraft 1.18.2 及以下版本中不受支持
    //$$  */
    //$$ public static final Item CHERRY_BOAT = UNSUPPORTED;
    //$$ /**
    //$$  * 红树林门物品 - 在 Minecraft 1.18.2 及以下版本中不受支持
    //$$  */
    //$$ public static final Item MANGROVE_DOOR = UNSUPPORTED;
    //$$ /**
    //$$  * 竹门物品 - 在 Minecraft 1.18.2 及以下版本中不受支持
    //$$  */
    //$$ public static final Item BAMBOO_DOOR = UNSUPPORTED;
    //$$ /**
    //$$  * 樱花门物品 - 在 Minecraft 1.18.2 及以下版本中不受支持
    //$$  */
    //$$ public static final Item CHERRY_DOOR = UNSUPPORTED;
    //$$ /**
    //$$  * 红树林台阶物品 - 在 Minecraft 1.18.2 及以下版本中不受支持
    //$$  */
    //$$ public static final Item MANGROVE_SLAB = UNSUPPORTED;
    //$$ /**
    //$$  * 竹台阶物品 - 在 Minecraft 1.18.2 及以下版本中不受支持
    //$$  */
    //$$ public static final Item BAMBOO_SLAB = UNSUPPORTED;
    //$$ /**
    //$$  * 樱花台阶物品 - 在 Minecraft 1.18.2 及以下版本中不受支持
    //$$  */
    //$$ public static final Item CHERRY_SLAB = UNSUPPORTED;
    //$$ /**
    //$$  * 红树林楼梯物品 - 在 Minecraft 1.18.2 及以下版本中不受支持
    //$$  */
    //$$ public static final Item MANGROVE_STAIRS = UNSUPPORTED;
    //$$ /**
    //$$  * 竹楼梯物品 - 在 Minecraft 1.18.2 及以下版本中不受支持
    //$$  */
    //$$ public static final Item BAMBOO_STAIRS = UNSUPPORTED;
    //$$ /**
    //$$  * 樱花楼梯物品 - 在 Minecraft 1.18.2 及以下版本中不受支持
    //$$  */
    //$$ public static final Item CHERRY_STAIRS = UNSUPPORTED;
    //$$ /**
    //$$  * 红树林活板门物品 - 在 Minecraft 1.18.2 及以下版本中不受支持
    //$$  */
    //$$ public static final Item MANGROVE_TRAPDOOR = UNSUPPORTED;
    //$$ /**
    //$$  * 竹活板门物品 - 在 Minecraft 1.18.2 及以下版本中不受支持
    //$$  */
    //$$ public static final Item BAMBOO_TRAPDOOR = UNSUPPORTED;
    //$$ /**
    //$$  * 樱花活板门物品 - 在 Minecraft 1.18.2 及以下版本中不受支持
    //$$  */
    //$$ public static final Item CHERRY_TRAPDOOR = UNSUPPORTED;
    //$$ /**
    //$$  * 红树林原木物品 - 在 Minecraft 1.18.2 及以下版本中不受支持
    //$$  */
    //$$ public static final Item MANGROVE_LOG = UNSUPPORTED;
    //$$ /**
    //$$  * 去皮红树林原木物品 - 在 Minecraft 1.18.2 及以下版本中不受支持
    //$$  */
    //$$ public static final Item STRIPPED_MANGROVE_LOG = UNSUPPORTED;
    //$$ /**
    //$$  * 去皮红树林木物品 - 在 Minecraft 1.18.2 及以下版本中不受支持
    //$$  */
    //$$ public static final Item STRIPPED_MANGROVE_WOOD = UNSUPPORTED;
    //$$ /**
    //$$  * 樱花原木物品 - 在 Minecraft 1.18.2 及以下版本中不受支持
    //$$  */
    //$$ public static final Item CHERRY_LOG = UNSUPPORTED;
    //$$ /**
    //$$  * 樱花木物品 - 在 Minecraft 1.18.2 及以下版本中不受支持
    //$$  */
    //$$ public static final Item CHERRY_WOOD = UNSUPPORTED;
    //$$ /**
    //$$  * 去皮樱花原木物品 - 在 Minecraft 1.18.2 及以下版本中不受支持
    //$$  */
    //$$ public static final Item STRIPPED_CHERRY_LOG = UNSUPPORTED;
    //$$ /**
    //$$  * 去皮樱花木物品 - 在 Minecraft 1.18.2 及以下版本中不受支持
    //$$  */
    //$$ public static final Item STRIPPED_CHERRY_WOOD = UNSUPPORTED;
    //$$ /**
    //$$  * 竹筏物品 - 在 Minecraft 1.18.2 及以下版本中不受支持
    //$$  */
    //$$ public static final Item BAMBOO_RAFT = UNSUPPORTED;
    //$$ /**
    //$$  * 雕纹书架物品 - 在 Minecraft 1.18.2 及以下版本中不受支持
    //$$  */
    //$$ public static final Item CHISELED_BOOKSHELF = UNSUPPORTED;
    //$$ /**
    //$$  * 饰纹陶罐物品 - 在 Minecraft 1.18.2 及以下版本中不受支持
    //$$  */
    //$$ public static final Item DECORATED_POT = UNSUPPORTED;
    //$$ /**
    //$$  * 画笔物品 - 在 Minecraft 1.18.2 及以下版本中不受支持
    //$$  */
    //$$ public static final Item BRUSH = UNSUPPORTED;
    //$$ /**
    //$$  * 竹块物品 - 在 Minecraft 1.18.2 及以下版本中不受支持
    //$$  */
    //$$ public static final Item BAMBOO_BLOCK = UNSUPPORTED;
    //$$ /**
    //$$  * 下界合金升级锻造模板物品 - 在 Minecraft 1.18.2 及以下版本中不受支持
    //$$  */
    //$$ public static final Item NETHERITE_UPGRADE_SMITHING_TEMPLATE = UNSUPPORTED;
    //$$ /**
    //$$  * 去皮竹块物品 - 在 Minecraft 1.18.2 及以下版本中不受支持
    //$$  */
    //$$ public static final Item STRIPPED_BAMBOO_BLOCK = UNSUPPORTED;
    //$$ /**
    //$$  * 火炬花种子物品 - 在 Minecraft 1.18.2 及以下版本中不受支持
    //$$  */
    //$$ public static final Item TORCHFLOWER_SEEDS = UNSUPPORTED;
    //$$ /**
    //$$  * 粉红色花瓣物品 - 在 Minecraft 1.18.2 及以下版本中不受支持
    //$$  */
    //$$ public static final Item PINK_PETALS = UNSUPPORTED;
    //$$ /**
    //$$  * 红树林根物品 - 在 Minecraft 1.18.2 及以下版本中不受支持
    //$$  */
    //$$ public static final Item MANGROVE_ROOTS = UNSUPPORTED;
    //$$ /**
    //$$  * 泥泞的红树林根物品 - 在 Minecraft 1.18.2 及以下版本中不受支持
    //$$  */
    //$$ public static final Item MUDDY_MANGROVE_ROOTS = UNSUPPORTED;
    //$$ /**
    //$$  * 泥巴物品 - 在 Minecraft 1.18.2 及以下版本中不受支持
    //$$  */
    //$$ public static final Item MUD = UNSUPPORTED;
    //#endif

    //#if MC <=11605
    //$$ /**
    //$$  * 粗深板岩物品 - 在 Minecraft 1.16.5 及以下版本中不受支持
    //$$  */
    //$$ public static final Item COBBLED_DEEPSLATE = UNSUPPORTED;
    //$$ /**
    //$$  * 方解石物品 - 在 Minecraft 1.16.5 及以下版本中不受支持
    //$$  */
    //$$ public static final Item CALCITE = UNSUPPORTED;
    //$$ /**
    //$$  * 凝灰岩物品 - 在 Minecraft 1.16.5 及以下版本中不受支持
    //$$  */
    //$$ public static final Item TUFF = UNSUPPORTED;
    //$$ /**
    //$$  * 原铁物品 - 在 Minecraft 1.16.5 及以下版本中不受支持
    //$$  */
    //$$ public static final Item RAW_IRON = UNSUPPORTED;
    //$$ /**
    //$$  * 原金物品 - 在 Minecraft 1.16.5 及以下版本中不受支持
    //$$  */
    //$$ public static final Item RAW_GOLD = UNSUPPORTED;
    //$$ /**
    //$$  * 原铜物品 - 在 Minecraft 1.16.5 及以下版本中不受支持
    //$$  */
    //$$ public static final Item RAW_COPPER = UNSUPPORTED;
    //$$ /**
    //$$  * 紫水晶碎片物品 - 在 Minecraft 1.16.5 及以下版本中不受支持
    //$$  */
    //$$ public static final Item AMETHYST_SHARD = UNSUPPORTED;
    //$$ /**
    //$$  * 滴水石锥物品 - 在 Minecraft 1.16.5 及以下版本中不受支持
    //$$  */
    //$$ public static final Item POINTED_DRIPSTONE = UNSUPPORTED;
    //$$ /**
    //$$  * 紫水晶块物品 - 在 Minecraft 1.16.5 及以下版本中不受支持
    //$$  */
    //$$ public static final Item AMETHYST_BLOCK = UNSUPPORTED;
    //$$ /**
    //$$  * 滴水石块物品 - 在 Minecraft 1.16.5 及以下版本中不受支持
    //$$  */
    //$$ public static final Item DRIPSTONE_BLOCK = UNSUPPORTED;
    //$$ /**
    //$$  * 铜锭物品 - 在 Minecraft 1.16.5 及以下版本中不受支持
    //$$  */
    //$$ public static final Item COPPER_INGOT = UNSUPPORTED;
    //$$ /**
    //$$  * 生根泥土物品 - 在 Minecraft 1.16.5 及以下版本中不受支持
    //$$  */
    //$$ public static final Item ROOTED_DIRT = UNSUPPORTED;
    //$$ /**
    //$$  * 发光墨囊物品 - 在 Minecraft 1.16.5 及以下版本中不受支持
    //$$  */
    //$$ public static final Item GLOW_INK_SAC = UNSUPPORTED;
    //$$ /**
    //$$  * 发光地衣物品 - 在 Minecraft 1.16.5 及以下版本中不受支持
    //$$  */
    //$$ public static final Item GLOW_LICHEN = UNSUPPORTED;
    //$$ /**
    //$$  * 深板岩物品 - 在 Minecraft 1.16.5 及以下版本中不受支持
    //$$  */
    //$$ public static final Item DEEPSLATE = UNSUPPORTED;
    //$$ /**
    //$$  * 平滑玄武岩物品 - 在 Minecraft 1.16.5 及以下版本中不受支持
    //$$  */
    //$$ public static final Item SMOOTH_BASALT = UNSUPPORTED;
    //$$ /**
    //$$  * 铜矿石物品 - 在 Minecraft 1.16.5 及以下版本中不受支持
    //$$  */
    //$$ public static final Item COPPER_ORE = UNSUPPORTED;
    //$$ /**
    //$$  * 铜块物品 - 在 Minecraft 1.16.5 及以下版本中不受支持
    //$$  */
    //$$ public static final Item COPPER_BLOCK = UNSUPPORTED;
    //$$ /**
    //$$  * 原铁块物品 - 在 Minecraft 1.16.5 及以下版本中不受支持
    //$$  */
    //$$ public static final Item RAW_IRON_BLOCK = UNSUPPORTED;
    //$$ /**
    //$$  * 原金块物品 - 在 Minecraft 1.16.5 及以下版本中不受支持
    //$$  */
    //$$ public static final Item RAW_GOLD_BLOCK = UNSUPPORTED;
    //$$ /**
    //$$  * 原铜块物品 - 在 Minecraft 1.16.5 及以下版本中不受支持
    //$$  */
    //$$ public static final Item RAW_COPPER_BLOCK = UNSUPPORTED;
    //$$ /**
    //$$  * 切制铜台阶物品 - 在 Minecraft 1.16.5 及以下版本中不受支持
    //$$  */
    //$$ public static final Item CUT_COPPER_SLAB = UNSUPPORTED;
    //$$ /**
    //$$  * 切制铜楼梯物品 - 在 Minecraft 1.16.5 及以下版本中不受支持
    //$$  */
    //$$ public static final Item CUT_COPPER_STAIRS = UNSUPPORTED;
    //$$ /**
    //$$  * 粗深板岩台阶物品 - 在 Minecraft 1.16.5 及以下版本中不受支持
    //$$  */
    //$$ public static final Item COBBLED_DEEPSLATE_SLAB = UNSUPPORTED;
    //$$ /**
    //$$  * 粗深板岩楼梯物品 - 在 Minecraft 1.16.5 及以下版本中不受支持
    //$$  */
    //$$ public static final Item COBBLED_DEEPSLATE_STAIRS = UNSUPPORTED;
    //$$ /**
    //$$  * 深板岩墙物品 - 在 Minecraft 1.16.5 及以下版本中不受支持
    //$$  */
    //$$ public static final Item DEEPSLATE_WALL = UNSUPPORTED;
    //$$ /**
    //$$  * 抛光深板岩台阶物品 - 在 Minecraft 1.16.5 及以下版本中不受支持
    //$$  */
    //$$ public static final Item POLISHED_DEEPSLATE_SLAB = UNSUPPORTED;
    //$$ /**
    //$$  * 抛光深板岩楼梯物品 - 在 Minecraft 1.16.5 及以下版本中不受支持
    //$$  */
    //$$ public static final Item POLISHED_DEEPSLATE_STAIRS = UNSUPPORTED;
    //$$ /**
    //$$  * 抛光深板岩墙物品 - 在 Minecraft 1.16.5 及以下版本中不受支持
    //$$  */
    //$$ public static final Item POLISHED_DEEPSLATE_WALL = UNSUPPORTED;
    //$$ /**
    //$$  * 深板岩砖台阶物品 - 在 Minecraft 1.16.5 及以下版本中不受支持
    //$$  */
    //$$ public static final Item DEEPSLATE_BRICK_SLAB = UNSUPPORTED;
    //$$ /**
    //$$  * 深板岩砖楼梯物品 - 在 Minecraft 1.16.5 及以下版本中不受支持
    //$$  */
    //$$ public static final Item DEEPSLATE_BRICK_STAIRS = UNSUPPORTED;
    //$$ /**
    //$$  * 深板岩砖墙物品 - 在 Minecraft 1.16.5 及以下版本中不受支持
    //$$  */
    //$$ public static final Item DEEPSLATE_BRICK_WALL = UNSUPPORTED;
    //$$ /**
    //$$  * 深板岩瓦台阶物品 - 在 Minecraft 1.16.5 及以下版本中不受支持
    //$$  */
    //$$ public static final Item DEEPSLATE_TILE_SLAB = UNSUPPORTED;
    //$$ /**
    //$$  * 深板岩瓦楼梯物品 - 在 Minecraft 1.16.5 及以下版本中不受支持
    //$$  */
    //$$ public static final Item DEEPSLATE_TILE_STAIRS = UNSUPPORTED;
    //$$ /**
    //$$  * 深板岩瓦墙物品 - 在 Minecraft 1.16.5 及以下版本中不受支持
    //$$  */
    //$$ public static final Item DEEPSLATE_TILE_WALL = UNSUPPORTED;
    //$$ /**
    //$$  * 雕纹深板岩物品 - 在 Minecraft 1.16.5 及以下版本中不受支持
    //$$  */
    //$$ public static final Item CHISELED_DEEPSLATE = UNSUPPORTED;
    //$$ /**
    //$$  * 苔藓块物品 - 在 Minecraft 1.16.5 及以下版本中不受支持
    //$$  */
    //$$ public static final Item MOSS_BLOCK = UNSUPPORTED;
    //$$ /**
    //$$  * 苔藓地毯物品 - 在 Minecraft 1.16.5 及以下版本中不受支持
    //$$  */
    //$$ public static final Item MOSS_CARPET = UNSUPPORTED;
    //$$ /**
    //$$  * 紫水晶簇物品 - 在 Minecraft 1.16.5 及以下版本中不受支持
    //$$  */
    //$$ public static final Item AMETHYST_CLUSTER = UNSUPPORTED;
    //$$ /**
    //$$  * 萌芽紫水晶物品 - 在 Minecraft 1.16.5 及以下版本中不受支持
    //$$  */
    //$$ public static final Item BUDDING_AMETHYST = UNSUPPORTED;
    //$$ /**
    //$$  * 开花的杜鹃花丛物品 - 在 Minecraft 1.16.5 及以下版本中不受支持
    //$$  */
    //$$ public static final Item FLOWERING_AZALEA = UNSUPPORTED;
    //$$ /**
    //$$  * 美西螈桶物品 - 在 Minecraft 1.16.5 及以下版本中不受支持
    //$$  */
    //$$ public static final Item AXOLOTL_BUCKET = UNSUPPORTED;
    //$$ /**
    //$$  * 细雪桶物品 - 在 Minecraft 1.16.5 及以下版本中不受支持
    //$$  */
    //$$ public static final Item POWDER_SNOW_BUCKET = UNSUPPORTED;
    //$$ /**
    //$$  * 抛光深板岩物品 - 在 Minecraft 1.16.5 及以下版本中不受支持
    //$$  */
    //$$ public static final Item POLISHED_DEEPSLATE = UNSUPPORTED;
    //$$ /**
    //$$  * 深板岩砖物品 - 在 Minecraft 1.16.5 及以下版本中不受支持
    //$$  */
    //$$ public static final Item DEEPSLATE_BRICKS = UNSUPPORTED;
    //$$ /**
    //$$  * 深板岩瓦物品 - 在 Minecraft 1.16.5 及以下版本中不受支持
    //$$  */
    //$$ public static final Item DEEPSLATE_TILES = UNSUPPORTED;
    //$$ /**
    //$$  * 切制铜块物品 - 在 Minecraft 1.16.5 及以下版本中不受支持
    //$$  */
    //$$ public static final Item CUT_COPPER = UNSUPPORTED;
    //$$ /**
    //$$  * 裂纹深板岩砖物品 - 在 Minecraft 1.16.5 及以下版本中不受支持
    //$$  */
    //$$ public static final Item CRACKED_DEEPSLATE_BRICKS = UNSUPPORTED;
    //$$ /**
    //$$  * 裂纹深板岩瓦物品 - 在 Minecraft 1.16.5 及以下版本中不受支持
    //$$  */
    //$$ public static final Item CRACKED_DEEPSLATE_TILES = UNSUPPORTED;
    //$$ /**
    //$$  * 望远镜物品 - 在 Minecraft 1.16.5 及以下版本中不受支持
    //$$  */
    //$$ public static final Item SPYGLASS = UNSUPPORTED;
    //$$ /**
    //$$  * 蜡烛物品 - 在 Minecraft 1.16.5 及以下版本中不受支持
    //$$  */
    //$$ public static final Item CANDLE = UNSUPPORTED;
    //$$ /**
    //$$  * 避雷针物品 - 在 Minecraft 1.16.5 及以下版本中不受支持
    //$$  */
    //$$ public static final Item LIGHTNING_ROD = UNSUPPORTED;
    //$$ /**
    //$$  * 染色玻璃物品 - 在 Minecraft 1.16.5 及以下版本中不受支持
    //$$  */
    //$$ public static final Item TINTED_GLASS = UNSUPPORTED;
    //$$ /**
    //$$  * 发光物品展示框物品 - 在 Minecraft 1.16.5 及以下版本中不受支持
    //$$  */
    //$$ public static final Item GLOW_ITEM_FRAME = UNSUPPORTED;
    //#endif

}
