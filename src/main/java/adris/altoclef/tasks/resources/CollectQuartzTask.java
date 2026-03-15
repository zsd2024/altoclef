package adris.altoclef.tasks.resources;

import adris.altoclef.AltoClef;
import adris.altoclef.tasks.ResourceTask;
import adris.altoclef.tasks.movement.DefaultGoToDimensionTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.Dimension;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.MiningRequirement;
import adris.altoclef.util.helpers.WorldHelper;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.Items;

/**
 * 收集石英任务
 * 用于收集下界的石英，需要前往下界挖掘下界石英矿石
 */
public class CollectQuartzTask extends ResourceTask {

    private final int _count; // 目标石英数量

    public CollectQuartzTask(int count) {
        super(Items.QUARTZ, count);
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
        if (WorldHelper.getCurrentDimension() != Dimension.NETHER) {
            setDebugState("前往下界");
            return new DefaultGoToDimensionTask(Dimension.NETHER);
        }

        setDebugState("挖掘");
        return new MineAndCollectTask(new ItemTarget(Items.QUARTZ, _count), new Block[]{Blocks.NETHER_QUARTZ_ORE}, MiningRequirement.WOOD);
    }

    @Override
    protected void onResourceStop(AltoClef mod, Task interruptTask) {
        // 任务结束时的清理
    }

    @Override
    protected boolean isEqualResource(ResourceTask other) {
        return other instanceof CollectQuartzTask;
    }

    @Override
    protected String toDebugStringName() {
        return "收集 " + _count + " 个石英";
    }
}
