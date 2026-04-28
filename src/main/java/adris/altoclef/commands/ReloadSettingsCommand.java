package adris.altoclef.commands;

import adris.altoclef.AltoClef;
import adris.altoclef.commandsystem.ArgParser;
import adris.altoclef.commandsystem.Command;
import adris.altoclef.util.helpers.ConfigHelper;

/**
 * 重载设置命令 - 重载机器人设置和butler白名单/黑名单
 */
public class ReloadSettingsCommand extends Command {
    public ReloadSettingsCommand() {
        super("reload_settings", "重载机器人设置和butler白名单/黑名单。");
    }

    @Override
    protected void call(AltoClef mod, ArgParser parser) {
        ConfigHelper.reloadAllConfigs();
        mod.log("重载成功!");
        finish();
    }
}