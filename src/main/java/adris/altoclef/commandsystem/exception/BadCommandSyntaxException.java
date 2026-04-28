package adris.altoclef.commandsystem.exception;

/**
 * 命令语法错误异常
 * 当命令的语法格式不正确时抛出此异常
 */
public class BadCommandSyntaxException extends CommandException {

    /**
     * 构造函数，使用指定的消息创建命令语法错误异常
     * 
     * @param message 异常消息
     */
    public BadCommandSyntaxException(String message) {
        super(message);
    }

    /**
     * 构造函数，使用指定的消息和子异常创建命令语法错误异常
     * 
     * @param message 异常消息
     * @param child 子异常
     */
    public BadCommandSyntaxException(String message, Exception child) {
        super(message, child);
    }

}
