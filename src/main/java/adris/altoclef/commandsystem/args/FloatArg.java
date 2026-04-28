package adris.altoclef.commandsystem.args;

import adris.altoclef.commandsystem.exception.BadCommandSyntaxException;
import adris.altoclef.commandsystem.exception.CommandException;
import adris.altoclef.commandsystem.StringReader;

import java.util.stream.Stream;

/**
 * 浮点数参数类
 * 用于解析和处理命令中的浮点数参数
 */
public class FloatArg extends Arg<Float> {

    /**
     * 构造函数
     * @param name 参数名称
     */
    public FloatArg(String name) {
        super(name);
    }

    /**
     * 构造函数
     * @param name 参数名称
     * @param defaultValue 默认值
     */
    public FloatArg(String name, Float defaultValue) {
        super(name, defaultValue);
    }

    /**
     * 构造函数
     * @param name 参数名称
     * @param defaultValue 默认值
     * @param showDefault 是否显示默认值
     */
    public FloatArg(String name, Float defaultValue, boolean showDefault) {
        super(name, defaultValue, showDefault);
    }


    /**
     * 解析浮点数参数
     * @param parser 字符串读取器
     * @return 解析后的浮点数值
     * @throws CommandException 当解析失败时抛出命令异常
     */
    public static Float parse(StringReader parser) throws CommandException {
        String value = parser.next();
        try {
            return Float.parseFloat(value);
        } catch (NumberFormatException e) {
            throw new BadCommandSyntaxException("无法将 '" + value + "' 解析为浮点数");
        }
    }

    @Override
    public Stream<String> getSuggestions(StringReader reader) {
        return Stream.empty();
    }


    @Override
    protected StringParser<Float> getParser() {
        return FloatArg::parse;
    }

    @Override
    public String getTypeName() {
        return "Float";
    }

    @Override
    public Class<Float> getType() {
        return Float.class;
    }
}
