package adris.altoclef;

import adris.altoclef.multiversion.versionedfields.Blocks;
import adris.altoclef.multiversion.versionedfields.Entities;
import adris.altoclef.multiversion.versionedfields.Items;
import adris.altoclef.tasks.CraftInInventoryTask;
import adris.altoclef.tasks.ResourceTask;
import adris.altoclef.tasks.container.CraftInTableTask;
import adris.altoclef.tasks.container.SmeltInFurnaceTask;
import adris.altoclef.tasks.container.UpgradeInSmithingTableTask;
import adris.altoclef.tasks.resources.*;
import adris.altoclef.tasks.resources.wood.*;
import adris.altoclef.tasks.squashed.CataloguedResourceTask;
import adris.altoclef.util.*;
import adris.altoclef.util.helpers.ItemHelper;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.entity.mob.*;
import net.minecraft.entity.passive.*;
import net.minecraft.item.Item;
import net.minecraft.util.DyeColor;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * 包含所有可获得资源的硬编码列表。
 * <p>
 * 大多数资源对应单个物品，但某些资源（如"log"或"door"）包含一系列物品。
 * <p>
 * 调用`TaskCatalogue.getItemTask`以返回给定资源键的任务。
 * 调用`TaskCatalogue.getSquashedItemTask`以返回获取多个资源的任务，合并其步骤。
 */
@SuppressWarnings({"rawtypes"})
public class TaskCatalogue {

    private static final HashMap<String, Item[]> nameToItemMatches = new HashMap<>();  // 资源名称到物品数组的映射
    private static final HashMap<String, CataloguedResource> nameToResourceTask = new HashMap<>();  // 资源名称到资源任务的映射
    private static final HashMap<Item, CataloguedResource> itemToResourceTask = new HashMap<>();    // 物品到资源任务的映射
    private static final HashSet<Item> resourcesObtainable = new HashSet<>();  // 可获得的资源集合

    public static void init() {
        nameToItemMatches.keySet();
    }

