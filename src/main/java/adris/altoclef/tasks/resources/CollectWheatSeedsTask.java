package adris.altoclef.tasks.resources;

import adris.altoclef.AltoClef;
import adris.altoclef.tasks.ResourceTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.MiningRequirement;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.Items;

/**
 * 收集小麦种子任务
 * 用于收集小麦种子，优先从成熟的小麦中收集（保留种子），否则破坏草方块获得种子
 */
public class CollectWheatSeedsTask extends ResourceTask {

    private final int _count; // 目标小麦种子数量

    public CollectWheatSeedsTask(int count) {
        super(Items.WHEAT_SEEDS, count);
        _count = count;
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
        // 如果找到小麦方块，收集小麦但不捡起小麦（保留种子）
        if (mod.getBlockScanner().anyFound(Blocks.WHEAT)) {
            return new CollectCropTask(Items.AIR, 999, Blocks.WHEAT, Items.WHEAT_SEEDS);
        }
        // 否则，破坏草方块
        return new MineAndCollectTask(Items.WHEAT_SEEDS, _count, new Block[]{Blocks.SHORT_GRASS, Blocks.TALL_GRASS}, MiningRequirement.HAND);
    }

    @Override
    protected void onResourceStop(AltoClef mod, Task interruptTask) {
        // 任务结束时的清理
    }

    @Override
    protected boolean isEqualResource(ResourceTask other) {
        return other instanceof CollectWheatSeedsTask;
    }

    @Override
    protected String toDebugStringName() {
        return "收集 " + _count + " 个小麦种子。";
    }
}
