package adris.altoclef.util.serialization;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.util.Collection;

/**
 * 抽象向量序列化器
 * 用于将向量对象序列化为JSON字符串格式（如"x,y,z"）
 */
public abstract class AbstractVectorSerializer<T> extends StdSerializer<T> {
    /**
     * 默认构造函数
     */
    protected AbstractVectorSerializer() {
        this(null);
    }

    /**
     * 带类型参数的构造函数
     * @param vc 要序列化的向量类类型
     */
    protected AbstractVectorSerializer(Class<T> vc) {
        super(vc);
    }

    /**
     * 获取向量对象的各个部分字符串表示
     * @param value 要序列化的向量对象
     * @return 各部分的字符串集合
     */
    protected abstract Collection<String> getParts(T value);

    @Override
    public void serialize(T value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        Collection<String> parts = getParts(value);
        // 将向量的各个部分用逗号连接成字符串
        gen.writeString(String.join(",", parts));
    }
}

    protected AbstractVectorSerializer(Class<T> vc) {
        super(vc);
    }

    protected abstract Collection<String> getParts(T value);

    @Override
    public void serialize(T value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        Collection<String> parts = getParts(value);
        gen.writeString(String.join(",", parts));
    }
}
