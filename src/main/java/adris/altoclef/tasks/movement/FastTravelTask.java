package adris.altoclef.tasks.movement;

import adris.altoclef.AltoClef;
import adris.altoclef.TaskCatalogue;
import adris.altoclef.tasks.construction.compound.ConstructNetherPortalObsidianTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.Dimension;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.helpers.WorldHelper;
import adris.altoclef.util.time.TimerGame;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.DeathScreen;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import org.apache.commons.lang3.NotImplementedException;

import java.util.Objects;
import java.util.Optional;

/**
 * 快速传送任务 - 在主世界和下界之间快速传送以到达目标位置
 * 通过在下界建造传送门来快速缩短长距离旅行的时间
 */
@SuppressWarnings("ConstantConditions")
public class FastTravelTask extends Task {

    // 当我们在下界的理想传送门建造位置的此范围内时，认为我们"足够接近"
    private static final double IN_NETHER_CLOSE_ENOUGH_THRESHOLD = 15;

    // 如果缺少传送门材料，则收集打火石+钢和钻石镐。否则直接步行。
    private final boolean collectPortalMaterialsIfAbsent;
    private final BlockPos target; // 目标位置
    private final Integer threshold; // 阈值
    // 如果我们在"足够接近"阈值后无法移动到精确中心，则放弃并放置传送门
    private final TimerGame _attemptToMoveToIdealNetherCoordinateTimeout = new TimerGame(15);
    private boolean _forceOverworldWalking; // 强制在主世界步行
    private Task _goToOverworldTask; // 前往主世界任务

    /**
     * 创建快速传送任务实例
     *
     * @param overworldTarget                传送后的主世界目标位置
     * @param threshold                      快速传送与步行的阈值
     * @param collectPortalMaterialsIfAbsent 如果我们没有（10个黑曜石或钻石镐）和（打火石或火焰弹），则收集这些物品。否则全程步行。
     */
    public FastTravelTask(BlockPos overworldTarget, Integer threshold, boolean collectPortalMaterialsIfAbsent) {
        target = overworldTarget;
        this.threshold = null;
        this.collectPortalMaterialsIfAbsent = collectPortalMaterialsIfAbsent;
    }

    /**
     * 创建快速传送任务实例
     *
     * @param overworldTarget                传送后的主世界目标位置
     *                                       机器人将根据设置中的阈值使用下界传送。
     * @param collectPortalMaterialsIfAbsent 如果我们没有（10个黑曜石或钻石镐）和（打火石或火焰弹），则收集这些物品。否则全程步行。
     */
    public FastTravelTask(BlockPos overworldTarget, boolean collectPortalMaterialsIfAbsent) {
        this(overworldTarget, null, collectPortalMaterialsIfAbsent);
    }

    @Override
    protected void onStart() {
        // 计算下界的目标位置（主世界坐标除以8）
        BlockPos netherTarget = new BlockPos(target.getX() / 8, target.getY(), target.getZ() / 8);

        // 创建前往主世界任务，进入下界传送门
        _goToOverworldTask = new EnterNetherPortalTask(new ConstructNetherPortalObsidianTask(), Dimension.OVERWORLD,
                checkPos -> WorldHelper.inRangeXZ(checkPos,netherTarget,7));
    }

