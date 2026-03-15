package adris.altoclef.tasks.movement;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.TaskCatalogue;
import adris.altoclef.multiversion.blockpos.BlockPosVer;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.Dimension;
import adris.altoclef.util.helpers.LookHelper;
import adris.altoclef.util.helpers.WorldHelper;
import adris.altoclef.util.time.TimerGame;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EyeOfEnderEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

import java.util.List;
import java.util.Optional;

/**
 * 定位要塞坐标任务 - 通过投掷末影之眼来定位要塞坐标
 */
public class LocateStrongholdCoordinatesTask extends Task {

    private static final int EYE_RETHROW_DISTANCE = 10; // 重新投掷前的目标距离到要塞猜测

    private static final int SECOND_EYE_THROW_DISTANCE = 30; // 第一次投掷和第二次投掷之间的目标距离

    private final int _targetEyes; // 目标末影之眼数量
    private final int _minimumEyes; // 最小末影之眼数量
    private final TimerGame _throwTimer = new TimerGame(5); // 投掷计时器
    private LocateStrongholdCoordinatesTask.EyeDirection _cachedEyeDirection = null; // 缓存的末影之眼方向
    private LocateStrongholdCoordinatesTask.EyeDirection _cachedEyeDirection2 = null; // 缓存的第二个末影之眼方向
    private Entity _currentThrownEye = null; // 当前投掷的末影之眼
    private Vec3i _strongholdEstimatePos = null; // 要塞估计位置

    public LocateStrongholdCoordinatesTask(int targetEyes, int minimumEyes) {
        _targetEyes = targetEyes;
        _minimumEyes = minimumEyes;
    }

    public LocateStrongholdCoordinatesTask(int targetEyes) {
        this(targetEyes, 12);
    }


    /**
     * 计算两个方向线的交点
     * @param start1 第一条线的起始点
     * @param direction1 第一条线的方向
     * @param start2 第二条线的起始点
     * @param direction2 第二条线的方向
     * @return 交点的XZ坐标
     */
    @SuppressWarnings("UnnecessaryLocalVariable")
    static Vec3i calculateIntersection(Vec3d start1, Vec3d direction1, Vec3d start2, Vec3d direction2) {
        Vec3d s1 = start1;
        Vec3d s2 = start2;
        Vec3d d1 = direction1;
        Vec3d d2 = direction2;
        // 求解 s1 + d1 * t1 = s2 + d2 * t2
        double t2 = ((d1.z * s2.x) - (d1.z * s1.x) - (d1.x * s2.z) + (d1.x * s1.z)) / ((d1.x * d2.z) - (d1.z * d2.x));
        BlockPos blockPos = BlockPosVer.ofFloored(start2.add(direction2.multiply(t2)));
        return new Vec3i(blockPos.getX(), 0, blockPos.getZ());
    }

    @Override
    protected void onStart() {

    }

    /**
     * 检查是否正在搜索
     * @return 是否正在搜索
     */
    public boolean isSearching() {
        return _cachedEyeDirection != null;
    }

