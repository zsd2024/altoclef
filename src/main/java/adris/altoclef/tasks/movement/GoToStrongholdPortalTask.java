package adris.altoclef.tasks.movement;

import adris.altoclef.AltoClef;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.helpers.StorageHelper;
import adris.altoclef.util.helpers.WorldHelper;
import net.minecraft.block.Blocks;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;

/**
 * 前往要塞传送门任务 - 前往要塞并建造末地传送门
 */
public class GoToStrongholdPortalTask extends Task {

    private LocateStrongholdCoordinatesTask _locateCoordsTask; // 定位要塞坐标任务
    private final int _targetEyes; // 目标末影之眼数量
    private final int MINIMUM_EYES = 12; // 最小末影之眼数量
    private BlockPos _strongholdCoordinates; // 要塞坐标

    public GoToStrongholdPortalTask(int targetEyes) {
        _targetEyes = targetEyes;
        _strongholdCoordinates = null;
        _locateCoordsTask = new LocateStrongholdCoordinatesTask(targetEyes);
    }

    @Override
    protected void onStart() {

    }

    @Override
    protected Task onTick() {
        AltoClef mod = AltoClef.getInstance();

        /*
            如果我们不知道要塞在哪里，找出要塞在哪里。
            如果我们知道要塞在哪里，快速传送过去
            如果在那里搜索它
          */
        if (_strongholdCoordinates == null) {
            // 如果有任何屏幕打开，防止卡住
            StorageHelper.closeScreen();

            _strongholdCoordinates = _locateCoordsTask.getStrongholdCoordinates().orElse(null);
            if (_strongholdCoordinates == null) {
                // 如果末影之眼数量少于最小值且有末影之眼掉落，则拾取
                if (mod.getItemStorage().getItemCount(Items.ENDER_EYE) < MINIMUM_EYES && mod.getEntityTracker().itemDropped(Items.ENDER_EYE)) {
                    setDebugState("拾取掉落的末影之眼");
                    return new PickupDroppedItemTask(Items.ENDER_EYE, MINIMUM_EYES);
                }
                setDebugState("三角测量要塞...");
                return _locateCoordsTask;
            }
        }

        // 如果距离要塞坐标小于10且没有找到末地传送门框架，则重新三角测量
        if (mod.getPlayer().getPos().distanceTo(WorldHelper.toVec3d(_strongholdCoordinates)) < 10 && !mod.getBlockScanner().anyFound(Blocks.END_PORTAL_FRAME)) {
            mod.log("三角测量要塞时出现问题... 要么动作被打断，要么第二只眼睛指向了不同的要塞");
            mod.log("我们现在将尝试再次三角测量...");
            _strongholdCoordinates = null;
            _locateCoordsTask = new LocateStrongholdCoordinatesTask(_targetEyes);
            return null;
        }
        // 搜索石砖区块，但在我们漫游时，前往下界
        setDebugState("搜索要塞...");
        /*return new SearchChunkForBlockTask(Blocks.STONE_BRICKS) {
            @Override
            protected Task onTick(AltoClef mod) {
                if (WorldHelper.getCurrentDimension() != Dimension.OVERWORLD) {
                    return getWanderTask(mod);
                }
                return super.onTick(mod);
            }

            @Override
            protected Task getWanderTask(AltoClef mod) {
                return new FastTravelTask(_strongholdCoordinates, 300, true);
            }
        };*/
        return new FastTravelTask(_strongholdCoordinates, 300, true);
    }

    @Override
    protected void onStop(Task interruptTask) {

    }

    @Override
    protected boolean isEqual(Task other) {
        return other instanceof GoToStrongholdPortalTask;
    }

    @Override
    protected String toDebugString() {
        return "定位要塞";
    }
}