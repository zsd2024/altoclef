package adris.altoclef.tasks.resources;

import adris.altoclef.AltoClef;
import adris.altoclef.multiversion.versionedfields.Blocks;
import adris.altoclef.multiversion.versionedfields.Items;
import adris.altoclef.tasks.CraftInInventoryTask;
import adris.altoclef.tasks.ResourceTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.*;
import net.minecraft.block.Block;

/**
 * 收集滴水石块任务
 * 用于收集滴水石块，可通过挖掘滴水石块或尖锐滴水石获得，或用4个尖锐滴水石合成
 */
public class CollectDripstoneBlockTask extends ResourceTask {

    private final int _count; // 目标滴水石块数量

    public CollectDripstoneBlockTask(int targetCount) {
        super(Items.DRIPSTONE_BLOCK, targetCount);
        _count = targetCount;
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
        // 如果已有4个或更多尖锐滴水石，尝试合成滴水石块
        if (mod.getItemStorage().getItemCount(Items.POINTED_DRIPSTONE) >= 4) {
            int target = mod.getItemStorage().getItemCount(Items.DRIPSTONE_BLOCK) + 1;
            ItemTarget s = new ItemTarget(Items.POINTED_DRIPSTONE, 1);
            return new CraftInInventoryTask(new RecipeTarget(Items.DRIPSTONE_BLOCK, target, CraftingRecipe.newShapedRecipe("dri", new ItemTarget[]{s, s, s, s}, 1)));
        }
        // 否则挖掘滴水石块或尖锐滴水石
        return new MineAndCollectTask(new ItemTarget(Items.DRIPSTONE_BLOCK, Items.POINTED_DRIPSTONE), new Block[]{Blocks.DRIPSTONE_BLOCK, Blocks.POINTED_DRIPSTONE}, MiningRequirement.WOOD).forceDimension(Dimension.OVERWORLD);
    }

    @Override
    protected void onResourceStop(AltoClef mod, Task interruptTask) {
        // 任务结束时的清理
    }

    @Override
    protected boolean isEqualResource(ResourceTask other) {
        return other instanceof CollectDripstoneBlockTask;
    }

    @Override
    protected String toDebugStringName() {
        return "收集 " + _count + " 个滴水石块。";
    }
}
