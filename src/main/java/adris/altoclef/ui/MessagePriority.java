package adris.altoclef.ui;

/**
 * 消息优先级枚举
 * 定义不同类型消息的优先级，用于控制消息发送的顺序
 */
public enum MessagePriority {
    // 尽快发送（最高优先级）
    ASAP(3),
    // 及时发送（中等优先级）
    TIMELY(2),
    // 可选发送（低优先级）
    OPTIONAL(1),
    // 未授权（最低优先级，通常不会发送）
    UNAUTHORIZED(0);

    // 优先级重要性值
    private final int _importance;

    /**
     * 构造函数
     * @param importance 优先级重要性值
     */
    MessagePriority(int importance) {
        _importance = importance;
    }

    /**
     * 获取优先级重要性值
     * @return 重要性值
     */
    public int getImportance() {
        return _importance;
    }
}
