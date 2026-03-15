package adris.altoclef.tasks;

import adris.altoclef.AltoClef;
import adris.altoclef.control.InputControls;
import adris.altoclef.tasks.construction.PlaceStructureBlockTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.helpers.LookHelper;
import adris.altoclef.util.time.TimerGame;
import baritone.api.utils.input.Input;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.state.property.Properties;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
//#if MC <= 12006
//$$ import adris.altoclef.mixins.EntityAccessor;
//#endif

import java.util.ArrayList;
import java.util.List;

/**
 * 确保下界传送门安全的任务
 * 该任务会检查传送门周围是否有不安全的方块，如有则替换为安全方块
 */
public class SafeNetherPortalTask extends Task {
    // 等待计时器
    private final TimerGame wait = new TimerGame(1);
    // 按键是否已重置
    private boolean keyReset = false;
    // 任务是否已完成
    private boolean finished = false;
    // 传送门方块位置列表
    private List<BlockPos> positions = null;
    // 检查方向列表
    private List<Direction> directions = null;
    // 传送门轴向
    private Direction.Axis axis = null;

    @Override
    protected void onStart() {
        // 清除所有按键状态并重置等待计时器
        AltoClef.getInstance().getClientBaritone().getInputOverrideHandler().clearAllKeys();
        wait.reset();
    }

    @Override
    protected Task onTick() {
        if (!wait.elapsed()) {
            return null;
        }
        AltoClef mod = AltoClef.getInstance();

        if (!keyReset) {
            keyReset = true;
            // 确保按键状态被清除
            mod.getClientBaritone().getInputOverrideHandler().clearAllKeys();
        }

        // 检查传送门冷却时间，如果小于10则表示不在传送门中
        if (mod.getPlayer().getPortalCooldown() < 10) {
            if (positions != null && directions != null) {
                // 检查传送门两侧的方块
                BlockPos pos1 = mod.getPlayer().getSteppingPos().offset(axis, 1);
                BlockPos pos2 = mod.getPlayer().getSteppingPos().offset(axis, -1);

                // 检查是否需要替换pos1位置的方块
                if (mod.getWorld().getBlockState(pos1).isAir() || mod.getWorld().getBlockState(pos1).getBlock().equals(Blocks.SOUL_SAND)) {
                    boolean passed = false;
                    for (Direction dir : Direction.values()) {
                        if (mod.getWorld().getBlockState(pos1.up().offset(dir)).getBlock().equals(Blocks.NETHER_PORTAL)) {
                            passed = true;
                            break;
                        }
                    }
                    if (passed) {
                        return new ReplaceSafeBlock(pos1);
                    }
                }

                // 检查是否需要替换pos2位置的方块
                if (mod.getWorld().getBlockState(pos2).isAir() || mod.getWorld().getBlockState(pos2).getBlock().equals(Blocks.SOUL_SAND)) {
                    boolean passed = false;
                    for (Direction dir : Direction.values()) {
                        if (mod.getWorld().getBlockState(pos2.up().offset(dir)).getBlock().equals(Blocks.NETHER_PORTAL)) {
                            passed = true;
                            break;
                        }
                    }
                    if (passed) {
                        return new ReplaceSafeBlock(pos2);
                    }
                }
            }
            finished = true;
            setDebugState("我们不在传送门中");
            return null;
        }

        // 获取玩家位置的方块状态
        BlockState state = mod.getWorld().getBlockState(mod.getPlayer().getBlockPos());
        if (positions == null || directions == null) {
            // 如果传送门位置和方向未初始化
            if (state.getBlock().equals(Blocks.NETHER_PORTAL)) {
                // 获取传送门的水平轴向
                axis = state.get(Properties.HORIZONTAL_AXIS);

                positions = new ArrayList<>();
                positions.add(mod.getPlayer().getBlockPos());
                // 查找相连的传送门方块
                for (Direction dir : Direction.values()) {
                    if (dir.getAxis().isVertical()) continue;

                    BlockPos pos = mod.getPlayer().getBlockPos().offset(dir);
                    if (mod.getWorld().getBlockState(pos).getBlock().equals(Blocks.NETHER_PORTAL)) {
                        positions.add(pos);
                    }
                }

                // 设置默认检查方向（东西方向）
                directions = List.of(Direction.WEST, Direction.EAST);

                if (axis == Direction.Axis.X) {
                    // 如果传送门是X轴方向，则检查南北方向
                    directions = List.of(Direction.NORTH, Direction.SOUTH);
                }
            } else {
                finished = true;
                setDebugState("我们没有站在下界传送门方块内");
            }
        } else {
            // 检查传送门底部周围是否有不安全的方块
            for (BlockPos pos : positions) {
                for (Direction dir : directions) {
                    BlockPos newPos = pos.down().offset(dir);
                    if (mod.getWorld().getBlockState(newPos).isAir() || mod.getWorld().getBlockState(newPos).getBlock().equals(Blocks.SOUL_SAND)) {
                        setDebugState("更改方块...");
                        return new ReplaceSafeBlock(newPos);
                    }
                }
            }

            // 没有找到不安全的方块
            finished = true;
            setDebugState("传送门是安全的");
            return null;
        }


        return null;
    }

