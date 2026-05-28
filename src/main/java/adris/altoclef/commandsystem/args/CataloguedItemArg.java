package adris.altoclef.commandsystem.args;

import adris.altoclef.TaskCatalogue;
import adris.altoclef.commandsystem.StringReader;
import adris.altoclef.commandsystem.exception.BadCommandSyntaxException;
import adris.altoclef.commandsystem.exception.CommandException;
import adris.altoclef.commandsystem.exception.CommandNotFinishedException;

import java.util.stream.Stream;

/**
 * 目录物品参数 - 用于解析任务目录中存在的物品名称参数
 */
public class CataloguedItemArg extends Arg<String> {

    /**
     * 构造函数 - 无默认值
     * @param name 参数名称
     */
    public CataloguedItemArg(String name) {
        super(name);
    }

    /**
     * 构造函数 - 带默认值
     * @param name 参数名称
     * @param defaultValue 默认值
     */
    public CataloguedItemArg(String name, String defaultValue) {
        super(name, defaultValue);
    }

    /**
     * 构造函数 - 带默认值和显示控制
     * @param name 参数名称
     * @param defaultValue 默认值
     * @param showDefault 是否在帮助信息中显示默认值
     */
    public CataloguedItemArg(String name, String defaultValue, boolean showDefault) {
        super(name, defaultValue, showDefault);
    }


    /**
     * 解析目录物品参数
     * @param parser 字符串读取器
     * @return 物品名称
     * @throws CommandException 命令异常
     */
    public static String parse(StringReader parser) throws CommandException {
        String value = parser.next();

        if (TaskCatalogue.taskExists(value)) return value;

        boolean begins = listSuggestions().anyMatch(s -> s.startsWith(value));

        String errorMsg = "不存在名为 '" + value+"' 的目录物品";
        if (begins) {
            throw new CommandNotFinishedException(errorMsg);
        } else {
            throw new BadCommandSyntaxException(errorMsg);
        }
    }

    /**
     * 列出所有建议的物品名称
     * @return 物品名称流
     */
    public static Stream<String> listSuggestions() {
        return TaskCatalogue.resourceNames().stream();
    }

    @Override
    public Stream<String> getSuggestions(StringReader reader) {
        return listSuggestions();
    }

    @Override
    protected StringParser<String> getParser() {
        return CataloguedItemArg::parse;
    }

    @Override
    public String getTypeName() {
        return "物品";
    }

    @Override
    public Class<String> getType() {
        return String.class;
    }

}
