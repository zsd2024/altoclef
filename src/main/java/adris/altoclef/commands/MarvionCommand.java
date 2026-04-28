package adris.altoclef.commands;

import adris.altoclef.AltoClef;
import adris.altoclef.commandsystem.ArgParser;
import adris.altoclef.commandsystem.Command;

/**
 * Marvion命令 - 不支持，保留此命令以防有人使用
 */
public class MarvionCommand extends Command {

    public MarvionCommand() {
        super("marvion", "不支持，保留此命令以防有人使用");
    }

    @Override
    protected void call(AltoClef mod, ArgParser parser) {
        mod.logWarning("此命令不存在，如果想要完成游戏请使用 '@gamer'");
    }

}
