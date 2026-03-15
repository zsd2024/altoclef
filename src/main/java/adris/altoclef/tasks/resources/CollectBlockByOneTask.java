package adris.altoclef.tasks.resources;

import adris.altoclef.AltoClef;
import adris.altoclef.multiversion.versionedfields.Blocks;
import adris.altoclef.multiversion.versionedfields.Items;
import adris.altoclef.tasks.ResourceTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.MiningRequirement;
import net.minecraft.block.Block;
import net.minecraft.item.Item;

import java.util.Arrays;

/**
 * 按个收集方块任务
 * 用于逐个收集特定类型的方块，每次只收集一个单位以避免一次性挖掘过多
 */
public class CollectBlockByOneTask extends ResourceTask {

    private final Item item; // 目标物品
    private final Block[] blocks; // 可挖掘的方块数组
    private final MiningRequirement requirement; // 挖掘要求
    private final int count; // 目标数量

    public CollectBlockByOneTask(Item item, Block[] blocks, MiningRequirement requirement, int targetCount) {
        super(item, targetCount);
        this.item = item;
        this.blocks = blocks;
        this.requirement = requirement;
        count = targetCount;
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
        // 每次只挖掘1个方块，避免一次性挖掘过多
        return new MineAndCollectTask(item, 1, blocks, requirement);
    }

    @Override
    protected void onResourceStop(AltoClef mod, Task interruptTask) {
        // 任务停止时的清理
    }

    @Override
    protected boolean isEqualResource(ResourceTask other) {
        if (other instanceof CollectBlockByOneTask task) {
            return task.count == count && task.item.equals(item) && Arrays.stream(task.blocks).allMatch(block -> Arrays.stream(blocks).toList().contains(block));
        }
        return false;
    }

    @Override
    protected String toDebugStringName() {
        return "收集 "+item;
    }


    /**
     * 收集圆石任务
     * 用于收集圆石，可通过挖掘石头或现有圆石获得
     */
    public static class CollectCobblestoneTask extends CollectBlockByOneTask {

        public CollectCobblestoneTask(int targetCount) {
            super(Items.COBBLESTONE, new Block[]{Blocks.STONE, Blocks.COBBLESTONE}, MiningRequirement.WOOD, targetCount);
        }
    }

    /**
     * 收集深板岩圆石任务
     * 用于收集深板岩圆石，可通过挖掘深板岩或现有深板岩圆石获得
     */
    public static class CollectCobbledDeepslateTask extends CollectBlockByOneTask {

        public CollectCobbledDeepslateTask(int targetCount) {
            super(Items.COBBLED_DEEPSLATE, new Block[]{Blocks.DEEPSLATE, Blocks.COBBLED_DEEPSLATE}, MiningRequirement.WOOD, targetCount);
        }
    }

    /**
     * 收集末地石任务
     * 用于收集末地石，只能通过挖掘末地石获得
     */
    public static class CollectEndStoneTask extends CollectBlockByOneTask {

        public CollectEndStoneTask(int targetCount) {
            super(Items.END_STONE, new Block[]{Blocks.END_STONE}, MiningRequirement.WOOD, targetCount);
        }
    }

}
