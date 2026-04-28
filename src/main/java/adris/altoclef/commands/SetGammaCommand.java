package adris.altoclef.commands;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.commandsystem.ArgParser;
import adris.altoclef.commandsystem.Command;
import adris.altoclef.commandsystem.exception.CommandException;
import adris.altoclef.commandsystem.args.DoubleArg;
import adris.altoclef.multiversion.OptionsVer;

/**
 * 设置伽马命令 - 设置亮度值
 */
public class SetGammaCommand extends Command {

    public SetGammaCommand() throws CommandException {
        super("gamma", "设置亮度为一个值",
                new DoubleArg("gamma", 1.0)
        );
    }

    @Override
    protected void call(AltoClef mod, ArgParser parser) throws CommandException {
        double gammaValue = parser.get(Double.class);
        changeGamma(gammaValue);
    }

    /**
     * 更改伽马值
     * @param value 伽马值
     */
    public static void changeGamma(double value) {
        Debug.logMessage("伽马值设置为 " + value);

        OptionsVer.setGamma(value);
    }

}