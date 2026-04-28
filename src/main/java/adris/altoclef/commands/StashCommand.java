package adris.altoclef.commands;

import adris.altoclef.AltoClef;
import adris.altoclef.commandsystem.*;
import adris.altoclef.commandsystem.args.IntArg;
import adris.altoclef.commandsystem.args.ItemTargetArg;
import adris.altoclef.commandsystem.args.ListArg;
import adris.altoclef.commandsystem.exception.CommandException;
import adris.altoclef.tasks.container.StoreInStashTask;
import adris.altoclef.util.BlockRange;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.helpers.WorldHelper;
import net.minecraft.util.math.BlockPos;

import java.util.List;

/**
 * 藏匿处命令
 * 用于将物品存储到箱子/容器藏匿处中
 */
public class StashCommand extends Command {
    /**
     * 构造函数
     * 初始化命令名称为"stash"，描述为"将物品存储到箱子/容器藏匿处中。如果物品列表为空，则存入所有非装备物品。"
     * 参数包括起始坐标(x_start, y_start, z_start)、结束坐标(x_end, y_end, z_end)和可选的物品列表
     * 
     * @throws CommandException 命令初始化异常
     */
    public StashCommand() throws CommandException {
        // stash <stash_x> <stash_y> <stash_z> <stash_radius> [item list]
        super("stash", "将物品存储到箱子/容器藏匿处中。如果物品列表为空，则存入所有非装备物品。",
                new IntArg("x_start"),
                new IntArg("y_start"),
                new IntArg("z_start"),
                new IntArg("x_end"),
                new IntArg("y_end"),
                new IntArg("z_end"),
                new ListArg<>(new ItemTargetArg("stack"), "物品列表（为空则存入全部）", null, false)
        );
    }

    /**
     * 执行命令逻辑
     * 
     * @param mod AltoClef主模块实例
     * @param parser 命令参数解析器
     * @throws CommandException 命令执行异常
     */
    @Override
    protected void call(AltoClef mod, ArgParser parser) throws CommandException {
        // 解析起始坐标
        BlockPos start = new BlockPos(
                parser.get(Integer.class),
                parser.get(Integer.class),
                parser.get(Integer.class)
        );
        // 解析结束坐标
        BlockPos end = new BlockPos(
                parser.get(Integer.class),
                parser.get(Integer.class),
                parser.get(Integer.class)
        );

        // 获取物品列表
        List<ItemTarget> itemList = parser.get(List.class);

        ItemTarget[] items;
        if (itemList == null) {
            // 如果物品列表为空，获取所有非装备或工具物品
            items = DepositCommand.getAllNonEquippedOrToolItemsAsTarget(mod);
        } else {
            // 否则使用指定的物品列表
            items = itemList.toArray(new ItemTarget[0]);
        }

        // 运行存储到藏匿处的任务
        mod.runUserTask(new StoreInStashTask(true, new BlockRange(start, end, WorldHelper.getCurrentDimension()), items), this::finish);
    }
}
