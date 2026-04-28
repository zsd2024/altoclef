package adris.altoclef.eventbus.events;

import net.minecraft.util.math.BlockPos;

/**
 * 方块破坏事件
 * 当玩家开始破坏方块时触发此事件
 */
public class BlockBreakingEvent {
    // 正在被破坏的方块位置
    public BlockPos blockPos;
    // 破坏进度（0.0 到 1.0）
    public double progress;

    /**
     * 构造函数
     * 
     * @param blockPos 方块位置
     * @param progress 破坏进度
     */
    public BlockBreakingEvent(BlockPos blockPos, double progress) {
        this.blockPos = blockPos;
        this.progress = progress;
    }
}
