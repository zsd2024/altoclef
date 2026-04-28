package adris.altoclef.util;

import adris.altoclef.Debug;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.Items;

/**
 * 挖掘需求工具类
 * 定义了挖掘不同方块所需的最低工具等级
 */
public enum MiningRequirement implements Comparable<MiningRequirement> {
    /** 徒手挖掘 */
    HAND(Items.AIR), 
    /** 木制镐子 */
    WOOD(Items.WOODEN_PICKAXE), 
    /** 石制镐子 */
    STONE(Items.STONE_PICKAXE), 
    /** 铁制镐子 */
    IRON(Items.IRON_PICKAXE), 
    /** 钻石镐子 */
    DIAMOND(Items.DIAMOND_PICKAXE);

    /** 最低要求的镐子物品 */
    private final Item _minPickaxe;

    /**
     * 构造函数
     * @param minPickaxe 最低要求的镐子物品
     */
    MiningRequirement(Item minPickaxe) {
        _minPickaxe = minPickaxe;
    }

    // FIXME 这对蜘蛛网不起作用，因为蜘蛛网需要用剪刀破坏...
    /**
     * 获取挖掘指定方块的最低工具需求
     * @param block 要挖掘的方块
     * @return 挖掘该方块所需的最低工具等级
     */
    public static MiningRequirement getMinimumRequirementForBlock(Block block) {
        if (block.getDefaultState().isToolRequired()) {
            for (MiningRequirement req : MiningRequirement.values()) {
                if (req == MiningRequirement.HAND) continue;
                Item pick = req.getMinimumPickaxe();
                if (pick.getDefaultStack().isSuitableFor(block.getDefaultState())) {
                    return req;
                }
            }
            Debug.logWarning("未能找到任何有效的工具来挖掘: " + block + "。我假设任何地方都不需要下界合金，所以可能是其他地方出了问题。");
            return MiningRequirement.DIAMOND;
        }
        return MiningRequirement.HAND;
    }

    /**
     * 获取最低要求的镐子物品
     * @return 最低要求的镐子物品
     */
    public Item getMinimumPickaxe() {
        return _minPickaxe;
    }

}

    // FIXME this doesnt work for cobwebs because they are broken with shears...
    public static MiningRequirement getMinimumRequirementForBlock(Block block) {
        if (block.getDefaultState().isToolRequired()) {
            for (MiningRequirement req : MiningRequirement.values()) {
                if (req == MiningRequirement.HAND) continue;
                Item pick = req.getMinimumPickaxe();
                if (pick.getDefaultStack().isSuitableFor(block.getDefaultState())) {
                    return req;
                }
            }
            Debug.logWarning("Failed to find ANY effective tool against: " + block + ". I assume netherite is not required anywhere, so something else probably went wrong.");
            return MiningRequirement.DIAMOND;
        }
        return MiningRequirement.HAND;
    }

    public Item getMinimumPickaxe() {
        return _minPickaxe;
    }

}
