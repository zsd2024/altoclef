package adris.altoclef.commands;

import adris.altoclef.AltoClef;
import adris.altoclef.commandsystem.ArgParser;
import adris.altoclef.commandsystem.Command;
import adris.altoclef.tasks.speedrun.beatgame.BeatMinecraftTask;

/**
 * 游戏命令 - 完成游戏 (Miran版本)
 */
public class GamerCommand extends Command {
    public GamerCommand() {
        super("gamer", "完成游戏 (Miran版本)");
    }

    @Override
    protected void call(AltoClef mod, ArgParser parser) {
        // 执行完成游戏任务
        mod.runUserTask(new BeatMinecraftTask(mod), this::finish);
    }
}