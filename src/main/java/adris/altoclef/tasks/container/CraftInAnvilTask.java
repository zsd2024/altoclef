package adris.altoclef.tasks.container;

import adris.altoclef.AltoClef;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.ItemTarget;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import org.apache.commons.lang3.NotImplementedException;

/**
 * 铁砧合成任务
 * （注意：此功能尚未实现）
 */
public class CraftInAnvilTask extends DoStuffInContainerTask {
    public CraftInAnvilTask() {
        super(new Block[]{Blocks.ANVIL, Blocks.CHIPPED_ANVIL, Blocks.DAMAGED_ANVIL}, new ItemTarget("铁砧"));
    }

    @Override
    protected boolean isSubTaskEqual(DoStuffInContainerTask other) {
        throw new NotImplementedException("铁砧功能尚未实现，抱歉");
    }

    @Override
    protected boolean isContainerOpen(AltoClef mod) {
        throw new NotImplementedException("铁砧功能尚未实现，抱歉");
    }

    @Override
    protected Task containerSubTask(AltoClef mod) {
        throw new NotImplementedException("铁砧功能尚未实现，抱歉");
    }

    @Override
    protected double getCostToMakeNew(AltoClef mod) {
        throw new NotImplementedException("铁砧功能尚未实现，抱歉");
    }
}
