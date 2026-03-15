package adris.altoclef.tasks.resources;

import adris.altoclef.AltoClef;
import adris.altoclef.tasks.ResourceTask;
import adris.altoclef.tasks.container.CraftInTableTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.CraftingRecipe;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.MiningRequirement;
import adris.altoclef.util.RecipeTarget;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.Items;

/**
 * 收集干草块任务
 * 用于收集干草块，优先尝试挖掘现有干草块，如果没有则使用小麦合成
 * TODO: 这个任务实际上可以移除，因为它就是一个挖掘任务后跟一个收集任务。
 */
public class CollectHayBlockTask extends ResourceTask {

    private final int count; // 目标干草块数量

    public CollectHayBlockTask(int count) {
        super(Items.HAY_BLOCK, count);
        this.count = count;
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

        // 如果找到了干草块，直接挖掘
        if (mod.getBlockScanner().anyFound(Blocks.HAY_BLOCK)) {
            return new MineAndCollectTask(Items.HAY_BLOCK, count, new Block[]{Blocks.HAY_BLOCK}, MiningRequirement.HAND);
        }

        // 否则使用小麦合成干草块
        ItemTarget w = new ItemTarget(Items.WHEAT, 1);
        return new CraftInTableTask(new RecipeTarget(Items.HAY_BLOCK, count, CraftingRecipe.newShapedRecipe("hay_block", new ItemTarget[]{w, w, w, w, w, w, w, w, w}, 1)));
    }

    @Override
    protected void onResourceStop(AltoClef mod, Task interruptTask) {
        // 任务结束时的清理
    }

    @Override
    protected boolean isEqualResource(ResourceTask other) {
        return other instanceof CollectHayBlockTask;
    }

    @Override
    protected String toDebugStringName() {
        return "收集 " + count + " 个干草块。";
    }
}
