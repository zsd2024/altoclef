package adris.altoclef.tasks.resources;

import adris.altoclef.AltoClef;
import adris.altoclef.TaskCatalogue;
import adris.altoclef.tasks.InteractWithBlockTask;
import adris.altoclef.tasks.ResourceTask;
import adris.altoclef.tasks.movement.TimeoutWanderTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.MiningRequirement;
import adris.altoclef.util.helpers.ItemHelper;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;

import java.util.Optional;

/**
 * 收集去皮原木任务
 * 用于收集去皮原木，通过使用斧头对原木进行去皮操作获得
 */
public class CollectStrippedLogTask extends ResourceTask {
    private static final Item[] _axes = new Item[]{Items.WOODEN_AXE, Items.STONE_AXE, Items.GOLDEN_AXE, Items.IRON_AXE,
            Items.DIAMOND_AXE, Items.NETHERITE_AXE}; // 可用的斧头类型
    private final Item[] _strippedLogs; // 去皮原木类型
    private final Item[] _strippableLogs; // 可去皮原木类型
    private final int _targetCount; // 目标数量

    public CollectStrippedLogTask(Item[] strippedLogs, Item[] strippableLogs, int count) {
        super(new ItemTarget(strippedLogs, count));
        _strippedLogs = strippedLogs;
        _strippableLogs = strippableLogs;
        _targetCount = count;
    }

    public CollectStrippedLogTask(int count) {
        this(ItemHelper.STRIPPED_LOGS, ItemHelper.STRIPPABLE_LOGS, count);
    }

    public CollectStrippedLogTask(Item strippedLogs, Item strippableLogs, int count) {
        this(new Item[]{strippedLogs}, new Item[]{strippableLogs}, count);
    }

    public CollectStrippedLogTask(Item strippedLog, int count) {
        this(strippedLog, ItemHelper.strippedToLogs(strippedLog), count);
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
        // 检查是否有斧头，如果没有则获取斧头
        if (!mod.getItemStorage().hasItem(_axes)) {
            setDebugState("获取斧头用于去皮");
            return TaskCatalogue.getItemTask(Items.WOODEN_AXE, 1);
        }
        // 如果去皮原木数量不足目标数量
        if (mod.getItemStorage().getItemCount(_strippedLogs) < _targetCount) {
            // 寻找最近的去皮原木方块
            Optional<BlockPos> strippedLogBlockPos = mod.getBlockScanner().getNearestBlock(ItemHelper.itemsToBlocks(_strippedLogs));
            if (strippedLogBlockPos.isPresent()) {
                setDebugState("获取去皮原木");
                return new MineAndCollectTask(new ItemTarget(_strippedLogs), ItemHelper.itemsToBlocks(_strippedLogs), MiningRequirement.HAND);
            }
        }
        // 寻找最近的可去皮原木方块
        Optional<BlockPos> strippableLogBlockPos = mod.getBlockScanner().getNearestBlock(ItemHelper.itemsToBlocks(_strippableLogs));
        if (strippableLogBlockPos.isPresent()) {
            setDebugState("去皮原木");
            return new InteractWithBlockTask(new ItemTarget(_axes), strippableLogBlockPos.get());
        }
        setDebugState("搜索原木");
        return new TimeoutWanderTask();
    }

    @Override
    protected void onResourceStop(AltoClef mod, Task interruptTask) {
        // 任务结束时的清理
    }

    @Override
    protected boolean isEqualResource(ResourceTask other) {
        if (other instanceof CollectStrippedLogTask task) {
            return task._targetCount == _targetCount;
        }
        return false;
    }

    @Override
    protected String toDebugStringName() {
        return "收集去皮原木";
    }
}
