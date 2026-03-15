package adris.altoclef.tasks.examples;

import adris.altoclef.AltoClef;
import adris.altoclef.tasks.movement.GetToBlockTask;
import adris.altoclef.tasks.movement.TimeoutWanderTask;
import adris.altoclef.tasksystem.Task;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;

import java.util.Optional;

/**
 * 示例任务2 - 寻找橡树并站在树顶上，同时避免破坏树木
 */
public class ExampleTask2 extends Task {

    // 目标位置
    private BlockPos target = null;

    @Override
    protected void onStart() {
        // 额外功能：机器人不会破坏树木
        AltoClef mod = AltoClef.getInstance();

        mod.getBehaviour().push();
        // 避免破坏橡树叶和橡木原木
        mod.getBehaviour().avoidBlockBreaking(blockPos -> {
            BlockState s = mod.getWorld().getBlockState(blockPos);
            return s.getBlock() == Blocks.OAK_LEAVES || s.getBlock() == Blocks.OAK_LOG;
        });
    }

    @Override
    protected Task onTick() {
        AltoClef mod = AltoClef.getInstance();

        /*
         * 寻找一棵树
         * 前往树顶（在最后一片叶子方块上方）
         *
         * 定位最近的原木
         * 站在最后一片叶子的上方
         */

        // 如果目标位置已设定，则前往目标位置
        if (target != null) {
            return new GetToBlockTask(target);
        }

        // 如果找到了橡木原木，则定位最近的原木并找到其顶部
        if (mod.getBlockScanner().anyFound(Blocks.OAK_LOG)) {
            Optional<BlockPos> nearest = mod.getBlockScanner().getNearestBlock(Blocks.OAK_LOG);
            if (nearest.isPresent()) {
                // 找到叶子的顶部位置
                BlockPos check = new BlockPos(nearest.get());
                while (mod.getWorld().getBlockState(check).getBlock() == Blocks.OAK_LOG ||
                        mod.getWorld().getBlockState(check).getBlock() == Blocks.OAK_LEAVES) {
                    check = check.up();
                }
                target = check;
            }
            return null;
        }

        // 如果没找到目标，则随机徘徊
        return new TimeoutWanderTask();
    }

    @Override
    protected void onStop(Task interruptTask) {
        // 任务停止时弹出行为堆栈
        AltoClef.getInstance().getBehaviour().pop();
    }

    @Override
    protected boolean isEqual(Task other) {
        // 所有ExampleTask2实例都被认为是相等的
        return other instanceof ExampleTask2;
    }

    @Override
    public boolean isFinished() {
        if (target != null) {
            // 检查玩家是否已经到达目标位置
            return AltoClef.getInstance().getPlayer().getBlockPos().equals(target);
        }
        return super.isFinished();
    }

    @Override
    protected String toDebugString() {
        return "站在树上";
    }
}
