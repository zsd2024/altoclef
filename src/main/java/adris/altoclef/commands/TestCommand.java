package adris.altoclef.commands;

import adris.altoclef.AltoClef;
import adris.altoclef.Playground;
import adris.altoclef.commandsystem.ArgParser;
import adris.altoclef.commandsystem.Command;
import adris.altoclef.commandsystem.exception.CommandException;
import adris.altoclef.commandsystem.args.StringArg;

/**
 * 测试命令类
 * 通用的测试命令，用于开发和调试目的
 */
public class TestCommand extends Command {

    /**
     * 构造函数
     * 初始化命令名称为"test"，描述为"通用测试命令"，并添加一个可选的字符串参数"extra"
     */
    public TestCommand() {
        super("test", "通用测试命令",
                new StringArg("extra", "")
        );
    }

    @Override
    protected void call(AltoClef mod, ArgParser parser) throws CommandException {
        // 调用临时测试函数，传入模块实例和额外参数
        Playground.TEMP_TEST_FUNCTION(mod, parser.get(String.class));
        finish();
    }
}