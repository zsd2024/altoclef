package adris.altoclef.tasks.movement;

import adris.altoclef.AltoClef;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.helpers.WorldHelper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.BiomeKeys;

/**
 * 定位沙漠神庙任务 - 寻找并定位沙漠神庙
 */
public class LocateDesertTempleTask extends Task {

    private BlockPos _finalPos; // 最终位置

    @Override
    protected void onStart() {
    }

    @Override
    protected Task onTick() {
        // 获取沙漠神庙位置
        BlockPos desertTemplePos = WorldHelper.getADesertTemple();
        if (desertTemplePos != null) {
            _finalPos = desertTemplePos.up(14); // 将位置向上移动14格
        }
        if (_finalPos != null) {
            setDebugState("前往找到的沙漠神庙");
            return new GetToBlockTask(_finalPos, false);
        }
        // 在沙漠生物群系中搜索
        return new SearchWithinBiomeTask(BiomeKeys.DESERT);
    }

    @Override
    protected void onStop(Task interruptTask) {
    }

    @Override
    protected boolean isEqual(Task other) {
        return other instanceof LocateDesertTempleTask;
    }

    @Override
    protected String toDebugString() {
        return "寻找神庙";
    }

    @Override
    public boolean isFinished() {
        // 当玩家位置等于最终位置时完成
        return AltoClef.getInstance().getPlayer().getBlockPos().equals(_finalPos);
    }
}
