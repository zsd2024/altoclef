package adris.altoclef.trackers;

import adris.altoclef.AltoClef;

/**
 * 跟踪器基类 - 定义所有跟踪器的基本功能
 * 用于跟踪游戏中的各种状态，如实体、方块、物品等
 */
public abstract class Tracker {

    protected AltoClef mod;
    // 需要更新标识
    private boolean dirty = true;

    public Tracker(TrackerManager manager) {
        manager.addTracker(this);
    }

    /**
     * 设置跟踪器为脏状态，表示需要更新
     */
    public void setDirty() {
        dirty = true;
    }

    // 虚函数
    protected boolean isDirty() {
        return dirty;
    }

    /**
     * 确保跟踪器状态已更新
     */
    protected void ensureUpdated() {
        if (isDirty()) {
            updateState();
            dirty = false;
        }
    }

    /**
     * 更新跟踪器状态的抽象方法，由子类实现
     */
    protected abstract void updateState();

    /**
     * 重置跟踪器状态的抽象方法，由子类实现
     */
    protected abstract void reset();
}
