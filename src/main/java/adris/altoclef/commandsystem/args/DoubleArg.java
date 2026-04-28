package adris.altoclef.commandsystem.args;

import adris.altoclef.commandsystem.exception.BadCommandSyntaxException;
import adris.altoclef.commandsystem.exception.CommandException;
import adris.altoclef.commandsystem.StringReader;

import java.util.stream.Stream;

/**
 * 双精度浮点数参数 - 用于解析双精度浮点数类型的命令参数
 */
public class DoubleArg extends Arg<Double> {

    /**
     * 构造函数 - 无默认值
     * @param name 参数名称
     */
    public DoubleArg(String name) {
        super(name);
    }

    /**
     * 构造函数 - 带默认值
     * @param name 参数名称
     * @param defaultValue 默认值
     */
    public DoubleArg(String name, Double defaultValue) {
        super(name, defaultValue);
    }

    /**
     * 构造函数 - 带默认值和显示控制
     * @param name 参数名称
     * @param defaultValue 默认值
     * @param showDefault 是否在帮助信息中显示默认值
     */
    public DoubleArg(String name, Double defaultValue, boolean showDefault) {
        super(name, defaultValue, showDefault);
    }


    /**
     * 解析双精度浮点数参数
     * @param parser 字符串读取器
     * @return 解析后的双精度浮点数值
     * @throws CommandException 命令异常
     */
    public static Double parse(StringReader parser) throws CommandException {
        String value = parser.next();
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            throw new BadCommandSyntaxException("无法将 '"+ value + "' 解析为双精度浮点数");
        }
    }

    @Override
    public Stream<String> getSuggestions(StringReader reader) {
        return Stream.empty();
    }

    @Override
    protected StringParser<Double> getParser() {
        return DoubleArg::parse;
    }

    @Override
    public String getTypeName() {
        return "双精度浮点数";
    }

    @Override
    public Class<Double> getType() {
        return Double.class;
    }

}

    public DoubleArg(String name, Double defaultValue) {
        super(name, defaultValue);
    }

    public DoubleArg(String name, Double defaultValue, boolean showDefault) {
        super(name, defaultValue, showDefault);
    }


    public static Double parse(StringReader parser) throws CommandException {
        String value = parser.next();
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            throw new BadCommandSyntaxException("Failed to parse '"+ value + "' into a Double");
        }
    }

    @Override
    public Stream<String> getSuggestions(StringReader reader) {
        return Stream.empty();
    }

    @Override
    protected StringParser<Double> getParser() {
        return DoubleArg::parse;
    }

    @Override
    public String getTypeName() {
        return "Double";
    }

    @Override
    public Class<Double> getType() {
        return Double.class;
    }

}
