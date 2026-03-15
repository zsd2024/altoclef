package adris.altoclef.tasks.resources;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.TaskCatalogue;
import adris.altoclef.tasks.ResourceTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.CraftingRecipe;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.helpers.ItemHelper;
import net.minecraft.item.Item;

import java.util.function.Function;

/**
 * 使用匹配去皮原木制作任务
 * 用于处理使用去皮原木作为主要材料的合成任务
 */
public class CraftWithMatchingStrippedLogsTask extends CraftWithMatchingMaterialsTask {

    // 可视化目标物品
    private final ItemTarget _visualTarget;
    // 获取目标物品的函数
    private final Function<ItemHelper.WoodItems, Item> _getTargetItem;

    public CraftWithMatchingStrippedLogsTask(Item[] validTargets, Function<ItemHelper.WoodItems, Item> getTargetItem, CraftingRecipe recipe, boolean[] sameMask, int count) {
        super(new ItemTarget(validTargets, count), recipe, sameMask);
        _getTargetItem = getTargetItem;
        _visualTarget = new ItemTarget(validTargets, count);
    }


    @Override
    protected Task getSpecificSameResourceTask(AltoClef mod, Item[] toGet) {
        for (Item strippedLogToGet : toGet) {
            Item log = ItemHelper.strippedToLogs(strippedLogToGet);
            // 将原木转换为去皮原木
            if (mod.getItemStorage().getItemCount(log) >= 1) {
                return TaskCatalogue.getItemTask(strippedLogToGet, 1);//new CraftInInventoryTask(new ItemTarget(plankToGet, 1), CraftingRecipe.newShapedRecipe("planks", new ItemTarget[]{new ItemTarget(log, 1), empty, empty, empty}, 4), false, true);
            }
        }
        Debug.logError("CraftWithMatchingStrippedLogs: 不应该发生！");
        return null;
    }

    @Override
    protected Item getSpecificItemCorrespondingToMajorityResource(Item majority) {
        // 遍历所有木材物品，找到匹配去皮原木的类型
        for (ItemHelper.WoodItems woodItems : ItemHelper.getWoodItems()) {
            if (woodItems.strippedLog == majority) {
                // 返回对应的目标物品
                return _getTargetItem.apply(woodItems);
            }
        }
        return null;
    }


    @Override
    protected boolean isEqualResource(ResourceTask other) {
        if (other instanceof CraftWithMatchingStrippedLogsTask task) {
            return task._visualTarget.equals(_visualTarget);
        }
        return false;
    }

    @Override
    protected String toDebugStringName() {
        return "获取: " + _visualTarget;
    }


    @Override
    protected boolean shouldAvoidPickingUp(AltoClef mod) {
        return false;
    }

}
