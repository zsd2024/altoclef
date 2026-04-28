package adris.altoclef.commandsystem.args;

import adris.altoclef.commandsystem.exception.CommandException;
import adris.altoclef.commandsystem.StringReader;
import adris.altoclef.util.ItemTarget;

import java.util.stream.Stream;

/**
 * 物品目标参数类
 * 用于解析和处理命令中的物品目标参数，包含物品名称和数量
 */
public class ItemTargetArg extends Arg<ItemTarget> {

    /**
     * 构造函数
     * @param name 参数名称
     */
    public ItemTargetArg(String name) {
        super(name);
    }

    /**
     * 构造函数
     * @param name 参数名称
     * @param defaultValue 默认值
     */
    public ItemTargetArg(String name, ItemTarget defaultValue) {
        super(name, defaultValue);
    }

    /**
     * 构造函数
     * @param name 参数名称
     * @param defaultValue 默认值
     * @param showDefault 是否显示默认值
     */
    public ItemTargetArg(String name, ItemTarget defaultValue, boolean showDefault) {
        super(name, defaultValue, showDefault);
    }


    /**
     * 解析物品目标参数
     * @param reader 字符串读取器
     * @return 解析后的物品目标对象
     * @throws CommandException 当解析失败时抛出命令异常
     */
    private static ItemTarget parse(StringReader reader) throws CommandException {
        String item = CataloguedItemArg.parse(reader);
        int count = 1;

        Integer parsed = Arg.parseIfSupplied(reader, IntArg::parse);
        if (parsed != null) {
            count = parsed;
        }

        return new ItemTarget(item, count);
    }

    @Override
    public Class<ItemTarget> getType() {
        return ItemTarget.class;
    }

    @Override
    public Stream<String> getSuggestions(StringReader reader) {
        if (getSupplied(reader, CataloguedItemArg::parse) == ParseResult.NOT_FINISHED) {
            return CataloguedItemArg.listSuggestions();
        }
        return Stream.empty();
    }

    @Override
    public String getTypeName() {
        return "Item Target";
    }

    @Override
    protected StringParser<ItemTarget> getParser() {
        return ItemTargetArg::parse;
    }
}
