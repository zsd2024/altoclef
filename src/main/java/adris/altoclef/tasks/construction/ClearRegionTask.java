package adris.altoclef.tasks.construction;

import adris.altoclef.AltoClef;
import adris.altoclef.tasksystem.ITaskRequiresGrounded;
import adris.altoclef.tasksystem.Task;
import baritone.Baritone;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;


/**
 * 清理指定区域的任务，将区域内的方块全部破坏
 */
public class ClearRegionTask extends Task implements ITaskRequiresGrounded {

    // 区域起始位置
    private final BlockPos _from;
    // 区域结束位置
    private final BlockPos _to;

    // TODO: 在失败时添加进度检查器
    // 进度检查器1用于移动
    // 进度检查器2用于检查方块破坏是否正在进行
    // 使用"与"逻辑，即两者都必须失败才算作失败

    public ClearRegionTask(BlockPos from, BlockPos to) {
        _from = from;
        _to = to;
    }

    @Override
    protected void onStart() {
        // 任务开始时不需要特别处理
    }

    @Override
    protected Task onTick() {
        Baritone baritone = AltoClef.getInstance().getClientBaritone();

        // 如果构建进程未激活，则启动清理区域任务
        if (!baritone.getBuilderProcess().isActive()) {
            baritone.getBuilderProcess().clearArea(_from, _to);
        }
        return null;
    }

    @Override
    protected void onStop(Task interruptTask) {
        // 任务停止时释放Baritone构建进程的控制
        AltoClef.getInstance().getClientBaritone().getBuilderProcess().onLostControl();
    }

    @Override
    public boolean isFinished() {
        // 计算区域的尺寸
        int x = _from.getX() - _to.getX();
        int y = _from.getY() - _to.getY();
        int z = _from.getZ() - _to.getZ();
        // 遍历整个区域，检查是否所有方块都已清理
        for (int xx = 0; xx < Math.abs(x); ++xx) {
            for (int yy = 0; yy < Math.abs(y); ++yy) {
                for (int zz = 0; zz < Math.abs(z); ++zz) {
                    BlockPos toCheck = new BlockPos(_from).add(xx * -Integer.signum(x),yy * -Integer.signum(y),zz * -Integer.signum(z));
                    assert MinecraftClient.getInstance().world != null;
                    // 如果发现任何不是空气的方块，则任务未完成
                    if (!MinecraftClient.getInstance().world.isAir(toCheck)) {
                        return false;
                    }
                }
            }
        }
        // 如果所有方块都已清理，则任务完成
        return true;
    }

    @Override
    protected boolean isEqual(Task other) {
        if (other instanceof ClearRegionTask) {
            ClearRegionTask task = (ClearRegionTask) other;
            // 比较区域的起始和结束位置是否相同
            return (task._from.equals(_from) && task._to.equals(_to));
        }
        return false;
    }

    @Override
    protected String toDebugString() {
        return "清理区域从 " + _from.toShortString() + " 到 " + _to.toShortString();
    }
}
