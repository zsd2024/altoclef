package adris.altoclef.tasks.resources;

import adris.altoclef.AltoClef;
import adris.altoclef.tasks.CraftInInventoryTask;
import adris.altoclef.tasks.ResourceTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.*;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.Items;

/**
 * 收集红砂岩任务
 * 用于收集红砂岩，优先使用红沙合成，否则挖掘红砂岩或红沙
 */
public class CollectRedSandstoneTask extends ResourceTask {

    private final int _count; // 目标红砂岩数量

    public CollectRedSandstoneTask(int targetCount) {
        super(Items.RED_SANDSTONE, targetCount);
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
        // 如果已有4个或更多的红沙，尝试合成红砂岩
        if (mod.getItemStorage().getItemCount(Items.RED_SAND) >= 4) {
            int target = mod.getItemStorage().getItemCount(Items.RED_SANDSTONE) + 1;
            ItemTarget s = new ItemTarget(Items.RED_SAND, 1);
            return new CraftInInventoryTask(new RecipeTarget(Items.RED_SANDSTONE, target, CraftingRecipe.newShapedRecipe("red_sandstone", new ItemTarget[]{s, s, s, s}, 1)));
        }
        // 否则挖掘红砂岩或红沙
        return new MineAndCollectTask(new ItemTarget(Items.RED_SANDSTONE, Items.RED_SAND), new Block[]{Blocks.RED_SANDSTONE, Blocks.RED_SAND}, MiningRequirement.WOOD).forceDimension(Dimension.OVERWORLD);
    }

    @Override
    protected void onResourceStop(AltoClef mod, Task interruptTask) {
        // 任务结束时的清理
    }

    @Override
    protected boolean isEqualResource(ResourceTask other) {
        return other instanceof CollectRedSandstoneTask;
    }

    @Override
    protected String toDebugStringName() {
        return "收集 " + _count + " 个红砂岩。";
    }
}
