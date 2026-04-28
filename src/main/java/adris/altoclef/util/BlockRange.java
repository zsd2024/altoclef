package adris.altoclef.util;

import adris.altoclef.util.helpers.WorldHelper;
import com.fasterxml.jackson.annotation.JsonIgnore;
import net.minecraft.util.math.BlockPos;

import java.util.Objects;

/**
 * 方块范围工具类
 * 表示一个三维空间中的方块范围，用于判断某个位置是否在指定范围内
 */
public class BlockRange {
    /** 范围的起始位置 */
    public BlockPos start;
    /** 范围的结束位置 */
    public BlockPos end;
    /** 范围所在的维度，默认为主世界 */
    public Dimension dimension = Dimension.OVERWORLD;

    // 用于反序列化
    private BlockRange() {
    }

    /**
     * 构造函数
     * @param start 范围的起始位置
     * @param end 范围的结束位置
     * @param dimension 范围所在的维度
     */
    public BlockRange(BlockPos start, BlockPos end, Dimension dimension) {
        this.start = start;
        this.end = end;
        this.dimension = dimension;
    }

    /**
     * 判断指定位置是否在当前范围内（使用当前世界维度）
     * @param pos 要检查的位置
     * @return 如果位置在范围内返回true，否则返回false
     */
    public boolean contains(BlockPos pos) {
        return contains(pos, WorldHelper.getCurrentDimension());
    }

    /**
     * 判断指定位置是否在当前范围内（指定维度）
     * @param pos 要检查的位置
     * @param dimension 要检查的维度
     * @return 如果位置在范围内且维度匹配返回true，否则返回false
     */
    public boolean contains(BlockPos pos, Dimension dimension) {
        if (this.dimension != dimension)
            return false;
        return (start.getX() <= pos.getX() && pos.getX() <= end.getX() &&
                start.getZ() <= pos.getZ() && pos.getZ() <= end.getZ() &&
                start.getY() <= pos.getY() && pos.getY() <= end.getY());
    }

    /**
     * 获取范围的中心位置
     * @return 范围的中心位置
     */
    @JsonIgnore
    public BlockPos getCenter() {
        BlockPos sum = start.add(end);
        return new BlockPos(sum.getX() / 2, sum.getY() / 2, sum.getZ() / 2);
    }

    /**
     * 获取范围的字符串表示
     * @return 范围的字符串表示，格式为"[起始位置 -> 结束位置, (维度)]"
     */
    public String toString() {
        return "[" + start.toShortString() + " -> " + end.toShortString() + ", (" + dimension + ")]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BlockRange that = (BlockRange) o;
        return Objects.equals(start, that.start) && Objects.equals(end, that.end);
    }

    @Override
    public int hashCode() {
        return Objects.hash(start, end);
    }
}
