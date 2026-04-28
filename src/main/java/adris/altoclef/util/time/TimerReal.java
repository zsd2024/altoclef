package adris.altoclef.util.time;

/**
 * 真实时间计时器
 * 基于系统当前时间（System.currentTimeMillis()）实现的计时器
 * 用于处理基于真实世界时间的计时功能
 */
public class TimerReal extends BaseTimer {
    /**
     * 构造函数
     * 
     * @param intervalSeconds 计时间隔（秒）
     */
    public TimerReal(double intervalSeconds) {
        super(intervalSeconds);
    }

    @Override
    /**
     * 获取当前真实时间（基于系统时间）
     * 
     * @return 当前系统时间（秒）
     */
    protected double currentTime() {
        return (double) System.currentTimeMillis() / 1000.0;
    }
}
