package adris.altoclef.commandsystem.args;

import adris.altoclef.commandsystem.StringReader;
import adris.altoclef.commandsystem.exception.CommandNotFinishedException;

import java.util.stream.Stream;

/**
 * 字符串参数类，用于解析命令中的字符串类型参数
 * 字符串不能为空，否则会抛出 CommandNotFinishedException 异常
 */
public class StringArg extends Arg<String> {

    /**
     * 构造函数，创建一个没有默认值的字符串参数
     *
     * @param name 参数名称
     */
    public StringArg(String name) {
        super(name);
    }

    /**
     * 构造函数，创建一个带有默认值的字符串参数
     *
     * @param name 参数名称
     * @param defaultValue 默认值
     */
    public StringArg(String name, String defaultValue) {
        super(name, defaultValue);
    }

    /**
     * 构造函数，创建一个带有默认值并指定是否显示默认值的字符串参数
     *
     * @param name 参数名称
     * @param defaultValue 默认值
     * @param showDefault 是否在帮助信息中显示默认值
     */
    public StringArg(String name, String defaultValue, boolean showDefault) {
        super(name, defaultValue, showDefault);
    }


    @Override
    public Stream<String> getSuggestions(StringReader reader) {
        return Stream.empty();
    }

    @Override
    protected StringParser<String> getParser() {
        return reader -> {
            String value = reader.next();
            if (value.isEmpty()) throw new CommandNotFinishedException("字符串不能为空");

            return value;
        };
    }

    @Override
    public String getTypeName() {
        return "字符串";
    }

    @Override
    public Class<String> getType() {
        return String.class;
    }

}
