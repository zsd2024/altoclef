package adris.altoclef.tasks.movement;

import adris.altoclef.AltoClef;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.helpers.LookHelper;
import adris.altoclef.util.helpers.StorageHelper;
import adris.altoclef.util.time.TimerGame;
import baritone.api.pathing.goals.Goal;
import baritone.api.utils.input.Input;
import baritone.pathing.movement.MovementHelper;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;

/**
 * 从水中出来任务 - 让玩家从水中出来
 */
public class GetOutOfWaterTask extends CustomBaritoneGoalTask{

    private boolean startedShimmying = false; // 是否开始摆动
    private final TimerGame shimmyTaskTimer = new TimerGame(5); // 摆动任务计时器

    @Override
    protected void onStart() {

    }

    @Override
    protected Task onTick() {
        AltoClef mod = AltoClef.getInstance();

        // 首先到达水面
        if (mod.getPlayer().getAir() < mod.getPlayer().getMaxAir() || mod.getPlayer().isSubmergedInWater()) {
            return super.onTick();
        }

        // 检查下方是否有方块
        boolean hasBlockBelow = false;
        for (int i = 0; i < 3; i++) {
            if (mod.getWorld().getBlockState(mod.getPlayer().getSteppingPos().down(i)).getBlock() != Blocks.WATER) {
                hasBlockBelow = true;
            }
        }
        // 检查上方是否有空气
        boolean hasAirAbove = mod.getWorld().getBlockState(mod.getPlayer().getBlockPos().up(2)).getBlock().equals(Blocks.AIR);

        // 如果上方有空气，下方有方块，且有可丢弃方块
        if (hasAirAbove && hasBlockBelow && StorageHelper.getNumberOfThrowawayBlocks(mod) > 0) {
            mod.getInputControls().tryPress(Input.JUMP);
            if (mod.getPlayer().isOnGround()) {

                if (!startedShimmying) {
                    startedShimmying = true;
                    shimmyTaskTimer.reset();
                }
                return new SafeRandomShimmyTask(); // 开始安全随机摆动
            }

            // 装备可丢弃方块
            mod.getSlotHandler().forceEquipItem(mod.getClientBaritoneSettings().acceptableThrowawayItems.value.toArray(new Item[0]));
            LookHelper.lookAt(mod, mod.getPlayer().getSteppingPos().down());
            mod.getInputControls().tryPress(Input.CLICK_RIGHT);
        }

        return super.onTick();
    }

    @Override
    protected void onStop(Task interruptTask) {

    }

    @Override
    protected Goal newGoal(AltoClef mod) {
        return new EscapeFromWaterGoal();
    }

    @Override
    protected boolean isEqual(Task other) {
        return false;
    }

    @Override
    protected String toDebugString() {
        return "";
    }

    @Override
    public boolean isFinished() {
        // 当玩家不再接触水并且在地面上时完成
        return !AltoClef.getInstance().getPlayer().isTouchingWater() && AltoClef.getInstance().getPlayer().isOnGround();
    }

    private static class EscapeFromWaterGoal implements Goal {

        private static boolean isWater(int x, int y, int z) {
            if (MinecraftClient.getInstance().world == null) return false;
            return MovementHelper.isWater(MinecraftClient.getInstance().world.getBlockState(new BlockPos(x, y, z)));
        }

        private static boolean isWaterAdjacent(int x, int y, int z) {
            // 检查相邻方块是否为水
            return isWater(x + 1, y, z) || isWater(x - 1, y, z) || isWater(x, y, z + 1) || isWater(x, y, z - 1)
                    || isWater(x + 1, y, z - 1) || isWater(x + 1, y, z + 1) || isWater(x - 1, y, z - 1)
                    || isWater(x - 1, y, z + 1);
        }

        @Override
        public boolean isInGoal(int x, int y, int z) {
            // 如果当前位置不是水且周围的方块也不是水，则达到目标
            return !isWater(x, y, z) && !isWaterAdjacent(x, y, z);
        }

        @Override
        public double heuristic(int x, int y, int z) {
            if (isWater(x, y, z)) {
                return 1;
            } else if (isWaterAdjacent(x, y, z)) {
                return 0.5f;
            }

            return 0;
        }
    }
}
