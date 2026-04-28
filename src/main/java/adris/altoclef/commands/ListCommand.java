package adris.altoclef.commands;

import adris.altoclef.AltoClef;
import adris.altoclef.TaskCatalogue;
import adris.altoclef.commandsystem.ArgParser;
import adris.altoclef.commandsystem.Command;
import adris.altoclef.commandsystem.exception.CommandException;
import adris.altoclef.ui.MessagePriority;

import java.util.Arrays;

/**
 * 列表命令 - 列出所有可获得的物品
 */
public class ListCommand extends Command {
    public ListCommand() {
        super("list", "列出所有可获得的物品");
    }

    @Override
    protected void call(AltoClef mod, ArgParser parser) throws CommandException {
        mod.log("#### 所有可获得物品列表 ####", MessagePriority.OPTIONAL);
        mod.log(Arrays.toString(TaskCatalogue.resourceNames().toArray()), MessagePriority.OPTIONAL);
        mod.log("############# 列表结束 ###############", MessagePriority.OPTIONAL);
    }
}
