package adris.altoclef.tasks.resources;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.TaskCatalogue;
import adris.altoclef.tasks.ResourceTask;
import adris.altoclef.tasks.entity.AbstractDoToEntityTask;
import adris.altoclef.tasksystem.Task;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.CowEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;

import java.util.Optional;

/**
 * 收集牛奶任务
 * 用于收集牛奶桶，通过与牛交互来获取
 */
public class CollectMilkTask extends ResourceTask {

    private final int count; // 目标牛奶桶数量

    public CollectMilkTask(int targetCount) {
        super(Items.MILK_BUCKET, targetCount);
        count = targetCount;
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
        // 确保我们有一个桶
        if (!mod.getItemStorage().hasItem(Items.BUCKET)) {
            return TaskCatalogue.getItemTask(Items.BUCKET, 1);
        }
        // 维度检查
        if (!mod.getEntityTracker().entityFound(CowEntity.class) && isInWrongDimension(mod)) {
            return getToCorrectDimensionTask(mod);
        }
        return new MilkCowTask();
    }

    @Override
    protected void onResourceStop(AltoClef mod, Task interruptTask) {
        // 任务结束时的清理
    }

    @Override
    protected boolean isEqualResource(ResourceTask other) {
        return other instanceof CollectMilkTask;
    }

    @Override
    protected String toDebugStringName() {
        return "收集 " + count + " 个牛奶桶。";
    }

    /**
     * 挤牛奶任务
     * 用于与最近的牛进行交互以获得牛奶
     */
    static class MilkCowTask extends AbstractDoToEntityTask {

        public MilkCowTask() {
            super(0, -1, -1);
        }

        @Override
        protected boolean isSubEqual(AbstractDoToEntityTask other) {
            return other instanceof MilkCowTask;
        }

        @Override
        protected Task onEntityInteract(AltoClef mod, Entity entity) {
            if (!mod.getItemStorage().hasItem(Items.BUCKET)) {
                Debug.logWarning("挤牛奶失败，因为你没有桶。");
                return null;
            }
            if (mod.getSlotHandler().forceEquipItem(Items.BUCKET)) {
                mod.getController().interactEntity(mod.getPlayer(), entity, Hand.MAIN_HAND);
            }


            return null;
        }

        @Override
        protected Optional<Entity> getEntityTarget(AltoClef mod) {
            return mod.getEntityTracker().getClosestEntity(mod.getPlayer().getPos(), CowEntity.class);
        }

        @Override
        protected String toDebugString() {
            return "挤牛奶";
        }
    }
}