    static {
        /// 在此处定义资源任务
        {
            String p = "planks";  // 木板
            String s = "stick";   // 木棍
            String o = null;      // 空

            /// 原始资源
            mine("log", MiningRequirement.HAND, ItemHelper.LOG, ItemHelper.LOG).anyDimension();  // 木头
            woodTasks("log", wood -> wood.log, (wood, count) -> new MineAndCollectTask(wood.log, count, new Block[]{Block.getBlockFromItem(wood.log)}, MiningRequirement.HAND), true);
            mine("dirt", MiningRequirement.HAND, new Block[]{Blocks.DIRT, Blocks.GRASS_BLOCK, Blocks.DIRT_PATH}, Items.DIRT);  // 泥土
            simple("cobblestone", Items.COBBLESTONE, CollectBlockByOneTask.CollectCobblestoneTask::new).dontMineIfPresent();  // 圆石
            simple("cobbled_deepslate", Items.COBBLED_DEEPSLATE, CollectBlockByOneTask.CollectCobbledDeepslateTask::new).dontMineIfPresent();  // 深板岩圆石
            mine("andesite", MiningRequirement.WOOD, Blocks.ANDESITE, Items.ANDESITE);  // 闪长岩
            mine("granite", MiningRequirement.WOOD, Blocks.GRANITE, Items.GRANITE);  // 花岗岩
            mine("diorite", MiningRequirement.WOOD, Blocks.DIORITE, Items.DIORITE);  // 安山岩
            mine("calcite", MiningRequirement.WOOD, Blocks.CALCITE, Items.CALCITE);  // 方解石
            mine("tuff", MiningRequirement.WOOD, Blocks.TUFF, Items.TUFF);  // 凝灰岩
            mine("netherrack", MiningRequirement.WOOD, Blocks.NETHERRACK, Items.NETHERRACK).forceDimension(Dimension.NETHER);  // 下界岩
            mine("magma_block", MiningRequirement.WOOD, Blocks.MAGMA_BLOCK, Items.MAGMA_BLOCK).forceDimension(Dimension.NETHER);  // 岩浆块
            mine("blackstone", MiningRequirement.WOOD, Blocks.BLACKSTONE, Items.BLACKSTONE).forceDimension(Dimension.NETHER);  // 黑石
            mine("basalt", MiningRequirement.WOOD, Blocks.BASALT, Items.BASALT).forceDimension(Dimension.NETHER);  // 玄武岩
            mine("soul_sand", Items.SOUL_SAND).forceDimension(Dimension.NETHER);  // 灵魂沙
            mine("soul_soil", Items.SOUL_SOIL).forceDimension(Dimension.NETHER);  // 灵魂土
            mine("glowstone_dust", Blocks.GLOWSTONE, Items.GLOWSTONE_DUST).forceDimension(Dimension.NETHER);  // 萤石粉
            mine("coal", MiningRequirement.WOOD, new Block[]{Blocks.COAL_ORE, Blocks.DEEPSLATE_COAL_ORE}, Items.COAL);  // 煤炭
            mine("raw_iron", MiningRequirement.STONE, new Block[]{Blocks.IRON_ORE, Blocks.DEEPSLATE_IRON_ORE}, Items.RAW_IRON);  // 粗铁
            mine("raw_gold", MiningRequirement.IRON, new Block[]{Blocks.GOLD_ORE, Blocks.DEEPSLATE_GOLD_ORE}, Items.RAW_GOLD);  // 粗金
            mine("raw_copper", MiningRequirement.STONE, new Block[]{Blocks.COPPER_ORE, Blocks.DEEPSLATE_COPPER_ORE}, Items.RAW_COPPER);  // 粗铜
            mine("diamond", MiningRequirement.IRON, new Block[]{Blocks.DIAMOND_ORE, Blocks.DEEPSLATE_DIAMOND_ORE}, Items.DIAMOND);  // 钻石
            mine("emerald", MiningRequirement.IRON, new Block[]{Blocks.EMERALD_ORE, Blocks.DEEPSLATE_EMERALD_ORE}, Items.EMERALD);  // 绿宝石
            mine("redstone", MiningRequirement.IRON, new Block[]{Blocks.REDSTONE_ORE, Blocks.DEEPSLATE_REDSTONE_ORE}, Items.REDSTONE);  // 红石
            mine("lapis_lazuli", MiningRequirement.STONE, new Block[]{Blocks.LAPIS_ORE, Blocks.DEEPSLATE_LAPIS_ORE}, Items.LAPIS_LAZULI);  // 青金石
            alias("lapis", "lapis_lazuli");  // 青金石别名
            mine("amethyst_shard", MiningRequirement.WOOD, Blocks.AMETHYST_CLUSTER, Items.AMETHYST_SHARD);  // 紫水晶碎片
            mine("pointed_dripstone", MiningRequirement.WOOD, Blocks.POINTED_DRIPSTONE, Items.POINTED_DRIPSTONE);  // 滴水石锥
            mine("sand", Blocks.SAND, Items.SAND);  // 沙子
            mine("red_sand", Blocks.RED_SAND, Items.RED_SAND);  // 红沙
            mine("gravel", Blocks.GRAVEL, Items.GRAVEL);  // 沙砾
            mine("clay_ball", Blocks.CLAY, Items.CLAY_BALL);  // 粘土球
            mine("ancient_debris", MiningRequirement.DIAMOND, Blocks.ANCIENT_DEBRIS, Items.ANCIENT_DEBRIS).forceDimension(Dimension.NETHER);  // 古代残骸
            mine("gilded_blackstone", MiningRequirement.STONE, Blocks.GILDED_BLACKSTONE, Items.GILDED_BLACKSTONE).forceDimension(Dimension.NETHER);  // 镶金黑石
            mine("oak_sapling", Blocks.OAK_LEAVES, Items.OAK_SAPLING);  // 橡树树苗
            mine("spruce_sapling", Blocks.SPRUCE_LEAVES, Items.SPRUCE_SAPLING);  // 云杉树苗
            mine("birch_sapling", Blocks.BIRCH_LEAVES, Items.BIRCH_SAPLING);  // 白桦树苗
            mine("jungle_sapling", Blocks.JUNGLE_LEAVES, Items.JUNGLE_SAPLING);  // 丛林树苗
            mine("acacia_sapling", Blocks.ACACIA_LEAVES, Items.ACACIA_SAPLING);  // 金合欢树苗
            mine("dark_oak_sapling", Blocks.DARK_OAK_LEAVES, Items.DARK_OAK_SAPLING);  // 深色橡树树苗
            mine("mangrove_propagule", Blocks.MANGROVE_PROPAGULE, Items.MANGROVE_PROPAGULE);  // 红树胎生苗
            mine("cherry_sapling", Blocks.CHERRY_LEAVES, Items.CHERRY_SAPLING);  // 樱花树苗
            simple("sapling", ItemHelper.SAPLINGS, CollectSaplingsTask::new);  // 树苗
            simple("sandstone", Items.SANDSTONE, CollectSandstoneTask::new).dontMineIfPresent();  // 砂岩
            simple("red_sandstone", Items.RED_SANDSTONE, CollectRedSandstoneTask::new).dontMineIfPresent();  // 红砂岩
            simple("coarse_dirt", Items.COARSE_DIRT, CollectCoarseDirtTask::new).dontMineIfPresent();  // 砾质土
            simple("amethyst_block", Items.AMETHYST_BLOCK, CollectAmethystBlockTask::new).dontMineIfPresent();  // 紫水晶块
            simple("dripstone_block", Items.DRIPSTONE_BLOCK, CollectDripstoneBlockTask::new).dontMineIfPresent();  // 滴水石块
            simple("flint", Items.FLINT, CollectFlintTask::new);  // 燧石
            simple("obsidian", Items.OBSIDIAN, CollectObsidianTask::new).dontMineIfPresent();  // 黑曜石
            simple("wool", ItemHelper.WOOL, CollectWoolTask::new);  // 羊毛
            simple("egg", Items.EGG, CollectEggsTask::new);  // 鸡蛋
            mob("bone", Items.BONE, SkeletonEntity.class);  // 骨头
            mob("gunpowder", Items.GUNPOWDER, CreeperEntity.class);  // 火药
            simple("ender_pearl", Items.ENDER_PEARL, KillEndermanTask::new);  // 末影珍珠
            mob("spider_eye", Items.SPIDER_EYE, SpiderEntity.class);  // 蜘蛛眼
            mob("leather", Items.LEATHER, CowEntity.class);  // 皮革
            mob("feather", Items.FEATHER, ChickenEntity.class);  // 羽毛
            mob("rotten_flesh", Items.ROTTEN_FLESH, ZombieEntity.class);  // 腐肉
            mob("rabbit_foot", Items.RABBIT_FOOT, RabbitEntity.class);  // 兔脚
            mob("rabbit_hide", Items.RABBIT_HIDE, RabbitEntity.class);  // 兔皮
            mob("slime_ball", Items.SLIME_BALL, SlimeEntity.class);  // 粘液球
            mob("wither_skeleton_skull", Items.WITHER_SKELETON_SKULL, WitherSkeletonEntity.class).forceDimension(Dimension.NETHER);  // 凋灵骷髅头颅
            mob("ink_sac", Items.INK_SAC, SquidEntity.class);  // 墨囊 (警告，可能无法工作)
            mob("glow_ink_sac", Items.GLOW_INK_SAC, Entities.GLOW_SQUID);  // 亮眼墨囊 (警告，可能无法工作)
            mob("string", Items.STRING, SpiderEntity.class);  // 线 (警告，可能无法工作)
            mine("sugar_cane", Items.SUGAR_CANE);  // 甘蔗
            mine("brown_mushroom", MiningRequirement.HAND, new Block[]{Blocks.BROWN_MUSHROOM, Blocks.BROWN_MUSHROOM_BLOCK}, Items.BROWN_MUSHROOM);  // 棕色蘑菇
            mine("red_mushroom", MiningRequirement.HAND, new Block[]{Blocks.RED_MUSHROOM, Blocks.RED_MUSHROOM_BLOCK}, Items.RED_MUSHROOM);  // 红色蘑菇
            mine("mushroom", MiningRequirement.HAND, new Block[]{Blocks.BROWN_MUSHROOM, Blocks.BROWN_MUSHROOM_BLOCK, Blocks.RED_MUSHROOM, Blocks.RED_MUSHROOM_BLOCK}, Items.BROWN_MUSHROOM, Items.RED_MUSHROOM);  // 蘑菇
            mine("melon_slice", MiningRequirement.HAND, Blocks.MELON, Items.MELON_SLICE);  // 西瓜片
            mine("pumpkin", MiningRequirement.HAND, Blocks.PUMPKIN, Items.PUMPKIN);  // 南瓜
            mine("bell", MiningRequirement.WOOD, Blocks.BELL, Items.BELL);  // 钟
            mine("nether_wart", MiningRequirement.HAND, Blocks.NETHER_WART, Items.NETHER_WART).forceDimension(Dimension.NETHER);  // 下界 wart
            mine("crimson_fungus", MiningRequirement.HAND, Blocks.CRIMSON_FUNGUS, Items.CRIMSON_FUNGUS).forceDimension(Dimension.NETHER);  // 绯红菌
            mine("warped_fungus", MiningRequirement.HAND, Blocks.WARPED_FUNGUS, Items.WARPED_FUNGUS).forceDimension(Dimension.NETHER);  // 诡异菌
            mine("crimson_roots", MiningRequirement.HAND, Blocks.CRIMSON_ROOTS, Items.CRIMSON_ROOTS).forceDimension(Dimension.NETHER);  // 绯红菌索
            mine("warped_roots", MiningRequirement.HAND, Blocks.WARPED_ROOTS, Items.WARPED_ROOTS).forceDimension(Dimension.NETHER);  // 诡异菌索
            mine("weeping_vines", MiningRequirement.HAND, Blocks.WEEPING_VINES, Items.WEEPING_VINES).forceDimension(Dimension.NETHER);  // 垂泪藤
            mine("twisting_vines", MiningRequirement.HAND, Blocks.TWISTING_VINES, Items.TWISTING_VINES).forceDimension(Dimension.NETHER);  // 缠怨藤
            mine("nether_wart_block", MiningRequirement.HAND, Blocks.NETHER_WART_BLOCK, Items.NETHER_WART_BLOCK).forceDimension(Dimension.NETHER);  // 下界 wart块
            mine("warped_wart_block", MiningRequirement.HAND, Blocks.WARPED_WART_BLOCK, Items.WARPED_WART_BLOCK).forceDimension(Dimension.NETHER);  // 诡异 wart块
            mine("shroomlight", MiningRequirement.HAND, Blocks.SHROOMLIGHT, Items.SHROOMLIGHT).forceDimension(Dimension.NETHER);  // 菌光体
            simple("blaze_rod", Items.BLAZE_ROD, CollectBlazeRodsTask::new).forceDimension(Dimension.NETHER);  // 烈焰棒 (说实话并不太简单 lmao)
            //simple("quartz", Items.QUARTZ, CollectQuartzTask::new);  // 石英
            mine("quartz", MiningRequirement.WOOD, Blocks.NETHER_QUARTZ_ORE, Items.QUARTZ).forceDimension(Dimension.NETHER);  // 下界石英
            simple("cocoa_beans", Items.COCOA_BEANS, CollectCocoaBeansTask::new);  // 可可豆
            shear("cobweb", Blocks.COBWEB, Items.COBWEB).dontMineIfPresent();  // 蜘蛛网
            colorfulTasks("wool", color -> color.wool, (color, count) -> new CollectWoolTask(color.color, count));  // 彩色羊毛
            // 杂项绿色植物
            shear("leaves", ItemHelper.itemsToBlocks(ItemHelper.LEAVES), ItemHelper.LEAVES).dontMineIfPresent();  // 叶子
            for (CataloguedResource resource : woodTasks(
                    "leaves",
                    woodItems -> woodItems.leaves,
                    (woodItems, count) -> {
                        if (woodItems.isNetherWood()) {
                            // 下界的"叶子"不需要剪切，可以直接挖掘
                            return new MineAndCollectTask(woodItems.leaves, count, new Block[]{Block.getBlockFromItem(woodItems.leaves)}, MiningRequirement.HAND).forceDimension(Dimension.NETHER);
                        } else {
                            return new ShearAndCollectBlockTask(woodItems.leaves, count, Block.getBlockFromItem(woodItems.leaves));
                        }
                    })
            ) {
                resource.dontMineIfPresent();
            }
            mine("bamboo", Blocks.BAMBOO, Items.BAMBOO);  // 竹子
            shear("vine", Blocks.VINE, Items.VINE).dontMineIfPresent();  // 藤蔓
            shear("grass", Blocks.SHORT_GRASS, Items.SHORT_GRASS).dontMineIfPresent();  // 草
            shear("lily_pad", Blocks.LILY_PAD, Items.LILY_PAD).dontMineIfPresent();  // 睡莲
            shear("tall_grass", Blocks.TALL_GRASS, Items.TALL_GRASS).dontMineIfPresent();  // 高草丛
            shear("fern", Blocks.FERN, Items.FERN).dontMineIfPresent();  // 蕨
            shear("large_fern", Blocks.LARGE_FERN, Items.LARGE_FERN).dontMineIfPresent();  // 大型蕨
            shear("dead_bush", Blocks.DEAD_BUSH, Items.DEAD_BUSH).dontMineIfPresent();  // 枯死灌木
            shear("glow_lichen", Blocks.GLOW_LICHEN, Items.GLOW_LICHEN).dontMineIfPresent();  // 发光地衣
            // 花朵
            simple("flower", ItemHelper.FLOWER, CollectFlowerTask::new);  // 花
            mine("allium", Items.ALLIUM);  // 绒球葱
            mine("azure_bluet", Items.AZURE_BLUET);  // 婴儿靴
            mine("blue_orchid", Items.BLUE_ORCHID);  // 兰花
            mine("cactus", Items.CACTUS);  // 仙人掌
            mine("cornflower", Items.CORNFLOWER);  // 矢车菊
            mine("dandelion", Items.DANDELION);  // 蒲公英
            mine("lilac", Items.LILAC);  // 丁香
            mine("lily_of_the_valley", Items.LILY_OF_THE_VALLEY);  // 铃兰
            mine("orange_tulip", Items.ORANGE_TULIP);  // 橙色郁金香
            mine("oxeye_daisy", Items.OXEYE_DAISY);  // 滨菊
            mine("pink_tulip", Items.PINK_TULIP);  // 粉色郁金香
            mine("poppy", Items.POPPY);  // 虞美人
            mine("peony", Items.PEONY);  // 牡丹
            mine("red_tulip", Items.RED_TULIP);  // 红色郁金香
            mine("rose_bush", Items.ROSE_BUSH);  // 玫瑰丛
            mine("sunflower", Items.SUNFLOWER);  // 向日葵
            mine("white_tulip", Items.WHITE_TULIP);  // 白色郁金香
            // 农作物
            simple("wheat", Items.WHEAT, CollectWheatTask::new);  // 小麦
            crop("carrot", Items.CARROT, Blocks.CARROTS, Items.CARROT);  // 胡萝卜
            crop("potato", Items.POTATO, Blocks.POTATOES, Items.POTATO);  // 土豆
            crop("poisonous_potato", Items.POISONOUS_POTATO, Blocks.POTATOES, Items.POTATO);  // 毒马铃薯
            crop("beetroot", Items.BEETROOT, Blocks.BEETROOTS, Items.BEETROOT_SEEDS);  // 甜菜根
            simple("wheat_seeds", Items.WHEAT_SEEDS, CollectWheatSeedsTask::new);  // 小麦种子
            crop("beetroot_seeds", Items.BEETROOT_SEEDS, Blocks.BEETROOTS, Items.BEETROOT_SEEDS);  // 甜菜种子


            // 材料
            //mine("netherite_upgrade_smithing_template", MiningRequirement.HAND, Blocks.CHEST, Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE).forceDimension(Dimension.NETHER);  // 下界合金升级锻造模板
            simple("netherite_upgrade_smithing_template", Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE, GetSmithingTemplateTask::new);  // 下界合金升级锻造模板
            alias("netherite_upgrade", "netherite_upgrade_smithing_template");  // 下界合金升级别名
            simple("planks", ItemHelper.PLANKS, CollectPlanksTask::new).dontMineIfPresent();  // 木板
            // 按树种分类的木板。目前，下界木板需要指定其原木在下界。
            for (CataloguedResource woodCatalogue : woodTasks("planks", wood -> wood.planks, (wood, count) -> {
                CollectPlanksTask result = new CollectPlanksTask(wood.planks, count);
                if (wood.isNetherWood()) {
                    // 有点笨拙
                    result.logsInNether();
                }
                return result;
            }, true)) {
                // 也不要单独挖掘木板！由内部处理。
                woodCatalogue.dontMineIfPresent();
            }
            simple("stripped_logs", ItemHelper.STRIPPED_LOGS, CollectStrippedLogTask::new).dontMineIfPresent();  // 去皮原木
            for (CataloguedResource woodCatalogue : woodTasks("stripped_logs", wood -> wood.strippedLog,
                    (wood, count) -> new CollectStrippedLogTask(wood.strippedLog, count))) {
                woodCatalogue.dontMineIfPresent();
            }
            // shapedRecipe2x2("stick", Items.STICK, 4, p, o, p, o);  // 木棍
            simple("stick", Items.STICK, CollectSticksTask::new);  // 木棍
            smelt("stone", Items.STONE, "cobblestone").dontMineIfPresent();  // 石头
            smelt("deepslate", Items.DEEPSLATE, "cobbled_deepslate").dontMineIfPresent();  // 深板岩
            smelt("smooth_stone", Items.SMOOTH_STONE, "stone");  // 平滑石头
            smelt("smooth_quartz", Items.SMOOTH_QUARTZ, "quartz_block");  // 平滑石英块
            smelt("smooth_basalt", Items.SMOOTH_BASALT, "basalt");  // 平滑玄武岩
            smelt("glass", Items.GLASS, "sand").dontMineIfPresent();  // 玻璃
            simple("iron_ingot", Items.IRON_INGOT, CollectIronIngotTask::new).forceDimension(Dimension.OVERWORLD);  // 铁锭
            smelt("copper_ingot", Items.COPPER_INGOT, "raw_copper", Items.COPPER_ORE);  // 铜锭
            smelt("charcoal", Items.CHARCOAL, "log");  // 木炭
            smelt("brick", Items.BRICK, "clay_ball");  // 砖
            smelt("nether_brick", Items.NETHER_BRICK, "netherrack");  // 下界砖
            smelt("green_dye", Items.GREEN_DYE, "cactus");  // 绿色染料
            simple("gold_ingot", Items.GOLD_INGOT, CollectGoldIngotTask::new).anyDimension();  // 金锭（也适用于下界）
            shapedRecipe3x3Block("iron_block", Items.IRON_BLOCK, "iron_ingot");  // 铁块
            shapedRecipe3x3Block("gold_block", Items.GOLD_BLOCK, "gold_ingot");  // 金块
            shapedRecipe3x3Block("copper_block", Items.COPPER_BLOCK, "copper_ingot");  // 铜块
            shapedRecipe3x3Block("raw_iron_block", Items.RAW_IRON_BLOCK, "raw_iron");  // 粗铁块
            shapedRecipe3x3Block("raw_gold_block", Items.RAW_GOLD_BLOCK, "raw_gold");  // 粗金块
            shapedRecipe3x3Block("raw_copper_block", Items.RAW_COPPER_BLOCK, "raw_copper");  // 粗铜块
            shapedRecipe3x3Block("diamond_block", Items.DIAMOND_BLOCK, "diamond");  // 钻石块
            shapedRecipe3x3Block("redstone_block", Items.REDSTONE_BLOCK, "redstone");  // 红石块
            shapedRecipe3x3Block("coal_block", Items.COAL_BLOCK, "coal");  // 煤炭块
            shapedRecipe3x3Block("emerald_block", Items.EMERALD_BLOCK, "emerald");  // 绿宝石块
            shapedRecipe3x3Block("lapis_block", Items.LAPIS_BLOCK, "lapis_lazuli");  // 青金石块
            shapedRecipe3x3Block("slime_block", Items.SLIME_BLOCK, "slime_ball");  // 粘液块
            shapedRecipe3x3Block("melon", Items.MELON, "melon_slice").dontMineIfPresent();  // 西瓜
            shapedRecipe2x2Block("glowstone", Items.GLOWSTONE, "glowstone_dust").dontMineIfPresent();  // 萤石
            shapedRecipe2x2Block("clay", Items.CLAY, "clay_ball").dontMineIfPresent();  // 粘土块
            smelt("netherite_scrap", Items.NETHERITE_SCRAP, "ancient_debris");  // 下界合金碎片
            shapedRecipe3x3("netherite_ingot", Items.NETHERITE_INGOT, 1, "netherite_scrap", "netherite_scrap", "netherite_scrap", "netherite_scrap", "gold_ingot", "gold_ingot", "gold_ingot", "gold_ingot", o);  // 下界合金锭
            simple("gold_nugget", Items.GOLD_NUGGET, CollectGoldNuggetsTask::new);  // 金粒
            {
                String g = "gold_nugget";  // 金粒
                shapedRecipe3x3("glistering_melon_slice", Items.GLISTERING_MELON_SLICE, 1, g, g, g, g, "melon_slice", g, g, g, g);  // 闪烁西瓜片
            }
            shapedRecipe2x2("sugar", Items.SUGAR, 1, "sugar_cane", o, o, o);  // 糖
            shapedRecipe2x2("bone_meal", Items.BONE_MEAL, 3, "bone", o, o, o);  // 骨粉
            shapedRecipe2x2("melon_seeds", Items.MELON_SEEDS, 1, "melon_slice", o, o, o);  // 西瓜种子
            shapedRecipe2x2("bamboo_planks", Items.BAMBOO_PLANKS, 2, "bamboo_block", o, o, o);  // 竹板
            shapedRecipe3x3Block("bamboo_block", Items.BAMBOO_BLOCK, "bamboo");  // 竹块
            simple("hay_block", Items.HAY_BLOCK, CollectHayBlockTask::new).dontMineIfPresent();  // 干草块
            shapedRecipe2x2Block("polished_andesite", Items.POLISHED_ANDESITE, 4, "andesite");  // 磨制闪长岩
            shapedRecipe2x2Block("polished_diorite", Items.POLISHED_DIORITE, 4, "diorite");  // 磨制安山岩
            shapedRecipe2x2Block("polished_granite", Items.POLISHED_GRANITE, 4, "granite");  // 磨制花岗岩
            shapedRecipe2x2Block("quartz_block", Items.QUARTZ_BLOCK, "quartz");  // 石英块
            shapedRecipe2x2Block("polished_blackstone", Items.POLISHED_BLACKSTONE, 4, "blackstone");  // 磨制黑石
            shapedRecipe2x2Block("polished_blackstone_bricks", Items.POLISHED_BLACKSTONE_BRICKS, 4, "polished_blackstone");  // 磨制黑石砖
            shapedRecipe2x2Block("polished_basalt", Items.POLISHED_BASALT, 4, "basalt");  // 磨制玄武岩
            shapedRecipe2x2Block("polished_deepslate", Items.POLISHED_DEEPSLATE, 4, "cobbled_deepslate");  // 磨制深板岩
            shapedRecipe2x2Block("deepslate_bricks", Items.DEEPSLATE_BRICKS, 4, "polished_deepslate");  // 深板岩砖
            shapedRecipe2x2Block("deepslate_tiles", Items.DEEPSLATE_TILES, 4, "deepslate_bricks");  // 深板岩瓦
            shapedRecipe2x2Block("cut_copper", Items.CUT_COPPER, 4, "copper_block");  // 切制铜块
            shapedRecipe2x2Block("cut_sandstone", Items.CUT_SANDSTONE, 4, "sandstone");  // 切制砂岩
            shapedRecipe2x2Block("cut_red_sandstone", Items.CUT_RED_SANDSTONE, 4, "red_sandstone");  // 切制红砂岩
            shapedRecipe2x2Block("quartz_bricks", Items.QUARTZ_BRICKS, 4, "quartz_block");  // 石英砖
            shapedRecipe2x2("quartz_pillar", Items.QUARTZ_PILLAR, 4, "quartz_block", o, "quartz_block", o);  // 石英柱
            shapedRecipe2x2Block("stone_bricks", Items.STONE_BRICKS, 4, "stone");  // 石砖
            shapedRecipe2x2("mossy_stone_bricks", Items.MOSSY_STONE_BRICKS, 1, "stone_bricks", "vine", o, o);  // 苔石砖
            shapedRecipe2x2("mossy_cobblestone", Items.MOSSY_COBBLESTONE, 1, "cobblestone", "vine", o, o);  // 苔圆石
            simple("nether_bricks", Items.NETHER_BRICKS, CollectNetherBricksTask::new).dontMineIfPresent();  // 下界砖块
            shapedRecipe2x2Block("red_nether_bricks", Items.RED_NETHER_BRICKS, 4, "nether_wart");  // 红色下界砖块
            smelt("cracked_stone_bricks", Items.CRACKED_STONE_BRICKS, "stone_bricks");  // 裂纹石砖
            smelt("cracked_nether_bricks", Items.CRACKED_NETHER_BRICKS, "nether_bricks");  // 裂纹下界砖
            smelt("cracked_polished_blackstone_bricks", Items.CRACKED_POLISHED_BLACKSTONE_BRICKS, "polished_blackstone_bricks");  // 裂纹磨制黑石砖
            smelt("cracked_deepslate_bricks", Items.CRACKED_DEEPSLATE_BRICKS, "deepslate_bricks");  // 裂纹深板岩砖
            smelt("cracked_deepslate_tiles", Items.CRACKED_DEEPSLATE_TILES, "deepslate_tiles");  // 裂纹深板岩瓦
            smelt("smooth_sandstone", Items.SMOOTH_SANDSTONE, "sandstone");  // 平滑砂岩
            smelt("smooth_red_sandstone", Items.SMOOTH_RED_SANDSTONE, "red_sandstone");  // 平滑红砂岩
            {
                String B = "nether_bricks";  // 下界砖
                String b = "nether_brick";   // 下界砖块
                shapedRecipe3x3("nether_brick_fence", Items.NETHER_BRICK_FENCE, 6, o, o, o, B, b, B, B, b, B);  // 下界砖栅栏
            }
            shapedRecipe3x3("brush", Items.BRUSH, 1, o, "feather", o, o, "copper_ingot", o, o, s, o);  // 刷子
            shapedRecipe3x3("paper", Items.PAPER, 3, "sugar_cane", "sugar_cane", "sugar_cane", o, o, o, o, o, o);  // 纸
            shapedRecipe2x2("book", Items.BOOK, 1, "paper", "paper", "paper", "leather");  // 书
            shapedRecipe2x2("writable_book", Items.WRITABLE_BOOK, 1, "book", "ink_sac", o, "feather");  // 书与羽毛
            alias("book_and_quill", "writable_book");  // 书与羽毛别名
            shapedRecipe3x3("bowl", Items.BOWL, 4, p, o, p, o, p, o, o, o, o);  // 碗
            shapedRecipe2x2("blaze_powder", Items.BLAZE_POWDER, 2, "blaze_rod", o, o, o);  // 烈焰粉
            shapedRecipe2x2("ender_eye", Items.ENDER_EYE, 1, "blaze_powder", "ender_pearl", o, o);  // 末影之眼
            alias("eye_of_ender", "ender_eye");  // 末影之眼别名
            shapedRecipe2x2("fermented_spider_eye", Items.FERMENTED_SPIDER_EYE, 1, "brown_mushroom", "sugar", o, "spider_eye");  // 发酵蛛眼
            shapedRecipe3x3("fire_charge", Items.FIRE_CHARGE, 3, o, "blaze_powder", o, o, "coal", o, o, "gunpowder", o);  // 火焰弹
            shapedRecipe2x2("flower_banner_pattern", Items.FLOWER_BANNER_PATTERN, 1, "paper", "oxeye_daisy", o, o);  // 花朵旗帜图案
            simple("magma_cream", Items.MAGMA_CREAM, CollectMagmaCreamTask::new);  // 岩浆膏
            // Slabs + Stairs + Walls
            shapedRecipeSlab("cobblestone_slab", Items.COBBLESTONE_SLAB, "cobblestone");
            shapedRecipeStairs("cobblestone_stairs", Items.COBBLESTONE_STAIRS, "cobblestone");
            shapedRecipeWall("cobblestone_wall", Items.COBBLESTONE_WALL, "cobblestone");
            shapedRecipeSlab("stone_slab", Items.STONE_SLAB, "stone");
            shapedRecipeStairs("stone_stairs", Items.STONE_STAIRS, "stone");
            shapedRecipeSlab("smooth_stone_slab", Items.SMOOTH_STONE_SLAB, "smooth_stone");
            shapedRecipeSlab("stone_brick_slab", Items.STONE_BRICK_SLAB, "stone_bricks");
            shapedRecipeStairs("stone_brick_stairs", Items.STONE_BRICK_STAIRS, "stone_bricks");
            shapedRecipeWall("stone_brick_wall", Items.STONE_BRICK_WALL, "stone_bricks");
            shapedRecipeSlab("mossy_stone_brick_slab", Items.MOSSY_STONE_BRICK_SLAB, "mossy_stone_bricks");
            shapedRecipeStairs("mossy_stone_brick_stairs", Items.MOSSY_STONE_BRICK_STAIRS, "mossy_stone_bricks");
            shapedRecipeWall("mossy_stone_brick_wall", Items.MOSSY_STONE_BRICK_WALL, "mossy_stone_bricks");
            shapedRecipeSlab("mossy_cobblestone_slab", Items.MOSSY_COBBLESTONE_SLAB, "mossy_cobblestone");
            shapedRecipeStairs("mossy_cobblestone_stairs", Items.MOSSY_COBBLESTONE_STAIRS, "mossy_cobblestone");
            shapedRecipeWall("mossy_cobblestone_wall", Items.MOSSY_COBBLESTONE_WALL, "mossy_cobblestone");
            shapedRecipeSlab("andesite_slab", Items.ANDESITE_SLAB, "andesite");
            shapedRecipeStairs("andesite_stairs", Items.ANDESITE_STAIRS, "andesite");
            shapedRecipeWall("andesite_wall", Items.ANDESITE_WALL, "andesite");
            shapedRecipeSlab("granite_slab", Items.GRANITE_SLAB, "granite");
            shapedRecipeStairs("granite_stairs", Items.GRANITE_STAIRS, "granite");
            shapedRecipeWall("granite_wall", Items.GRANITE_WALL, "granite");
            shapedRecipeSlab("diorite_slab", Items.DIORITE_SLAB, "diorite");
            shapedRecipeStairs("diorite_stairs", Items.DIORITE_STAIRS, "diorite");
            shapedRecipeWall("diorite_wall", Items.DIORITE_WALL, "diorite");
            shapedRecipeSlab("polished_andesite_slab", Items.POLISHED_ANDESITE_SLAB, "polished_andesite");
            shapedRecipeStairs("polished_andesite_stairs", Items.POLISHED_ANDESITE_STAIRS, "polished_andesite");
            shapedRecipeSlab("polished_granite_slab", Items.POLISHED_GRANITE_SLAB, "polished_granite");
            shapedRecipeStairs("polished_granite_stairs", Items.POLISHED_GRANITE_STAIRS, "polished_granite");
            shapedRecipeSlab("polished_diorite_slab", Items.POLISHED_DIORITE_SLAB, "polished_diorite");
            shapedRecipeStairs("polished_diorite_stairs", Items.POLISHED_DIORITE_STAIRS, "polished_diorite");
            shapedRecipeSlab("sandstone_slab", Items.SANDSTONE_SLAB, "sandstone");
            shapedRecipeStairs("sandstone_stairs", Items.SANDSTONE_STAIRS, "sandstone");
            shapedRecipeWall("sandstone_wall", Items.SANDSTONE_WALL, "sandstone");
            shapedRecipeSlab("cut_sandstone_slab", Items.CUT_SANDSTONE_SLAB, "cut_sandstone");
            shapedRecipeSlab("smooth_sandstone_slab", Items.SMOOTH_SANDSTONE_SLAB, "smooth_sandstone");
            shapedRecipeStairs("smooth_sandstone_stairs", Items.SMOOTH_SANDSTONE_STAIRS, "smooth_sandstone");
            shapedRecipeSlab("red_sandstone_slab", Items.RED_SANDSTONE_SLAB, "red_sandstone");
            shapedRecipeStairs("red_sandstone_stairs", Items.RED_SANDSTONE_STAIRS, "red_sandstone");
            shapedRecipeWall("red_sandstone_wall", Items.RED_SANDSTONE_WALL, "red_sandstone");
            shapedRecipeSlab("cut_red_sandstone_slab", Items.CUT_RED_SANDSTONE_SLAB, "cut_red_sandstone");
            shapedRecipeSlab("smooth_red_sandstone_slab", Items.SMOOTH_RED_SANDSTONE_SLAB, "smooth_red_sandstone");
            shapedRecipeStairs("smooth_red_sandstone_stairs", Items.SMOOTH_RED_SANDSTONE_STAIRS, "smooth_red_sandstone");
            shapedRecipeSlab("nether_brick_slab", Items.NETHER_BRICK_SLAB, "nether_bricks");
            shapedRecipeStairs("nether_brick_stairs", Items.NETHER_BRICK_STAIRS, "nether_bricks");
            shapedRecipeWall("nether_brick_wall", Items.NETHER_BRICK_WALL, "nether_bricks");
            shapedRecipeSlab("red_nether_brick_slab", Items.RED_NETHER_BRICK_SLAB, "red_nether_bricks");
            shapedRecipeStairs("red_nether_brick_stairs", Items.RED_NETHER_BRICK_STAIRS, "red_nether_bricks");
            shapedRecipeWall("red_nether_brick_wall", Items.RED_NETHER_BRICK_WALL, "red_nether_bricks");
            shapedRecipeSlab("quartz_slab", Items.QUARTZ_SLAB, "quartz_block");
            shapedRecipeStairs("quartz_stairs", Items.QUARTZ_STAIRS, "quartz_block");
            shapedRecipeSlab("smooth_quartz_slab", Items.SMOOTH_QUARTZ_SLAB, "smooth_quartz");
            shapedRecipeStairs("smooth_quartz_stairs", Items.SMOOTH_QUARTZ_STAIRS, "smooth_quartz");
            shapedRecipeSlab("blackstone_slab", Items.BLACKSTONE_SLAB, "blackstone");
            shapedRecipeStairs("blackstone_stairs", Items.BLACKSTONE_STAIRS, "blackstone");
            shapedRecipeWall("blackstone_wall", Items.BLACKSTONE_WALL, "blackstone");
            shapedRecipeSlab("polished_blackstone_slab", Items.POLISHED_BLACKSTONE_SLAB, "polished_blackstone");
            shapedRecipeStairs("polished_blackstone_stairs", Items.POLISHED_BLACKSTONE_STAIRS, "polished_blackstone");
            shapedRecipeWall("polished_blackstone_wall", Items.POLISHED_BLACKSTONE_WALL, "polished_blackstone");
            shapedRecipeSlab("polished_blackstone_brick_slab", Items.POLISHED_BLACKSTONE_BRICK_SLAB, "polished_blackstone_bricks");
            shapedRecipeStairs("polished_blackstone_brick_stairs", Items.POLISHED_BLACKSTONE_BRICK_STAIRS, "polished_blackstone_bricks");
            shapedRecipeWall("polished_blackstone_brick_wall", Items.POLISHED_BLACKSTONE_BRICK_WALL, "polished_blackstone_bricks");
            shapedRecipeSlab("cut_copper_slab", Items.CUT_COPPER_SLAB, "cut_copper");
            shapedRecipeStairs("cut_copper_stairs", Items.CUT_COPPER_STAIRS, "cut_copper");
            shapedRecipeSlab("cobbled_deepslate_slab", Items.COBBLED_DEEPSLATE_SLAB, "cobbled_deepslate");
            shapedRecipeStairs("cobbled_deepslate_stairs", Items.COBBLED_DEEPSLATE_STAIRS, "cobbled_deepslate");
            shapedRecipeWall("cobbled_deepslate_wall", Items.COBBLED_DEEPSLATE_WALL, "cobbled_deepslate");
            shapedRecipeSlab("polished_deepslate_slab", Items.POLISHED_DEEPSLATE_SLAB, "polished_deepslate");
            shapedRecipeStairs("polished_deepslate_stairs", Items.POLISHED_DEEPSLATE_STAIRS, "polished_deepslate");
            shapedRecipeWall("polished_deepslate_wall", Items.POLISHED_DEEPSLATE_WALL, "polished_deepslate");
            shapedRecipeSlab("deepslate_brick_slab", Items.DEEPSLATE_BRICK_SLAB, "deepslate_bricks");
            shapedRecipeStairs("deepslate_brick_stairs", Items.DEEPSLATE_BRICK_STAIRS, "deepslate_bricks");
            shapedRecipeWall("deepslate_brick_wall", Items.DEEPSLATE_BRICK_WALL, "deepslate_bricks");
            shapedRecipeSlab("deepslate_tile_slab", Items.DEEPSLATE_TILE_SLAB, "deepslate_tiles");
            shapedRecipeStairs("deepslate_tile_stairs", Items.DEEPSLATE_TILE_STAIRS, "deepslate_tiles");
            shapedRecipeWall("deepslate_tile_wall", Items.DEEPSLATE_TILE_WALL, "deepslate_tiles");
            shapedRecipe2x2("chiseled_sandstone", Items.CHISELED_SANDSTONE, 1, "sandstone_slab", o, "sandstone_slab", o);
            shapedRecipe2x2("chiseled_red_sandstone", Items.CHISELED_RED_SANDSTONE, 1, "red_sandstone_slab", o, "red_sandstone_slab", o);
            shapedRecipe2x2("chiseled_stone_bricks", Items.CHISELED_STONE_BRICKS, 1, "stone_brick_slab", o, "stone_brick_slab", o);
            shapedRecipe2x2("chiseled_nether_bricks", Items.CHISELED_NETHER_BRICKS, 1, "nether_brick_slab", o, "nether_brick_slab", o);
            shapedRecipe2x2("chiseled_quartz_block", Items.CHISELED_QUARTZ_BLOCK, 1, "quartz_slab", o, "quartz_slab", o);
            shapedRecipe2x2("chiseled_deepslate", Items.CHISELED_DEEPSLATE, 1, "cobbled_deepslate_slab", o, "cobbled_deepslate_slab", o);
            /// 工具
            tools("wooden", "planks", Items.WOODEN_PICKAXE, Items.WOODEN_SHOVEL, Items.WOODEN_SWORD, Items.WOODEN_AXE, Items.WOODEN_HOE);  // 木制工具
            tools("stone", "cobblestone", Items.STONE_PICKAXE, Items.STONE_SHOVEL, Items.STONE_SWORD, Items.STONE_AXE, Items.STONE_HOE);  // 石制工具
            tools("iron", "iron_ingot", Items.IRON_PICKAXE, Items.IRON_SHOVEL, Items.IRON_SWORD, Items.IRON_AXE, Items.IRON_HOE);  // 铁制工具
            tools("golden", "gold_ingot", Items.GOLDEN_PICKAXE, Items.GOLDEN_SHOVEL, Items.GOLDEN_SWORD, Items.GOLDEN_AXE, Items.GOLDEN_HOE);  // 金制工具
            tools("diamond", "diamond", Items.DIAMOND_PICKAXE, Items.DIAMOND_SHOVEL, Items.DIAMOND_SWORD, Items.DIAMOND_AXE, Items.DIAMOND_HOE);  // 钻石工具
            armor("leather", "leather", Items.LEATHER_HELMET, Items.LEATHER_CHESTPLATE, Items.LEATHER_LEGGINGS, Items.LEATHER_BOOTS);  // 皮革盔甲
            armor("iron", "iron_ingot", Items.IRON_HELMET, Items.IRON_CHESTPLATE, Items.IRON_LEGGINGS, Items.IRON_BOOTS);  // 铁制盔甲
            armor("golden", "gold_ingot", Items.GOLDEN_HELMET, Items.GOLDEN_CHESTPLATE, Items.GOLDEN_LEGGINGS, Items.GOLDEN_BOOTS);  // 金制盔甲
            armor("diamond", "diamond", Items.DIAMOND_HELMET, Items.DIAMOND_CHESTPLATE, Items.DIAMOND_LEGGINGS, Items.DIAMOND_BOOTS);  // 钻石盔甲
            smith("netherite_helmet", Items.NETHERITE_HELMET, "netherite_ingot", "diamond_helmet");  // 下界合金头盔
            smith("netherite_chestplate", Items.NETHERITE_CHESTPLATE, "netherite_ingot", "diamond_chestplate");  // 下界合金胸甲
            smith("netherite_leggings", Items.NETHERITE_LEGGINGS, "netherite_ingot", "diamond_leggings");  // 下界合金护腿
            smith("netherite_boots", Items.NETHERITE_BOOTS, "netherite_ingot", "diamond_boots");  // 下界合金靴子
            smith("netherite_pickaxe", Items.NETHERITE_PICKAXE, "netherite_ingot", "diamond_pickaxe");  // 下界合金镐
            smith("netherite_axe", Items.NETHERITE_AXE, "netherite_ingot", "diamond_axe");  // 下界合金斧
            smith("netherite_shovel", Items.NETHERITE_SHOVEL, "netherite_ingot", "diamond_shovel");  // 下界合金锹
            smith("netherite_sword", Items.NETHERITE_SWORD, "netherite_ingot", "diamond_sword");  // 下界合金剑
            smith("netherite_hoe", Items.NETHERITE_HOE, "netherite_ingot", "diamond_hoe");  // 下界合金锄
            shapedRecipe3x3("bow", Items.BOW, 1, "string", s, o, "string", o, s, "string", s, o);  // 弓
            shapedRecipe3x3("arrow", Items.ARROW, 4, "flint", o, o, s, o, o, "feather", o, o);  // 箭
            {
                String i = "iron_ingot";  // 铁锭
                shapedRecipe3x3("bucket", Items.BUCKET, 1, i, o, i, o, i, o, o, o, o);  // 桶
                shapedRecipe2x2("flint_and_steel", Items.FLINT_AND_STEEL, 1, i, o, o, "flint");  // 打火石
                shapedRecipe2x2("shears", Items.SHEARS, 1, i, o, o, i);  // 剪刀
                shapedRecipe2x2("iron_nugget", Items.IRON_NUGGET, 9, i, o, o, o);  // 铁粒
                shapedRecipe3x3("compass", Items.COMPASS, 1, o, i, o, i, "redstone", i, o, i, o);  // 指南针
                shapedRecipe3x3("shield", Items.SHIELD, 1, p, i, p, p, p, p, o, p, o);  // 盾牌
                String g = "gold_ingot";  // 金锭
                shapedRecipe3x3("clock", Items.CLOCK, 1, o, g, o, g, "redstone", g, o, g, o);  // 时钟
            }
            simple("water_bucket", Items.WATER_BUCKET, CollectBucketLiquidTask.CollectWaterBucketTask::new);  // 水桶
            simple("lava_bucket", Items.LAVA_BUCKET, CollectBucketLiquidTask.CollectLavaBucketTask::new);  // 熔岩桶
            {
                String a = "paper";  // 纸
                shapedRecipe3x3("map", Items.MAP, 1, a, a, a, a, "compass", a, a, a, a);  // 地图
            }
            shapedRecipe3x3("fishing_rod", Items.FISHING_ROD, 1, o, o, s, o, s, "string", s, o, "string");  // 钓鱼竿
            shapedRecipe2x2("carrot_on_a_stick", Items.CARROT_ON_A_STICK, 1, "fishing_rod", "carrot", o, o);  // 胡萝卜钓竿
            shapedRecipe2x2("warped_fungus_on_a_stick", Items.WARPED_FUNGUS_ON_A_STICK, 1, "fishing_rod", "warped_fungus", o, o);  // 诡异菌钓竿
            shapedRecipe3x3("spyglass", Items.SPYGLASS, 1, o, "amethyst_shard", o, o, "copper_ingot", o, o, "copper_ingot", o);  // 望远镜
            shapedRecipe3x3("glass_bottle", Items.GLASS_BOTTLE, 3, "glass", o, "glass", o, "glass", o, o, o, o);  // 玻璃瓶
            {
                String l = "leather";  // 皮革
                shapedRecipe3x3("leather_horse_armor", Items.LEATHER_HORSE_ARMOR, 1, l, o, l, l, l, l, l, o, l);  // 皮革马铠
            }
            alias("wooden_pick", "wooden_pickaxe");  // 木制镐别名
            alias("stone_pick", "stone_pickaxe");  // 石制镐别名
            alias("iron_pick", "iron_pickaxe");  // 铁制镐别名
            alias("gold_pick", "golden_pickaxe");  // 金制镐别名
            alias("diamond_pick", "diamond_pickaxe");  // 钻石镐别名
            alias("netherite_pick", "netherite_pickaxe");  // 下界合金镐别名
            simple("boat", ItemHelper.WOOD_BOAT, CollectBoatTask::new);  // 船
            woodTasks("boat", woodItems -> woodItems.boat, (woodItems, count) -> new CollectBoatTask(woodItems.boat, woodItems.prefix + "_planks", count));  // 木船任务
            shapedRecipe3x3("lead", Items.LEAD, 1, "string", "string", o, "string", "slime_ball", o, o, o, "string");  // 拴绳

            simple("honeycomb", Items.HONEYCOMB, CollectHoneycombTask::new);  // 蜂巢
            {
                String h = "honeycomb";  // 蜂巢
                shapedRecipe2x2Block("honeycomb_block", Items.HONEYCOMB_BLOCK, h);  // 蜂巢块
                shapedRecipe2x2("candle", Items.CANDLE, 1, "string", o, h, o);  // 蜡烛
                shapedRecipe3x3("beehive", Items.BEEHIVE, 1, p, p, p, h, h, h, p, p, p);  // 蜂箱
            }

            // 家具
            shapedRecipe2x2("crafting_table", Items.CRAFTING_TABLE, 1, p, p, p, p).dontMineIfPresent();  // 工作台
            shapedRecipe3x3("smithing_table", Items.SMITHING_TABLE, 1, "iron_ingot", "iron_ingot", o, p, p, o, p, p, o);  // 锻造台
            shapedRecipe3x3("grindstone", Items.GRINDSTONE, 1, s, "stone_slab", s, p, o, p, o, o, o);  // 砂轮
            simple("wooden_pressure_plate", ItemHelper.WOOD_PRESSURE_PLATE, CollectWoodenPressurePlateTask::new);  // 木质压力板
            woodTasks("pressure_plate", woodItems -> woodItems.pressurePlate, (woodItems, count) -> new CollectWoodenPressurePlateTask(woodItems.pressurePlate, woodItems.prefix + "_planks", count));  // 压力板任务
            simple("wooden_button", ItemHelper.WOOD_BUTTON, CollectWoodenButtonTask::new);  // 木质按钮
            woodTasks("button", woodItems -> woodItems.button, (woodItems, count) -> new CraftInInventoryTask(new RecipeTarget(woodItems.button, 1, CraftingRecipe.newShapedRecipe(woodItems.prefix + "_button", new ItemTarget[]{new ItemTarget(woodItems.planks, 1), null, null, null}, 1))));  // 按钮任务
            shapedRecipe2x2("stone_pressure_plate", Items.STONE_PRESSURE_PLATE, 1, o, o, "stone", "stone");  // 石质压力板
            shapedRecipe2x2("stone_button", Items.STONE_BUTTON, 1, "stone", o, o, o);  // 石质按钮
            shapedRecipe2x2("polished_blackstone_pressure_plate", Items.POLISHED_BLACKSTONE_PRESSURE_PLATE, 1, o, o, "polished_blackstone", "polished_blackstone");  // 磨制黑石压力板
            shapedRecipe2x2("polished_blackstone_button", Items.POLISHED_BLACKSTONE_BUTTON, 1, "polished_blackstone", o, o, o);  // 磨制黑石按钮
            simple("sign", ItemHelper.WOOD_SIGN, CollectSignTask::new).dontMineIfPresent(); // 默认情况下，我们保存这些地方的告示牌。
            woodTasks("sign", woodItems -> woodItems.sign, (woodItems, count) -> new CollectSignTask(woodItems.sign, woodItems.prefix + "_planks", count));  // 告示牌任务
            {
                String c = "cobblestone";  // 圆石
                shapedRecipe3x3("furnace", Items.FURNACE, 1, c, c, c, c, o, c, c, c, c).dontMineIfPresent();  // 熔炉
                shapedRecipe3x3("dropper", Items.DROPPER, 1, c, c, c, c, o, c, c, "redstone", c);  // 投掷器
                shapedRecipe3x3("dispenser", Items.DISPENSER, 1, c, c, c, c, "bow", c, c, "redstone", c);  // 发射器
                shapedRecipe3x3("brewing_stand", Items.BREWING_STAND, 1, o, o, o, o, "blaze_rod", o, c, c, c);  // 酿造台
                shapedRecipe3x3("piston", Items.PISTON, 1, p, p, p, c, "iron_ingot", c, c, "redstone", c);  // 活塞
                shapedRecipe3x3("observer", Items.OBSERVER, 1, c, c, c, "redstone", "redstone", "quartz", c, c, c);  // 观察者
                shapedRecipe2x2("lever", Items.LEVER, 1, s, o, c, o);  // 拉杆
            }
            simple("hanging_sign", ItemHelper.WOOD_HANGING_SIGN, CollectHangingSignTask::new).dontMineIfPresent();  // 悬挂告示牌
            woodTasks("hanging_sign", woodItems -> woodItems.hangingSign, (woodItems, count) -> new CollectHangingSignTask(woodItems.hangingSign, woodItems.prefix + "_stripped_logs", count));  // 悬挂告示牌任务
            shapedRecipe3x3("chest", Items.CHEST, 1, p, p, p, p, o, p, p, p, p).dontMineIfPresent();  // 箱子
            shapedRecipe2x2("torch", Items.TORCH, 4, "coal", o, s, o);  // 火把
            simple("bed", ItemHelper.BED, CollectBedTask::new);  // 床
            colorfulTasks("bed", colors -> colors.bed, (colors, count) -> new CollectBedTask(colors.bed, colors.colorName + "_wool", count));  // 彩色床任务
            {
                String i = "iron_ingot";  // 铁锭
                String b = "iron_block";   // 铁块
                String m = "smooth_stone"; // 平滑石头
                shapedRecipe3x3("anvil", Items.ANVIL, 1, b, b, b, o, i, o, i, i, i);  // 铁砧
                shapedRecipe3x3("cauldron", Items.CAULDRON, 1, i, o, i, i, o, i, i, i, i);  // 炼药锅
                shapedRecipe3x3("minecart", Items.MINECART, 1, o, o, o, i, o, i, i, i, i);  // 矿车
                shapedRecipe3x3("iron_door", Items.IRON_DOOR, 3, i, i, o, i, i, o, i, i, o);  // 铁门
                shapedRecipe3x3("iron_bars", Items.IRON_BARS, 16, i, i, i, i, i, i, o, o, o);  // 铁栏杆
                shapedRecipe3x3("blast_furnace", Items.BLAST_FURNACE, 1, i, i, i, i, "furnace", i, m, m, m);  // 高炉
                shapedRecipe2x2Block("iron_trapdoor", Items.IRON_TRAPDOOR, i);  // 铁活板门
            }
            shapedRecipe3x3("armor_stand", Items.ARMOR_STAND, 1, s, s, s, o, s, o, s, "smooth_stone_slab", s);  // 盔甲架
            {
                String b = "obsidian";  // 黑曜石
                shapedRecipe3x3("enchanting_table", Items.ENCHANTING_TABLE, 1, o, "book", o, "diamond", b, "diamond", b, b, b);  // 附魔台
                shapedRecipe3x3("ender_chest", Items.ENDER_CHEST, 1, b, b, b, b, "ender_eye", b, b, b, b).dontMineIfPresent();  // 末影箱
            }
            {
                String b = "brick";  // 砖
                shapedRecipe3x3("decorated_pot", Items.DECORATED_POT, 1, o, b, o, b, o, b, o, b, o);  // 装饰陶罐
                shapedRecipe3x3("flower_pot", Items.FLOWER_POT, 1, b, o, b, o, b, o, o, o, o);  // 花盆
                shapedRecipe2x2Block("bricks", Items.BRICKS, b);  // 砖块
                shapedRecipeSlab("brick_slab", Items.BRICK_SLAB, b);  // 砖台阶
                shapedRecipeStairs("brick_stairs", Items.BRICK_STAIRS, b);  // 砖楼梯
                shapedRecipeStairs("brick_wall", Items.BRICK_WALL, "brick");  // 砖墙
            }
            shapedRecipe3x3("ladder", Items.LADDER, 3, s, o, s, s, s, s, s, o, s);  // 梯子
            shapedRecipe3x3("jukebox", Items.JUKEBOX, 1, p, p, p, p, "diamond", p, p, p, p);  // 唱片机
            shapedRecipe3x3("note_block", Items.NOTE_BLOCK, 1, p, p, p, p, "redstone", p, p, p, p);  // 音符盒
            shapedRecipe3x3("redstone_lamp", Items.REDSTONE_LAMP, 1, o, "redstone", o, "redstone", "glowstone", "redstone", o, "redstone", o);  // 红石灯
            shapedRecipe3x3("bookshelf", Items.BOOKSHELF, 1, p, p, p, "book", "book", "book", p, p, p);  // 书架
            shapedRecipe2x2("loom", Items.LOOM, 1, "string", "string", p, p);  // 织布机
            {
                String g = "glass";  // 玻璃
                shapedRecipe3x3("glass_pane", Items.GLASS_PANE, 16, g, g, g, g, g, g, o, o, o).dontMineIfPresent();  // 玻璃板
            }
            simple("carved_pumpkin", Items.CARVED_PUMPKIN, count -> new CarveThenCollectTask(Items.CARVED_PUMPKIN, count, Blocks.CARVED_PUMPKIN, Items.PUMPKIN, Blocks.PUMPKIN, Items.SHEARS));  // 雕刻南瓜
            shapedRecipe2x2("jack_o_lantern", Items.JACK_O_LANTERN, 1, "carved_pumpkin", o, "torch", o);  // 南瓜灯
            shapedRecipe3x3("target", Items.TARGET, 1, o, "redstone", o, "redstone", "hay_block", "redstone", o, "redstone", o);  // 目标
            shapedRecipe3x3("campfire", Items.CAMPFIRE, 1, o, s, o, s, "coal", s, "log", "log", "log");  // 营火
            shapedRecipe3x3("soul_campfire", Items.SOUL_CAMPFIRE, 1, o, s, o, s, "soul_soil", s, "log", "log", "log");  // 灵魂营火
            shapedRecipe3x3("soul_torch", Items.SOUL_TORCH, 4, o, "coal", o, o, s, o, o, "soul_soil", o);  // 灵魂火把
            {
                String l = "log";  // 原木
                shapedRecipe3x3("smoker", Items.SMOKER, 1, o, l, o, l, "furnace", l, o, l, o);  // 烟熏炉
            }
            {
                String i = "iron_nugget";  // 铁粒
                shapedRecipe3x3("lantern", Items.LANTERN, 1, i, i, i, i, "torch", i, i, i, i);  // 灯笼
                shapedRecipe3x3("soul_lantern", Items.SOUL_LANTERN, 1, i, i, i, i, "soul_torch", i, i, i, i);  // 灵魂灯笼
                shapedRecipe3x3("chain", Items.CHAIN, 1, o, i, o, o, "iron_ingot", o, o, i, o);  // 锁链
            }
            {
                String c = "chiseled_stone_bricks";  // 雕纹石砖
                shapedRecipe3x3("lodestone", Items.LODESTONE, 1, c, c, c, c, "netherite_ingot", c, c, c, c);  // 磁石
            }
            shapedRecipe3x3("lightning_rod", Items.LIGHTNING_ROD, 1, o, "copper_ingot", o, o, "copper_ingot", o, o, "copper_ingot", o);  // 闪电Rod
            shapedRecipe3x3("tinted_glass", Items.TINTED_GLASS, 2, o, "amethyst_shard", o, "amethyst_shard", "glass", "amethyst_shard", o, "amethyst_shard", o);  // 染色玻璃

            // 一堆木制物品
            simple("wooden_stairs", ItemHelper.WOOD_STAIRS, CollectWoodenStairsTask::new);  // 木制楼梯
            woodTasks("stairs", woodItems -> woodItems.stairs, (woodItems, count) -> new CollectWoodenStairsTask(woodItems.stairs, woodItems.prefix + "_planks", count));  // 楼梯任务
            simple("wooden_slab", ItemHelper.WOOD_SLAB, CollectWoodenSlabTask::new);  // 木制台阶
            woodTasks("slab", woodItems -> woodItems.slab, (woodItems, count) -> new CollectWoodenSlabTask(woodItems.slab, woodItems.prefix + "_planks", count));  // 台阶任务
            simple("wooden_door", ItemHelper.WOOD_DOOR, CollectWoodenDoorTask::new);  // 木制门
            woodTasks("door", woodItems -> woodItems.door, (woodItems, count) -> new CollectWoodenDoorTask(woodItems.door, woodItems.prefix + "_planks", count));  // 门任务
            simple("wooden_trapdoor", ItemHelper.WOOD_TRAPDOOR, CollectWoodenTrapDoorTask::new);  // 木制活板门
            woodTasks("trapdoor", woodItems -> woodItems.trapdoor, (woodItems, count) -> new CollectWoodenTrapDoorTask(woodItems.trapdoor, woodItems.prefix + "_planks", count));  // 活板门任务
            simple("wooden_fence", ItemHelper.WOOD_FENCE, CollectFenceTask::new);  // 木制栅栏
            woodTasks("fence", woodItems -> woodItems.fence, (woodItems, count) -> new CollectFenceTask(woodItems.fence, woodItems.prefix + "_planks", count));  // 栅栏任务
            simple("wooden_fence_gate", ItemHelper.WOOD_FENCE_GATE, CollectFenceGateTask::new);  // 木制栅栏门
            woodTasks("fence_gate", woodItems -> woodItems.fenceGate, (woodItems, count) -> new CollectFenceGateTask(woodItems.fenceGate, woodItems.prefix + "_planks", count));  // 栅栏门任务
            {
                String r = "wooden_slab";  // 木制台阶
                shapedRecipe3x3("chiseled_bookshelf", Items.CHISELED_BOOKSHELF, 1, p, p, p, r, r, r, p, p, p).dontMineIfPresent();  // 雕纹书架
                shapedRecipe3x3("barrel", Items.BARREL, 1, p, r, p, p, o, p, p, r, p);  // 木桶
                shapedRecipe3x3("cartography_table", Items.CARTOGRAPHY_TABLE, 1, "paper", "paper", o, p, p, o, p, p, o);  // 制图台
                shapedRecipe3x3("composter", Items.COMPOSTER, 1, r, o, r, r, o, r, r, r, r);  // 堆肥桶
                shapedRecipe3x3("fletching_table", Items.FLETCHING_TABLE, 1, "flint", "flint", o, p, p, o, p, p, o);  // 制箭台
                shapedRecipe3x3("lectern", Items.LECTERN, 1, r, r, r, o, "bookshelf", o, o, r, o);  // 讲台
            }            // 大多数人说"门"时总是想到"木门"。
            alias("door", "wooden_door");  // 门别名
            alias("trapdoor", "wooden_trapdoor");  // 活板门别名
            alias("fence", "wooden_fence");  // 栅栏别名
            alias("fence_gate", "wooden_fence_gate");  // 栅栏门别名

            shapedRecipe2x2("heavy_weighted_pressure_plate", Items.HEAVY_WEIGHTED_PRESSURE_PLATE, 1, "iron_ingot", "iron_ingot", o, o);  // 重质测重压力板
            shapedRecipe2x2("light_weighted_pressure_plate", Items.LIGHT_WEIGHTED_PRESSURE_PLATE, 1, "gold_ingot", "gold_ingot", o, o);  // 轻质测重压力板

            shapedRecipe3x3("daylight_detector", Items.DAYLIGHT_DETECTOR, 1, "glass", "glass", "glass", "quartz", "quartz", "quartz", "wooden_slab", "wooden_slab", "wooden_slab");  // 阳光探测器
            shapedRecipe3x3("tripwire_hook", Items.TRIPWIRE_HOOK, 2, "iron_ingot", o, o, "stick", o, o, "planks", o, o);  // 绊线钩
            shapedRecipe2x2("trapped_chest", Items.TRAPPED_CHEST, 1, "chest", "tripwire_hook", o, o);  // 陷阱箱
            shapedRecipe3x3("crossbow", Items.CROSSBOW, 1, s, "iron_ingot", s, "string", "tripwire_hook", "string", o, s, o);  // 弩
            {
                String t = "gunpowder";  // 火药
                String n = "sand";       // 沙子
                shapedRecipe3x3("tnt", Items.TNT, 1, t, n, t, n, t, n, t, n, t);  // TNT
            }
            shapedRecipe2x2("sticky_piston", Items.STICKY_PISTON, 1, "slime_ball", o, "piston", o);  // 粘性活塞
            shapedRecipe2x2("redstone_torch", Items.REDSTONE_TORCH, 1, "redstone", o, s, o);  // 红石火把
            shapedRecipe3x3("repeater", Items.REPEATER, 1, "redstone_torch", "redstone", "redstone_torch", "stone", "stone", "stone", o, o, o);  // 红石中继器
            shapedRecipe3x3("comparator", Items.COMPARATOR, 1, o, "redstone_torch", o, "redstone_torch", "quartz", "redstone_torch", "stone", "stone", "stone");  // 红石比较器
            {
                // 一些铁轨
                String i = "iron_ingot";  // 铁锭
                String g = "gold_ingot";  // 金锭
                shapedRecipe3x3("rail", Items.RAIL, 16, i, o, i, i, s, i, i, o, i);  // 铁轨
                shapedRecipe3x3("powered_rail", Items.POWERED_RAIL, 6, g, o, g, g, s, g, g, "redstone", g);  // 动力铁路
                shapedRecipe3x3("detector_rail", Items.DETECTOR_RAIL, 6, i, o, i, i, "stone_pressure_plate", i, i, "redstone", i);  // 探测铁轨
                shapedRecipe3x3("activator_rail", Items.ACTIVATOR_RAIL, 6, i, s, i, i, "redstone_torch", i, i, s, i);  // 激活铁轨
                shapedRecipe3x3("hopper", Items.HOPPER, 1, i, o, i, i, "chest", i, o, i, o);  // 漏斗
            }
            shapedRecipe3x3("painting", Items.PAINTING, 1, s, s, s, s, "wool", s, s, s, s);  // 画
            shapedRecipe3x3("item_frame", Items.ITEM_FRAME, 1, s, s, s, s, "leather", s, s, s, s);  // 物品展示框
            shapedRecipe2x2("glow_item_frame", Items.GLOW_ITEM_FRAME, 1, "item_frame", "glow_ink_sac", o, o);  // 发光物品展示框
            shapedRecipe2x2("chest_minecart", Items.CHEST_MINECART, 1, "chest", o, "minecart", o);  // 运输矿车
            shapedRecipe2x2("furnace_minecart", Items.FURNACE_MINECART, 1, "furnace", o, "minecart", o);  // 动力矿车
            shapedRecipe2x2("hopper_minecart", Items.HOPPER_MINECART, 1, "hopper", o, "minecart", o);  // 漏斗矿车
            shapedRecipe2x2("tnt_minecart", Items.TNT_MINECART, 1, "tnt", o, "minecart", o);  // TNT矿车
            alias("minecart_with_chest", "chest_minecart");  // 箱子矿车别名
            alias("minecart_with_furnace", "furnace_minecart");  // 熔炉矿车别名
            alias("minecart_with_hopper", "hopper_minecart");  // 漏斗矿车别名
            alias("minecart_with_tnt", "tnt_minecart");  // TNT矿车别名


            /// 食物
            mobCook("porkchop", Items.PORKCHOP, Items.COOKED_PORKCHOP, PigEntity.class);  // 猪排
            mobCook("beef", Items.BEEF, Items.COOKED_BEEF, CowEntity.class);  // 牛肉
            mobCook("chicken", Items.CHICKEN, Items.COOKED_CHICKEN, ChickenEntity.class);  // 鸡肉
            mobCook("mutton", Items.MUTTON, Items.COOKED_MUTTON, SheepEntity.class);  // 羊肉
            mobCook("rabbit", Items.RABBIT, Items.COOKED_RABBIT, RabbitEntity.class);  // 兔肉
            mobCook("salmon", Items.SALMON, Items.COOKED_SALMON, SalmonEntity.class);  // 鲑鱼
            mobCook("cod", Items.COD, Items.COOKED_COD, CodEntity.class);  // 鳕鱼
            simple("milk", Items.MILK_BUCKET, CollectMilkTask::new);  // 牛奶
            mine("apple", Blocks.OAK_LEAVES, Items.APPLE);  // 苹果
            smelt("baked_potato", Items.BAKED_POTATO, "potato");  // 烤马铃薯
            shapedRecipe2x2("mushroom_stew", Items.MUSHROOM_STEW, 1, "red_mushroom", "brown_mushroom", "bowl", o);
            shapedRecipe2x2("suspicious_stew", Items.SUSPICIOUS_STEW, 1, "red_mushroom", "brown_mushroom", "bowl", "flower");
            shapedRecipe3x3("bread", Items.BREAD, 1, "wheat", "wheat", "wheat", o, o, o, o, o, o);
            shapedRecipe3x3("cookie", Items.COOKIE, 8, "wheat", "cocoa_beans", "wheat", o, o, o, o, o, o);
            shapedRecipe2x2("pumpkin_pie", Items.PUMPKIN_PIE, 1, "pumpkin", "sugar", o, "egg");
            shapedRecipe3x3("cake", Items.CAKE, 1, "milk", "milk", "milk", "sugar", "egg", "sugar", "wheat", "wheat", "wheat").dontMineIfPresent();
            {
                String g = "gold_nugget";
                shapedRecipe3x3("golden_carrot", Items.GOLDEN_CARROT, 1, g, g, g, g, "carrot", g, g, g, g);
                String i = "gold_ingot";
                shapedRecipe3x3("golden_apple", Items.GOLDEN_APPLE, 1, i, i, i, i, "apple", i, i, i, i);
            }
            shapedRecipe3x3("rabbit_stew", Items.RABBIT_STEW, 1, o, "cooked_rabbit", o, "carrot", "baked_potato", "mushroom", o, "bowl", o);
            {
                String b = "beetroot";
                shapedRecipe3x3("beetroot_soup", Items.BEETROOT_SOUP, 1, b, b, b, b, b, b, o, "bowl", o);
            }
        }
    }

