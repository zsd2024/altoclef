package adris.altoclef.commandsystem.args;

import adris.altoclef.commandsystem.exception.BadCommandSyntaxException;
import adris.altoclef.commandsystem.exception.CommandException;
import adris.altoclef.commandsystem.StringReader;

import java.util.stream.Stream;

/**
 * 整数参数类
 * 用于解析和处理命令中的整数参数
 */
public class IntArg extends Arg<Integer> {

    /**
     * 构造函数
     * @param name 参数名称
     */
    public IntArg(String name) {
        super(name);
    }

    /**
     * 构造函数
     * @param name 参数名称
     * @param defaultValue 默认值
     */
    public IntArg(String name, Integer defaultValue) {
        super(name, defaultValue);
    }

    /**
     * 构造函数
     * @param name 参数名称
     * @param defaultValue 默认值
     * @param showDefault 是否显示默认值
     */
    public IntArg(String name, Integer defaultValue, boolean showDefault) {
        super(name, defaultValue, showDefault);
    }

    /**
     * 解析整数参数
     * @param parser 字符串读取器
     * @return 解析后的整数值
     * @throws CommandException 当解析失败时抛出命令异常
     */
    public static Integer parse(StringReader parser) throws CommandException {
        String value = parser.next();
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new BadCommandSyntaxException("无法将 '" + value + "' 解析为整数");
        }
    }

    @Override
    public Stream<String> getSuggestions(StringReader reader) {
        return Stream.empty();
    }

    @Override
    protected StringParser<Integer> getParser() {
        return IntArg::parse;
    }

    @Override
    public String getTypeName() {
        return "Integer";
    }

    @Override
    public Class<Integer> getType() {
        return Integer.class;
    }

}
