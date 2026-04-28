package adris.altoclef.multiversion.blockpos;

import adris.altoclef.multiversion.Pattern;
import net.minecraft.util.math.*;
import adris.altoclef.multiversion.blockpos.BlockPosHelper;

/**
 * 方块位置版本适配器
 * 提供跨 Minecraft 版本的方块位置操作兼容性方法
 */
public class BlockPosVer {


    /**
     * 从 Position 对象创建向下取整的 BlockPos
     * 
     * @param pos 位置对象
     * @return 向下取整后的方块位置
     */
    public static BlockPos ofFloored(Position pos) {
        return new BlockPos(MathHelper.floor(pos.getX()), MathHelper.floor(pos.getY()), MathHelper.floor(pos.getZ()));
    }


    /**
     * 计算方块位置到指定位置的平方距离
     * 
     * @param pos 方块位置
     * @param obj 目标位置
     * @return 平方距离
     */
    public static double getSquaredDistance(BlockPos pos, Position obj) {
        //#if MC >= 11802
        return pos.getSquaredDistance(obj);
        //#else
        //$$ return pos.getSquaredDistance(obj.getX(), obj.getY(), obj.getZ(), true);
        //#endif
    }

    /**
     * 向北移动一个单位（兼容不同 Minecraft 版本）
     * 
     * @param blockPos 原始位置
     * @return 向北移动后的位置
     */
    @Pattern
    private static Vec3i north(Vec3i blockPos) {
        //#if MC >= 11701
        return blockPos.north();
        //#else
        //$$ return blockPos.offset(Direction.NORTH, 1);
        //#endif
    }

    /**
     * 向北移动指定数量的单位（兼容不同 Minecraft 版本）
     * 
     * @param blockPos 原始位置
     * @param amount 移动单位数量
     * @return 向北移动后的位置
     */
    @Pattern
    private static Vec3i north(Vec3i blockPos, int amount) {
        //#if MC >= 11701
        return blockPos.north(amount);
        //#else
        //$$ return blockPos.offset(Direction.NORTH, amount);
        //#endif
    }

    /**
     * 向东移动一个单位（兼容不同 Minecraft 版本）
     * 
     * @param blockPos 原始位置
     * @return 向东移动后的位置
     */
    @Pattern
    private static Vec3i east(Vec3i blockPos) {
        //#if MC >= 11701
        return blockPos.east();
        //#else
        //$$ return blockPos.offset(Direction.EAST, 1);
        //#endif
    }

    /**
     * 向东移动指定数量的单位（兼容不同 Minecraft 版本）
     * 
     * @param blockPos 原始位置
     * @param amount 移动单位数量
     * @return 向东移动后的位置
     */
    @Pattern
    private static Vec3i east(Vec3i blockPos, int amount) {
        //#if MC >= 11701
        return blockPos.east(amount);
        //#else
        //$$ return blockPos.offset(Direction.EAST, amount);
        //#endif
    }

    /**
     * 向西移动一个单位（兼容不同 Minecraft 版本）
     * 
     * @param blockPos 原始位置
     * @return 向西移动后的位置
     */
    @Pattern
    private static Vec3i west(Vec3i blockPos) {
        //#if MC >= 11701
        return blockPos.west();
        //#else
        //$$ return blockPos.offset(Direction.WEST, 1);
        //#endif
    }

    /**
     * 向西移动指定数量的单位（兼容不同 Minecraft 版本）
     * 
     * @param blockPos 原始位置
     * @param amount 移动单位数量
     * @return 向西移动后的位置
     */
    @Pattern
    private static Vec3i west(Vec3i blockPos, int amount) {
        //#if MC >= 11701
        return blockPos.west(amount);
        //#else
        //$$ return blockPos.offset(Direction.WEST, amount);
        //#endif
    }

    /**
     * 向南移动一个单位（兼容不同 Minecraft 版本）
     * 
     * @param blockPos 原始位置
     * @return 向南移动后的位置
     */
    @Pattern
    private static Vec3i south(Vec3i blockPos) {
        //#if MC >= 11701
        return blockPos.south();
        //#else
        //$$ return blockPos.offset(Direction.SOUTH, 1);
        //#endif
    }

    /**
     * 向南移动指定数量的单位（兼容不同 Minecraft 版本）
     * 
     * @param blockPos 原始位置
     * @param amount 移动单位数量
     * @return 向南移动后的位置
     */
    @Pattern
    private static Vec3i south(Vec3i blockPos, int amount) {
        //#if MC >= 11701
        return blockPos.south(amount);
        //#else
        //$$ return blockPos.offset(Direction.SOUTH, amount);
        //#endif
    }

    /**
     * 添加坐标偏移量（兼容不同 Minecraft 版本）
     * 
     * @param blockPos 原始位置
     * @param x X轴偏移量
     * @param y Y轴偏移量
     * @param z Z轴偏移量
     * @return 添加偏移后的位置
     */
    @Pattern
    private static Vec3i add(Vec3i blockPos, int x, int y, int z) {
        //#if MC >= 11701
        return blockPos.add(x,y,z);
        //#else
        //$$ return adris.altoclef.multiversion.blockpos.BlockPosHelper.add(blockPos,x,y,z);
        //#endif
    }


}
