package adris.altoclef.commandsystem.args;

import adris.altoclef.commandsystem.StringReader;
import adris.altoclef.commandsystem.exception.BadCommandSyntaxException;
import adris.altoclef.commandsystem.exception.CommandException;
import adris.altoclef.commandsystem.exception.CommandNotFinishedException;

import java.util.stream.Stream;

/**
 * 参数基类 - 所有命令参数类型的抽象基类
 * 提供参数解析、验证和帮助信息生成的基础功能
 */
public abstract class Arg<T> {


    /** 是否具有默认值 */
    public final boolean hasDefault;
    /** 参数名称 */
    public final String name;
    /** 默认值 */
    public final T defaultValue;
    /** 是否在帮助信息中显示默认值 */
    public final boolean showDefault;

    /**
     * 构造函数 - 无默认值
     * @param name 参数名称
     */
    protected Arg(String name) {
        this.name = name;
        this.hasDefault = false;
        this.defaultValue = null;
        this.showDefault = false;
    }

    /**
     * 构造函数 - 带默认值（默认显示默认值）
     * @param name 参数名称
     * @param defaultValue 默认值
     */
    protected Arg(String name, T defaultValue) {
        this(name, defaultValue, true);
    }

    /**
     * 构造函数 - 带默认值和显示控制
     * @param name 参数名称
     * @param defaultValue 默认值
     * @param showDefault 是否在帮助信息中显示默认值
     */
    protected Arg(String name, T defaultValue, boolean showDefault) {
        this.name = name;
        this.defaultValue = defaultValue;
        this.showDefault = showDefault;
        this.hasDefault = true;
    }



    /**
     * 如果提供了参数，则消耗它
     * @param reader 字符串读取器
     * @return 解析结果
     */
    public ParseResult consumeIfSupplied(StringReader reader) {
        StringReader copy = reader.copy();
        ParseResult result = getSupplied(copy, getParser());

        if (result == ParseResult.CONSUMED) {
            reader.set(copy);
        }

        return result;
    }

    /**
     * 获取提供的参数解析结果
     * @param parser 字符串读取器
     * @param parseFunction 解析函数
     * @return 解析结果
     */
    public static <T> ParseResult getSupplied(StringReader parser, StringParser<T> parseFunction) {
        try {
            parseFunction.parse(parser);
        } catch (BadCommandSyntaxException ignored) {
            return ParseResult.BAD_SYNTAX;
        } catch (CommandNotFinishedException ignored) {
            return ParseResult.NOT_FINISHED;
        } catch (CommandException e) {
            throw new IllegalStateException("解析时出现未知异常类型 "+e.getClass().getSimpleName(), e);
        }
        return ParseResult.CONSUMED;
    }

    /**
     * 如果提供了参数，则解析它
     * @param parser 字符串读取器
     * @param parseFunction 解析函数
     * @return 解析后的值，如果解析失败则返回null
     */
    public static <T> T parseIfSupplied(StringReader parser, StringParser<T> parseFunction) {
        try {
            StringReader copy = parser.copy();
            T parsedValue = parseFunction.parse(copy);

            parser.set(copy);
            return parsedValue;
        } catch (CommandException ignored) {
            return null;
        }
    }

    /**
     * 获取帮助信息表示
     * @return 帮助字符串
     */
    public String getHelpRepresentation() {
        if (hasDefault) {
            if (showDefault) {
                return "<" + name + "=" + defaultValue + ">";
            }
            return "<" + name + ">";
        }
        return "[" + name + "]";
    }

    /**
     * 获取参数类型
     * @return 参数的Class对象
     */
    public abstract Class<T> getType();

    /**
     * 解析参数
     * @param parser 字符串读取器
     * @return 解析后的参数值
     * @throws CommandException 命令异常
     */
    public final T parseArg(StringReader parser) throws CommandException {
        return this.getParser().parse(parser);
    }

    /**
     * 获取参数建议
     * @param reader 字符串读取器
     * @return 建议流
     */
    public abstract Stream<String> getSuggestions(StringReader reader);

    /**
     * 获取解析器
     * @return 字符串解析器
     */
    protected abstract StringParser<T> getParser();

    /**
     * 获取类型名称
     * @return 类型名称字符串
     */
    public abstract String getTypeName();

    /**
     * 字符串解析器函数接口
     * @param <T> 解析结果类型
     */
    @FunctionalInterface
    public interface StringParser<T> {
        /**
         * 解析字符串
         * @param reader 字符串读取器
         * @return 解析结果
         * @throws CommandException 命令异常
         */
        T parse(StringReader reader) throws CommandException;
    }

    /**
     * 解析结果枚举
     */
    public enum ParseResult {
        /** 已成功消耗 */ CONSUMED, 
        /** 命令未完成 */ NOT_FINISHED, 
        /** 语法错误 */ BAD_SYNTAX
    }

}
