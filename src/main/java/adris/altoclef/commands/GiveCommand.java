package adris.altoclef.commands;

import adris.altoclef.AltoClef;
import adris.altoclef.commandsystem.GotoTarget;
import adris.altoclef.commandsystem.args.*;
import adris.altoclef.commandsystem.ArgParser;
import adris.altoclef.commandsystem.Command;
import adris.altoclef.commandsystem.exception.CommandException;
import adris.altoclef.tasks.entity.GiveItemToPlayerTask;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.helpers.ItemHelper;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * 给予命令 - 收集物品并给予你或其他人
 */
public class GiveCommand extends Command {

    public GiveCommand() {
        super("give", "收集物品并给予你或其他人",
                new StringArg("username"),
                new ListArg<>(new ItemTargetArg("items"), "items"),
                new GoToTargetArg("cordinates", null, false)
        );
    }

    @Override
    protected void call(AltoClef mod, ArgParser parser) throws CommandException {
        mod.logWarning("此命令已弃用!");
        // 解析目标用户名或回退到当前butler用户
        String username = parser.get(String.class);
        // FIXME 参数默认值未设置为可以工作的方式
        if (username == null) {
            if (mod.getButler().hasCurrentUser()) {
                username = mod.getButler().getCurrentUser();
            } else {
                mod.logWarning("当前没有butler用户。在没有用户参数的情况下运行此命令只能通过butler执行。");
                finish();
                return;
            }
        }
        // 解析请求的物品列表
        List<ItemTarget> requested = parser.get(List.class);
        if (requested == null || requested.isEmpty()) {
            mod.logWarning("未指定要给予的物品。");
            finish();
            return;
        }

        // 对于每个请求的物品，检查库存中是否有匹配的物品需要注册
        List<ItemTarget> resolved = new ArrayList<>();
        for (ItemTarget req : requested) {
            ItemTarget best = req;
            for (int i = 0; i < mod.getPlayer().getInventory().size(); i++) {
                ItemStack stack = mod.getPlayer().getInventory().getStack(i);
                if (!stack.isEmpty()) {
                    String invName = ItemHelper.stripItemName(stack.getItem());
                    if (invName.equalsIgnoreCase(req.getCatalogueName())) {
                        best = new ItemTarget(stack.getItem(), req.getTargetCount());
                        break;
                    }
                }
            }
            resolved.add(best);
        }
        GotoTarget cordinates = parser.get(GotoTarget.class);

        // 为每个解析的物品提交一个给予任务
        for (ItemTarget target : resolved) {
            if (cordinates == null) {
                mod.log(String.format("用户: %s : 物品: %s x %d.", username, target.getCatalogueName(), target.getTargetCount()));
                mod.runUserTask(new GiveItemToPlayerTask(username, target), this::finish);
            } else {
                mod.log(String.format("用户: %s : 物品: %s x %d. 最终位置: %d, %d, %d", username, target.getCatalogueName(), target.getTargetCount(), cordinates.getX(), cordinates.getY(), cordinates.getZ()));
                mod.runUserTask(new GiveItemToPlayerTask(username, cordinates, target), this::finish);
            }
        }
    }
}