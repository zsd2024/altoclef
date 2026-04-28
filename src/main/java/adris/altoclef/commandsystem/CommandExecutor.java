package adris.altoclef.commandsystem;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.commandsystem.exception.CommandException;
import adris.altoclef.commandsystem.exception.RuntimeCommandException;

import java.util.Collection;
import java.util.HashMap;
import java.util.function.Consumer;

/**
 * 命令执行器
 * 负责注册、查找和执行命令，处理命令前缀和多命令分隔
 */
public class CommandExecutor {

    /** 命令表：命令名称到命令对象的映射 */
    private final HashMap<String, Command> commandSheet = new HashMap<>();
    /** AltoClef 主模块实例 */
    private final AltoClef mod;

    /**
     * 构造函数
     * @param mod AltoClef 主模块实例
     */
    public CommandExecutor(AltoClef mod) {
        this.mod = mod;
    }

    /**
     * 注册新命令
     * @param commands 要注册的命令数组
     */
    public void registerNewCommand(Command... commands) {
        for (Command command : commands) {
            for (String name : command.getNames()) {
                if (commandSheet.containsKey(name)) {
                    Debug.logInternal("名称为 " + name + " 的命令已存在！不能重复注册该名称。");
                    continue;
                }
                
                commandSheet.put(name, command);
            }
        }
    }

    /**
     * 获取命令前缀
     * @return 命令前缀字符串
     */
    public String getCommandPrefix() {
        return mod.getModSettings().getCommandPrefix();
    }

    /**
     * 判断是否为客户端命令
     * @param line 命令行字符串
     * @return 如果是客户端命令返回true，否则返回false
     */
    public boolean isClientCommand(String line) {
        return line.startsWith(getCommandPrefix());
    }

    /**
     * 递归执行命令序列
     * 这是我们"嵌套"命令完成的方式，以便按顺序完成它们。
     * @param commands 命令数组
     * @param parts 命令部分数组
     * @param index 当前执行的命令索引
     * @param onFinish 完成回调
     * @param getException 异常处理回调
     */
    private void executeRecursive(Command[] commands, String[] parts, int index, Runnable onFinish, Consumer<CommandException> getException) {
        if (index >= commands.length) {
            onFinish.run();
            return;
        }
        Command command = commands[index];
        String part = parts[index];
        try {
            if (command == null) {
                getException.accept(new RuntimeCommandException("无效的命令:" + part));
                executeRecursive(commands, parts, index + 1, onFinish, getException);
            } else {
                command.run(mod, part.strip(), () -> executeRecursive(commands, parts, index + 1, onFinish, getException));
            }
        } catch (CommandException ae) {
            try {
                getException.accept(new RuntimeCommandException(ae.getMessage() + "\n用法: " + command.getHelpRepresentation(new StringReader(part).nextOrEmpty()), ae));
            } catch (RuntimeCommandException e) {
                throw new IllegalStateException("不应该发生！");
            }
        }
    }

    /**
     * 执行命令（完整版本）
     * @param line 命令行字符串
     * @param onFinish 完成回调
     * @param getException 异常处理回调
     */
    public void execute(String line, Runnable onFinish, Consumer<CommandException> getException) {
        if (!isClientCommand(line)) return;
        line = line.substring(getCommandPrefix().length());
        // 执行以分号分隔的多个命令
        String[] parts = line.split(";");
        Command[] commands = new Command[parts.length];
        try {
            for (int i = 0; i < parts.length; ++i) {
                String part = parts[i].strip();
                if (part.startsWith(getCommandPrefix())) {
                    part = part.substring(getCommandPrefix().length());
                }

                commands[i] = getCommand(part);
            }
        } catch (CommandException e) {
            getException.accept(e);
        }
        executeRecursive(commands, parts, 0, onFinish, getException);
    }

    /**
     * 执行命令（带异常处理）
     * @param line 命令行字符串
     * @param getException 异常处理回调
     */
    public void execute(String line, Consumer<CommandException> getException) {
        execute(line, () -> {
        }, getException);
    }

    /**
     * 执行命令（默认异常处理）
     * @param line 命令行字符串
     */
    public void execute(String line) {
        execute(line, ex -> Debug.logWarning(ex.getMessage()));
    }

    /**
     * 执行命令（自动添加前缀）
     * @param line 命令行字符串
     */
    public void executeWithPrefix(String line) {
        if (!line.startsWith(getCommandPrefix())) {
            line = getCommandPrefix() + line;
        }
        execute(line);
    }

    /**
     * 根据命令行获取命令对象
     * @param line 命令行字符串
     * @return 命令对象，如果命令为空则返回null
     * @throws RuntimeCommandException 运行时命令异常
     */
    private Command getCommand(String line) throws RuntimeCommandException {
        line = line.trim();
        if (line.length() != 0) {
            String command = line;
            int firstSpace = line.indexOf(' ');
            if (firstSpace != -1) {
                command = line.substring(0, firstSpace);
            }

            if (!commandSheet.containsKey(command)) {
                throw new RuntimeCommandException("命令 " + command + " 不存在。");
            }

            return commandSheet.get(command);
        }
        return null;

    }

    /**
     * 获取所有已注册的命令
     * @return 命令集合
     */
    public Collection<Command> allCommands() {
        return commandSheet.values();
    }

    /**
     * 获取所有已注册的命令名称
     * @return 命令名称集合
     */
    public Collection<String> allCommandNames() {
        return commandSheet.keySet();
    }

    /**
     * 根据名称获取命令
     * @param name 命令名称
     * @return 命令对象，如果不存在则返回null
     */
    public Command get(String name) {
        return (commandSheet.getOrDefault(name, null));
    }
}
