package adris.altoclef.commands;

import adris.altoclef.AltoClef;
import adris.altoclef.TaskCatalogue;
import adris.altoclef.commandsystem.*;
import adris.altoclef.commandsystem.args.ItemTargetArg;
import adris.altoclef.commandsystem.args.ListArg;
import adris.altoclef.commandsystem.exception.CommandException;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.ItemTarget;
import net.minecraft.item.ItemStack;

import java.util.List;

/**
 * 获取命令 - 获取物品/资源
 */
public class GetCommand extends Command {

    public GetCommand() throws CommandException {
        super("get", "获取一个物品/资源",
                new ListArg<>(new ItemTargetArg("stack"), "items")
        );
    }


    /**
     * 获取物品
     * @param mod AltoClef模块
     * @param items 待获取的物品列表
     */
    private void getItems(AltoClef mod, List<ItemTarget> items) {
        Task targetTask;
        if (items == null || items.isEmpty()) {
            mod.log("您必须至少指定一个物品!");
            finish();
            return;
        }
        if (items.size() == 1) {
            // 单个物品获取任务
            targetTask = TaskCatalogue.getItemTask(items.getFirst());
        } else {
            // 多个物品获取任务
            targetTask = TaskCatalogue.getSquashedItemTask(items.toArray(new ItemTarget[0]));
        }
        if (targetTask != null) {
            mod.runUserTask(targetTask, this::finish);
        } else {
            finish();
        }
    }

    @Override
    protected void call(AltoClef mod, ArgParser parser) throws CommandException {
        List<ItemTarget> items = parser.get(List.class);

        getItems(mod, items);
    }
}