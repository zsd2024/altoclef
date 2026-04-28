package adris.altoclef.util.serialization;

import com.fasterxml.jackson.core.JsonToken;
import net.minecraft.util.math.Vec3d;

import java.util.List;

/**
 * 三维向量反序列化器
 * 用于将JSON格式的三维向量数据反序列化为Minecraft Vec3d对象
 */
public class Vec3dDeserializer extends AbstractVectorDeserializer<Vec3d, Double> {
    @Override
    /**
     * 获取向量类型名称
     * 
     * @return 类型名称 "Vec3d"
     */
    protected String getTypeName() {
        return "Vec3d";
    }

    @Override
    /**
     * 获取向量分量名称数组
     * 注意：这里只返回两个分量"x"和"y"，但实际上Vec3d有三个分量(x, y, z)
     * 第三个分量在deserializeFromUnits方法中通过索引2获取
     * 
     * @return 分量名称数组
     */
    protected String[] getComponents() {
        return new String[]{"x", "y"};
    }

    @Override
    /**
     * 解析单个单位值
     * 将字符串转换为Double类型
     * 
     * @param unit 要解析的字符串
     * @return 解析后的Double值
     * @throws Exception 解析异常
     */
    protected Double parseUnit(String unit) throws Exception {
        return Double.parseDouble(unit);
    }

    @Override
    /**
     * 从单位值列表创建Vec3d对象
     * 
     * @param units 包含x, y, z三个分量的列表
     * @return 创建的Vec3d对象
     */
    protected Vec3d deserializeFromUnits(List<Double> units) {
        return new Vec3d(units.get(0), units.get(1), units.get(2));
    }

    @Override
    /**
     * 验证JSON token是否为有效的单位值
     * 
     * @param token 要验证的JSON token
     * @return 如果token是整数或浮点数则返回true，否则返回false
     */
    protected boolean isUnitTokenValid(JsonToken token) {
        return token == JsonToken.VALUE_NUMBER_INT || token == JsonToken.VALUE_NUMBER_FLOAT;
    }

}
