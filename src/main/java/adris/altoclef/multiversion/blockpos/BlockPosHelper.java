package adris.altoclef.multiversion.blockpos;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

/**
 * 方块位置帮助器
 * 为 Minecraft 1.16.5 及更早版本提供方块位置操作的兼容性方法
 */
public class BlockPosHelper {

    //#if MC <= 11605
    //$$ /**
    //$$  * 向 Vec3i 位置添加偏移量
    //$$  * 
    //$$  * @param blockPos 原始位置
    //$$  * @param x X轴偏移量
    //$$  * @param y Y轴偏移量  
    //$$  * @param z Z轴偏移量
    //$$  * @return 添加偏移后的新位置，如果所有偏移量都为0则返回原位置
    //$$  */
    //$$ public static Vec3i add(Vec3i blockPos, int x, int y, int z) {
    //$$     return x == 0 && y == 0 && z == 0 ? blockPos : new Vec3i(blockPos.getX() + x, blockPos.getY() + y, blockPos.getZ() + z);
    //$$ }
    //$$ 
    //$$ /**
    //$$  * 向 BlockPos 位置添加偏移量
    //$$  * 
    //$$  * @param blockPos 原始位置
    //$$  * @param x X轴偏移量
    //$$  * @param y Y轴偏移量
    //$$  * @param z Z轴偏移量  
    //$$  * @return 添加偏移后的新位置，如果所有偏移量都为0则返回原位置
    //$$  */
    //$$ public static BlockPos add(BlockPos blockPos, int x, int y, int z) {
    //$$     return x == 0 && y == 0 && z == 0 ? blockPos : new BlockPos(blockPos.getX() + x, blockPos.getY() + y, blockPos.getZ() + z);
    //$$ }
    //#endif


}
