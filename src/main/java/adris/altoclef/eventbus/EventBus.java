package adris.altoclef.eventbus;

import net.minecraft.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

/**
 * 事件总线主类，用于解决依赖问题。
 * 允许我们在全局范围内发送和接收事件，从而解耦代码库。
 * <p>
 * 从技术上讲，`ConfigHelper` 也做了类似的事情，但这里是一个更通用的情况。
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class EventBus {

    // 存储事件类型与其订阅者列表的映射
    private static final HashMap<Class, List<Subscription>> topics = new HashMap<>();
    // 存储待添加的订阅（在发布事件期间）
    private static final List<Pair<Class, Subscription>> toAdd = new ArrayList<>();
    // 锁定标志，防止在迭代订阅列表时修改列表
    private static boolean lock;

    /**
     * 发布事件到所有订阅者
     * 
     * @param event 要发布的事件对象
     */
    public static <T> void publish(T event) {
        Class type = event.getClass();

        // 添加所有需要添加的订阅
        for (Pair<Class, Subscription> toAdd : toAdd) {
            subscribeInternal(toAdd.getLeft(), toAdd.getRight());
        }
        toAdd.clear();

        if (topics.containsKey(type)) {
            List<Subscription> subscribers = topics.get(type);

            // 在调用订阅时可能会删除订阅
            List<Subscription> toDelete = new ArrayList<>();

            // 遍历订阅列表。在迭代过程中不应修改列表。
            lock = true;
            for (Subscription subRaw : subscribers) {
                Subscription<T> sub;
                try {
                    sub = (Subscription<T>) subRaw;
                    if (sub.shouldDelete()) {
                        toDelete.add(sub);
                    } else {
                        sub.accept(event);
                    }
                } catch (ClassCastException e) {
                    System.err.println("尝试发布类型不匹配的事件: " + event);
                    e.printStackTrace();
                }
            }
            // 删除所有标记为删除的订阅
            lock = false;
        }
    }

    /**
     * 内部订阅方法，将订阅添加到指定事件类型的订阅列表中
     * 
     * @param type 事件类型
     * @param sub 订阅对象
     */
    private static <T> void subscribeInternal(Class<T> type, Subscription<T> sub) {
        if (!topics.containsKey(type)) {
            topics.put(type, new ArrayList<>());
        }
        topics.get(type).add(sub);
    }

    /**
     * 订阅指定类型的事件
     * 
     * @param type 事件类型
     * @param consumeEvent 事件处理回调
     * @return 订阅对象，可用于取消订阅
     */
    public static <T> Subscription<T> subscribe(Class<T> type, Consumer<T> consumeEvent) {
        Subscription<T> sub = new Subscription<>(consumeEvent);
        if (lock) {
            // 如果正在发布事件，则将订阅加入待添加列表
            toAdd.add(new Pair<>(type, sub));
        } else {
            subscribeInternal(type, sub);
        }
        return sub;
    }

    /**
     * 取消订阅
     * 
     * @param subscription 要取消的订阅对象
     */
    public static <T> void unsubscribe(Subscription<T> subscription) {
        if (subscription != null)
            subscription.delete();
    }
}
        toAdd.clear();

        if (topics.containsKey(type)) {
            List<Subscription> subscribers = topics.get(type);

            // Subscriptions can be deleted while they're called
            List<Subscription> toDelete = new ArrayList<>();

            // Go through our subscription list. We shouldn't modify the list while we're iterating it.
            lock = true;
            for (Subscription subRaw : subscribers) {
                Subscription<T> sub;
                try {
                    sub = (Subscription<T>) subRaw;
                    if (sub.shouldDelete()) {
                        toDelete.add(sub);
                    } else {
                        sub.accept(event);
                    }
                } catch (ClassCastException e) {
                    System.err.println("TRIED PUBLISHING MISMAPPED EVENT: " + event);
                    e.printStackTrace();
                }
            }
            // Delete all subscriptions
            lock = false;
        }
    }

    private static <T> void subscribeInternal(Class<T> type, Subscription<T> sub) {
        if (!topics.containsKey(type)) {
            topics.put(type, new ArrayList<>());
        }
        topics.get(type).add(sub);
    }

    public static <T> Subscription<T> subscribe(Class<T> type, Consumer<T> consumeEvent) {
        Subscription<T> sub = new Subscription<>(consumeEvent);
        if (lock) {
            toAdd.add(new Pair<>(type, sub));
        } else {
            subscribeInternal(type, sub);
        }
        return sub;
    }

    public static <T> void unsubscribe(Subscription<T> subscription) {
        if (subscription != null)
            subscription.delete();
    }
}
