package adris.altoclef.trackers.blacklisting;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.util.MiningRequirement;
import adris.altoclef.util.helpers.StorageHelper;
import net.minecraft.util.math.Vec3d;

import java.util.HashMap;

/**
 * 有时我们会尝试访问某物并多次失败。
 * <p>
 * 这让我们知道一个方块是无法到达的，并且会在搜索时智能地忽略它。
 */
public abstract class AbstractObjectBlacklist<T> {

    private final HashMap<T, BlacklistEntry> entries = new HashMap<>();

    /**
     * 将项目加入黑名单
     * @param mod AltoClef实例
     * @param item 要加入黑名单的项目
     * @param numberOfFailuresAllowed 允许的失败次数
     */
    public void blackListItem(AltoClef mod, T item, int numberOfFailuresAllowed) {
        if (!entries.containsKey(item)) {
            BlacklistEntry entry = new BlacklistEntry();
            entry.numberOfFailuresAllowed = numberOfFailuresAllowed;
            entry.numberOfFailures = 0;
            entry.bestDistanceSq = Double.POSITIVE_INFINITY;
            entry.bestTool = MiningRequirement.HAND;
            entries.put(item, entry);
        }
        BlacklistEntry entry = entries.get(item);
        double newDistance = getPos(item).squaredDistanceTo(mod.getPlayer().getPos());
        MiningRequirement newTool = StorageHelper.getCurrentMiningRequirement();
        // 对于距离，添加一个小的阈值，这样就不会每次我们稍微靠近一点就重置。
        if (newTool.ordinal() > entry.bestTool.ordinal() || (newDistance < entry.bestDistanceSq - 1)) {
            if (newTool.ordinal() > entry.bestTool.ordinal()) entry.bestTool = newTool;
            if (newDistance < entry.bestDistanceSq) entry.bestDistanceSq = newDistance;
            entry.numberOfFailures = 0;
            Debug.logMessage("黑名单重置: " + item.toString());
        }
        entry.numberOfFailures++;
        entry.numberOfFailuresAllowed = numberOfFailuresAllowed;
        Debug.logMessage("黑名单: " + item.toString() + ": 尝试 " + entry.numberOfFailures + " / " + entry.numberOfFailuresAllowed);
    }

    /**
     * 获取项目位置的抽象方法
     * @param item 项目
     * @return 项目的位置
     */
    protected abstract Vec3d getPos(T item);

    /**
     * 检查项目是否不可到达
     * @param item 项目
     * @return 是否不可到达
     */
    public boolean unreachable(T item) {
        if (entries.containsKey(item)) {
            BlacklistEntry entry = entries.get(item);
            return entry.numberOfFailures > entry.numberOfFailuresAllowed;
        }
        return false;
    }

    /**
     * 清空黑名单
     */
    public void clear() {
        entries.clear();
    }

    // 键: BlockPos
    private static class BlacklistEntry {
        public int numberOfFailuresAllowed; // 允许的失败次数
        public int numberOfFailures; // 当前失败次数
        public double bestDistanceSq; // 最佳距离平方
        public MiningRequirement bestTool; // 最佳工具
    }
}
