package adris.altoclef.util.serialization;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.util.*;

/**
 * 抽象向量反序列化器
 * 用于将JSON格式的向量数据反序列化为具体的向量对象
 * 支持两种格式：字符串格式（如"x,y,z"）和对象格式（如{"x":1,"y":2,"z":3}）
 */
public abstract class AbstractVectorDeserializer<T, UnitType> extends StdDeserializer<T> {
    /**
     * 默认构造函数
     */
    protected AbstractVectorDeserializer() {
        this(null);
    }

    /**
     * 带类型参数的构造函数
     * @param vc 要反序列化的向量类类型
     */
    protected AbstractVectorDeserializer(Class<T> vc) {
        super(vc);
    }

    /**
     * 获取向量类型的名称（用于错误消息）
     * @return 向量类型的名称
     */
    protected abstract String getTypeName();

    /**
     * 获取向量的组件名称数组（如["x", "y", "z"]）
     * @return 组件名称数组
     */
    protected abstract String[] getComponents();

    /**
     * 解析单个单位值
     * @param unit 要解析的字符串值
     * @return 解析后的单位类型值
     * @throws Exception 解析失败时抛出异常
     */
    protected abstract UnitType parseUnit(String unit) throws Exception;

    /**
     * 从单位值列表创建向量对象
     * @param units 单位值列表
     * @return 创建的向量对象
     */
    protected abstract T deserializeFromUnits(List<UnitType> units);

    /**
     * 检查JSON token是否为有效的单位值token
     * @param unitToken 要检查的JSON token
     * @return 如果token有效则返回true，否则返回false
     */
    protected abstract boolean isUnitTokenValid(JsonToken unitToken);


    /**
     * 尝试从映射中获取指定键的值，如果不存在则抛出异常
     * @param p JSON解析器
     * @param map 包含组件值的映射
     * @param key 要查找的键
     * @return 找到的值
     * @throws JsonParseException 如果键不存在则抛出此异常
     */
    UnitType trySet(JsonParser p, Map<String, UnitType> map, String key) throws JsonParseException {
        if (map.containsKey(key)) {
            return map.get(key);
        }
        throw new JsonParseException(p, getTypeName() + " 应该包含键 " + key + "，但未找到。");
    }

    /**
     * 尝试解析向量字符串的一部分
     * @param p JSON解析器
     * @param whole 完整的向量字符串
     * @param part 要解析的部分字符串
     * @return 解析后的单位值
     * @throws JsonParseException 如果解析失败则抛出此异常
     */
    UnitType tryParse(JsonParser p, String whole, String part) throws JsonParseException {
        try {
            return parseUnit(part.trim());
        } catch (Exception e) {
            throw new JsonParseException(p, "解析 " + getTypeName() + " 字符串 \"" 
                    + whole + "\" 失败，特别是部分 \"" + part + "\"。");
        }
    }

