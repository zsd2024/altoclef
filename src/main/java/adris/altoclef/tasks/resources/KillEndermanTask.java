package adris.altoclef.tasks.resources;

import adris.altoclef.AltoClef;
import adris.altoclef.tasks.ResourceTask;
import adris.altoclef.tasks.entity.KillEntitiesTask;
import adris.altoclef.tasks.entity.KillEntityTask;
import adris.altoclef.tasks.movement.GetWithinRangeOfBlockTask;
import adris.altoclef.tasks.movement.TimeoutWanderTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.Dimension;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.helpers.WorldHelper;
import adris.altoclef.util.time.TimerGame;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;

import java.util.Optional;
import java.util.function.Predicate;

/**
 * 击杀末影人任务
 * 用于击杀末影人以获取末影珍珠
 */
public class KillEndermanTask extends ResourceTask {

    private final int _count;

    private final TimerGame _lookDelay = new TimerGame(0.2);

    public KillEndermanTask(int count) {
        super(new ItemTarget(Items.ENDER_PEARL, count));
        _count = count;
        forceDimension(Dimension.NETHER);
    }

    @Override
    protected boolean shouldAvoidPickingUp(AltoClef mod) {
        return false;
    }

    @Override
    protected void onResourceStart(AltoClef mod) {
        // 任务开始时的初始化
    }

    @Override
    protected Task onResourceTick(AltoClef mod) {
        // 检查维度和末影人是否存在
        if (!mod.getEntityTracker().entityFound(EndermanEntity.class)) {
            if (WorldHelper.getCurrentDimension() != Dimension.NETHER) {
                return getToCorrectDimensionTask(mod);
            }
            // 查找最近的诡异森林相关方块
            Optional<BlockPos> nearest = mod.getBlockScanner().getNearestBlock(Blocks.TWISTING_VINES, Blocks.TWISTING_VINES_PLANT, Blocks.WARPED_HYPHAE, Blocks.WARPED_NYLIUM);
            if (nearest.isPresent()) {
                if (WorldHelper.inRangeXZ(nearest.get(), mod.getPlayer().getBlockPos(), 40)) {
                    setDebugState("等待末影人生成...");
                    return null;
                }

                setDebugState("前往诡异森林生物群系");
                return new GetWithinRangeOfBlockTask(nearest.get(), 35);
            }

            setDebugState("未找到诡异森林生物群系");
            return new TimeoutWanderTask();
        }

        // 定义在下界屋顶下方的实体谓词（Y坐标小于125）
        Predicate<Entity> belowNetherRoof = (entity) -> WorldHelper.getCurrentDimension() != Dimension.NETHER || entity.getY() < 125;
        final int TOO_FAR_AWAY = WorldHelper.getCurrentDimension() == Dimension.NETHER ? 10 : 256;

        // 攻击愤怒的末影人
        for (EndermanEntity entity : mod.getEntityTracker().getTrackedEntities(EndermanEntity.class)) {
            if (belowNetherRoof.test(entity) && entity.isAngry() && entity.getPos().isInRange(mod.getPlayer().getPos(), TOO_FAR_AWAY)) {
                return new KillEntityTask(entity);
            }
        }

        // 攻击最近的末影人
        return new KillEntitiesTask(belowNetherRoof, EndermanEntity.class);
    }

    @Override
    protected void onResourceStop(AltoClef mod, Task interruptTask) {
        // 任务停止时的清理
    }

    @Override
    protected boolean isEqualResource(ResourceTask other) {
        if (other instanceof KillEndermanTask task) {
            return task._count == _count;
        }
        return false;
    }

    @Override
    protected String toDebugStringName() {
        return "狩猎末影人获取珍珠 - " + AltoClef.getInstance().getItemStorage().getItemCount(Items.ENDER_PEARL) + "/" + _count;
    }
}