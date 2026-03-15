package adris.altoclef.tasks.resources;

import adris.altoclef.AltoClef;
import adris.altoclef.tasks.CraftInInventoryTask;
import adris.altoclef.tasks.ResourceTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.CraftingRecipe;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.MiningRequirement;
import adris.altoclef.util.RecipeTarget;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.Items;

/**
 * 收集小麦任务
 * 用于收集小麦，优先从干草块合成，否则挖掘小麦作物
 */
public class CollectWheatTask extends ResourceTask {

    private final int _count; // 目标小麦数量

    public CollectWheatTask(int targetCount) {
        super(Items.WHEAT, targetCount);
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
        // 我们可能有足够的干草块来满足需求
        int potentialCount = mod.getItemStorage().getItemCount(Items.WHEAT) + 9 * mod.getItemStorage().getItemCount(Items.HAY_BLOCK);
        if (potentialCount >= _count) {
            setDebugState("合成小麦");
            return new CraftInInventoryTask(new RecipeTarget(Items.WHEAT, _count, CraftingRecipe.newShapedRecipe("wheat", new ItemTarget[]{new ItemTarget(Items.HAY_BLOCK, 1), null, null, null}, 9)));
        }
        if (mod.getBlockScanner().anyFound(Blocks.HAY_BLOCK) || mod.getEntityTracker().itemDropped(Items.HAY_BLOCK)) {
            return new MineAndCollectTask(Items.HAY_BLOCK, 99999999, new Block[]{Blocks.HAY_BLOCK}, MiningRequirement.HAND);
        }
        // 收集小麦
        return new CollectCropTask(new ItemTarget(Items.WHEAT, _count), new Block[]{Blocks.WHEAT}, Items.WHEAT_SEEDS);
    }

    @Override
    protected void onResourceStop(AltoClef mod, Task interruptTask) {
        // 任务结束时的清理
    }

    @Override
    protected boolean isEqualResource(ResourceTask other) {
        return other instanceof CollectWheatTask;
    }

    @Override
    protected String toDebugStringName() {
        return "收集 " + _count + " 个小麦。";
    }

}
