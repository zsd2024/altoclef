package adris.altoclef.util.progresscheck;

import adris.altoclef.util.time.TimerGame;

/**
 * 线性进度检查器
 * 简单的进度检查器，要求我们始终取得进展。
 * 在每个超时周期内，必须至少取得最小进度值，否则视为失败。
 */
public class LinearProgressChecker implements IProgressChecker<Double> {

    /** 最小进度阈值，低于此值视为未取得足够进展 */
    private final double minProgress;
    /** 计时器，用于跟踪超时时间 */
    private final TimerGame timer;

    /** 上一次记录的进度值 */
    private double lastProgress;
    /** 当前进度值 */
    private double currentProgress;

    /** 是否为第一次设置进度 */
    private boolean first;

    /** 是否已失败 */
    private boolean failed;

    /**
     * 构造线性进度检查器
     * @param timeout 超时时间（秒）
     * @param minProgress 最小进度阈值
     */
    public LinearProgressChecker(double timeout, double minProgress) {
        this.minProgress = minProgress;
        timer = new TimerGame(timeout);
        reset();
    }

    @Override
    public void setProgress(Double progress) {
        currentProgress = progress;
        if (first) {
            // 第一次设置进度，记录为初始值
            lastProgress = progress;
            first = false;
        }
        if (timer.elapsed()) {
            // 计时器超时，检查是否取得了足够进展
            double improvement = progress - lastProgress;
            if (improvement < minProgress) {
                // 进展不足，标记为失败
                failed = true;
            }
            first = false;
            timer.reset();
            lastProgress = progress;
        }
    }

    @Override
    public boolean failed() {
        return failed;
    }

    @Override
    public void reset() {
        //_first = true;
        failed = false;
        timer.reset();
        first = true;
    }
}
