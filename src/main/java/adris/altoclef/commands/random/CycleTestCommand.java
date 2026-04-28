package adris.altoclef.commands.random;

import adris.altoclef.AltoClef;
import adris.altoclef.commandsystem.ArgParser;
import adris.altoclef.commandsystem.Command;
import adris.altoclef.tasks.speedrun.OneCycleTask;

/**
 * 循环测试命令
 * 用于执行一次完整的末影龙击杀循环任务
 */
public class CycleTestCommand extends Command {

    /**
     * 构造函数
     * 初始化命令名称为"cycle"，描述为"执行一次末影龙循环 B)"
     */
    public CycleTestCommand() {
        super("cycle", "执行一次末影龙循环 B)");
    }

    /**
     * 执行命令逻辑
     * 
     * @param mod AltoClef主模块实例
     * @param parser 命令参数解析器
     */
    @Override
    protected void call(AltoClef mod, ArgParser parser) {
        mod.runUserTask(new OneCycleTask(), this::finish);
    }
}