    @Override
    protected Task onTick() {
        AltoClef mod = AltoClef.getInstance();

        // 计算下界的目标位置
        BlockPos netherTarget = new BlockPos(target.getX() / 8, target.getY(), target.getZ() / 8);

        // 检查是否有建造传送门的材料
        boolean canBuildPortal = mod.getItemStorage().hasItem(Items.DIAMOND_PICKAXE) || mod.getItemStorage().getItemCount(Items.OBSIDIAN) >= 10;
        // 检查是否有点燃传送门的材料
        boolean canLightPortal = mod.getItemStorage().hasItem(Items.FLINT_AND_STEEL, Items.FIRE_CHARGE);

        // 边缘情况：如果我们在下界死亡，停止强制步行，重新开始
        if (MinecraftClient.getInstance().currentScreen instanceof DeathScreen) {
            _forceOverworldWalking = false;
        }

        switch (WorldHelper.getCurrentDimension()) {
            case OVERWORLD -> {
                _attemptToMoveToIdealNetherCoordinateTimeout.reset();
                // 步行：当强制步行或接近目标时
                if (_forceOverworldWalking || WorldHelper.inRangeXZ(mod.getPlayer(), target, getOverworldThreshold(mod))) {
                    _forceOverworldWalking = true;
                    setDebugState("步行：我们距离目标足够近");

                    // 如果发现末地传送门框架，前去传送门
                    if (mod.getBlockScanner().anyFound(Blocks.END_PORTAL_FRAME)) {
                        setDebugState("前往传送门");
                        return new GetToBlockTask(mod.getBlockScanner().getNearestBlock(Blocks.END_PORTAL_FRAME).get());
                    }
                    return new GetToBlockTask(target);
                }
                // 收集材料
                if (!canBuildPortal || !canLightPortal) {
                    if (collectPortalMaterialsIfAbsent) {
                        setDebugState("收集传送门建造材料");
                        if (!canBuildPortal)
                            return TaskCatalogue.getItemTask(Items.DIAMOND_PICKAXE, 1);
                        if (!canLightPortal)
                            return TaskCatalogue.getItemTask(Items.FLINT_AND_STEEL, 1);
                    } else {
                        setDebugState("步行：我们没有传送门建造材料");
                        return new GetToBlockTask(target);
                    }
                }
                // 前往下界
                return new DefaultGoToDimensionTask(Dimension.NETHER);
            }
            case NETHER -> {

                if (!_forceOverworldWalking) {
                    // 步行一段时间后，当我们回到主世界时，再次开始步行。
                    Optional<BlockPos> portalEntrance = mod.getMiscBlockTracker().getLastUsedNetherPortal(Dimension.NETHER);
                    if (portalEntrance.isPresent() && !portalEntrance.get().isWithinDistance(mod.getPlayer().getPos(), 3)) {
                        _forceOverworldWalking = true;
                    }
                }

                // 如果我们要前往主世界，继续前往
                if (_goToOverworldTask.isActive() && !_goToOverworldTask.isFinished()) {
                    setDebugState("返回主世界");

                    return _goToOverworldTask;
                }

                // 如果需要，拾取掉落的物品
                if (mod.getItemStorage().getItemCount(Items.OBSIDIAN) < 10) {
                    setDebugState("确保我们能够建造传送门");
                    return TaskCatalogue.getItemTask(Items.OBSIDIAN, 10);
                }
                if (!canLightPortal && mod.getEntityTracker().itemDropped(Items.FLINT_AND_STEEL, Items.FIRE_CHARGE)) {
                    setDebugState("确保我们能够点燃传送门");
                    return new PickupDroppedItemTask(new ItemTarget(Items.FLINT_AND_STEEL, Items.FIRE_CHARGE), true);
                }

                // 如果我们足够接近目标XZ坐标并且路径安全可取消
                if (WorldHelper.inRangeXZ(mod.getPlayer(), netherTarget, IN_NETHER_CLOSE_ENOUGH_THRESHOLD) &&
                        mod.getClientBaritone().getPathingBehavior().isSafeToCancel()) {
                    // 如果我们精确到达目标XZ坐标或尝试时间过长
                    if ((mod.getPlayer().getBlockX() == netherTarget.getX() && mod.getPlayer().getBlockZ() == netherTarget.getZ()) || _attemptToMoveToIdealNetherCoordinateTimeout.elapsed()) {
                        return _goToOverworldTask;
                    }
                }

                _attemptToMoveToIdealNetherCoordinateTimeout.reset();
                setDebugState("前往理想坐标");
                return new GetToXZTask(netherTarget.getX(), netherTarget.getZ());
            }
            case END -> {
                setDebugState("为什么在这里运行这个？");
                return new DefaultGoToDimensionTask(Dimension.OVERWORLD);
            }
        }
        throw new NotImplementedException("未实现维度: " + WorldHelper.getCurrentDimension());
        /*
            如果我们在主世界：
                如果我们在旅行阈值外且未强制步行：
                    如果我们需要收集额外的打火石和钢（或火焰弹）和钻石镐：
                        收集
                    否则
                        步行
                否则：
                    强制步行
                    步行
                前往下界
            如果我们在下界：
                如果我们正在建造传送门：
                    继续建造
                如果我们掉落了钻石镐，捡起它
                如果我们没有打火石和钢和火焰弹且掉落了任何，捡起它
                如果我们掉落了黑曜石且少于10个，捡起它
                如果我们足够接近计算的XZ坐标：
                    运行
                    建造传送门
                前往计算的下界坐标
            否则（我们在末地，极不可能但也要考虑）
                前往主世界
          */
    }

