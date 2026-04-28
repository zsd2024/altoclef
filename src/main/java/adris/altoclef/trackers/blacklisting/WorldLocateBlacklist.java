package adris.altoclef.trackers.blacklisting;

import adris.altoclef.util.helpers.WorldHelper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

/**
 * 世界定位黑名单类
 * 用于跟踪和管理无法到达的世界坐标位置，避免重复搜索已知无效的方块位置
 */
public class WorldLocateBlacklist extends AbstractObjectBlacklist<BlockPos> {
    
    /**
     * 获取方块位置的向量表示
     * @param item 方块位置对象
     * @return 方块位置的向量坐标
     */
    @Override
    protected Vec3d getPos(BlockPos item) {
        return WorldHelper.toVec3d(item);
    }
}
