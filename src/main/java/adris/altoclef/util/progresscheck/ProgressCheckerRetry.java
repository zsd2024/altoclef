package adris.altoclef.util.progresscheck;

/**
 * 进度检查器重试包装器
 * 一个可以在真正失败前允许失败几次的进度检查器。
 * 包装另一个进度检查器，当被包装的检查器失败时，会重试指定次数。
 */
public class ProgressCheckerRetry<T> implements IProgressChecker<T> {

    /** 被包装的子检查器 */
    private final IProgressChecker<T> subChecker;
    /** 允许的最大失败尝试次数 */
    private final int allowedAttempts;

    /** 当前失败计数 */
    private int failCount;

    /**
     * 构造进度检查器重试包装器
     * @param subChecker 被包装的子检查器
     * @param allowedAttempts 允许的最大失败尝试次数
     */
    public ProgressCheckerRetry(IProgressChecker<T> subChecker, int allowedAttempts) {
        this.subChecker = subChecker;
        this.allowedAttempts = allowedAttempts;
    }

    @Override
    public void setProgress(T progress) {
        subChecker.setProgress(progress);

        // 如果子检查器失败，则增加失败计数并重置子检查器进行重试。
        if (subChecker.failed()) {
            failCount++;
            subChecker.reset();
        }
    }

    @Override
    public boolean failed() {
        // 当失败次数达到或超过允许的最大尝试次数时，才真正失败
        return failCount >= allowedAttempts;
    }

    @Override
    public void reset() {
        subChecker.reset();
        failCount = 0;
    }
}
