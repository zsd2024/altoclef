package adris.altoclef.multiversion.world;

import net.minecraft.world.World;
import net.minecraft.util.math.BlockPos;

/**
 * 世界帮助器
 * 
 * 此类为旧版本 Minecraft (1.16.5 及更早版本) 提供世界高度相关的方法实现。
 * 在这些版本中，世界高度是固定的，因此可以直接返回常量值。
 */
public class WorldHelper {

    //#if MC <= 11605
    //$$ /**
    //$$  * 获取世界的顶部 Y 坐标
    //$$  * 
    //$$  * @param world 世界实例（在此版本中未使用）
    //$$  * @return 顶部 Y 坐标，固定为 255
    //$$  */
    //$$ public static int getTopY(World world) {
    //$$     return 255;
    //$$ }
    //$$ 
    //$$ /**
    //$$  * 获取世界的底部 Y 坐标
    //$$  * 
    //$$  * @param world 世界实例（在此版本中未使用）
    //$$  * @return 底部 Y 坐标，固定为 0
    //$$  */
    //$$ public static int getBottomY(World world) {
    //$$     return 0;
    //$$ }
    //$$
    //$$ /**
    //$$  * 检查指定位置是否超出世界高度限制
    //$$  * 
    //$$  * @param world 世界实例
    //$$  * @param pos 位置
    //$$  * @return 如果位置超出高度限制返回 true，否则返回 false
    //$$  */
    //$$ public static boolean isOutOfHeightLimit(World world,BlockPos pos) {
    //$$      return isOutOfHeightLimit(pos.getY());
    //$$   }
    //$$
    //$$ /**
    //$$  * 检查指定 Y 坐标是否超出世界高度限制
    //$$  * 
    //$$  * @param y Y 坐标
    //$$  * @return 如果 Y 坐标超出高度限制返回 true，否则返回 false
    //$$  */
    //$$ private static boolean isOutOfHeightLimit(int y) {
    //$$      return y < getBottomY(null) || y >= getTopY(null);
    //$$   }
    //#endif

}
