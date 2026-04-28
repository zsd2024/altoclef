package adris.altoclef.util.serialization;

import net.minecraft.util.math.ChunkPos;

import java.util.Arrays;
import java.util.Collection;

/**
 * 区块位置序列化器
 * 用于将ChunkPos对象序列化为JSON格式的字符串
 */
public class ChunkPosSerializer extends AbstractVectorSerializer<ChunkPos> {
    @Override
    protected Collection<String> getParts(ChunkPos value) {
        // 获取区块位置的各个部分（x和z坐标）作为字符串集合
        return Arrays.asList("" + value.x, "" + value.z);
    }
}
