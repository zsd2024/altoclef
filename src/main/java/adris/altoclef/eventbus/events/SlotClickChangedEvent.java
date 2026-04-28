package adris.altoclef.eventbus.events;

import adris.altoclef.util.slots.Slot;
import net.minecraft.item.ItemStack;

/**
 * 槽位点击变化事件
 * 当玩家点击物品槽位导致槽位内容发生变化时触发此事件
 */
public class SlotClickChangedEvent {
    /** 发生变化的槽位 */
    public Slot slot;
    /** 变化前的物品堆栈 */
    public ItemStack before;
    /** 变化后的物品堆栈 */
    public ItemStack after;

    /**
     * 构造槽位点击变化事件
     * 
     * @param slot 发生变化的槽位
     * @param before 变化前的物品堆栈
     * @param after 变化后的物品堆栈
     */
    public SlotClickChangedEvent(Slot slot, ItemStack before, ItemStack after) {
        this.slot = slot;
        this.before = before;
        this.after = after;
    }
}