    private static CataloguedResource put(String name, Item[] matches, Function<Integer, ResourceTask> getTask) {
        List<Item> supportedMatches = new ArrayList<>();
        for (Item item : matches) {
            if (item != Items.UNSUPPORTED) {
                supportedMatches.add(item);
            }
        }
        matches = supportedMatches.toArray(new Item[0]);

        CataloguedResource result = new CataloguedResource(matches, getTask);
        Block[] blocks = ItemHelper.itemsToBlocks(matches);
        // DEFAULT BEHAVIOUR: Mine if present & assume overworld is required!
        if (blocks.length != 0) {
            result.mineIfPresent();
        }
        result.forceDimension(Dimension.OVERWORLD);
        if (nameToResourceTask.containsKey(name)) {
            throw new IllegalStateException("Tried cataloguing " + name + " twice!");
        }
        nameToResourceTask.put(name, result);
        nameToItemMatches.put(name, matches);
        resourcesObtainable.addAll(Arrays.asList(matches));

        // If this resource is just one item, consider it collectable.
        if (matches.length == 1) {
            if (itemToResourceTask.containsKey(matches[0])) {
                throw new IllegalStateException("Tried cataloguing " + matches[0].getTranslationKey() + " twice!");
            }
            itemToResourceTask.put(matches[0], result);
        }

        return result;
    }

