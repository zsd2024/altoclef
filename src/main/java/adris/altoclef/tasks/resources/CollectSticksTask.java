package adris.altoclef.tasks.resources;

import adris.altoclef.AltoClef;
import adris.altoclef.tasks.CraftInInventoryTask;
import adris.altoclef.tasks.ResourceTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.trackers.storage.ItemStorageTracker;
import adris.altoclef.util.CraftingRecipe;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.MiningRequirement;
import adris.altoclef.util.RecipeTarget;
import adris.altoclef.util.helpers.ItemHelper;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;

import java.util.Optional;

/**
 * 收集木棍任务
 * 用于收集木棍，优先尝试从竹子合成，其次挖掘枯死灌木，最后从木板合成
 */
public class CollectSticksTask extends ResourceTask {

    private final int _targetCount; // 目标木棍数量

    public CollectSticksTask(int targetCount) {
        super(Items.STICK, targetCount);
        _targetCount = targetCount;
    }

    @Override
    protected boolean shouldAvoidPickingUp(AltoClef mod) {
        return false;
    }

    @Override
    protected void onResourceStart(AltoClef mod) {
        mod.getBehaviour().push();
    }

    @Override
    protected double getPickupRange(AltoClef mod) {
        ItemStorageTracker storage = mod.getItemStorage();
        if (storage.getItemCount(ItemHelper.PLANKS)*4+storage.getItemCount(ItemHelper.LOG)*4*4 > _targetCount) return 10;

        return 35;
    }

    @Override
    protected Task onResourceTick(AltoClef mod) {
        // 尝试用竹子合成木棍
        if (mod.getItemStorage().getItemCount(Items.BAMBOO) >= 2) {
            return new CraftInInventoryTask(new RecipeTarget(Items.STICK, Math.min(mod.getItemStorage().getItemCount(Items.BAMBOO)/2,_targetCount), CraftingRecipe.newShapedRecipe("sticks", new ItemTarget[]{new ItemTarget("bamboo"), null, new ItemTarget("bamboo"), null}, 1)));
        }

        Optional<BlockPos> nearestBush = mod.getBlockScanner().getNearestBlock(Blocks.DEAD_BUSH);
        // 如果附近有枯死灌木，去获取它
        if (nearestBush.isPresent() && nearestBush.get().isWithinDistance(mod.getPlayer().getPos(), 20)) {
            ResourceTask task = new MineAndCollectTask(Items.DEAD_BUSH, 1, new Block[]{Blocks.DEAD_BUSH}, MiningRequirement.HAND);
            task.setAllowContainers(false);

            return task;
        }
        // 否则从木头合成
        return new CraftInInventoryTask(new RecipeTarget(Items.STICK, _targetCount, CraftingRecipe.newShapedRecipe("sticks", new ItemTarget[]{new ItemTarget("planks"), null, new ItemTarget("planks"), null}, 4)));
    }

    @Override
    protected void onResourceStop(AltoClef mod, Task interruptTask) {
        mod.getBehaviour().pop();
    }

    @Override
    protected boolean isEqualResource(ResourceTask other) {
        return other instanceof CollectSticksTask;
    }

    @Override
    protected String toDebugStringName() {
        return "制作 " + _targetCount + " 个木棍";
    }
}
