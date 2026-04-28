package adris.altoclef.commands;

import adris.altoclef.AltoClef;
import adris.altoclef.commandsystem.ArgParser;
import adris.altoclef.commandsystem.Command;
import adris.altoclef.tasksystem.Task;

import java.util.List;

/**
 * 状态命令类
 * 用于获取当前正在执行的任务状态
 */
public class StatusCommand extends Command {
    /**
     * 构造函数
     * 初始化命令名称为"status"，描述为"获取当前执行命令的状态"
     */
    public StatusCommand() {
        super("status", "获取当前执行命令的状态");
    }

    @Override
    protected void call(AltoClef mod, ArgParser parser) {
        // 获取用户任务链中的所有任务
        List<Task> tasks = mod.getUserTaskChain().getTasks();
        if (tasks.isEmpty()) {
            // 如果没有正在运行的任务，记录日志
            mod.log("当前没有正在运行的任务。");
        } else {
            // 记录当前正在执行的任务信息
            mod.log("当前任务: " + tasks.get(0).toString());
        }
        finish();
    }
}