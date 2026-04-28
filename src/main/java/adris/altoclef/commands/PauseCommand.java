package adris.altoclef.commands;

import adris.altoclef.AltoClef;
import adris.altoclef.commandsystem.ArgParser;
import adris.altoclef.commandsystem.Command;

/**
 * 暂停命令 - 暂停当前正在运行的任务
 */
public class PauseCommand extends Command {
    public PauseCommand() {
        super("pause", "暂停当前正在运行的任务");
    }

    @Override
    protected void call(AltoClef mod, ArgParser parser) {
        if (mod.isPaused()) {
            log("机器人已暂停!");
        } else if (!mod.getUserTaskChain().isActive()) {
            log("机器人没有当前任务!");
        } else {
            // 保存当前任务并暂停
            mod.setStoredTask(mod.getUserTaskChain().getCurrentTask());
            mod.setPaused(true);
            mod.getUserTaskChain().stop();
            mod.getTaskRunner().disable();
            log("暂停机器人和时间");
        }
        finish();
    }
}