package adris.altoclef.commands;

import adris.altoclef.AltoClef;
import adris.altoclef.commandsystem.ArgParser;
import adris.altoclef.commandsystem.Command;
import adris.altoclef.commandsystem.exception.CommandException;
import adris.altoclef.commandsystem.args.EnumArg;
import adris.altoclef.tasks.movement.GoToStrongholdPortalTask;
import adris.altoclef.tasks.movement.LocateDesertTempleTask;

/**
 * 定位结构命令 - 定位世界生成的结构
 */
public class LocateStructureCommand extends Command {

    public LocateStructureCommand() {
        super("locate_structure", "定位一个世界生成的结构。",
                new EnumArg<>("structure", Structure.class)
        );
    }

    @Override
    protected void call(AltoClef mod, ArgParser parser) throws CommandException {
        Structure structure = parser.get(Structure.class);
        switch (structure) {
            case STRONGHOLD:
                // 定位要塞任务
                mod.runUserTask(new GoToStrongholdPortalTask(1), this::finish);
                break;
            case DESERT_TEMPLE:
                // 定位沙漠神庙任务
                mod.runUserTask(new LocateDesertTempleTask(), this::finish);
                break;
        }
    }

    public enum Structure {
        DESERT_TEMPLE,  // 沙漠神庙
        STRONGHOLD      // 要塞
    }
}