package adris.altoclef.commandsystem;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.commandsystem.args.Arg;
import adris.altoclef.commandsystem.args.GoToTargetArg;
import adris.altoclef.commandsystem.args.ListArg;
import adris.altoclef.commandsystem.exception.CommandException;
import adris.altoclef.commandsystem.exception.RuntimeCommandException;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

/**
 * 命令基类
 * 所有具体命令都继承此类，提供命令的基本功能和结构
 */
public abstract class Command {

    /** 参数解析器 */
    private final ArgParser parser;
    /** 参数数组 */
    private final Arg<?>[] args;
    /** 命令名称列表（支持多个别名） */
    private final List<String> names;
    /** 命令描述 */
    private final String description;
    /** AltoClef 主模块实例 */
    private AltoClef mod;
    /** 命令完成后的回调 */
    private Runnable onFinish = null;
    /** 最小参数数量 */
    private int minArgCount = 0;

    /**
     * 构造函数（单个名称）
     * @param name 命令名称
     * @param description 命令描述
     * @param args 参数数组
     */
    public Command(String name, String description, Arg<?>... args) {
        this(List.of(name), description, args);
    }

    /**
     * 构造函数（多个名称）
     * @param names 命令名称列表
     * @param description 命令描述
     * @param args 参数数组
     */
    public Command(List<String> names, String description, Arg<?>... args) {
        this.names = names;
        this.description = description;
        parser = new ArgParser(args);
        this.args = args;
    }

    /**
     * 执行命令
     * @param mod AltoClef 主模块实例
     * @param line 命令行字符串
     * @param onFinish 完成回调
     * @throws CommandException 命令异常
     */
    public void run(AltoClef mod, String line, Runnable onFinish) throws CommandException {
        this.onFinish = onFinish;
        this.mod = mod;
        parser.loadArgs(line, true);
        call(mod, parser);
    }

    /**
     * 标记命令完成，触发完成回调
     */
    protected void finish() {
        if (onFinish != null)
            //noinspection unchecked
            onFinish.run();
    }

    /**
     * 获取帮助信息表示（从头开始）
     * @param usedName 使用的命令名称
     * @return 帮助信息字符串
     * @throws RuntimeCommandException 运行时命令异常
     */
    public String getHelpRepresentation(String usedName)  throws RuntimeCommandException{
        return getHelpRepresentation(usedName,-1);
    }

    /**
     * 获取帮助信息表示（从指定参数开始）
     * @param usedName 使用的命令名称
     * @param fromArg 起始参数索引
     * @return 帮助信息字符串
     * @throws RuntimeCommandException 运行时命令异常
     */
    public String getHelpRepresentation(String usedName,int fromArg) throws RuntimeCommandException {
        if (!names.contains(usedName)) {
            throw new RuntimeCommandException("无法使用名称 '"+usedName+"' 调用命令，可用名称: "+names);
        }

        StringBuilder sb;
        if (fromArg < 0) {
            fromArg = 0;
            sb = new StringBuilder(usedName).append(" ");
        } else {
            sb = new StringBuilder();
        }

        Arg<?>[] parserArgs = parser.getArgs();
        for (int i = fromArg; i < parserArgs.length; i++) {
            Arg<?> arg = parserArgs[i];
            sb.append(arg.getHelpRepresentation());

            if (i + 1 < parserArgs.length) {
                sb.append(" ");
            }
        }
        return sb.toString();
    }

    /**
     * 记录普通日志消息
     * @param message 消息内容
     */
    protected void log(Object message) {
        Debug.logMessage(message.toString());
    }

    /**
     * 记录错误日志消息
     * @param message 消息内容
     */
    protected void logError(Object message) {
        Debug.logError(message.toString());
    }

    /**
     * 抽象方法：执行命令的具体逻辑
     * 子类必须实现此方法
     * @param mod AltoClef 主模块实例
     * @param parser 参数解析器
     * @throws CommandException 命令异常
     */
    protected abstract void call(AltoClef mod, ArgParser parser) throws CommandException;

    /**
     * 获取命令名称列表
     * @return 不可修改的命令名称列表
     */
    public List<String> getNames() {
        return Collections.unmodifiableList(names);
    }

    /**
     * 获取命令描述
     * @return 命令描述字符串
     */
    public String getDescription() {
        return description;
    }

    /**
     * 解析 Tab 补全建议
     * @param line 命令行字符串
     * @return 补全建议流
     * @throws CommandException 命令异常
     */
    public final Stream<String> resolveTabCompletions(String line) throws CommandException {
        StringReader reader = new StringReader(line);
        reader.next(); // 移除命令名称

        for (int i = 0; i < args.length; i++) {
            Arg<?> arg = args[i];

            if (!reader.hasNext() && !line.endsWith(" ")) {
                return Stream.empty();
            }
            Arg.ParseResult result = arg.consumeIfSupplied(reader);
            if (result == Arg.ParseResult.BAD_SYNTAX) {
                return Stream.empty();
            }

            if (result == Arg.ParseResult.NOT_FINISHED) {
                if (reader.hasNext()) {
                    if (i == args.length - 1) {
                        StringReader copy = reader.copy();
                        try {
                            arg.parseArg(copy);
                        } catch (CommandException ignored) {
                        }
                        String wasParsing = reader.peek();

                        // 这意味着错误不在最后一个元素上 => 补全建议无效
                        if (copy.hasNext() || (line.endsWith(" ") && !wasParsing.isBlank() && !(arg instanceof ListArg<?>))) {
                             if (!(arg instanceof GoToTargetArg)) {
                                return Stream.empty();
                            }
                        }
                    }
                }

                return arg.getSuggestions(reader);
            }
        }
        return Stream.empty();
    }

    /**
     * 获取参数数组
     * @return 参数数组
     */
    public Arg<?>[] getArgs() {
        return args;
    }
}
