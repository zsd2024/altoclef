package adris.altoclef.commands;

import adris.altoclef.AltoClef;
import adris.altoclef.commandsystem.ArgParser;
import adris.altoclef.commandsystem.Command;

import java.util.List;

/**
 * 停止命令类
 * 用于停止任务运行器（停止所有自动化操作）
 */
public class StopCommand extends Command {

    /**
     * 构造函数
     * 初始化命令名称为"stop"和"cancel"，描述为"停止任务运行器（停止所有自动化）"
     */
    public StopCommand() {
        super(List.of("stop", "cancel"), "停止任务运行器（停止所有自动化）");
    }

    @Override
    protected void call(AltoClef mod, ArgParser parser) {
        // 停止所有任务
        mod.stopTasks();
        finish();
    }
}
