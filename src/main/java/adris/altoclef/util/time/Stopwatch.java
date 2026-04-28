package adris.altoclef.util.time;

/**
 * 秒表计时器类
 * 用于测量经过的时间，支持开始计时和获取当前经过的时间
 */
public class Stopwatch {

    /** 是否正在运行计时 */
    boolean running = false;
    /** 开始计时的时间戳（秒） */
    private double startTime = 0;

    /**
     * 获取当前系统时间（秒）
     * 
     * @return 当前系统时间（秒）
     */
    private static double currentTime() {
        return (double) System.currentTimeMillis() / 1000.0;
    }

    /**
     * 开始计时
     * 将开始时间设置为当前时间，并标记为运行状态
     */
    public void begin() {
        startTime = currentTime();
        running = true;
    }

    /**
     * 获取当前经过的时间
     * 
     * @return 如果正在运行则返回经过的时间（秒），否则返回0
     */
    public double time() {
        if (!running) return 0;
        return currentTime() - startTime;
    }
}
