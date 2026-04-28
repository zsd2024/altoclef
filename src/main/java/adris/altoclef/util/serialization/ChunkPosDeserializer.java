package adris.altoclef.util.serialization;

import com.fasterxml.jackson.core.JsonToken;
import net.minecraft.util.math.ChunkPos;

import java.util.List;

/**
 * 区块位置反序列化器
 * 用于将JSON格式的区块位置数据反序列化为ChunkPos对象
 */
public class ChunkPosDeserializer extends AbstractVectorDeserializer<ChunkPos, Integer> {
    @Override
    protected String getTypeName() {
        return "ChunkPos";
    }

    @Override
    protected String[] getComponents() {
        // 返回区块位置的组件名称数组，包含x和z坐标
        return new String[]{"x", "z"};
    }

    @Override
    protected Integer parseUnit(String unit) throws Exception {
        // 将字符串单元解析为整数
        return Integer.parseInt(unit);
    }

    @Override
    protected ChunkPos deserializeFromUnits(List<Integer> units) {
        // 从整数单元列表创建ChunkPos对象
        return new ChunkPos(units.get(0), units.get(1));
    }

    @Override
    protected boolean isUnitTokenValid(JsonToken unitToken) {
        // 检查JSON令牌是否有效（此处返回false，表示不使用此验证方法）
        return false;
    }
}
