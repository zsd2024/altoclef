package adris.altoclef.tasks.resources;

import adris.altoclef.AltoClef;
import adris.altoclef.BotBehaviour;
import adris.altoclef.TaskCatalogue;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.MiningRequirement;
import adris.altoclef.util.helpers.ItemHelper;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.Items;

/**
 * 剪切并收集方块任务
 * 用于使用剪刀剪切方块并收集掉落物品（如树叶、羊毛等）
 */
public class ShearAndCollectBlockTask extends MineAndCollectTask {

    public ShearAndCollectBlockTask(ItemTarget[] itemTargets, Block... blocksToMine) {
        super(itemTargets, blocksToMine, MiningRequirement.HAND);
    }

    public ShearAndCollectBlockTask(Item[] items, int count, Block... blocksToMine) {
        this(new ItemTarget[]{new ItemTarget(items, count)}, blocksToMine);
    }

    public ShearAndCollectBlockTask(Item item, int count, Block... blocksToMine) {
        this(new Item[]{item}, count, blocksToMine);
    }

    @Override
    protected void onStart() {
        BotBehaviour botBehaviour = AltoClef.getInstance().getBehaviour();

        botBehaviour.push();
        // 强制使用剪刀工具来剪切可剪切的方块
        botBehaviour.forceUseTool((blockState, itemStack) ->
                itemStack.getItem() == Items.SHEARS && ItemHelper.areShearsEffective(blockState.getBlock())
        );
        super.onStart();
    }

    @Override
    protected void onStop(Task interruptTask) {
        AltoClef.getInstance().getBehaviour().pop();
        super.onStop(interruptTask);
    }

    @Override
    protected Task onResourceTick(AltoClef mod) {
        // 检查是否有剪刀，如果没有则先获取剪刀
        if (!mod.getItemStorage().hasItem(Items.SHEARS)) {
            return TaskCatalogue.getItemTask(Items.SHEARS, 1);
        }
        return super.onResourceTick(mod);
    }
}
