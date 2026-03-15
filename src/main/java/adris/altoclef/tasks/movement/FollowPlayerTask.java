package adris.altoclef.tasks.movement;

import adris.altoclef.AltoClef;
import adris.altoclef.tasksystem.Task;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.Optional;

/**
 * 跟随玩家任务 - 跟随指定名称的玩家
 */
public class FollowPlayerTask extends Task {

    private final String _playerName; // 要跟随的玩家名称

    public FollowPlayerTask(String playerName) {
        _playerName = playerName;
    }

    @Override
    protected void onStart() {

    }

    @Override
    protected Task onTick() {
        AltoClef mod = AltoClef.getInstance();

        // 获取玩家最近的位置
        Optional<Vec3d> lastPos = mod.getEntityTracker().getPlayerMostRecentPosition(_playerName);

        if (lastPos.isEmpty()) {
            setDebugState("未找到/检测到玩家。在玩家加载到渲染距离之前什么都不做。");
            return null;
        }
        Vec3d target = lastPos.get();

        // 如果距离目标很近但玩家未加载，则停止任务
        if (target.isInRange(mod.getPlayer().getPos(), 1) && !mod.getEntityTracker().isPlayerLoaded(_playerName)) {
            mod.logWarning("未能到达玩家 \"" + _playerName + "\"。我们移动到上次看到他们的位置，但现在不知道他们在哪里。");
            stop();
            return null;
        }

        // 获取玩家实体
        Optional<PlayerEntity> player = mod.getEntityTracker().getPlayerEntity(_playerName);
        if (player.isEmpty()) {
            // 前往最后的位置
            return new GetToBlockTask(new BlockPos((int) target.x, (int) target.y, (int) target.z), false);
        }
        // 前往玩家实体
        return new GetToEntityTask(player.get(), 2);
    }

    @Override
    protected void onStop(Task interruptTask) {

    }

    @Override
    protected boolean isEqual(Task other) {
        if (other instanceof FollowPlayerTask task) {
            return task._playerName.equals(_playerName);
        }
        return false;
    }

    @Override
    protected String toDebugString() {
        return "前往玩家 " + _playerName;
    }
}
