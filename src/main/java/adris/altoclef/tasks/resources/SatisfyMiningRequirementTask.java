package adris.altoclef.tasks.resources;

import adris.altoclef.TaskCatalogue;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.MiningRequirement;
import adris.altoclef.util.helpers.StorageHelper;
import net.minecraft.item.Items;

/**
 * 满足挖掘要求任务
 * 确保我们拥有达到或超过挖掘等级要求的工具
 */
public class SatisfyMiningRequirementTask extends Task {

    private final MiningRequirement requirement;

    public SatisfyMiningRequirementTask(MiningRequirement requirement) {
        this.requirement = requirement;
    }

    @Override
    protected void onStart() {
        // 任务开始时的初始化
    }

    @Override
    protected Task onTick() {
        // 根据挖掘要求等级获取相应的工具
        switch (requirement) {
            case HAND:
                // 如果编程正确，永远不会发生这种情况
                break;
            case WOOD:
                return TaskCatalogue.getItemTask(Items.WOODEN_PICKAXE, 1);
            case STONE:
                return TaskCatalogue.getItemTask(Items.STONE_PICKAXE, 1);
            case IRON:
                return TaskCatalogue.getItemTask(Items.IRON_PICKAXE, 1);
            case DIAMOND:
                return TaskCatalogue.getItemTask(Items.DIAMOND_PICKAXE, 1);
        }
        return null;
    }

    @Override
    protected void onStop(Task interruptTask) {
        // 任务停止时的清理
    }

    @Override
    protected boolean isEqual(Task other) {
        if (other instanceof SatisfyMiningRequirementTask task) {
            return task.requirement == requirement;
        }
        return false;
    }

    @Override
    protected String toDebugString() {
        return "满足挖掘要求: " + requirement;
    }

    @Override
    public boolean isFinished() {
        // 检查库存中是否有满足要求的工具
        return StorageHelper.miningRequirementMetInventory(requirement);
    }
}