    // This is here so that we can use strings for item targets (optionally) and stuff like that.
    public static Item[] getItemMatches(String name) {
        if (!nameToItemMatches.containsKey(name)) {
            return new Item[0];
        }
        return nameToItemMatches.get(name);
    }

    public static boolean isObtainable(Item item) {
        return resourcesObtainable.contains(item);
    }

    public static ItemTarget getItemTarget(String name, int count) {
        return new ItemTarget(name, count);
    }

    public static CataloguedResourceTask getSquashedItemTask(ItemTarget... targets) {
        return new CataloguedResourceTask(true, targets);
    }

    public static ResourceTask getItemTask(String name, int count) {

        if (!taskExists(name)) {
            Debug.logWarning("Task " + name + " does not exist. Error possibly.");
            Debug.logStack();
            return null;
        }

        return nameToResourceTask.get(name).getResource(count);
    }

    public static ResourceTask getItemTask(Item item, int count) {
        if (!taskExists(item)) {
            Debug.logWarning("Task " + item + " does not exist. Error possibly.");
            Debug.logStack();
            return null;
        }

        return itemToResourceTask.get(item).getResource(count);
    }

    public static ResourceTask getItemTask(ItemTarget target) {
        if (target.isCatalogueItem()) {
            return getItemTask(target.getCatalogueName(), target.getTargetCount());
        } else if (target.getMatches().length == 1) {
            return getItemTask(target.getMatches()[0], target.getTargetCount());
        } else {
            return getSquashedItemTask(target);
        }
    }

