package adris.altoclef.tasks.resources;

import adris.altoclef.AltoClef;
import adris.altoclef.tasks.DoToClosestBlockTask;
import adris.altoclef.tasks.ResourceTask;
import adris.altoclef.tasks.construction.DestroyBlockTask;
import adris.altoclef.tasks.movement.SearchWithinBiomeTask;
import adris.altoclef.tasksystem.Task;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CocoaBlock;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.BiomeKeys;

import java.util.HashSet;
import java.util.function.Predicate;

/**
 * 收集可可豆任务
 * 用于收集丛林生物群系中可可果方块的可可豆
 */
public class CollectCocoaBeansTask extends ResourceTask {
    private final int _count; // 目标可可豆数量
    private final HashSet<BlockPos> _wasFullyGrown = new HashSet<>(); // 记录曾经成熟过的可可果位置

    public CollectCocoaBeansTask(int targetCount) {
        super(Items.COCOA_BEANS, targetCount);
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

        // 定义成熟可可果的判断条件
        Predicate<BlockPos> validCocoa = (blockPos) -> {
            if (!mod.getChunkTracker().isChunkLoaded(blockPos)) {
                return _wasFullyGrown.contains(blockPos);
            }

            BlockState s = mod.getWorld().getBlockState(blockPos);
            boolean mature = s.get(CocoaBlock.AGE) == 2; // 年龄为2表示完全成熟
            if (_wasFullyGrown.contains(blockPos)) {
                if (!mature) _wasFullyGrown.remove(blockPos); // 如果不再成熟，从记录中移除
            } else {
                if (mature) _wasFullyGrown.add(blockPos); // 如果成熟，添加到记录中
            }
            return mature;
        };

        // 破坏成熟的可可果方块
        if (mod.getBlockScanner().anyFound(validCocoa, Blocks.COCOA)) {
            setDebugState("破坏可可果方块");
            return new DoToClosestBlockTask(DestroyBlockTask::new, validCocoa, Blocks.COCOA);
        }

        // 维度检查
        if (isInWrongDimension(mod)) {
            return getToCorrectDimensionTask(mod);
        }

        // 搜索丛林生物群系
        setDebugState("在丛林周围探索");
        return new SearchWithinBiomeTask(BiomeKeys.JUNGLE);
    }

    @Override
    protected void onResourceStop(AltoClef mod, Task interruptTask) {
        // 任务结束时的清理
    }

    @Override
    protected boolean isEqualResource(ResourceTask other) {
        return other instanceof CollectCocoaBeansTask;
    }

    @Override
    protected String toDebugStringName() {
        return "收集 " + _count + " 个可可豆。";
    }
}