    @Override
    public T deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String[] neededComponents = getComponents();
        // 处理字符串格式（如"x,y,z"）
        if (p.getCurrentToken() == JsonToken.VALUE_STRING) {
            String bposString = p.getValueAsString();
            String[] parts = bposString.split(",");
            if (parts.length != neededComponents.length) {
                throw new JsonParseException(p, "无效的 " + getTypeName() + " 字符串: \"" + bposString + "\", 必须是 \"" + String.join(",", neededComponents) + "\" 格式。");
            }
            ArrayList<UnitType> resultingUnits = new ArrayList<>();
            for (String part : parts) {
                resultingUnits.add(tryParse(p, bposString, part));
            }
            return deserializeFromUnits(resultingUnits);
        } 
        // 处理对象格式（如{"x":1,"y":2,"z":3}）
        else if (p.getCurrentToken() == JsonToken.START_OBJECT) {
            Map<String, UnitType> parts = new HashMap<>();
            p.nextToken();
            while (p.getCurrentToken() != JsonToken.END_OBJECT) {
                if (p.getCurrentToken() == JsonToken.FIELD_NAME) {
                    p.nextToken();
                    if (!isUnitTokenValid(p.currentToken())) {
                        throw new JsonParseException(p, getTypeName() + " 的token无效。得到: " + p.getCurrentToken());
                    }
                    try {
                        parts.put(p.getCurrentName(), parseUnit(p.getValueAsString()));
                    } catch (Exception e) {
                        throw new JsonParseException(p, "解析组件 " + p.getCurrentName() + " 失败");
                    }
                    p.nextToken();
                } else {
                    throw new JsonParseException(p, "结构无效，期望字段名（如 " + String.join(",", neededComponents) + ")");
                }
            }
            if (parts.size() != neededComponents.length) {
                throw new JsonParseException(p, "期望 [" + String.join(",", neededComponents) + "] 键作为方块位置对象的一部分。得到 " + Arrays.toString(parts.keySet().toArray(String[]::new)));
            }
            ArrayList<UnitType> resultingUnits = new ArrayList<>();
            for (String componentName : neededComponents) {
                resultingUnits.add(trySet(p, parts, componentName));
            }
            return deserializeFromUnits(resultingUnits);
        }
        throw new JsonParseException(p, "无效的token: " + p.getCurrentToken());
    }
}

    protected AbstractVectorDeserializer(Class<T> vc) {
        super(vc);
    }

    protected abstract String getTypeName();

    protected abstract String[] getComponents();

    protected abstract UnitType parseUnit(String unit) throws Exception;

    protected abstract T deserializeFromUnits(List<UnitType> units);

    protected abstract boolean isUnitTokenValid(JsonToken unitToken);


    UnitType trySet(JsonParser p, Map<String, UnitType> map, String key) throws JsonParseException {
        if (map.containsKey(key)) {
            return map.get(key);
        }
        throw new JsonParseException(p, getTypeName() + " should have key for " + key + " key, but one was not found.");
    }

    UnitType tryParse(JsonParser p, String whole, String part) throws JsonParseException {
        try {
            return parseUnit(part.trim());
        } catch (Exception e) {
            throw new JsonParseException(p, "Failed to parse " + getTypeName() + " string \""
                    + whole + "\", specificaly part \"" + part + "\".");
        }
    }

    @Override
    public T deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String[] neededComponents = getComponents();
        if (p.getCurrentToken() == JsonToken.VALUE_STRING) {
            String bposString = p.getValueAsString();
            String[] parts = bposString.split(",");
            if (parts.length != neededComponents.length) {
                throw new JsonParseException(p, "Invalid " + getTypeName() + " string: \"" + bposString + "\", must be in form \"" + String.join(",", neededComponents) + "\".");
            }
            ArrayList<UnitType> resultingUnits = new ArrayList<>();
            for (String part : parts) {
                resultingUnits.add(tryParse(p, bposString, part));
            }
            return deserializeFromUnits(resultingUnits);
        } else if (p.getCurrentToken() == JsonToken.START_OBJECT) {
            Map<String, UnitType> parts = new HashMap<>();
            p.nextToken();
            while (p.getCurrentToken() != JsonToken.END_OBJECT) {
                if (p.getCurrentToken() == JsonToken.FIELD_NAME) {
                    p.nextToken();
                    if (!isUnitTokenValid(p.currentToken())) {
                        throw new JsonParseException(p, "Invalid token for " + getTypeName() + ". Got: " + p.getCurrentToken());
                    }
                    try {
                        parts.put(p.getCurrentName(), parseUnit(p.getValueAsString()));
                    } catch (Exception e) {
                        throw new JsonParseException(p, "Failed to parse unit " + p.getCurrentName());
                    }
                    p.nextToken();
                } else {
                    throw new JsonParseException(p, "Invalid structure, expected field name (like " + String.join(",", neededComponents) + ")");
                }
            }
            if (parts.size() != neededComponents.length) {
                throw new JsonParseException(p, "Expected [" + String.join(",", neededComponents) + "] keys to be part of a blockpos object. Got " + Arrays.toString(parts.keySet().toArray(String[]::new)));
            }
            ArrayList<UnitType> resultingUnits = new ArrayList<>();
            for (String componentName : neededComponents) {
                resultingUnits.add(trySet(p, parts, componentName));
            }
            return deserializeFromUnits(resultingUnits);
        }
        throw new JsonParseException(p, "Invalid token: " + p.getCurrentToken());
    }
}
