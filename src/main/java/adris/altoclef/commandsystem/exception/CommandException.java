package adris.altoclef.commandsystem.exception;

/**
 * 命令异常基类
 * 所有命令相关异常的抽象基类
 */
public abstract class CommandException extends Exception {

    /**
     * 构造函数，使用指定的消息创建命令异常
     * 
     * @param message 异常消息
     */
    public CommandException(String message) {
        super(message);
    }

    /**
     * 构造函数，使用指定的消息和子异常创建命令异常
     * 
     * @param message 异常消息
     * @param child 子异常
     */
    public CommandException(String message, Exception child) {
        super(message, child);
    }
}
