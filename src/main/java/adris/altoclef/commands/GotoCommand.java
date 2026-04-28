package adris.altoclef.commands;

import adris.altoclef.AltoClef;
import adris.altoclef.commandsystem.*;
import adris.altoclef.commandsystem.args.GoToTargetArg;
import adris.altoclef.commandsystem.exception.CommandException;
import adris.altoclef.tasks.movement.DefaultGoToDimensionTask;
import adris.altoclef.tasks.movement.GetToBlockTask;
import adris.altoclef.tasks.movement.GetToXZTask;
import adris.altoclef.tasks.movement.GetToYTask;
import adris.altoclef.tasksystem.Task;
import net.minecraft.util.math.BlockPos;

/**
 * 前往命令 - 告诉机器人前往一组坐标
 * 
 * 在所有命令中，这个命令最能说明为什么我们需要一个更好的参数解析系统。
 */
public class GotoCommand extends Command {

    public GotoCommand() {
        // x z
        // x y z
        // x y z dimension
        // (dimension)
        // (x z dimension)
        super("goto", "告诉机器人前往一组坐标",
                new GoToTargetArg("[x y z dimension]/[x z dimension]/[y dimension]/[dimension]/[x y z]/[x z]/[y]")
        );
    }

    /**
     * 获取移动任务
     * @param target 目标坐标
     * @return 相应的移动任务
     */
    public static Task getMovementTaskFor(GotoTarget target) {
        return switch (target.getType()) {
            case XYZ -> new GetToBlockTask(new BlockPos(target.getX(), target.getY(), target.getZ()), target.getDimension());
            case XZ -> new GetToXZTask(target.getX(), target.getZ(), target.getDimension());
            case Y -> new GetToYTask(target.getY(), target.getDimension());
            case NONE -> new DefaultGoToDimensionTask(target.getDimension());
        };
    }

    @Override
    protected void call(AltoClef mod, ArgParser parser) throws CommandException {
        GotoTarget target = parser.get(GotoTarget.class);
        mod.runUserTask(getMovementTaskFor(target), this::finish);
    }
}