    public static boolean taskExists(String name) {
        return nameToResourceTask.containsKey(name);
    }

    public static boolean taskExists(Item item) {
        return itemToResourceTask.containsKey(item);
    }

    public static Collection<String> resourceNames() {
        return nameToResourceTask.keySet();
    }

    private static CataloguedResource simple(String name, Item[] matches, Function<Integer, ResourceTask> getTask) {
        return put(name, matches, getTask);
    }

    private static CataloguedResource simple(String name, Item matches, Function<Integer, ResourceTask> getTask) {
        return simple(name, new Item[]{matches}, getTask);
    }

    // TODO: Do I really need this?
    //private static CataloguedResource task(Item matches, Function<Integer, ResourceTask> getTask){
    //
    //}

    private static CataloguedResource mine(String name, MiningRequirement requirement, Item[] toMine, Item... targets) {
        Block[] toMineBlocks = new Block[toMine.length];
        for (int i = 0; i < toMine.length; ++i) toMineBlocks[i] = Block.getBlockFromItem(toMine[i]);
        return mine(name, requirement, toMineBlocks, targets);
    }

    private static CataloguedResource mine(String name, MiningRequirement requirement, Block[] toMine, Item... targets) {
        return put(name, targets, count -> new MineAndCollectTask(new ItemTarget(targets, count), toMine, requirement)).dontMineIfPresent(); // Mining already taken care of!!
    }

