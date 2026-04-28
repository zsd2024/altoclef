package adris.altoclef.commands;

import adris.altoclef.AltoClef;
import adris.altoclef.commandsystem.ArgParser;
import adris.altoclef.commandsystem.Command;
import adris.altoclef.commandsystem.exception.CommandException;
import adris.altoclef.commandsystem.args.StringArg;
import adris.altoclef.tasks.movement.FollowPlayerTask;

/**
 * 跟随命令 - 跟随玩家
 */
public class FollowCommand extends Command {
    public FollowCommand() {
        super("follow", "跟随你或其他人",
                new StringArg("username", null)
        );
    }

    @Override
    protected void call(AltoClef mod, ArgParser parser) throws CommandException {
        String username = parser.get(String.class);

        if (username == null) {
            if (mod.getButler().hasCurrentUser()) {
                // 如果没有提供用户名，则使用当前butler用户
                username = mod.getButler().getCurrentUser();
            } else {
                mod.logWarning("当前没有butler用户。在没有用户参数的情况下运行此命令只能通过butler执行。");
                finish();
                return;
            }
        }

        // 执行跟随玩家任务
        mod.runUserTask(new FollowPlayerTask(username), this::finish);
    }
}