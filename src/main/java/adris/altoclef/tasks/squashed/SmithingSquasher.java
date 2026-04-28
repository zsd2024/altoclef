package adris.altoclef.tasks.squashed;


import adris.altoclef.AltoClef;
import adris.altoclef.tasks.ResourceTask;
import adris.altoclef.tasks.container.UpgradeInSmithingTableTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.helpers.StorageHelper;
import adris.altoclef.util.slots.Slot;
import adris.altoclef.util.slots.SmithingTableSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.SmithingScreenHandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 锻造合并器
 * 用于合并多个锻造升级任务，优化材料收集和锻造流程
 */
public class SmithingSquasher extends TypeSquasher<UpgradeInSmithingTableTask> {

    @Override
    protected List<ResourceTask> getSquashed(List<UpgradeInSmithingTableTask> tasks) {
        // 将材料和工具分组，然后返回相同的UpgradeInSmithing任务列表
        List<ResourceTask> result = new ArrayList<>();
        List<ItemTarget> units = new ArrayList<>();
        for (UpgradeInSmithingTableTask task : tasks) {
            units.add(task.getMaterials());
            units.add(task.getTools());
        }
        result.add(new GetMaterialsTask(units.toArray(ItemTarget[]::new)));
        // 然后执行锻造操作
        result.addAll(tasks);
        return result;
    }

    /**
     * 获取材料任务内部类
     * 负责收集锻造所需的材料和工具
     */
    private static class GetMaterialsTask extends ResourceTask {

        public GetMaterialsTask(ItemTarget[] targets) {
            super(targets);
        }

        @Override
        protected boolean shouldAvoidPickingUp(AltoClef mod) {
            return false;
        }

        @Override
        protected void onResourceStart(AltoClef mod) {

        }

        /**
         * 获取指定槽位中匹配目标的物品数量
         * 
         * @param mod AltoClef实例
         * @param slot 槽位
         * @param match 匹配目标
         * @return 物品数量
         */
        private int getItemsInSlot(AltoClef mod, Slot slot, ItemTarget match) {
            ItemStack stack = StorageHelper.getItemStackInSlot(slot);
            if (!stack.isEmpty() && match.matches(stack.getItem())) {
                return stack.getCount();
            }
            return 0;
        }

        @Override
        protected Task onResourceTick(AltoClef mod) {
            List<ItemTarget> resultingTargets = Arrays.asList(itemTargets);

            // 如果在锻造台界面中，减去所需数量，这样放入锻造台的物品不会被移除
            boolean inSmithingTable = (mod.getPlayer().currentScreenHandler instanceof SmithingScreenHandler);
            if (inSmithingTable) {
                for (int i = 0; i < resultingTargets.size(); ++i) {
                    ItemTarget target = resultingTargets.get(i);
                    int smithingTableCount = getItemsInSlot(mod, SmithingTableSlot.INPUT_SLOT_MATERIALS, target)
                            + getItemsInSlot(mod, SmithingTableSlot.INPUT_SLOT_TOOL, target)
                            + getItemsInSlot(mod, SmithingTableSlot.OUTPUT_SLOT, target);
                    resultingTargets.set(i, new ItemTarget(target, target.getTargetCount() - smithingTableCount));
                }
            }
            return new CataloguedResourceTask(resultingTargets.toArray(ItemTarget[]::new));
        }

        @Override
        protected void onResourceStop(AltoClef mod, Task interruptTask) {

        }

        @Override
        protected boolean isEqualResource(ResourceTask other) {
            return true; // 只有物品目标不同
        }

        @Override
        protected String toDebugStringName() {
            return "收集锻造材料";
        }
    }
}
