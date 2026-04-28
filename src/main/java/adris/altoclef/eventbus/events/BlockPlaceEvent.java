package adris.altoclef.eventbus.events;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

/**
 * 方块放置事件
 * 当玩家在世界中放置方块时触发此事件
 */
public class BlockPlaceEvent {
    /** 放置方块的位置 */
    public BlockPos blockPos;
    /** 放置方块的状态 */
    public BlockState blockState;

    /**
     * 构造函数
     * @param blockPos 放置方块的位置
     * @param blockState 放置方块的状态
     */
    public BlockPlaceEvent(BlockPos blockPos, BlockState blockState) {
        this.blockPos = blockPos;
        this.blockState = blockState;
    }
}
