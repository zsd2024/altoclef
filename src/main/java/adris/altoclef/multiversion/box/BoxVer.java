package adris.altoclef.multiversion.box;

import adris.altoclef.multiversion.Pattern;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

/**
 * 包围盒版本适配器
 * 提供不同 Minecraft 版本之间包围盒创建方法的兼容层
 */
public class BoxVer {

    /**
     * 根据中心点和偏移量创建包围盒
     * 
     * @param center 中心点坐标
     * @param x X轴偏移量
     * @param y Y轴偏移量
     * @param z Z轴偏移量
     * @return 创建的包围盒实例
     */
    @Pattern
    public Box of(Vec3d center, double x, double y, double z) {
       //#if MC >= 11701
       return Box.of(center, x, y, z);
       //#else
       //$$ return adris.altoclef.multiversion.box.BoxHelper.of(center, x, y, z);
       //#endif
    }
}
