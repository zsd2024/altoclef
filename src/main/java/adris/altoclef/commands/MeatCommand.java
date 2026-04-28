package adris.altoclef.commands;

import adris.altoclef.AltoClef;
import adris.altoclef.commandsystem.ArgParser;
import adris.altoclef.commandsystem.Command;
import adris.altoclef.commandsystem.exception.CommandException;
import adris.altoclef.commandsystem.args.IntArg;
import adris.altoclef.tasks.resources.CollectMeatTask;

/**
 * 肉类命令 - 收集指定数量的肉类
 */
public class MeatCommand extends Command {
    public MeatCommand() throws CommandException {
        super("meat", "收集指定数量的肉类",
                new IntArg("count")
        );
    }

    @Override
    protected void call(AltoClef mod, ArgParser parser) throws CommandException {
        // 执行收集肉类任务
        mod.runUserTask(new CollectMeatTask(parser.get(Integer.class)), this::finish);
    }
}