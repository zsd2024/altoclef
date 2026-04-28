package adris.altoclef.util.serialization;

import net.minecraft.util.math.Vec3d;

import java.util.Arrays;
import java.util.Collection;

/**
 * 三维向量序列化器
 * 用于将Minecraft Vec3d对象序列化为JSON格式
 */
public class Vec3dSerializer extends AbstractVectorSerializer<Vec3d> {
    @Override
    /**
     * 获取Vec3d对象的各个分量
     * 
     * @param value 要序列化的Vec3d对象
     * @return 包含x, y, z三个分量字符串的集合
     */
    protected Collection<String> getParts(Vec3d value) {
        return Arrays.asList("" + value.getX(), "" + value.getY(), "" + value.getZ());
    }
}
