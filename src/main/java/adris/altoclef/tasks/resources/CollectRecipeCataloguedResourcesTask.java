package adris.altoclef.tasks.resources;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.TaskCatalogue;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.CraftingRecipe;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.RecipeTarget;
import adris.altoclef.util.helpers.StorageHelper;
import net.minecraft.item.Item;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;
import java.util.HashMap;

/**
 * 收集配方目录资源任务
 * 收集配方所需的所有已目录化的资源材料
 */
public class CollectRecipeCataloguedResourcesTask extends Task {

    private final RecipeTarget[] _targets; // 目标配方数组
    private final boolean _ignoreUncataloguedSlots; // 是否忽略未目录化的槽位
    private boolean _finished = false; // 是否完成

    public CollectRecipeCataloguedResourcesTask(boolean ignoreUncataloguedSlots, RecipeTarget... targets) {
        _targets = targets;
        _ignoreUncataloguedSlots = ignoreUncataloguedSlots;
    }

    @Override
    protected void onStart() {
        _finished = false;
    }

    @Override
    protected Task onTick() {
        // TODO: 缓存这一次而不是每帧执行
        AltoClef mod = AltoClef.getInstance();

        // 需要获取的物品，包括目录化和单个物品
        HashMap<String, Integer> catalogueCount = new HashMap<>();
        HashMap<Item, Integer> itemCount = new HashMap<>();

        for (RecipeTarget target : _targets) {
            // 如果已有目标物品则跳过此配方
            //if (mod.getItemStorage().targetMet(target.getItem())) continue;

            // null = 空，总是满足
            if (target == null) continue;

            int weNeed = target.getTargetCount() - mod.getItemStorage().getItemCount(target.getOutputItem());

            if (weNeed > 0) {
                CraftingRecipe recipe = target.getRecipe();
                // 默认，遍历配方槽位并收集第一个
                for (int i = 0; i < recipe.getSlotCount(); ++i) {
                    ItemTarget slot = recipe.getSlot(i);
                    if (slot == null || slot.isEmpty()) continue;
                    int numberOfRepeats = (int) Math.floor(-0.1 + (double) weNeed / target.getRecipe().outputCount()) + 1;
                    if (!slot.isCatalogueItem()) {
                        if (slot.getMatches().length != 1) {
                            if (!_ignoreUncataloguedSlots) {
                                Debug.logWarning("配方 " + recipe + " 槽位 " + i
                                        + " 未目录化。请为此物品目标定义明确的"
                                        + " collectRecipeSubTask() 函数：" + slot
                                );
                            }
                        } else {
                            Item item = slot.getMatches()[0];
                            itemCount.put(item, itemCount.getOrDefault(item, 0) + numberOfRepeats);
                        }
                    } else {
                        String targetName = slot.getCatalogueName();
                        // 我们需要多少次"重复"配方
                        catalogueCount.put(targetName, catalogueCount.getOrDefault(targetName, 0) + numberOfRepeats);
                    }
                }
            }
        }


        // (与上述内容一起缓存!!)
        // 获取材料
        for (String catalogueMaterialName : catalogueCount.keySet()) {
            int count = catalogueCount.get(catalogueMaterialName);
            if (count > 0) {
                ItemTarget itemTarget = new ItemTarget(catalogueMaterialName, count);
                if (!StorageHelper.itemTargetsMet(mod, itemTarget)) {
                    setDebugState("获取 " + itemTarget);
                    return TaskCatalogue.getItemTask(catalogueMaterialName, count);
                }
            }
        }
        for (Item item : itemCount.keySet()) {
            int count = itemCount.get(item);
            if (count > 0) {
                if (mod.getItemStorage().getItemCount(item) < count) {
                    setDebugState("获取 " + item.getTranslationKey());
                    return TaskCatalogue.getItemTask(item, count);
                }
            }
        }
        _finished = true;

        return null;
    }


    @Override
    protected void onStop(Task interruptTask) {
        // 任务结束时的清理
    }

    @Override
    protected boolean isEqual(Task other) {
        if (other instanceof CollectRecipeCataloguedResourcesTask task) {
            return Arrays.equals(task._targets, _targets);
        }
        return false;
    }

    @Override
    protected String toDebugString() {
        return "收集配方资源: " + ArrayUtils.toString(_targets);
    }

    @Override
    public boolean isFinished() {
        if (_finished) {
            if (!StorageHelper.hasRecipeMaterialsOrTarget(AltoClef.getInstance(), this._targets)) {
                _finished = false;
                Debug.logMessage("无效的收集配方\"完成\"状态，重置。");
            }
        }
        return _finished;
    }
}
