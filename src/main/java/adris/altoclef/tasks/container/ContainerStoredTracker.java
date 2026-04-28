package adris.altoclef.tasks.container;

import adris.altoclef.AltoClef;
import adris.altoclef.eventbus.EventBus;
import adris.altoclef.eventbus.Subscription;
import adris.altoclef.eventbus.events.SlotClickChangedEvent;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.slots.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.Arrays;
import java.util.HashMap;
import java.util.function.Predicate;

/**
 * 容器存储跟踪器
 * 用于跟踪已存入容器的物品数量
 */
public class ContainerStoredTracker {
    private final HashMap<Item, Integer> _totalDeposited = new HashMap<>();
    private final Predicate<Slot> _acceptDeposit;

    private Subscription<SlotClickChangedEvent> _slotClickChangedSubscription;

    /**
     * 构造函数
     * @param acceptDeposit 接受存入的槽位判定条件
     */
    public ContainerStoredTracker(Predicate<Slot> acceptDeposit) {
        _acceptDeposit = acceptDeposit;
    }

    /**
     * 跟踪物品数量变化
     * @param item 物品
     * @param delta 变化量（正数表示增加，负数表示减少）
     */
    private void trackChange(Item item, int delta) {
        _totalDeposited.put(item, _totalDeposited.getOrDefault(item, 0) + delta);
    }

    /**
     * 开始跟踪物品存入事件
     */
    public void startTracking() {
        _slotClickChangedSubscription = EventBus.subscribe(SlotClickChangedEvent.class, evt -> {
            Slot slot = evt.slot;
            if (!slot.isSlotInPlayerInventory() && _acceptDeposit.test(slot)) {
                ItemStack before = evt.before;
                ItemStack after = evt.after;
                if (before.getItem() != after.getItem()) {
                    // 前后物品不同！丢失了之前的物品，添加了所有新物品。
                    if (!before.isEmpty())
                        trackChange(before.getItem(), -1 * before.getCount());
                    if (!after.isEmpty())
                        trackChange(after.getItem(), after.getCount());
                } else {
                    // 前后物品相同，跟踪数量差异。
                    trackChange(after.getItem(), after.getCount() - before.getCount());
                }
            }
        });
    }

    /**
     * 停止跟踪物品存入事件
     */
    public void stopTracking() {
        EventBus.unsubscribe(_slotClickChangedSubscription);
    }

    /**
     * 获取满足条件的容器中已添加的物品数量
     * @param items 要查询的物品数组
     * @return 已添加的物品总数
     */
    public int getStoredCount(Item... items) {
        int result = 0;
        for (Item item : items) {
            result += _totalDeposited.getOrDefault(item, 0);
        }
        return result;
    }

    /**
     * 检查是否已存入足够数量的物品以满足目标要求
     * @param target 物品目标
     * @return 如果已存入足够数量则返回true，否则返回false
     */
    public boolean matches(ItemTarget target) {
        return getStoredCount(target.getMatches()) >= target.getTargetCount();
    }

    /**
     * 获取尚未存入但可以存入的物品目标
     * @param mod AltoClef实例
     * @param toStore 要存入的物品目标数组
     * @return 可以存入的物品目标数组
     */
    public ItemTarget[] getUnstoredItemTargetsYouCanStore(AltoClef mod, ItemTarget[] toStore) {
        return Arrays.stream(toStore)
                .filter(target -> !matches(target) && mod.getItemStorage().hasItem(target.getMatches()))
                // 如果数量不足，则将目标数量减少到我们实际能添加的数量
                .map(target -> mod.getItemStorage().getItemCount(target) < target.getTargetCount() ? new ItemTarget(target, mod.getItemStorage().getItemCount(target)) : target)
                .toArray(ItemTarget[]::new);
    }
}
