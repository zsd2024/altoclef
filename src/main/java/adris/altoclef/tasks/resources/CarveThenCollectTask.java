package adris.altoclef.tasks.resources;

import adris.altoclef.AltoClef;
import adris.altoclef.TaskCatalogue;
import adris.altoclef.tasks.DoToClosestBlockTask;
import adris.altoclef.tasks.InteractWithBlockTask;
import adris.altoclef.tasks.ResourceTask;
import adris.altoclef.tasks.construction.DestroyBlockTask;
import adris.altoclef.tasks.construction.PlaceBlockNearbyTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.helpers.StorageHelper;
import net.minecraft.block.Block;
import net.minecraft.item.Item;

import java.util.Arrays;

/**
 * 雕刻然后收集任务
 * 用于先对某种方块进行雕刻操作，然后收集结果方块
 */
public class CarveThenCollectTask extends ResourceTask {

    private final ItemTarget _target; // 目标物品
    private final Block[] _targetBlocks; // 目标方块数组
    private final ItemTarget _toCarve; // 要雕刻的物品
    private final Block[] _toCarveBlocks; // 要雕刻的方块数组
    private final ItemTarget _carveWith; // 用于雕刻的工具

    public CarveThenCollectTask(ItemTarget target, Block[] targetBlocks, ItemTarget toCarve, Block[] toCarveBlocks, ItemTarget carveWith) {
        super(target);
        _target = target;
        _targetBlocks = targetBlocks;
        _toCarve = toCarve;
        _toCarveBlocks = toCarveBlocks;
        _carveWith = carveWith;
    }

    public CarveThenCollectTask(Item target, int targetCount, Block targetBlock, Item toCarve, Block toCarveBlock, Item carveWith) {
        this(new ItemTarget(target, targetCount), new Block[]{targetBlock}, new ItemTarget(toCarve, targetCount), new Block[]{toCarveBlock}, new ItemTarget(carveWith, 1));
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
        // 如果发现目标方块，破坏它！
        // 如果发现要雕刻的方块，雕刻它！
        // neededCarve = (neededTarget - currentTarget)
        // 如果neededCarve > currentCarveItems:
        //      收集雕刻物品！
        // 否则:
        //      放置要雕刻的物品

        // 如果我们的目标方块被放置，破坏它！
        if (mod.getBlockScanner().anyFound(_targetBlocks)) {
            setDebugState("破坏雕刻/目标方块");
            return new DoToClosestBlockTask(DestroyBlockTask::new, _targetBlocks);
        }
        // 收集我们的"雕刻工具"（可以是剪刀、斧头等）
        if (!StorageHelper.itemTargetsMetInventory(_carveWith)) {
            setDebugState("收集我们的雕刻工具");
            return TaskCatalogue.getItemTask(_carveWith);
        }
        // 如果发现要雕刻的方块，雕刻它
        if (mod.getBlockScanner().anyFound(_toCarveBlocks)) {
            setDebugState("雕刻方块");
            return new DoToClosestBlockTask(blockPos -> new InteractWithBlockTask(_carveWith, blockPos, false), _toCarveBlocks);
        }
        // 如果我们没有足够的雕刻方块则收集它们，否则放置它们
        int neededCarveItems = _target.getTargetCount() - mod.getItemStorage().getItemCount(_target);
        int currentCarveItems = mod.getItemStorage().getItemCount(_toCarve);
        if (neededCarveItems > currentCarveItems) {
            setDebugState("收集更多要雕刻的方块");
            return TaskCatalogue.getItemTask(_toCarve);
        } else {
            setDebugState("放置要雕刻的方块");
            return new PlaceBlockNearbyTask(_toCarveBlocks);
        }
    }

    @Override
    protected void onResourceStop(AltoClef mod, Task interruptTask) {
        // 任务结束时的清理
    }

    @Override
    protected boolean isEqualResource(ResourceTask other) {
        if (other instanceof CarveThenCollectTask task) {
            return (task._target.equals(_target) && task._toCarve.equals(_toCarve) && Arrays.equals(task._targetBlocks, _targetBlocks) && Arrays.equals(task._toCarveBlocks, _toCarveBlocks));
        }
        return false;
    }

    @Override
    protected String toDebugStringName() {
        return "雕刻后获取: " + _target;
    }
}
