package adris.altoclef.util.serialization;

import net.minecraft.util.math.BlockPos;

import java.util.Arrays;
import java.util.Collection;

/**
 * 方块位置序列化器
 * 用于将BlockPos对象序列化为JSON字符串格式（如"x,y,z"）
 */
public class BlockPosSerializer extends AbstractVectorSerializer<BlockPos> {
    @Override
    protected Collection<String> getParts(BlockPos value) {
        // 返回方块位置的x、y、z坐标作为字符串列表
        return Arrays.asList("" + value.getX(), "" + value.getY(), "" + value.getZ());
    }
}
}
