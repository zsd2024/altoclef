package adris.altoclef.tasks.slot;

import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.slots.Slot;

/**
 * 从玩家背包中移动物品到指定槽位的任务
 */
public class MoveItemToSlotFromInventoryTask extends MoveItemToSlotTask {
    /**
     * 构造函数
     * @param toMove 要移动的物品目标
     * @param destination 目标槽位
     */
    public MoveItemToSlotFromInventoryTask(ItemTarget toMove, Slot destination) {
        // 调用父类构造函数，指定从玩家背包中获取物品的槽位
        super(toMove, destination, mod -> mod.getItemStorage().getSlotsWithItemPlayerInventory(false, toMove.getMatches()));
    }
}