    @Override
    protected void onStop(Task interruptTask) {
        // 释放所有控制按键并清除Baritone的按键状态
        InputControls controls = AltoClef.getInstance().getInputControls();

        controls.release(Input.MOVE_FORWARD);
        controls.release(Input.SNEAK);
        controls.release(Input.CLICK_LEFT);
        AltoClef.getInstance().getClientBaritone().getInputOverrideHandler().clearAllKeys();
    }

    @Override
    protected boolean isEqual(Task other) {
        return other instanceof SafeNetherPortalTask;
    }

    @Override
    protected String toDebugString() {
        return "确保下界传送门安全";
    }

    @Override
    public boolean isFinished() {
        return finished;
    }

    /**
     * 替换安全方块的内部任务类
     * 用于在指定位置放置或移除方块以确保传送门安全
     */
    private static class ReplaceSafeBlock extends Task {

        // 需要处理的方块位置
        private final BlockPos pos;
        // 任务是否已完成
        private boolean finished = false;

        /**
         * 构造函数
         * @param pos 需要处理的方块位置
         */
        public ReplaceSafeBlock(BlockPos pos) {
            this.pos = pos;
        }


        @Override
        protected void onStart() {
            // 清除所有按键状态
            AltoClef.getInstance().getClientBaritone().getInputOverrideHandler().clearAllKeys();
        }

        @Override
        protected Task onTick() {
            AltoClef mod = AltoClef.getInstance();

            // 如果目标位置是空气，则放置方块
            if (mod.getWorld().getBlockState(pos).isAir()) {
                setDebugState("放置方块...");
                return new PlaceStructureBlockTask(pos);
            }

            // 如果目标位置是灵魂沙，则根据情况挖掉或靠近
            if (mod.getWorld().getBlockState(pos).getBlock().equals(Blocks.SOUL_SAND)) {
                LookHelper.lookAt(mod, pos);

                // 检查视线中是否有传送门方块
                HitResult result = mod.getPlayer().raycast(3, MinecraftClient.getInstance().getRenderTickCounter().getTickDelta(true), true);
                if (result instanceof BlockHitResult blockHitResult && mod.getWorld().getBlockState(blockHitResult.getBlockPos()).getBlock().equals(Blocks.NETHER_PORTAL)) {
                    // 视线内有传送门，靠近目标
                    setDebugState("靠近目标...");
                    mod.getInputControls().hold(Input.MOVE_FORWARD);
                    mod.getInputControls().hold(Input.SNEAK);
                } else {
                    // 视线内没有传送门，挖掉灵魂沙
                    setDebugState("破坏方块");
                    mod.getInputControls().release(Input.MOVE_FORWARD);
                    mod.getInputControls().release(Input.SNEAK);
                    mod.getInputControls().hold(Input.CLICK_LEFT);
                }
                return null;
            }

            this.finished = true;
            return null;
        }

        @Override
        protected void onStop(Task interruptTask) {
            // 释放所有控制按键并清除Baritone的按键状态
            InputControls controls = AltoClef.getInstance().getInputControls();

            controls.release(Input.MOVE_FORWARD);
            controls.release(Input.SNEAK);
            controls.release(Input.CLICK_LEFT);
            AltoClef.getInstance().getClientBaritone().getInputOverrideHandler().clearAllKeys();
        }

        @Override
        public boolean isFinished() {
            return finished;
        }

        @Override
        protected boolean isEqual(Task other) {
            return other instanceof ReplaceSafeBlock same && same.pos.equals(this.pos);
        }

        @Override
        protected String toDebugString() {
            return "确保 " + pos + " 是安全的";
        }
    }

}
