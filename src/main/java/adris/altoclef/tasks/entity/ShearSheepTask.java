package adris.altoclef.tasks.entity;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.tasksystem.Task;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;

import java.util.Optional;

/**
 * 剪羊毛任务类
 * 用于自动寻找并剪羊毛羊的任务实现
 */
public class ShearSheepTask extends AbstractDoToEntityTask {

    /**
     * 构造函数
     * 初始化剪羊毛任务，设置参数为(0, -1, -1)
     */
    public ShearSheepTask() {
        super(0, -1, -1);
    }

    @Override
    protected boolean isSubEqual(AbstractDoToEntityTask other) {
        return other instanceof ShearSheepTask;
    }

    /**
     * 与实体交互的处理方法
     * 当找到目标羊时执行此方法进行剪羊毛操作
     * 
     * @param mod AltoClef主实例
     * @param entity 目标实体（羊）
     * @return 返回null表示任务完成或无法继续
     */
    @Override
    protected Task onEntityInteract(AltoClef mod, Entity entity) {
        // 检查是否拥有剪刀
        if (!mod.getItemStorage().hasItem(Items.SHEARS)) {
            Debug.logWarning("由于没有剪刀，无法剪羊毛。");
            return null;
        }
        // 强制装备剪刀并交互
        if (mod.getSlotHandler().forceEquipItem(Items.SHEARS)) {
            mod.getController().interactEntity(mod.getPlayer(), entity, Hand.MAIN_HAND);
        }


        return null;
    }

    /**
     * 获取目标实体的方法
     * 寻找最近的可剪羊毛的羊
     * 
     * @param mod AltoClef主实例
     * @return 可选的目标羊实体
     */
    @Override
    protected Optional<Entity> getEntityTarget(AltoClef mod) {
        return mod.getEntityTracker().getClosestEntity(mod.getPlayer().getPos(),
                entity -> {
                    if (entity instanceof SheepEntity sheep) {
                        // 检查羊是否可剪且未被剪过
                        return sheep.isShearable() && !sheep.isSheared();
                    }
                    return false;
                }, SheepEntity.class
        );
    }

    @Override
    protected String toDebugString() {
        return "正在剪羊毛";
    }
}

    @Override
    protected boolean isSubEqual(AbstractDoToEntityTask other) {
        return other instanceof ShearSheepTask;
    }

    @Override
    protected Task onEntityInteract(AltoClef mod, Entity entity) {
        if (!mod.getItemStorage().hasItem(Items.SHEARS)) {
            Debug.logWarning("Failed to shear sheep because you have no shears.");
            return null;
        }
        if (mod.getSlotHandler().forceEquipItem(Items.SHEARS)) {
            mod.getController().interactEntity(mod.getPlayer(), entity, Hand.MAIN_HAND);
        }


        return null;
    }

    @Override
    protected Optional<Entity> getEntityTarget(AltoClef mod) {
        return mod.getEntityTracker().getClosestEntity(mod.getPlayer().getPos(),
                entity -> {
                    if (entity instanceof SheepEntity sheep) {
                        return sheep.isShearable() && !sheep.isSheared();
                    }
                    return false;
                }, SheepEntity.class
        );
    }

    @Override
    protected String toDebugString() {
        return "Shearing Sheep";
    }
}
