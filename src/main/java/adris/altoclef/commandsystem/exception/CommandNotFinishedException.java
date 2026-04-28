package adris.altoclef.commandsystem.exception;

/**
 * 命令未完成异常
 * 当命令执行过程中需要更多输入或条件才能完成时抛出此异常
 */
public class CommandNotFinishedException extends CommandException {

    /**
     * 构造函数，使用指定的消息创建命令未完成异常
     * 
     * @param message 异常消息
     */
    public CommandNotFinishedException(String message) {
        super(message);
    }

}
