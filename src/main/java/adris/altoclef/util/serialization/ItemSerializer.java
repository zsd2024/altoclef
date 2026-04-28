package adris.altoclef.util.serialization;

import adris.altoclef.util.helpers.ItemHelper;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import net.minecraft.item.Item;

import java.io.IOException;
import java.util.List;

/**
 * 物品序列化器
 * 用于将Minecraft物品对象列表序列化为JSON格式
 */
public class ItemSerializer extends StdSerializer<Object> {
    /**
     * 默认构造函数
     */
    public ItemSerializer() {
        this(null);
    }

    /**
     * 带参数的构造函数
     * 
     * @param vc 要序列化的对象类型
     */
    public ItemSerializer(Class<Object> vc) {
        super(vc);
    }

    @Override
    /**
     * 序列化方法
     * 将物品对象列表转换为JSON格式
     * 
     * @param value 要序列化的物品列表
     * @param gen JSON生成器
     * @param provider 序列化提供者
     * @throws IOException 输入输出异常
     */
    public void serialize(Object value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        List<Item> items = (List<Item>) value;
        // 开始写入JSON数组
        gen.writeStartArray();
        // 遍历物品列表，将每个物品的翻译键写入JSON
        for (Item item : items) {
            // 获取并清理物品的翻译键
            String key = ItemHelper.trimItemName(item.getTranslationKey());
            gen.writeString(key);
        }
        // 结束JSON数组
        gen.writeEndArray();
    }
}
