package adris.altoclef.trackers.blacklisting;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;

/**
 * 实体定位黑名单类
 * 用于跟踪和管理无法到达的实体位置，避免重复搜索已知无效的实体位置
 */
public class EntityLocateBlacklist extends AbstractObjectBlacklist<Entity> {
    
    /**
     * 获取实体的位置
     * @param item 实体对象
     * @return 实体的位置坐标
     */
    @Override
    protected Vec3d getPos(Entity item) {
        return item.getPos();
    }
}
