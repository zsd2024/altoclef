package adris.altoclef.commands;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.commandsystem.ArgParser;
import adris.altoclef.commandsystem.Command;

import java.util.List;

/**
 * 取消暂停命令类
 * 用于取消机器人的暂停状态，恢复任务执行
 */
public class UnPauseCommand extends Command {
    /**
     * 构造函数
     * 初始化命令名称为"unpause"和"resume"，描述为"取消机器人暂停"
     */
    public UnPauseCommand() {
        super(List.of("unpause", "resume"), "取消机器人暂停");
    }

    @Override
    protected void call(AltoClef mod, ArgParser parser) {
        // 检查机器人是否处于暂停状态
        if (!mod.isPaused()) {
            // 如果未暂停，记录日志
            mod.log("机器人未处于暂停状态");
        } else {
            // 如果已暂停，尝试恢复任务执行
            if (mod.getStoredTask() == null) {
                // 如果存储的任务为空，记录错误
                Debug.logError("存储的任务为空！");
            } else {
                // 运行存储的任务并启用任务运行器
                mod.runUserTask(mod.getStoredTask());
                mod.getTaskRunner().enable();
                mod.log("正在取消机器人和时间的暂停");
            }
            // 设置暂停状态为false
            mod.setPaused(false);
        }
        finish();
    }
}