    private static CataloguedResource mine(String name, MiningRequirement requirement, Block toMine, Item target) {
        return mine(name, requirement, new Block[]{toMine}, target);
    }

    private static CataloguedResource mine(String name, Block toMine, Item target) {
        return mine(name, MiningRequirement.HAND, toMine, target);
    }

    private static CataloguedResource mine(String name, Item target) {
        return mine(name, Block.getBlockFromItem(target), target);
    }

    private static CataloguedResource shear(String name, Block[] toShear, Item... targets) {
        return put(name, targets, count -> new ShearAndCollectBlockTask(new ItemTarget[]{new ItemTarget(targets, count)}, toShear)).dontMineIfPresent();
    }

    private static CataloguedResource shear(String name, Block toShear, Item... targets) {
        return shear(name, new Block[]{toShear}, targets);
    }

    private static CataloguedResource shapedRecipe2x2(String name, Item match, int outputCount, String s0, String s1, String s2, String s3) {
        CraftingRecipe recipe = CraftingRecipe.newShapedRecipe(name, new ItemTarget[]{t(s0), t(s1), t(s2), t(s3)}, outputCount);
        return put(name, new Item[]{match}, count -> new CraftInInventoryTask(new RecipeTarget(match, count, recipe)));
    }

    private static CataloguedResource shapedRecipe3x3(String name, Item match, int outputCount, String s0, String s1, String s2, String s3, String s4, String s5, String s6, String s7, String s8) {
        CraftingRecipe recipe = CraftingRecipe.newShapedRecipe(name, new ItemTarget[]{t(s0), t(s1), t(s2), t(s3), t(s4), t(s5), t(s6), t(s7), t(s8)}, outputCount);
        return put(name, new Item[]{match}, count -> new CraftInTableTask(new RecipeTarget(match, count, recipe)));
    }

