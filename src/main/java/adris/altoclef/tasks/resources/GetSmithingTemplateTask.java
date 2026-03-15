package adris.altoclef.tasks.resources;

import adris.altoclef.AltoClef;
import adris.altoclef.multiversion.versionedfields.Blocks;
import adris.altoclef.multiversion.versionedfields.Items;
import adris.altoclef.tasks.ResourceTask;
import adris.altoclef.tasks.construction.DestroyBlockTask;
import adris.altoclef.tasks.movement.DefaultGoToDimensionTask;
import adris.altoclef.tasks.movement.SearchChunkForBlockTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.Dimension;
import adris.altoclef.util.helpers.WorldHelper;
import net.minecraft.util.math.BlockPos;

/**
 * 获取锻造模板任务
 * 用于获取下界合金升级锻造模板，需要前往下界寻找堡垒遗迹中的箱子
 */
public class GetSmithingTemplateTask extends ResourceTask {

    // 黑石搜索任务，用于定位堡垒遗迹区域
    private final Task _searcher = new SearchChunkForBlockTask(Blocks.BLACKSTONE);
    private final int _count;
    // 箱子位置
    private BlockPos _chestloc = null;

    public GetSmithingTemplateTask(int count) {
        super(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE, count);
        _count = count;
    }

    @Override
    protected void onResourceStart(AltoClef mod) {
        // 任务开始时的初始化工作
    }

    @Override
    protected Task onResourceTick(AltoClef mod) {
        // 必须前往下界
        if (WorldHelper.getCurrentDimension() != Dimension.NETHER) {
            setDebugState("前往下界");
            return new DefaultGoToDimensionTask(Dimension.NETHER);
        }
        //if (_bastionloc != null && !mod.getChunkTracker().isChunkLoaded(_bastionloc)) {
        //    Debug.logMessage("Bastion at " + _bastionloc + " too far away. Re-searching.");
        //    _bastionloc = null;
        // }
        if (_chestloc == null) {
            // 寻找已知的箱子位置
            for (BlockPos pos : mod.getBlockScanner().getKnownLocations(Blocks.CHEST)) {
                if (WorldHelper.isInteractableBlock(pos)) {
                    _chestloc = pos;
                    break;
                }
            }
        }
        if (_chestloc != null) {
            //if (!_chestloc.isWithinDistance(mod.getPlayer().getPos(), 150)) {
            setDebugState("破坏箱子"); // TODO: 使其检查箱子而不是破坏
            if (WorldHelper.isInteractableBlock(_chestloc)) {
                return new DestroyBlockTask(_chestloc);
            } else {
                _chestloc = null;
                // 寻找新的可交互箱子
                for (BlockPos pos : mod.getBlockScanner().getKnownLocations(Blocks.CHEST)) {
                    if (WorldHelper.isInteractableBlock(pos)) {
                        _chestloc = pos;
                        break;
                    }
                }
            }
            //}
        }
        setDebugState("搜索/在堡垒遗迹周围移动");
        return _searcher;
    }

    @Override
    protected void onResourceStop(AltoClef mod, Task interruptTask) {
        // 任务结束时的清理工作
    }

    @Override
    protected boolean isEqualResource(ResourceTask other) {
        return other instanceof GetSmithingTemplateTask;
    }

    @Override
    protected String toDebugStringName() {
        return "收集 " + _count + " 个锻造模板";
    }

    @Override
    protected boolean shouldAvoidPickingUp(AltoClef mod) {
        return false;
    }
}
