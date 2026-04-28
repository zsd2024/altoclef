package adris.altoclef.commandsystem.exception;

/**
 * 运行时命令异常
 * 在命令执行过程中发生运行时错误时抛出此异常
 */
public class RuntimeCommandException extends CommandException {
    /**
     * 构造函数，使用指定的消息创建运行时命令异常
     * 
     * @param message 异常消息
     */
    public RuntimeCommandException(String message) {
        super(message);
    }

    /**
     * 构造函数，使用指定的消息和子异常创建运行时命令异常
     * 
     * @param message 异常消息
     * @param child 子异常
     */
    public RuntimeCommandException(String message, Exception child) {
        super(message, child);
    }

}
