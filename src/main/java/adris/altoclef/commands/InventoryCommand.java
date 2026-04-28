package adris.altoclef.commands;

import adris.altoclef.AltoClef;
import adris.altoclef.TaskCatalogue;
import adris.altoclef.commandsystem.ArgParser;
import adris.altoclef.commandsystem.Command;
import adris.altoclef.commandsystem.args.CataloguedItemArg;
import adris.altoclef.commandsystem.exception.CommandException;
import adris.altoclef.ui.MessagePriority;
import adris.altoclef.util.helpers.ItemHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.HashMap;

/**
 * 库存命令 - 打印机器人库存或返回机器人拥有某物品的数量
 */
public class InventoryCommand extends Command {
    public InventoryCommand() {
        super("inventory", "打印机器人的库存或返回机器人拥有某物品的数量",
                new CataloguedItemArg("item", null, false)
        );
    }

    @Override
    protected void call(AltoClef mod, ArgParser parser) throws CommandException {
        String item = parser.get(String.class);
        if (item == null) {
            // 打印库存
            // 获取物品数量
            HashMap<String, Integer> counts = new HashMap<>();
            for (int i = 0; i < mod.getPlayer().getInventory().size(); ++i) {
                ItemStack stack = mod.getPlayer().getInventory().getStack(i);
                if (!stack.isEmpty()) {
                    String name = ItemHelper.stripItemName(stack.getItem());
                    if (!counts.containsKey(name)) counts.put(name, 0);
                    counts.put(name, counts.get(name) + stack.getCount());
                }
            }
            // 打印
            mod.log("库存: ", MessagePriority.OPTIONAL);
            for (String name : counts.keySet()) {
                mod.log(name + " : " + counts.get(name), MessagePriority.OPTIONAL);
            }
            mod.log("(库存列表已发送) ", MessagePriority.OPTIONAL);
        } else {
            // 打印物品数量
            Item[] matches = TaskCatalogue.getItemMatches(item);
            if (matches == null || matches.length == 0) {
                mod.logWarning("物品 \"" + item + "\" 未被记录/未被识别。");
                finish();
                return;
            }
            int count = mod.getItemStorage().getItemCount(matches);
            if (count == 0) {
                mod.log(item + " 数量: (无)");
            } else {
                mod.log(item + " 数量: " + count);
            }
        }
        finish();
    }
}