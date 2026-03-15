package adris.altoclef.tasks.examples;

import adris.altoclef.AltoClef;
import adris.altoclef.TaskCatalogue;
import adris.altoclef.tasks.construction.PlaceBlockTask;
import adris.altoclef.tasks.movement.GetToBlockTask;
import adris.altoclef.tasksystem.Task;
import net.minecraft.block.Blocks;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;

/**
 * 示例任务 - 演示如何创建一个任务，该任务会获取一定数量的石镐，然后在指定位置放置圆石
 */
public class ExampleTask extends Task {

    // 要获取的石镐数量
    private final int numberOfStonePickaxesToGrab;
    // 放置圆石的位置
    private final BlockPos whereToPlaceCobblestone;

    public ExampleTask(int numberOfStonePickaxesToGrab, BlockPos whereToPlaceCobblestone) {
        this.numberOfStonePickaxesToGrab = numberOfStonePickaxesToGrab;
        this.whereToPlaceCobblestone = whereToPlaceCobblestone;
    }

    @Override
    protected void onStart() {
        AltoClef mod = AltoClef.getInstance();

        // 将当前行为推入堆栈并保护圆石不被丢弃
        mod.getBehaviour().push();
        mod.getBehaviour().addProtectedItems(Items.COBBLESTONE);
    }

    @Override
    protected Task onTick() {

        /*
         * 获取X把石镐
         * 确保我们有一个方块
         * 然后，放置方块。
         */
        AltoClef mod = AltoClef.getInstance();

        // 如果石镐数量不足，则获取石镐
        if (mod.getItemStorage().getItemCount(Items.STONE_PICKAXE) < numberOfStonePickaxesToGrab) {
            return TaskCatalogue.getItemTask(Items.STONE_PICKAXE, numberOfStonePickaxesToGrab);
        }

        // 如果没有圆石，则获取圆石
        if (!mod.getItemStorage().hasItem(Items.COBBLESTONE)) {
            return TaskCatalogue.getItemTask(Items.COBBLESTONE, 1);
        }

        // 检查目标区块是否已加载
        if (mod.getChunkTracker().isChunkLoaded(whereToPlaceCobblestone)) {
            // 如果目标位置不是圆石，则放置圆石
            if (mod.getWorld().getBlockState(whereToPlaceCobblestone).getBlock() != Blocks.COBBLESTONE) {
                return new PlaceBlockTask(whereToPlaceCobblestone, Blocks.COBBLESTONE); ///new PlaceStructureBlockTask(_whereToPlaceCobblestone);
            }
            return null;
        } else {
            // 如果区块未加载，则前往目标位置
            return new GetToBlockTask(whereToPlaceCobblestone);
        }
    }

    @Override
    protected void onStop(Task interruptTask) {
        // 任务停止时弹出行为堆栈
        AltoClef.getInstance().getBehaviour().pop();
    }

    @Override
    public boolean isFinished() {
        AltoClef mod = AltoClef.getInstance();

        // 检查是否已获取足够数量的石镐且目标位置已放置圆石
        return mod.getItemStorage().getItemCount(Items.STONE_PICKAXE) >= numberOfStonePickaxesToGrab &&
                mod.getWorld().getBlockState(whereToPlaceCobblestone).getBlock() == Blocks.COBBLESTONE;
    }

    @Override
    protected boolean isEqual(Task other) {
        if (other instanceof ExampleTask task) {
            // 比较石镐数量和放置位置是否相同
            return task.numberOfStonePickaxesToGrab == numberOfStonePickaxesToGrab
                    && task.whereToPlaceCobblestone.equals(whereToPlaceCobblestone);
        }
        return false;
    }

    @Override
    protected String toDebugString() {
        return "示例任务";
    }
}
