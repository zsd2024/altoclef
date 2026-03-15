package adris.altoclef.tasks.construction;

import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;

/**
 * 放置结构方块任务 - 在指定位置放置一个可丢弃的方块
 */
public class PlaceStructureBlockTask extends PlaceBlockTask {
    /**
     * 构造函数
     * @param target 目标放置位置
     */
    public PlaceStructureBlockTask(BlockPos target) {
        // 使用空的方块数组，允许使用可丢弃方块，且强制放置
        super(target, new Block[]{}, true, true);
    }
}