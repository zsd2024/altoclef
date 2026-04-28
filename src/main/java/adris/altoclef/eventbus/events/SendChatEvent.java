package adris.altoclef.eventbus.events;

/**
 * 发送聊天事件
 * 当玩家尝试发送聊天消息时触发此事件
 */
public class SendChatEvent {
    /** 要发送的聊天消息内容 */
    public String message;
    /** 是否已取消发送 */
    private boolean cancelled;

    /**
     * 构造函数
     * @param message 要发送的聊天消息内容
     */
    public SendChatEvent(String message) {
        this.message = message;
    }

    /**
     * 取消聊天消息的发送
     */
    public void cancel() {
        cancelled = true;
    }

    /**
     * 检查聊天消息是否已被取消发送
     * @return 如果消息已被取消则返回true，否则返回false
     */
    public boolean isCancelled() {
        return cancelled;
    }
}
