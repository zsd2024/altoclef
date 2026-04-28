package adris.altoclef.util.progresscheck;

import net.minecraft.util.math.Vec3d;

/**
 * 距离进度检查器，用于监控实体移动距离是否达到预期目标
 */
public class DistanceProgressChecker implements IProgressChecker<Vec3d> {

    private final IProgressChecker<Double> distanceChecker;
    private final boolean reduceDistance;
    private Vec3d start;
    private Vec3d prevPos;

    /**
     * 构造距离进度检查器
     * 
     * @param distanceChecker 底层的距离检查器
     * @param reduceDistance 是否减少距离（用于接近目标的情况）
     */
    public DistanceProgressChecker(IProgressChecker<Double> distanceChecker, boolean reduceDistance) {
        this.distanceChecker = distanceChecker;
        this.reduceDistance = reduceDistance;
        if (reduceDistance) {
            this.distanceChecker.setProgress(Double.NEGATIVE_INFINITY);
        }
        reset();
    }

    /**
     * 构造距离进度检查器
     * 
     * @param timeout 超时时间
     * @param minDistanceToMake 最小需要移动的距离
     * @param reduceDistance 是否减少距离（用于接近目标的情况）
     */
    public DistanceProgressChecker(double timeout, double minDistanceToMake, boolean reduceDistance) {
        this(new LinearProgressChecker(timeout, minDistanceToMake), reduceDistance);
    }

    /**
     * 构造距离进度检查器（默认不减少距离）
     * 
     * @param timeout 超时时间
     * @param minDistanceToMake 最小需要移动的距离
     */
    public DistanceProgressChecker(double timeout, double minDistanceToMake) {
        this(timeout, minDistanceToMake, false);
    }

    @Override
    public void setProgress(Vec3d position) {
        if (start == null) {
            start = position;
            return;
        }
        double delta = position.distanceTo(start);
        // 如果我们想要减少距离，则对距离进行惩罚
        if (reduceDistance) delta *= -1;
        prevPos = position;
        distanceChecker.setProgress(delta);
    }

    @Override
    public boolean failed() {
        return distanceChecker.failed();
    }

    @Override
    public void reset() {
        start = null;//_prevPos;
        distanceChecker.setProgress(0.0);
        distanceChecker.reset();
    }
}
