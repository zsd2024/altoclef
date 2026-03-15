package adris.altoclef.tasks.slot;

import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.slots.Slot;

/**
 * 从容器中移动物品到指定槽位的任务
 */
public class MoveItemToSlotFromContainerTask extends MoveItemToSlotTask {
    /**
     * 构造函数
     * @param toMove 要移动的物品目标
     * @param destination 目标槽位
     */
    public MoveItemToSlotFromContainerTask(ItemTarget toMove, Slot destination) {
        // 调用父类构造函数，指定从容器中获取物品的槽位
        super(toMove, destination, mod -> mod.getItemStorage().getSlotsWithItemContainer(toMove.getMatches()));
    }
}
