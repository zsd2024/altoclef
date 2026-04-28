package adris.altoclef.commands;

import adris.altoclef.AltoClef;
import adris.altoclef.commandsystem.ArgParser;
import adris.altoclef.commandsystem.Command;
import adris.altoclef.ui.MessagePriority;

/**
 * 帮助命令 - 列出所有命令
 */
public class HelpCommand extends Command {

    public HelpCommand() {
        super("help", "列出所有命令");
    }

    @Override
    protected void call(AltoClef mod, ArgParser parser) {
        mod.log("########## 帮助: ##########", MessagePriority.OPTIONAL);
        int padSize = 10;
        for (Command c : AltoClef.getCommandExecutor().allCommands()) {
            StringBuilder line = new StringBuilder();
            //line.append("");
            for (String name : c.getNames()) {
                line.append(name).append(": ");
                int toAdd = padSize - name.length();
                line.append(" ".repeat(Math.max(0, toAdd)));
                line.append(c.getDescription());
                mod.log(line.toString(), MessagePriority.OPTIONAL);
            }
        }
        mod.log("###########################", MessagePriority.OPTIONAL);
        finish();
    }
}