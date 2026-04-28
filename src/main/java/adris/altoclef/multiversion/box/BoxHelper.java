package adris.altoclef.multiversion.box;

import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

/**
 * 包围盒帮助器
 * 为 Minecraft 1.16.5 及更早版本提供包围盒创建的兼容性方法
 */
public class BoxHelper {

    //#if MC <= 11605
    //$$ /**
    //$$  * 根据中心点和尺寸创建包围盒
    //$$  * 
    //$$  * @param center 中心点坐标
    //$$  * @param dx X轴方向尺寸
    //$$  * @param dy Y轴方向尺寸
    //$$  * @param dz Z轴方向尺寸
    //$$  * @return 创建的包围盒
    //$$  */
    //$$ public static Box of(Vec3d center, double dx, double dy, double dz) {
    //$$     return new Box(center.x - dx / 2.0, center.y - dy / 2.0, center.z - dz / 2.0, center.x + dx / 2.0, center.y + dy / 2.0, center.z + dz / 2.0);
    //$$ }
    //#endif

}
