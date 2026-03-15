package adris.altoclef.tasks.speedrun;

import adris.altoclef.AltoClef;
import adris.altoclef.tasks.movement.GetToBlockTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.helpers.ItemHelper;
import adris.altoclef.util.helpers.LookHelper;
import adris.altoclef.util.helpers.WorldHelper;
import adris.altoclef.util.time.TimerGame;
import baritone.api.utils.input.Input;
import net.minecraft.block.Blocks;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.boss.dragon.EnderDragonPart;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.List;

/**
 * OneCycle任务类 - 用于执行末影龙速通
 * 此任务使用床来快速击败末影龙
 */
public class OneCycleTask extends Task {
    
    // 放置床的计时器
    TimerGame placeBedTimer = new TimerGame(0.6);
    // 等待计时器
    TimerGame waiTimer = new TimerGame(0.3);
    // 之前距离
    double prevDist = 100;



    @Override
    protected Task onTick() {
        AltoClef mod = AltoClef.getInstance();

        // 停止食物链，清空副手
        mod.getFoodChain().shouldStop(true);
        mod.getSlotHandler().forceEquipItemToOffhand(Items.AIR);

        // 如果蹲下则释放蹲下按键
        if (mod.getInputControls().isHeldDown(Input.SNEAK)) {
            mod.getInputControls().release(Input.SNEAK);
        }

        // 检查末影龙
        List<EnderDragonEntity> dragons = mod.getEntityTracker().getTrackedEntities(EnderDragonEntity.class);
        if (dragons.size() != 1) {
              mod.log("没有龙? :(");
        }

        for (EnderDragonEntity dragon : dragons) {

            // 获取末地传送门顶部
            BlockPos endPortalTop = KillEnderDragonWithBedsTask.locateExitPortalTop(mod).up();
            BlockPos obsidian = null;
            Direction dir = null;

            // 查找附近的黑曜石
            for (Direction direction : new Direction[]{Direction.EAST, Direction.WEST, Direction.NORTH, Direction.SOUTH}) {
                if (mod.getWorld().getBlockState(endPortalTop.offset(direction)).getBlock().equals(Blocks.OBSIDIAN)) {
                    obsidian = endPortalTop.offset(direction);
                    dir = direction.getOpposite();
                    break;
                }
            }

            if (dir == null) {
                mod.log("没有黑曜石? :(");
                return null;
            }

            // 计算目标位置
            Direction offsetDir = dir.getAxis() == Direction.Axis.X ? Direction.SOUTH : Direction.WEST;
            BlockPos targetBlock = endPortalTop.down(3).offset(offsetDir, 3).offset(dir);

            // 检查与目标位置的距离
            double d = distanceIgnoreY(WorldHelper.toVec3d(targetBlock), mod.getPlayer().getPos());
            if (d > 0.7) {
                mod.log(d + "");
                return new GetToBlockTask(targetBlock);
            }
            LookHelper.lookAt(mod,obsidian,dir);


            BlockPos bedHead = WorldHelper.getBedHead(endPortalTop);

            BlockPos bedTargetPosition = endPortalTop.up();
            mod.getSlotHandler().forceEquipItem(ItemHelper.BED);

            if (bedHead == null) {
                // 放置床
                if (placeBedTimer.elapsed() && Math.abs(dragon.getY() - endPortalTop.getY()) < 10) {
                    mod.getInputControls().tryPress(Input.CLICK_RIGHT);
                    waiTimer.reset();
                }
              /*  if (mod.getSlotHandler().forceEquipItem(ItemHelper.BED, true)) {
                    for (Direction dir : List.of(Direction.NORTH,Direction.SOUTH,Direction.EAST,Direction.WEST)) {
                        if (mod.getWorld().getBlockState(endPortalTop.add(dir.getVector())).getBlock().equals(Blocks.OBSIDIAN)) {
                            LookHelper.lookAt(mod, bedTargetPosition.down(), dir, true);

                            mod.getInputControls().tryPress(Input.CLICK_RIGHT);
                        }
                    }

                }*/
                continue;
            }
            if (!waiTimer.elapsed()) {
                return null;
            }


            // 计算龙与床的距离
            Vec3d dragonHeadPos = dragon.head.getBoundingBox().getCenter(); // dragon.head.getPos();
            Vec3d bedHeadPos = WorldHelper.toVec3d(bedHead);

            double dist = dragonHeadPos.distanceTo(bedHeadPos);
            double distXZ = distanceIgnoreY(dragonHeadPos, bedHeadPos);

            EnderDragonPart body = dragon.getBodyParts()[2];

            // 确定是否需要爆炸床
            double destroyDistance = Math.abs(body.getBoundingBox().getMin(Direction.Axis.Y) - bedHeadPos.getY());
            boolean tooClose = destroyDistance < 1.1;
            boolean skip = destroyDistance > 3 && dist > 4.5 && distXZ > 2.5;

            mod.log(destroyDistance + " : " + dist + " : " + distXZ);
            //   double dist = distanceIgnoreY(dragonHeadPos,WorldHelper.toVec3d(bedHead));

            // 爆炸床触发条件
            if ((dist < 1.5 || (prevDist < distXZ && destroyDistance < 4 && prevDist < 2.9)) || (destroyDistance < 2 && dist < 4)
                    || (destroyDistance < 1.7 && dist < 4.5) || tooClose || (destroyDistance < 2.4 && distXZ < 3.7) || (destroyDistance < 3.5 && distXZ < 2.4)) {

                if (!skip) {
                    mod.getInputControls().tryPress(Input.CLICK_RIGHT);
                    placeBedTimer.reset();
                }
            }

            prevDist = distXZ;

            double yDist = dragonHeadPos.getY() - bedHead.getY();
            // Debug.logMessage(dist+"");

           /* if ((dist < 0.9 && yDist < 3) || yDist<2) {
                mod.getInputControls().tryPress(Input.CLICK_RIGHT);

            }*/
           /* if (prevDist < dist && yDist < 4) {
                mod.getInputControls().tryPress(Input.CLICK_RIGHT);
            }

            prevDist = dist;*/

        }

        return null;
    }

    /**
     * 计算忽略Y轴的距离
     * @param vec 第一个向量
     * @param vec1 第二个向量
     * @return XZ平面的距离
     */
    public double distanceIgnoreY(Vec3d vec, Vec3d vec1) {
        double d = vec.x - vec1.x;
        double f = vec.z - vec1.z;
        return Math.sqrt(d * d + f * f);
    }

    @Override
    protected void onStop(Task interruptTask) {

    }

    @Override
    public boolean isFinished() {
        return AltoClef.getInstance().getEntityTracker().getTrackedEntities(EnderDragonEntity.class).isEmpty();
    }

    @Override
    protected void onStart() {

    }

    @Override
    protected boolean isEqual(Task other) {
        return false;
    }

    @Override
    protected String toDebugString() {
        return "One cycling (速通模式)";
    }
}
