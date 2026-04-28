package adris.altoclef.eventbus;

import java.util.function.Consumer;

/**
 * 事件订阅的包装对象
 * 
 * @param &lt;T&gt; 事件类型
 */
public class Subscription<T> {
    // 事件处理回调函数
    private final Consumer<T> callback;
    // 标记是否应该删除此订阅
    private boolean shouldDelete;

    /**
     * 构造函数
     * 
     * @param callback 事件处理回调函数
     */
    public Subscription(Consumer<T> callback) {
        this.callback = callback;
    }

    /**
     * 接收并处理事件
     * 
     * @param event 事件对象
     */
    public void accept(T event) {
        callback.accept(event);
    }

    /**
     * 标记此订阅为删除状态
     */
    public void delete() {
        shouldDelete = true;
    }

    /**
     * 检查此订阅是否应该被删除
     * 
     * @return 如果应该删除则返回true，否则返回false
     */
    public boolean shouldDelete() {
        return shouldDelete;
    }
}

    public void accept(T event) {
        callback.accept(event);
    }

    public void delete() {
        shouldDelete = true;
    }

    public boolean shouldDelete() {
        return shouldDelete;
    }
}
