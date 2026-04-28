package adris.altoclef.commands;

import adris.altoclef.AltoClef;
import adris.altoclef.commandsystem.ArgParser;
import adris.altoclef.commandsystem.Command;
import adris.altoclef.tasks.movement.IdleTask;

/**
 * 空闲命令 - 原地不动
 */
public class IdleCommand extends Command {
    public IdleCommand() {
        super("idle", "原地不动");
    }

    @Override
    protected void call(AltoClef mod, ArgParser parser) {
        // 执行空闲任务(原地不动)
        mod.runUserTask(new IdleTask(), this::finish);
    }
}
