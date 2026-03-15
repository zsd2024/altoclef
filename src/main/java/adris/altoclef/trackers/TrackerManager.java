package adris.altoclef.trackers;

import adris.altoclef.AltoClef;

import java.util.ArrayList;

/**
 * 跟踪器管理器 - 管理所有跟踪器的更新和重置
 */
public class TrackerManager {

    private final ArrayList<Tracker> _trackers = new ArrayList<>();

    private final AltoClef _mod;

    private boolean _wasInGame = false;

    public TrackerManager(AltoClef mod) {
        _mod = mod;
    }

    /**
     * 每个游戏刻度执行一次，更新所有跟踪器
     */
    public void tick() {
        boolean inGame = AltoClef.inGame();
        if (!inGame && _wasInGame) {
            // 当我们离开世界时重置
            for (Tracker tracker : _trackers) {
                tracker.reset();
            }
            // 这里代码比较混乱，以后需要修复
            _mod.getChunkTracker().reset(_mod);
            _mod.getMiscBlockTracker().reset();
        }
        _wasInGame = inGame;

        for (Tracker tracker : _trackers) {
            tracker.setDirty();
        }
    }

    /**
     * 添加跟踪器到管理器中
     * @param tracker 要添加的跟踪器
     */
    public void addTracker(Tracker tracker) {
        tracker.mod = _mod;
        _trackers.add(tracker);
    }
}
