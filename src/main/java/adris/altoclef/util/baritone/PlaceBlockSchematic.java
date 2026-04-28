package adris.altoclef.util.baritone;

import baritone.api.schematic.AbstractSchematic;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;

import java.util.List;

/**
 * 放置方块示意图工具类
 * 用于在指定位置放置特定类型的方块
 */
//@Deprecated
public class PlaceBlockSchematic extends AbstractSchematic {

    /**
     * 示意图范围（1x1x1）
     */
    private static final int RANGE = 1;
    
    /**
     * 需要放置的方块类型数组
     */
    private final Block[] blockToPlace;
    
    /**
     * 如果目标位置已有目标方块，是否跳过放置
     */
    private final boolean skipIfAlreadyThere;
    
    /**
     * 是否已完成放置（始终为false，因为这个字段未被使用）
     */
    private final boolean done;
    
    /**
     * 目标放置的方块状态
     */
    private BlockState targetPlace;

    /**
     * 构造函数
     * 
     * @param blocksToPlace 需要放置的方块类型数组
     * @param skipIfAlreadyThere 如果目标位置已有目标方块，是否跳过放置
     */
    public PlaceBlockSchematic(Block[] blocksToPlace, boolean skipIfAlreadyThere) {
        super(RANGE, RANGE, RANGE);
        blockToPlace = blocksToPlace;
        done = false;
        targetPlace = null;
        this.skipIfAlreadyThere = skipIfAlreadyThere;
    }

    /**
     * 构造函数（默认跳过已存在的方块）
     * 
     * @param blocksToPlace 需要放置的方块类型数组
     */
    public PlaceBlockSchematic(Block[] blocksToPlace) {
        this(blocksToPlace, true);
    }

    /**
     * 构造函数（单个方块）
     * 
     * @param blockToPlace 需要放置的方块类型
     */
    public PlaceBlockSchematic(Block blockToPlace) {
        this(new Block[]{blockToPlace});
    }


    /**
     * 检查是否已找到合适的放置位置
     * 
     * @return 如果已找到放置位置返回true，否则返回false
     */
    public boolean foundSpot() {
        return targetPlace != null;
    }

    // No restrictions.
    //@Override
    //public boolean inSchematic(int x, int y, int z, BlockState currentState) {
    //    return true;
    //}

    /**
     * 获取指定位置期望的方块状态
     * 
     * @param x X坐标
     * @param y Y坐标
     * @param z Z坐标
     * @param blockState 当前方块状态
     * @param list 可能的方块状态列表
     * @return 期望的方块状态
     */
    @Override
    public BlockState desiredState(int x, int y, int z, BlockState blockState, List<BlockState> list) {
        // 只在原点(0,0,0)放置方块
        if (x != 0 || y != 0 || z != 0) {
            return blockState;
        }
        // 如果目标位置已有目标方块，则记录它
        if (skipIfAlreadyThere && blockIsTarget(blockState.getBlock())) {
            //System.out.println("PlaceBlockNearbySchematic (already exists)");
            targetPlace = blockState;
        }
        boolean isDone = (targetPlace != null);
        if (isDone) {
            return targetPlace;
        }
        //System.out.print("oof: [");
        if (!list.isEmpty()) {
            for (BlockState possible : list) {
                if (possible == null) {
                /*
                if (ToolSet.areShearsEffective(blockState.getBlock()) || BlockTags.FLOWERS.contains(blockState.getBlock())) {
                    // 剪刀剪物品/花朵会导致这个问题，但它工作正常！
                } else {
                    Debug.logWarning("奇怪的问题，给定的可能状态为null。将忽略。");
                }
                 */
                    continue;
                }
                //System.out.print(possible.getBlock().getTranslationKey() + " ");
                if (blockIsTarget(possible.getBlock())) {
                    //System.out.print("PlaceBlockNearbySchematic  ( FOUND! )");
                    targetPlace = possible;
                    return possible;
                }
            }
        }
        //System.out.println("] ( :(((((( )");
        return blockState;
    }


    /**
     * 检查指定方块是否为目标方块之一
     * 
     * @param block 要检查的方块
     * @return 如果是目标方块返回true，否则返回false
     */
    private boolean blockIsTarget(Block block) {
        if (blockToPlace != null) {
            for (Block check : blockToPlace) {
                if (check == block) return true;
            }
        }
        return false;
    }
}
