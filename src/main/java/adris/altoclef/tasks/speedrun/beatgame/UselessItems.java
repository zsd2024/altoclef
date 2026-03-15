package adris.altoclef.tasks.speedrun.beatgame;

import adris.altoclef.multiversion.versionedfields.Items;
import adris.altoclef.tasks.speedrun.BeatMinecraftConfig;
import net.minecraft.item.Item;

import java.util.ArrayList;
import java.util.List;

/**
 * 无用物品类 - 定义在速通模式中被认为无用的物品列表
 * 这些物品不会被自动拾取
 */
public class UselessItems {

    // 甚至不要拾取这些物品
    public final Item[] uselessItems;

    public UselessItems(BeatMinecraftConfig config)  {
        List<Item> uselessItemList = new ArrayList<>(List.of(
                // 树苗
                Items.OAK_SAPLING,
                Items.SPRUCE_SAPLING,
                Items.BIRCH_SAPLING,
                Items.JUNGLE_SAPLING,
                Items.ACACIA_SAPLING,
                Items.DARK_OAK_SAPLING,
                Items.MANGROVE_PROPAGULE,
                Items.CHERRY_SAPLING,

                // 种子
                Items.BEETROOT_SEEDS,
                Items.MELON_SEEDS,
                Items.PUMPKIN_SEEDS,
                Items.WHEAT_SEEDS,
                Items.TORCHFLOWER_SEEDS,

                // 随机杂物，未来可能添加更多东西
                Items.FEATHER, // 羽毛
                Items.EGG, // 鸡蛋
                Items.PINK_PETALS, // 粉色花瓣
                Items.BONE, // 骨头
                Items.LEATHER, // 皮革
                Items.RAW_COPPER, // 粗铜
                Items.WARPED_ROOTS, // 诡异菌索
                Items.GUNPOWDER, // 火药
                Items.MOSSY_COBBLESTONE, // 苔石
                Items.SPRUCE_TRAPDOOR, // 云杉木活板门
                Items.SANDSTONE_STAIRS, // 砂岩楼梯
                Items.STONE_BRICKS, // 石砖
                Items.COARSE_DIRT, // 砂土
                Items.SMOOTH_STONE, // 平滑石头
                Items.FLOWER_POT, // 花盆
                Items.MANGROVE_ROOTS, // 红树根
                Items.POPPY, // 虞美人
                Items.MUDDY_MANGROVE_ROOTS, // 泥泞的红树根
                Items.SPIDER_EYE, // 蜘蛛眼
                Items.PINK_TULIP, // 粉色郁金香

                Items.SPRUCE_STAIRS, // 云杉木楼梯
                Items.OAK_STAIRS, // 橡木楼梯

                Items.LAPIS_LAZULI, // 青金石
                Items.SUNFLOWER, // 向日葵
                Items.REDSTONE, // 红石
                Items.CRIMSON_ROOTS, // 绯红菌索
                Items.OAK_DOOR, // 橡木门

                Items.STRING, // 线
                Items.WHITE_TERRACOTTA, // 白色陶瓦
                Items.RED_TERRACOTTA, // 红色陶瓦

                Items.MOSS_BLOCK, // 苔藓块
                Items.MOSS_CARPET, // 苔藓地毯
                Items.BOW, // 弓

                Items.EMERALD, // 绿宝石
                Items.IRON_NUGGET, // 铁粒
                Items.SHORT_GRASS, // 矮草
                Items.COBBLESTONE_WALL, // 圆石墙
                Items.COBBLESTONE_STAIRS, // 圆石楼梯
                Items.COBBLESTONE_SLAB, // 圆石台阶
                Items.CLAY_BALL, // 粘土球
                Items.DANDELION, // 蒲公英
                Items.SUGAR_CANE, // 甘蔗
                Items.CHEST, // 箱子
                Items.RAIL, // 铁轨
                Items.CALCITE, // 方解石
                Items.AMETHYST_BLOCK, // 紫水晶块
                Items.AMETHYST_CLUSTER, // 紫水晶簇
                Items.AMETHYST_SHARD, // 紫水晶碎片
                Items.BUDDING_AMETHYST, // 紫水晶母岩
                Items.SMOOTH_BASALT, // 平滑玄武岩
                Items.AZURE_BLUET, // 洋甘菊

                Items.ACACIA_DOOR, // 金合欢木门
                Items.OAK_FENCE, // 橡木栅栏
                Items.COMPOSTER, // 堆肥桶
                Items.OAK_PRESSURE_PLATE, // 橡木压力板
                Items.JUNGLE_DOOR, // 丛林木门
                Items.CHISELED_SANDSTONE, // 雕纹砂岩
                Items.CACTUS, // 仙人掌
                Items.MUD, // 泥巴
                Items.MANGROVE_LEAVES, // 红树叶
                Items.SMOOTH_SANDSTONE_SLAB, // 平滑砂岩台阶
                Items.SANDSTONE_WALL, // 砂岩墙
                Items.TNT, // TNT
                Items.PRISMARINE_CRYSTALS, // 海晶石晶体
                Items.SNOWBALL, // 雪球
                Items.DRIPSTONE_BLOCK, // 滴水石块
                Items.POINTED_DRIPSTONE, // 指向滴水石
                Items.ARROW, // 箭
                Items.YELLOW_TERRACOTTA, // 黄色陶瓦
                Items.TUFF, // 凝灰岩
                Items.SPRUCE_STAIRS, // 云杉木楼梯
                Items.SPRUCE_DOOR, // 云杉木门
                Items.SPRUCE_FENCE, // 云杉木栅栏
                Items.SPRUCE_FENCE_GATE, // 云杉木栅栏门
                Items.ORANGE_TERRACOTTA, // 橙色陶瓦
                Items.HEART_OF_THE_SEA, // 海洋之心
                Items.POTION, // 药水
                Items.FLOWERING_AZALEA, // 开花的杜鹃花丛
                Items.COPPER_INGOT, // 铜锭
                Items.ACACIA_SLAB, // 金合欢木台阶
                Items.RABBIT_HIDE, // 兔子皮
                Items.RABBIT_FOOT, // 兔子脚

                // 下界物品
                Items.SOUL_SAND, // 灵魂沙
                Items.SOUL_SOIL, // 灵魂土
                Items.NETHER_BRICK, // 下界砖
                Items.NETHER_BRICK_FENCE // 下界砖栅栏
        ));


        if (!config.barterPearlsInsteadOfEndermanHunt) {
            uselessItemList.add(Items.GOLD_NUGGET); // 金粒
        }


        uselessItems = uselessItemList.toArray(new Item[0]);
    }

}
