package adris.altoclef.util.time;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.mixins.ClientConnectionAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.ClientConnection;

/**
 * 游戏计时器类
 * 基于游戏刻（ticks）进行计时，适用于Minecraft游戏内的时间计算
 * 继承自BaseTimer，提供游戏特定的时间测量功能
 */
public class TimerGame extends BaseTimer {

    /** 上一次的游戏连接对象 */
    private ClientConnection lastConnection;

    /**
     * 构造函数
     * 
     * @param intervalSeconds 计时间隔（秒）
     */
    public TimerGame(double intervalSeconds) {
        super(intervalSeconds);
    }

    /**
     * 获取指定连接的游戏时间（秒）
     * 
     * @param connection 游戏连接对象
     * @return 游戏时间（秒），如果连接为空则返回0
     */
    private static double getTime(ClientConnection connection) {
        if (connection == null) return 0;
        return (double) ((ClientConnectionAccessor) connection).getTicks() / 20.0;
    }

    @Override
    protected double currentTime() {
        if (!AltoClef.inGame()) {
            Debug.logError("在非游戏状态下运行游戏计时器。");
            return 0;
        }
        // 如果我们更换了连接，游戏时间也会被重置。在这种情况下，需要调整我们的时间以反映这种变化。
        ClientConnection currentConnection = null;
        if (MinecraftClient.getInstance().getNetworkHandler() != null) {
            currentConnection = MinecraftClient.getInstance().getNetworkHandler().getConnection();
        }
        if (currentConnection != lastConnection) {
            if (lastConnection != null) {
                double prevTimeTotal = getTime(lastConnection);
                Debug.logInternal("(TimerGame: 检测到新连接，偏移 " + prevTimeTotal + " 秒)");
                setPrevTimeForce(getPrevTime() - prevTimeTotal);
            }
            lastConnection = currentConnection;
        }
        // 使用游戏刻进行计时。20TPS是正常值，如果变慢也没关系。
        // 在这里添加"mod"参数会在整个代码库中造成混乱。不会这样做。
        return getTime(currentConnection);
    }
}
