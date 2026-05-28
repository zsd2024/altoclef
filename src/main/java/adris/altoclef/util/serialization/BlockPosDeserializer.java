package adris.altoclef.util.serialization;

import com.fasterxml.jackson.core.JsonToken;
import net.minecraft.util.math.BlockPos;

import java.util.List;

/**
 * 方块位置反序列化器
 * 用于将JSON格式的方块位置数据反序列化为BlockPos对象
 * 支持两种格式：字符串格式（如"x,y,z"）和对象格式（如{"x":1,"y":2,"z":3}）
 */
public class BlockPosDeserializer extends AbstractVectorDeserializer<BlockPos, Integer> {
    @Override
    protected String getTypeName() {
        return "BlockPos";
    }

    @Override
    protected String[] getComponents() {
        return new String[]{"x", "y", "z"};
    }

    @Override
    protected Integer parseUnit(String unit) throws Exception {
        return Integer.parseInt(unit);
    }

    @Override
    protected BlockPos deserializeFromUnits(List<Integer> units) {
        return new BlockPos(units.get(0), units.get(1), units.get(2));
    }

    @Override
    protected boolean isUnitTokenValid(JsonToken token) {
        // 方块位置的组件必须是整数
        return token == JsonToken.VALUE_NUMBER_INT;
    }
}