    private static CataloguedResource shapedRecipe2x2Block(String name, Item match, String material) {
        return shapedRecipe2x2(name, match, 1, material, material, material, material);
    }

    private static CataloguedResource shapedRecipe2x2Block(String name, Item match, int outputCount, String material) {
        return shapedRecipe2x2(name, match, outputCount, material, material, material, material);
    }

    private static CataloguedResource shapedRecipe3x3Block(String name, Item match, String material) {
        return shapedRecipe3x3(name, match, 1, material, material, material, material, material, material, material, material, material);
    }

    private static CataloguedResource shapedRecipeSlab(String name, Item match, String material) {
        return shapedRecipe3x3(name, match, 6, null, null, null, null, null, null, material, material, material);
    }

    private static CataloguedResource shapedRecipeStairs(String name, Item match, String material) {
        return shapedRecipe3x3(name, match, 4, material, null, null, material, material, null, material, material, material);
    }

    private static CataloguedResource shapedRecipeWall(String name, Item match, String material) {
        return shapedRecipe3x3(name, match, 6, material, material, material, material, material, material, null, null, null);
    }

    private static CataloguedResource smelt(String name, Item[] matches, String materials, Item... optionalMaterials) {
        return put(name, matches, count -> new SmeltInFurnaceTask(new SmeltTarget(new ItemTarget(matches, count), new ItemTarget(materials, count), optionalMaterials)));
    }

