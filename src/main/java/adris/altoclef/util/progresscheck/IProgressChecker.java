package adris.altoclef.util.progresscheck;

/**
 * 进度检查器接口
 * 用于确定任务/命令在超过阈值时间段内是否没有取得任何进展。
 */
public interface IProgressChecker<T> {
    /**
     * 设置当前进度值
     * @param progress 当前进度值
     */
    void setProgress(T progress);

    /**
     * 检查是否失败（即在规定时间内未取得足够进展）
     * @return 如果失败返回true，否则返回false
     */
    boolean failed();

    /**
     * 重置检查器状态，清除之前的进度记录和失败状态
     */
    void reset();
}
