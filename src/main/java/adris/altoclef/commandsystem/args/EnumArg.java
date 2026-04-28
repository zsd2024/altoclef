package adris.altoclef.commandsystem.args;

import adris.altoclef.commandsystem.exception.BadCommandSyntaxException;
import adris.altoclef.commandsystem.exception.CommandException;
import adris.altoclef.commandsystem.StringReader;
import adris.altoclef.commandsystem.exception.CommandNotFinishedException;

import java.util.Arrays;
import java.util.stream.Stream;

/**
 * 枚举参数 - 用于解析枚举类型的命令参数
 * @param <T> 枚举类型
 */
public class EnumArg<T extends Enum<T>> extends Arg<T>{

    /** 枚举类 */
    private final Class<T> enumCl;

    /**
     * 构造函数
     * @param name 参数名称
     * @param enumCl 枚举类
     */
    public EnumArg(String name, Class<T> enumCl) {
        super(name);
        this.enumCl = enumCl;
    }

    /**
     * 解析枚举参数
     * @param reader 字符串读取器
     * @param enumCl 枚举类
     * @return 枚举常量
     * @throws CommandException 命令异常
     */
    public static <T extends Enum<T>> T parse(StringReader reader, Class<T> enumCl) throws CommandException {
        String value = reader.next();

        boolean begins = false;
        for (T enumConstant : enumCl.getEnumConstants()) {
            if (enumConstant.name().equals(value)) return enumConstant;

            if (enumConstant.name().startsWith(value)) begins = true;
        }

        String errorMsg = "值 '"+value+"' 不存在";
        if (begins) {
            throw new CommandNotFinishedException(errorMsg);
        } else {
            throw new BadCommandSyntaxException(errorMsg);
        }
    }


    @Override
    public Class<T> getType() {
        return enumCl;
    }


    @Override
    protected StringParser<T> getParser() {
        return reader -> parse(reader, enumCl);
    }

    @Override
    public Stream<String> getSuggestions(StringReader reader) {
        return suggestions(enumCl);
    }

    /**
     * 获取枚举建议
     * @param enumCl 枚举类
     * @return 枚举名称流
     */
    public static Stream<String> suggestions(Class<? extends Enum<?>> enumCl) {
        return Arrays.stream(enumCl.getEnumConstants()).map(Enum::name);
    }

    @Override
    public String getTypeName() {
        return enumCl.getSimpleName() + " 枚举";
    }

}

    public static <T extends Enum<T>> T parse(StringReader reader, Class<T> enumCl) throws CommandException {
        String value = reader.next();

        boolean begins = false;
        for (T enumConstant : enumCl.getEnumConstants()) {
            if (enumConstant.name().equals(value)) return enumConstant;

            if (enumConstant.name().startsWith(value)) begins = true;
        }

        String errorMsg = "Value '"+value+"' does not exist";
        if (begins) {
            throw new CommandNotFinishedException(errorMsg);
        } else {
            throw new BadCommandSyntaxException(errorMsg);
        }
    }


    @Override
    public Class<T> getType() {
        return enumCl;
    }


    @Override
    protected StringParser<T> getParser() {
        return reader -> parse(reader, enumCl);
    }

    @Override
    public Stream<String> getSuggestions(StringReader reader) {
        return suggestions(enumCl);
    }

    public static Stream<String> suggestions(Class<? extends Enum<?>> enumCl) {
        return Arrays.stream(enumCl.getEnumConstants()).map(Enum::name);
    }

    @Override
    public String getTypeName() {
        return "Enum of "+enumCl.getSimpleName();
    }

}
