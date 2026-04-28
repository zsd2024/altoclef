package adris.altoclef.commandsystem;

import adris.altoclef.TaskCatalogue;
import adris.altoclef.commandsystem.exception.CommandException;
import adris.altoclef.util.ItemTarget;

import java.util.HashMap;

/**
 * 物品列表类
 * 用于存储和管理一组物品目标（ItemTarget）
 */
public class ItemList {
    /** 物品目标数组 */
    public ItemTarget[] items;

    /**
     * 构造函数
     * @param items 物品目标数组
     */
    public ItemList(ItemTarget[] items) {
        this.items = items;
    }

}
