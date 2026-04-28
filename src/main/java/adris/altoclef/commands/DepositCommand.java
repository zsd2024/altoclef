package adris.altoclef.commands;

import adris.altoclef.AltoClef;
import adris.altoclef.commandsystem.*;
import adris.altoclef.commandsystem.args.ItemTargetArg;
import adris.altoclef.commandsystem.args.ListArg;
import adris.altoclef.commandsystem.exception.CommandException;
import adris.altoclef.tasks.container.StoreInAnyContainerTask;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.helpers.StorageHelper;
import adris.altoclef.util.slots.PlayerSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolItem;
import org.apache.commons.lang3.ArrayUtils;

import java.util.List;

/**
 * 存储命令 - 将物品存入容器中
 */
public class DepositCommand extends Command {
    public DepositCommand() {
        // deposit命令，用于存储我们的物品
        super("deposit", "存储我们所有的物品",
                new ListArg<>(new ItemTargetArg("itemStack"), "物品列表", null, false)
        );
    }

    /**
     * 获取所有非装备或工具类物品作为目标
     * @param mod AltoClef模块
     * @return ItemTarget数组
     */
    public static ItemTarget[] getAllNonEquippedOrToolItemsAsTarget(AltoClef mod) {
        return StorageHelper.getAllInventoryItemsAsTargets(slot -> {
            // 忽略护甲
            if (ArrayUtils.contains(PlayerSlot.ARMOR_SLOTS, slot))
                return false;
            ItemStack stack = StorageHelper.getItemStackInSlot(slot);
            // 忽略工具
            if (!stack.isEmpty()) {
                Item item = stack.getItem();
                return !(item instanceof ToolItem);
            }
            return false;
        });
    }

    @Override
    protected void call(AltoClef mod, ArgParser parser) throws CommandException {
        List<ItemTarget> itemList = parser.get(List.class);

        ItemTarget[] items;
        if (itemList == null) {
            // 如果没有指定物品列表，则获取所有非装备或工具类物品
            items = getAllNonEquippedOrToolItemsAsTarget(mod);
        } else {
            items = itemList.toArray(ItemTarget[]::new);
        }

        // 执行存储任务
        mod.runUserTask(new StoreInAnyContainerTask(false, items), this::finish);
    }
}
