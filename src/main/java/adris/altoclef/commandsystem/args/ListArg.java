package adris.altoclef.commandsystem.args;

import adris.altoclef.commandsystem.exception.BadCommandSyntaxException;
import adris.altoclef.commandsystem.exception.CommandException;
import adris.altoclef.commandsystem.StringReader;
import adris.altoclef.commandsystem.exception.CommandNotFinishedException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;

/**
 * 列表参数类，用于解析命令中的列表类型参数
 * 支持两种格式：单个元素（非列表）或方括号包围的列表 [item1, item2, ...]
 * 还支持别名映射，可以将特定字符串映射到预定义的列表值
 *
 * @param <T> 列表中元素的类型
 */
public class ListArg<T> extends Arg<List<T>> {


    /**
     * 列表中每个元素的参数解析器
     */
    private final Arg<T> argument;
    
    /**
     * 别名映射表，将字符串别名映射到对应的列表值
     */
    private final HashMap<String, List<T>> aliases = new HashMap<>();

    /**
     * 构造函数，创建一个没有默认值的列表参数
     *
     * @param argument 元素参数解析器
     * @param name 参数名称
     */
    public ListArg(Arg<T> argument, String name) {
        super(name);
        this.argument = argument;
    }

    /**
     * 构造函数，创建一个带有默认值的列表参数
     *
     * @param argument 元素参数解析器
     * @param name 参数名称
     * @param defaultValue 默认值
     */
    public ListArg(Arg<T> argument,String name, List<T> defaultValue) {
        super(name, defaultValue);
        this.argument = argument;
    }

    /**
     * 构造函数，创建一个带有默认值并指定是否显示默认值的列表参数
     *
     * @param argument 元素参数解析器
     * @param name 参数名称
     * @param defaultValue 默认值
     * @param showDefault 是否在帮助信息中显示默认值
     */
    public ListArg(Arg<T> argument,String name, List<T> defaultValue, boolean showDefault) {
        super(name, defaultValue, showDefault);
        this.argument = argument;
    }



    /**
     * 添加别名映射
     *
     * @param alias 别名字符串
     * @param value 对应的列表值
     * @return 当前实例，支持链式调用
     */
    public ListArg<T> addAlias(String alias, List<T> value) {
        aliases.put(alias, value);
        return this;
    }


    // FIXME 不支持嵌套列表，但可能没人需要这个功能
    /**
     * 解析列表参数的核心方法
     *
     * @param parser 字符串读取器
     * @param argumentFunc 元素解析函数
     * @param aliases 别名映射表
     * @return 解析得到的列表
     * @throws CommandException 命令异常
     */
    protected static <T> List<T> parse(StringReader parser, StringParser<T> argumentFunc, HashMap<String, List<T>> aliases) throws CommandException {
        // 不是列表格式，只包含单个元素
        // 需要使用 peek 来避免消耗子元素的信息
        if (!parser.peek().startsWith("[")) {
            if (aliases.containsKey(parser.peek())) {
                return aliases.get(parser.next());
            } else {
                return List.of(argumentFunc.parse(parser));
            }
        }

        ParseResult p = getParts(parser);
        String[] parts = p.parts;
        boolean hasClosingBracket = p.hasClosingBracket;

        List<T> result = new ArrayList<>();

        for (int i = 0; i < parts.length; i++) {
            String part = parts[i].strip();

            if (part.isEmpty()) {
                if (hasClosingBracket || i + 1 < parts.length) {
                    throw new BadCommandSyntaxException("期望令牌");
                }
                throw new CommandNotFinishedException("期望令牌");
            }

            StringReader partReader = new StringReader(part);
            if (aliases.containsKey(partReader.peek())) {
                result.addAll(aliases.get(partReader.next()));
            } else {
                result.add(argumentFunc.parse(partReader));
            }

            if (partReader.hasNext()) {
                throw new BadCommandSyntaxException("列表中出现意外令牌");
            }
        }

        if (!hasClosingBracket) {
            throw new CommandNotFinishedException("期望 ']'"); 
        }

        return result;
    }

    @Override
    public Stream<String> getSuggestions(StringReader reader) {
        try {
            StringReader copy = reader.copy();
            parse(copy, argument.getParser(), aliases);

            if (reader.hasNext() && reader.peek().startsWith(",")) {
                reader.nextChar();
            }
        } catch (BadCommandSyntaxException ignored) {
            return Stream.empty();
        } catch (CommandNotFinishedException ignored) {
            ParseResult parsed = getParts(reader);
            if (parsed.hasClosingBracket) {
                return Stream.empty();
            }
            String[] parts = parsed.parts;
            for (int i = 0; i < parts.length-1; i++) {
                if (parts[i].isEmpty()) {
                    return Stream.empty();
                }
            }

            if (parsed.endsInColumn) {
                return getArgSuggestions(new StringReader(""));
            }

            return getArgSuggestions(new StringReader(parts[parts.length-1]));
        } catch (CommandException e) {
            throw new IllegalStateException("非法类型 "+e.getClass().getSimpleName(), e);
        }

        // 所有内容都已消耗，无需返回任何内容
        return getArgSuggestions(new StringReader(""));
    }

    /**
     * 获取参数建议（包括元素建议和别名）
     *
     * @param reader 字符串读取器
     * @return 建议流
     */
    private Stream<String> getArgSuggestions(StringReader reader) {
        return Stream.concat(argument.getSuggestions(reader), aliases.keySet().stream());
    }



    /**
     * 从读取器中提取列表的各个部分
     *
     * @param reader 字符串读取器
     * @return 解析结果，包含各部分、是否有闭合括号、是否以逗号结尾
     */
    private static ParseResult getParts(StringReader reader) {
        String fullList = "";
        boolean hasClosingBracket = false;

        while (reader.hasNext()) {
            String line;
            try {
                line = reader.next();
            } catch (CommandException e) {
                throw new RuntimeException(e);
            }
            fullList += line + " ";

            if (line.endsWith("]")) {
                hasClosingBracket = true;
                break;
            }
        }
        fullList = fullList.strip();
        boolean endInColumn = fullList.endsWith(",");

        // 移除开头的 [ 和结尾的 ]
        if (fullList.startsWith("[")) {
            fullList = fullList.substring(1);
        }
        if (fullList.endsWith("]")) {
            fullList = fullList.substring(0, fullList.length()-1);
        }

        String[] parts = fullList.split(",", -1);
        for (int i = 0; i < parts.length; i++) {
            parts[i] = parts[i].strip();
        }

        return new ParseResult(parts, hasClosingBracket, endInColumn);
    }

    /**
     * 解析结果记录类
     *
     * @param parts 列表的各个部分
     * @param hasClosingBracket 是否包含闭合括号
     * @param endsInColumn 是否以逗号结尾
     */
    private static record ParseResult(String[] parts, boolean hasClosingBracket, boolean endsInColumn) {
    }


    @Override
    protected StringParser<List<T>> getParser() {
        return reader -> parse(reader, argument.getParser(), aliases);
    }

    @Override
    public String getTypeName() {
        return argument.getTypeName() +" 列表";
    }

    @Override
    public Class<List<T>> getType() {
        //noinspection unchecked - 这不是理想的做法，但无法更好地实现
        return (Class<List<T>>)(Class<?>) List.class;
    }

}