    @Override
    protected Task onTick() {
        AltoClef mod = AltoClef.getInstance();

        // 如果不在主世界，前往主世界
        if (WorldHelper.getCurrentDimension() != Dimension.OVERWORLD) {
            setDebugState("前往主世界");
            return new DefaultGoToDimensionTask(Dimension.OVERWORLD);
        }

        // 如果需要/想要，则拾取末影之眼
        if (mod.getItemStorage().getItemCount(Items.ENDER_EYE) < _minimumEyes && mod.getEntityTracker().itemDropped(Items.ENDER_EYE) &&
                !mod.getEntityTracker().entityFound(EyeOfEnderEntity.class)) {
            setDebugState("拾取掉落的末影之眼。");
            return new PickupDroppedItemTask(Items.ENDER_EYE, _targetEyes);
        }

        // 处理投掷的末影之眼
        if (mod.getEntityTracker().entityFound(EyeOfEnderEntity.class)) {
            if (_currentThrownEye == null || !_currentThrownEye.isAlive()) {
                Debug.logMessage("新末影之眼方向");
                Debug.logMessage(_currentThrownEye==null?"空":"不存活");
                List<EyeOfEnderEntity> enderEyes = mod.getEntityTracker().getTrackedEntities(EyeOfEnderEntity.class);
                if (!enderEyes.isEmpty()) {
                    for (EyeOfEnderEntity enderEye : enderEyes) {
                        _currentThrownEye = enderEye;
                    }
                }
                if (_cachedEyeDirection2 != null) {
                    _cachedEyeDirection = null;
                    _cachedEyeDirection2 = null;
                } else if (_cachedEyeDirection == null) {
                    _cachedEyeDirection = new LocateStrongholdCoordinatesTask.EyeDirection(_currentThrownEye.getPos());
                } else {
                    _cachedEyeDirection2 = new LocateStrongholdCoordinatesTask.EyeDirection(_currentThrownEye.getPos());
                }
            }
            if (_cachedEyeDirection2 != null) {
                _cachedEyeDirection2.updateEyePos(_currentThrownEye.getPos());
            } else if (_cachedEyeDirection != null) {
                _cachedEyeDirection.updateEyePos(_currentThrownEye.getPos());
            }

            if (mod.getEntityTracker().getClosestEntity(EyeOfEnderEntity.class).isPresent() &&
                    !mod.getClientBaritone().getPathingBehavior().isPathing()) {
                LookHelper.lookAt(mod,
                        mod.getEntityTracker().getClosestEntity(EyeOfEnderEntity.class).get().getEyePos());
            }

            setDebugState("等待末影之眼移动。");
            return null;
        }

        // 计算要塞位置
        if (_cachedEyeDirection2 != null && !mod.getEntityTracker().entityFound(EyeOfEnderEntity.class) && _strongholdEstimatePos == null) {
            if (_cachedEyeDirection2.getAngle() >= _cachedEyeDirection.getAngle()) {
                Debug.logMessage("第二只眼睛投掷在错误位置，或指向不同的要塞。重新投掷");
                _cachedEyeDirection = _cachedEyeDirection2;
                _cachedEyeDirection2 = null;
            } else {
                Vec3d throwOrigin = _cachedEyeDirection.getOrigin();
                Vec3d throwOrigin2 = _cachedEyeDirection2.getOrigin();
                Vec3d throwDelta = _cachedEyeDirection.getDelta();
                Vec3d throwDelta2 = _cachedEyeDirection2.getDelta();


                _strongholdEstimatePos = calculateIntersection(throwOrigin, throwDelta, throwOrigin2, throwDelta2); // 要塞估计
                Debug.logMessage("要塞在 " + (int) _strongholdEstimatePos.getX() + ", " + (int) _strongholdEstimatePos.getZ() + " (" + (int) mod.getPlayer().getPos().distanceTo(Vec3d.of(_strongholdEstimatePos)) + " 方块远)");
            }
        }


        // 到达估计位置后重新投掷末影之眼以获得更准确的要塞位置估计
        if (_strongholdEstimatePos != null) {
            if (((mod.getPlayer().getPos().distanceTo(Vec3d.of(_strongholdEstimatePos)) < EYE_RETHROW_DISTANCE) && WorldHelper.getCurrentDimension() == Dimension.OVERWORLD)) {
                _strongholdEstimatePos = null;
                _cachedEyeDirection = null;
                _cachedEyeDirection2 = null;
            }
        }


        // 由于没有末影之眼信息，投掷末影之眼
        if (!mod.getEntityTracker().entityFound(EyeOfEnderEntity.class) && _strongholdEstimatePos == null) {
            if (WorldHelper.getCurrentDimension() == Dimension.NETHER) {
                setDebugState("前往主世界。");
                return new DefaultGoToDimensionTask(Dimension.OVERWORLD);
            }
            if (!mod.getItemStorage().hasItem(Items.ENDER_EYE)) {
                setDebugState("收集末影之眼。");
                return TaskCatalogue.getItemTask(Items.ENDER_EYE, 1);
            }

            // 首先到达适当的投掷高度
            if (_cachedEyeDirection == null) {
                setDebugState("投掷第一只眼睛。");
            } else {
                setDebugState("投掷第二只眼睛。");
                double sqDist = mod.getPlayer().squaredDistanceTo(_cachedEyeDirection.getOrigin());
                // 如果第一只眼睛已投掷，垂直于眼睛方向移动，直到距离足够远
                if (sqDist < SECOND_EYE_THROW_DISTANCE * SECOND_EYE_THROW_DISTANCE && _cachedEyeDirection != null) {
                    return new GoInDirectionXZTask(_cachedEyeDirection.getOrigin(), _cachedEyeDirection.getDelta().rotateY((float) (Math.PI / 2)), 1);
                }
            }
            // 投掷它
            if (mod.getSlotHandler().forceEquipItem(Items.ENDER_EYE)) {
                assert MinecraftClient.getInstance().interactionManager != null;
                if (_throwTimer.elapsed()) {
                    if (LookHelper.tryAvoidingInteractable(mod)) {
                        MinecraftClient.getInstance().interactionManager.interactItem(mod.getPlayer(),Hand.MAIN_HAND);
                        //MinecraftClient.getInstance().options.keyUse.setPressed(true);
                        _throwTimer.reset();
                    }
                } else {
                    MinecraftClient.getInstance().interactionManager.stopUsingItem(mod.getPlayer());
                    //MinecraftClient.getInstance().options.keyUse.setPressed(false);
                }
            } else {
                Debug.logWarning("未能装备末影之眼投掷。");
            }
            return null;
        } else if (_cachedEyeDirection != null && !_cachedEyeDirection.hasDelta() ||
                _cachedEyeDirection2 != null && !_cachedEyeDirection2.hasDelta()) {
            setDebugState("等待投掷的末影之眼出现...");
            return null;
        }
        return null;
    }

    @Override
    protected void onStop(Task interruptTask) {
    }

    /**
     * 获取要塞坐标
     * @return 要塞坐标（如果存在）
     */
    public Optional<BlockPos> getStrongholdCoordinates() {
        if (_strongholdEstimatePos == null) {
            return Optional.empty();
        }
        return Optional.of(new BlockPos(_strongholdEstimatePos));
    }

    @Override
    protected boolean isEqual(Task other) {
        return other instanceof LocateStrongholdCoordinatesTask;
    }

    @Override
    protected String toDebugString() {
        return "定位要塞坐标";
    }

    @Override
    public boolean isFinished() {
        // 当估计要塞位置不为空时完成
        return _strongholdEstimatePos != null;
    }

    /**
     * 表示我们需要前往要塞的方向
     */
    private static class EyeDirection {
        private final Vec3d _start; // 起始位置
        private Vec3d _end; // 结束位置

        public EyeDirection(Vec3d startPos) {
            _start = startPos;
        }

        public void updateEyePos(Vec3d endPos) {
            _end = endPos;
        }

        public Vec3d getOrigin() {
            return _start;
        }

        public Vec3d getDelta() {
            if (_end == null) return Vec3d.ZERO;
            return _end.subtract(_start);
        }

        public double getAngle() {
            if (_end == null) return 0;
            return Math.atan2(getDelta().getX(), getDelta().getZ());
        }

        @SuppressWarnings("BooleanMethodIsAlwaysInverted")
        public boolean hasDelta() {
            return _end != null && getDelta().lengthSquared() > 0.00001;
        }
    }
}
