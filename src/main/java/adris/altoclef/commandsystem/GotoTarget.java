package adris.altoclef.commandsystem;

import adris.altoclef.util.Dimension;

import java.util.ArrayList;
import java.util.List;

/**
 * 目标位置类
 * 表示机器人需要前往的目标位置，包含坐标、维度和坐标类型信息
 */
public class GotoTarget {
    /** X坐标 */
    private final int x;
    /** Y坐标 */
    private final int y;
    /** Z坐标 */
    private final int z;
    /** 维度 */
    private final Dimension dimension;
    /** 坐标类型 */
    private final GotoTargetCoordType type;

    /**
     * 构造函数
     * @param x X坐标
     * @param y Y坐标
     * @param z Z坐标
     * @param dimension 维度
     * @param type 坐标类型
     */
    public GotoTarget(int x, int y, int z, Dimension dimension, GotoTargetCoordType type) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.dimension = dimension;
        this.type = type;
    }

    /**
     * 获取X坐标
     * @return X坐标值
     */
    public int getX() {
        return x;
    }

    /**
     * 获取Y坐标
     * @return Y坐标值
     */
    public int getY() {
        return y;
    }

    /**
     * 获取Z坐标
     * @return Z坐标值
     */
    public int getZ() {
        return z;
    }

    /**
     * 获取维度
     * @return 维度对象
     */
    public Dimension getDimension() {
        return dimension;
    }

    /**
     * 判断是否指定了维度
     * @return 如果指定了维度返回true，否则返回false
     */
    public boolean hasDimension() {
        return dimension != null;
    }

    /**
     * 获取坐标类型
     * @return 坐标类型枚举值
     */
    public GotoTargetCoordType getType() {
        return type;
    }

    /**
     * 可用的坐标类型组合
     */
    public enum GotoTargetCoordType {
        XYZ, // [x, y, z] 完整三维坐标
        XZ,  // [x, z] 仅X和Z坐标（水平位置）
        Y,   // [y] 仅Y坐标（高度）
        NONE // [] 无坐标
    }
}
