package adris.altoclef.commands;

import adris.altoclef.AltoClef;
import adris.altoclef.commandsystem.ArgParser;
import adris.altoclef.commandsystem.Command;
import adris.altoclef.commandsystem.exception.CommandException;
import adris.altoclef.commandsystem.args.IntArg;
import adris.altoclef.tasks.resources.CollectFoodTask;

/**
 * 食物命令 - 收集指定数量的食物
 */
public class FoodCommand extends Command {
    public FoodCommand() {
        super("food", "收集指定数量的食物",
                new IntArg("count")
        );
    }

    @Override
    protected void call(AltoClef mod, ArgParser parser) throws CommandException {
        // 执行收集食物任务
        mod.runUserTask(new CollectFoodTask(parser.get(Integer.class)), this::finish);
    }
}