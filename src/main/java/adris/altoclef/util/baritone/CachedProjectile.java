package adris.altoclef.util.baritone;

import adris.altoclef.util.time.TimerGame;
import net.minecraft.util.math.Vec3d;

import java.lang.reflect.Type;

/**
 * 缓存投射物工具类
 * 用于缓存投射物的位置、速度等信息，并提供缓存管理功能
 */
public class CachedProjectile {
    /** 缓存计时器，2秒后缓存失效 */
    private final TimerGame lastCache = new TimerGame(2);
    /** 投射物速度 */
    public Vec3d velocity;
    /** 投射物位置 */
    public Vec3d position;
    /** 重力值 */
    public double gravity;
    /** 投射物类型 */
    public Type projectileType;
    /** 缓存的命中位置 */
    private Vec3d cachedHit;
    /** 是否持有缓存 */
    private boolean cacheHeld = false;

    /**
     * 获取缓存的命中位置
     * 
     * @return 缓存的命中位置
     */
    public Vec3d getCachedHit() {
        return cachedHit;
    }

    /**
     * 设置缓存命中位置
     * 
     * @param cache 命中位置
     */
    public void setCacheHit(Vec3d cache) {
        cachedHit = cache;
        cacheHeld = true;
        lastCache.reset();
    }

    /**
     * 检查是否需要重新缓存
     * 
     * @return 如果没有缓存或缓存已过期，则返回true
     */
    public boolean needsToRecache() {
        return !cacheHeld || lastCache.elapsed();
    }
}