    private static CataloguedResource smelt(String name, Item match, String materials, Item... optionalMaterials) {
        return smelt(name, new Item[]{match}, materials, optionalMaterials);
    }

    private static CataloguedResource smith(String name, Item[] matches, String materials, String tool) {
        return put(name, matches, count -> new UpgradeInSmithingTableTask(new ItemTarget(tool, count), new ItemTarget(materials, count), new ItemTarget(matches, count)));//new SmeltInFurnaceTask(new SmeltTarget(new ItemTarget(matches, count), new ItemTarget(materials, count))));
    }

    private static CataloguedResource smith(String name, Item match, String materials, String tool) {
        return smith(name, new Item[]{match}, materials, tool);
    }

    private static CataloguedResource mob(String name, Item[] matches, Class mobClass) {
        return put(name, matches, count -> new KillAndLootTask(mobClass, new ItemTarget(matches, count)));
    }

    private static CataloguedResource mob(String name, Item match, Class mobClass) {
        return mob(name, new Item[]{match}, mobClass);
    }

    private static void mobCook(String uncookedName, String cookedName, Item uncooked, Item cooked, Class mobClass) {
        mob(uncookedName, uncooked, mobClass);
        smelt(cookedName, cooked, uncookedName);
    }

    private static void mobCook(String uncookedName, Item uncooked, Item cooked, Class mobClass) {
        mobCook(uncookedName, "cooked_" + uncookedName, uncooked, cooked, mobClass);
    }

    private static CataloguedResource crop(String name, Item[] matches, Block[] cropBlocks, Item[] cropSeeds) {
        return put(name, matches, count -> new CollectCropTask(new ItemTarget(matches, count), cropBlocks, cropSeeds));
    }

    public static CataloguedResource crop(String name, Item match, Block cropBlock, Item cropSeed) {
        return crop(name, new Item[]{match}, new Block[]{cropBlock}, new Item[]{cropSeed});
    }

    private static void colorfulTasks(String baseName, Function<ItemHelper.ColorfulItems, Item> getMatch, BiFunction<ItemHelper.ColorfulItems, Integer, ResourceTask> getTask) {
        for (DyeColor dCol : DyeColor.values()) {
            MapColor mCol = dCol.getMapColor();
            ItemHelper.ColorfulItems color = ItemHelper.getColorfulItems(mCol);
            String prefix = color.colorName;
            put(prefix + "_" + baseName, new Item[]{getMatch.apply(color)}, count -> getTask.apply(color, count));
        }
    }

    private static CataloguedResource[] woodTasks(Function<ItemHelper.WoodItems, String> getCatalogueName, Function<ItemHelper.WoodItems, Item> getMatch, BiFunction<ItemHelper.WoodItems, Integer, ResourceTask> getTask, boolean requireNetherForNetherStuff) {
        List<CataloguedResource> result = new ArrayList<>();
        for (WoodType woodType : WoodType.values()) {
            ItemHelper.WoodItems woodItems = ItemHelper.getWoodItems(woodType);
            Item match = getMatch.apply(woodItems);
            String cataloguedName = getCatalogueName.apply(woodItems);
            if (match == null) continue;
            boolean isNether = woodItems.isNetherWood();
            CataloguedResource t = put(cataloguedName, new Item[]{match}, count -> getTask.apply(woodItems, count));
            if (requireNetherForNetherStuff && isNether) {
                t.forceDimension(Dimension.NETHER);
            }
            result.add(t);
        }
        return result.toArray(CataloguedResource[]::new);
    }

    private static CataloguedResource[] woodTasks(String baseName, Function<ItemHelper.WoodItems, Item> getMatch, BiFunction<ItemHelper.WoodItems, Integer, ResourceTask> getTask, boolean requireNetherForNetherStuff) {
        return woodTasks(woodItem -> woodItem.prefix + "_" + baseName, getMatch, getTask, requireNetherForNetherStuff);
    }

    private static CataloguedResource[] woodTasks(String baseName, Function<ItemHelper.WoodItems, Item> getMatch, BiFunction<ItemHelper.WoodItems, Integer, ResourceTask> getTask) {
        return woodTasks(baseName, getMatch, getTask, false);
    }

    private static void tools(String toolMaterialName, String material, Item pickaxeItem, Item shovelItem, Item swordItem, Item axeItem, Item hoeItem) {
        String s = "stick";
        String o = null;
        //noinspection UnnecessaryLocalVariable
        String m = material;
        shapedRecipe3x3(toolMaterialName + "_pickaxe", pickaxeItem, 1, m, m, m, o, s, o, o, s, o);
        shapedRecipe3x3(toolMaterialName + "_shovel", shovelItem, 1, o, m, o, o, s, o, o, s, o);
        shapedRecipe3x3(toolMaterialName + "_sword", swordItem, 1, o, m, o, o, m, o, o, s, o);
        shapedRecipe3x3(toolMaterialName + "_axe", axeItem, 1, m, m, o, m, s, o, o, s, o);
        shapedRecipe3x3(toolMaterialName + "_hoe", hoeItem, 1, m, m, o, o, s, o, o, s, o);
    }

    private static void armor(String armorMaterialName, String material, Item helmetItem, Item chestplateItem, Item leggingsItem, Item bootsItem) {
        String o = null;
        //noinspection UnnecessaryLocalVariable
        String m = material;
        shapedRecipe3x3(armorMaterialName + "_helmet", helmetItem, 1, m, m, m, m, o, m, o, o, o);
        shapedRecipe3x3(armorMaterialName + "_chestplate", chestplateItem, 1, m, o, m, m, m, m, m, m, m);
        shapedRecipe3x3(armorMaterialName + "_leggings", leggingsItem, 1, m, m, m, m, o, m, m, o, m);
        shapedRecipe3x3(armorMaterialName + "_boots", bootsItem, 1, o, o, o, m, o, m, m, o, m);
    }

    private static void alias(String newName, String original) {
        if (!nameToResourceTask.containsKey(original) || !nameToItemMatches.containsKey(original)) {
            Debug.logWarning("Invalid resource: " + original + ". Will not create alias.");
        } else {
            nameToResourceTask.put(newName, nameToResourceTask.get(original));
            nameToItemMatches.put(newName, nameToItemMatches.get(original));
        }
    }

    private static ItemTarget t(String cataloguedName) {
        return new ItemTarget(cataloguedName);
    }

    private static class CataloguedResource {
        private final Item[] _targets;
        private final Function<Integer, ResourceTask> _getResource;

        private boolean _mineIfPresent;
        private boolean _forceDimension = false;
        private Dimension _targetDimension;

        public CataloguedResource(Item[] targets, Function<Integer, ResourceTask> getResource) {
            _targets = targets;
            _getResource = getResource;
        }

        public CataloguedResource mineIfPresent() {
            _mineIfPresent = true;
            return this;
        }

        public CataloguedResource dontMineIfPresent() {
            _mineIfPresent = false;
            return this;
        }

        public CataloguedResource forceDimension(Dimension dimension) {
            _forceDimension = true;
            _targetDimension = dimension;
            return this;
        }

        public CataloguedResource anyDimension() {
            _forceDimension = false;
            return this;
        }

        public ResourceTask getResource(int count) {
            ResourceTask result = _getResource.apply(count);
            if (_mineIfPresent) {
                result = result.mineIfPresent(ItemHelper.itemsToBlocks(_targets));
            }
            if (_forceDimension) {
                result = result.forceDimension(_targetDimension);
            }
            return result;
        }
    }
}