        switch (WorldHelper.getCurrentDimension()) {
            case OVERWORLD -> {
                _attemptToMoveToIdealNetherCoordinateTimeout.reset();
                // WALK
                if (_forceOverworldWalking || WorldHelper.inRangeXZ(mod.getPlayer(), target, getOverworldThreshold(mod))) {
                    _forceOverworldWalking = true;
                    setDebugState("Walking: We're close enough to our target");

                    if (mod.getBlockScanner().anyFound(Blocks.END_PORTAL_FRAME)) {
                        setDebugState("Walking to portal");
                        return new GetToBlockTask(mod.getBlockScanner().getNearestBlock(Blocks.END_PORTAL_FRAME).get());
                    }
                    return new GetToBlockTask(target);
                }
                // SUPPLIES
                if (!canBuildPortal || !canLightPortal) {
                    if (collectPortalMaterialsIfAbsent) {
                        setDebugState("Collecting portal building materials");
                        if (!canBuildPortal)
                            return TaskCatalogue.getItemTask(Items.DIAMOND_PICKAXE, 1);
                        if (!canLightPortal)
                            return TaskCatalogue.getItemTask(Items.FLINT_AND_STEEL, 1);
                    } else {
                        setDebugState("Walking: We don't have portal building materials");
                        return new GetToBlockTask(target);
                    }
                }
                // GO TO NETHER
                return new DefaultGoToDimensionTask(Dimension.NETHER);
            }
            case NETHER -> {

                if (!_forceOverworldWalking) {
                    // After walking a bit, the moment we go back into the overworld, walk again.
                    Optional<BlockPos> portalEntrance = mod.getMiscBlockTracker().getLastUsedNetherPortal(Dimension.NETHER);
                    if (portalEntrance.isPresent() && !portalEntrance.get().isWithinDistance(mod.getPlayer().getPos(), 3)) {
                        _forceOverworldWalking = true;
                    }
                }

                // If we're going to the overworld, keep going.
                if (_goToOverworldTask.isActive() && !_goToOverworldTask.isFinished()) {
                    setDebugState("Going back to overworld");

                    return _goToOverworldTask;
                }

                // PICKUP DROPPED STUFF if we need it
                if (mod.getItemStorage().getItemCount(Items.OBSIDIAN) < 10) {
                    setDebugState("Making sure we can build our portal");
                    return TaskCatalogue.getItemTask(Items.OBSIDIAN, 10);
                }
                if (!canLightPortal && mod.getEntityTracker().itemDropped(Items.FLINT_AND_STEEL, Items.FIRE_CHARGE)) {
                    setDebugState("Making sure we can light our portal");
                    return new PickupDroppedItemTask(new ItemTarget(Items.FLINT_AND_STEEL, Items.FIRE_CHARGE), true);
                }

                if (WorldHelper.inRangeXZ(mod.getPlayer(), netherTarget, IN_NETHER_CLOSE_ENOUGH_THRESHOLD) &&
                        mod.getClientBaritone().getPathingBehavior().isSafeToCancel()) {
                    // If we're precisely at our target XZ or if we've tried long enough
                    if ((mod.getPlayer().getBlockX() == netherTarget.getX() && mod.getPlayer().getBlockZ() == netherTarget.getZ()) || _attemptToMoveToIdealNetherCoordinateTimeout.elapsed()) {
                        return _goToOverworldTask;
                    }
                }

                _attemptToMoveToIdealNetherCoordinateTimeout.reset();
                setDebugState("Traveling to ideal coordinates");
                return new GetToXZTask(netherTarget.getX(), netherTarget.getZ());
            }
            case END -> {
                setDebugState("Why are you running this here?");
                return new DefaultGoToDimensionTask(Dimension.OVERWORLD);
            }
        }
        throw new NotImplementedException("Unimplemented dimension: " + WorldHelper.getCurrentDimension());
        /*
            if we're in the overworld:
                if we're outside of TRAVEL_THRESHHOLD and NOT forcefully walking:
                    if we need to collect extra flint & steel (or fire charge) AND a diamond pickaxe:
                        collect
                    else
                        walk
                else:
                    force walk
                    walk
                GO TO NETHER
            if we're in the nether:

                if we were building the portal:
                    keep building

                if we drop a diamond pickaxe, pick it up
                if we have no flint and steel & fire charge and dropped any, pick it up
                if we dropped obsidian and have less than 10, pick it up

                if we're close enough to our calculated XZ coordinates:
                    run
                    build portal
                go to calculated nether coordinates
            else (we're in the end, highly unlikely but may as well)
                go to overworld
         */

    }

    /**
     * 获取主世界阈值
     * @param mod AltoClef实例
     * @return 阈值距离
     */
    private int getOverworldThreshold(AltoClef mod) {
        int threshold;
        //noinspection ReplaceNullCheck
        if (this.threshold == null) {
            threshold = mod.getModSettings().getNetherFastTravelWalkingRange();
        } else {
            threshold = this.threshold;
        }
        // 我们永远不应该离开下界而仍然在步行区域外。
        threshold = Math.max((int) (IN_NETHER_CLOSE_ENOUGH_THRESHOLD * 8) + 32, threshold);
        // 少于16个方块的下界传送门指向相同的传送门（128主世界），所以确保我们不会重新做工作。只是冗余检查
        threshold = Math.max(16 * 8, threshold);
        return threshold;
    }

    @Override
    protected void onStop(Task interruptTask) {

    }

    @Override
    protected boolean isEqual(Task other) {
        if (other instanceof FastTravelTask task) {
            return task.target.equals(target) && task.collectPortalMaterialsIfAbsent == collectPortalMaterialsIfAbsent && Objects.equals(task.threshold, threshold);
        }
        return false;
    }

    @Override
    protected String toDebugString() {
        return "快速传送到 " + target.toShortString();
    }
}
