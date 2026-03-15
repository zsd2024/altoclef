package adris.altoclef.tasks.resources;

import adris.altoclef.AltoClef;
import adris.altoclef.tasks.CraftInInventoryTask;
import adris.altoclef.tasks.ResourceTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.*;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;

import java.util.Optional;

/**
 * 收集砂土任务
 * 用于收集砂土，可以挖掘已有的砂土方块或通过泥土和沙砾合成
 */
public class CollectCoarseDirtTask extends ResourceTask {

    private static final float CLOSE_ENOUGH_COARSE_DIRT = 128; // 判断砂土够近的距离阈值
    private final int _count; // 目标砂土数量

    public CollectCoarseDirtTask(int targetCount) {
        super(Items.COARSE_DIRT, targetCount);
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
        // 计算完成配方所需的最少泥土/沙砾数量，考虑已收集的砂土
        double c = Math.ceil((double) (_count - mod.getItemStorage().getItemCount(Items.COARSE_DIRT)) / 4) * 2;
        Optional<BlockPos> closest = mod.getBlockScanner().getNearestBlock(Blocks.COARSE_DIRT);

        // 如果泥土和沙砾不足以完成配方，且附近有距离足够的砂土，则收集砂土
        if (!(mod.getItemStorage().getItemCount(Items.DIRT) >= c &&
                mod.getItemStorage().getItemCount(Items.GRAVEL) >= c) &&
                closest.isPresent() && closest.get().isWithinDistance(mod.getPlayer().getPos(), CLOSE_ENOUGH_COARSE_DIRT)) {
            return new MineAndCollectTask(new ItemTarget(Items.COARSE_DIRT), new Block[]{Blocks.COARSE_DIRT}, MiningRequirement.HAND).forceDimension(Dimension.OVERWORLD);
        } else {
            // 否则通过合成获取砂土
            int target = _count;
            ItemTarget d = new ItemTarget(Items.DIRT, 1);
            ItemTarget g = new ItemTarget(Items.GRAVEL, 1);
            return new CraftInInventoryTask(new RecipeTarget(Items.COARSE_DIRT, target, CraftingRecipe.newShapedRecipe("coarse_dirt", new ItemTarget[]{d, g, g, d}, 4)));
        }
    }

    @Override
    protected void onResourceStop(AltoClef mod, Task interruptTask) {
        // 任务结束时的清理
    }

    @Override
    protected boolean isEqualResource(ResourceTask other) {
        return other instanceof CollectCoarseDirtTask;
    }

    @Override
    protected String toDebugStringName() {
        return "收集 " + _count + " 个砂土。";
    }
}
