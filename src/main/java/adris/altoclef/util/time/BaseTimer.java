package adris.altoclef.util.time;

/**
 * 基础计时器抽象类
 * 提供通用的计时功能，用于跟踪时间间隔和判断是否超时
 * 子类需要实现currentTime()方法来提供当前时间
 */
public abstract class BaseTimer {
    /** 上次重置的时间戳 */
    private double prevTime = 0;
    /** 计时间隔（秒） */
    private double interval;

    /**
     * 构造函数
     * 
     * @param intervalSeconds 计时间隔（秒）
     */
    public BaseTimer(double intervalSeconds) {
        interval = intervalSeconds;
    }

    /**
     * 获取自上次重置以来经过的时间
     * 
     * @return 经过的时间（秒）
     */
    public double getDuration() {
        return currentTime() - prevTime;
    }

    /**
     * 设置计时间隔
     * 
     * @param interval 新的计时间隔（秒）
     */
    public void setInterval(double interval) {
        this.interval = interval;
    }

    /**
     * 判断是否已超时
     * 
     * @return 如果经过的时间大于设定的间隔则返回true，否则返回false
     */
    public boolean elapsed() {
        return getDuration() > interval;
    }

    /**
     * 重置计时器
     * 将上次重置时间设置为当前时间
     */
    public void reset() {
        prevTime = currentTime();
    }

    /**
     * 强制计时器超时
     * 将上次重置时间设置为0，确保下次elapsed()调用返回true
     */
    public void forceElapse() {
        prevTime = 0;
    }

    /**
     * 获取当前时间（抽象方法）
     * 子类必须实现此方法以提供当前时间值
     * 
     * @return 当前时间（秒）
     */
    protected abstract double currentTime();

    /**
     * 强制设置上次重置时间
     * 
     * @param toSet 要设置的时间值
     */
    protected void setPrevTimeForce(double toSet) {
        prevTime = toSet;
    }

    /**
     * 获取上次重置时间
     * 
     * @return 上次重置的时间戳
     */
    protected double getPrevTime() {
        return prevTime;
    }
}
