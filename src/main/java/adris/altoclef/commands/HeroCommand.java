package adris.altoclef.commands;

import adris.altoclef.AltoClef;
import adris.altoclef.commandsystem.ArgParser;
import adris.altoclef.commandsystem.Command;
import adris.altoclef.commandsystem.exception.CommandException;
import adris.altoclef.tasks.entity.HeroTask;

/**
 * 英雄命令 - 击杀所有敌对生物
 */
public class HeroCommand extends Command {
    public HeroCommand() {
        super("hero", "击杀所有敌对生物");
    }

    @Override
    protected void call(AltoClef mod, ArgParser parser) throws CommandException {
        // 执行英雄任务(击杀所有敌对生物)
        mod.runUserTask(new HeroTask(), this::finish);
    }
}
