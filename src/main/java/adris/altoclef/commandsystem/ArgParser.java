package adris.altoclef.commandsystem;

import adris.altoclef.commandsystem.args.Arg;
import adris.altoclef.commandsystem.exception.CommandException;
import adris.altoclef.commandsystem.exception.RuntimeCommandException;

/**
 * 参数解析器
 * 用于解析命令行参数，按照预定义的参数类型顺序获取参数值
 */
public class ArgParser {

    /** 预定义的参数数组 */
    private final Arg<?>[] args;
    /** 当前已解析的参数计数器 */
    private int argCounter;
    /** 字符串读取器，用于读取命令行参数 */
    private StringReader reader;

    /**
     * 构造函数
     * @param args 预定义的参数数组
     */
    public ArgParser(Arg<?>... args) {
        this.args = args;
        argCounter = 0;
    }

    /**
     * 加载命令行参数
     * @param line 命令行字符串
     * @param removeFirst 是否移除第一个元素（通常是命令名称）
     * @throws CommandException 命令异常
     */
    public void loadArgs(String line, boolean removeFirst) throws CommandException {
        reader = new StringReader(line);
        // 丢弃第一个元素，因为它始终是命令的名称
        if (removeFirst && reader.hasNext()) {
            reader.next();
        }

        argCounter = 0;
    }

    /**
     * 获取下一个参数
     * @param type 参数类型
     * @param <T> 泛型类型
     * @return 解析后的参数值
     * @throws CommandException 命令异常
     */
    public <T> T get(Class<T> type) throws CommandException {
        if (argCounter >= args.length) {
            throw new RuntimeCommandException("尝试获取的参数数量超过了预定义的数量... 这是个错误操作。");
        }

        if (args[argCounter].getType().isAssignableFrom(type)) {
            //noinspection unchecked
            Arg<T> arg = (Arg<T>) args[argCounter];

            if (!reader.hasNext()) {
                if (args[argCounter].hasDefault) {
                    return arg.defaultValue;
                } else {
                    throw new RuntimeCommandException("命令未完成，期望参数类型为 "+arg.getTypeName());
                }
            }

            T result = arg.parseArg(reader);
            argCounter++;

            return result;
        }

        throw new RuntimeCommandException("类型不匹配！("+args[argCounter].getType() + " 与 "+type+")");
    }

    /**
     * 获取所有预定义的参数
     * @return 参数数组
     */
    public Arg<?>[] getArgs() {
        return args;
    }

}
