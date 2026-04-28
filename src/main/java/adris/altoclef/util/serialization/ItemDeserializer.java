package adris.altoclef.util.serialization;

import adris.altoclef.Debug;
import adris.altoclef.util.helpers.ItemHelper;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 物品反序列化器
 * 用于将JSON格式的物品数据反序列化为Minecraft物品对象列表
 */
public class ItemDeserializer extends StdDeserializer<Object> {
    /**
     * 默认构造函数
     */
    public ItemDeserializer() {
        this(null);
    }

    /**
     * 带参数的构造函数
     * 
     * @param vc 要反序列化的对象类型
     */
    public ItemDeserializer(Class<Object> vc) {
        super(vc);
    }

    @Override
    /**
     * 反序列化方法
     * 将JSON解析器中的数据转换为物品对象列表
     * 
     * @param p JSON解析器
     * @param ctxt 反序列化上下文
     * @return 物品对象列表
     * @throws IOException 输入输出异常
     * @throws JsonProcessingException JSON处理异常
     */
    public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        List<Item> result = new ArrayList<>();

        // 验证JSON格式是否为数组开始
        if (p.getCurrentToken() != JsonToken.START_ARRAY) {
            throw new JsonParseException(p, "期望数组开始");
        }
        // 遍历数组中的每个元素
        while (p.nextToken() != JsonToken.END_ARRAY) {
            Item item = null;
            // 如果当前token是整数值，表示使用旧的原始ID格式
            if (p.getCurrentToken() == JsonToken.VALUE_NUMBER_INT) {
                // 旧的原始ID（不推荐使用）
                int rawId = p.getIntValue();
                item = Item.byRawId(rawId);
            } else {
                // 使用翻译键（推荐方式）
                String itemKey = p.getText();
                // 清理物品名称
                itemKey = ItemHelper.trimItemName(itemKey);
                Identifier identifier = Identifier.of(itemKey);
                // 检查注册表中是否存在该物品
                if (Registries.ITEM.containsId(identifier)) {
                    item = Registries.ITEM.get(identifier);
                } else {
                    // 记录无效物品名称的警告
                    Debug.logWarning("无效的物品名称:" + itemKey + " 位置 " + p.getCurrentLocation().toString());
                }
            }
            // 如果成功获取到物品，则添加到结果列表中
            if (item != null) {
                result.add(item);
            }
        }

        return result;
    }
}
