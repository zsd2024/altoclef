package adris.altoclef.commands;

import adris.altoclef.AltoClef;
import adris.altoclef.commandsystem.ArgParser;
import adris.altoclef.commandsystem.Command;
import adris.altoclef.util.helpers.WorldHelper;

/**
 * 坐标命令 - 获取机器人当前坐标
 */
public class CoordsCommand extends Command {
    public CoordsCommand() {
        // coords命令，用于获取机器人当前坐标
        super("coords", "获取机器人的当前坐标");
    }

    @Override
    protected void call(AltoClef mod, ArgParser parser) {
        // 输出当前坐标和维度信息
        mod.log("当前坐标: " + mod.getPlayer().getBlockPos().toShortString() + " (当前维度: " + WorldHelper.getCurrentDimension() + ")");
        finish();
    }
}
