package adris.altoclef.eventbus.events;

import net.minecraft.network.message.MessageType;

/**
 * 聊天消息事件
 * 当聊天消息出现时触发此事件
 */
public class ChatMessageEvent {
    /** 聊天消息内容 */
    private final String message;
    /** 发送者名称 */
    private final String senderName;
    /** 消息类型 */
    private final MessageType messageType;

    /**
     * 构造函数
     * @param message 聊天消息内容
     * @param senderName 发送者名称
     * @param messageType 消息类型
     */
    public ChatMessageEvent(String message, String senderName, MessageType messageType) {
        this.message = message;
        this.senderName = senderName;
        this.messageType = messageType;
    }
    
    /**
     * 获取聊天消息内容
     * @return 聊天消息内容
     */
    public String messageContent() {
        return message;
    }

    /**
     * 获取发送者名称
     * @return 发送者名称
     */
    public String senderName() {
        return senderName;
    }

    /**
     * 获取消息类型
     * @return 消息类型
     */
    public MessageType messageType() {
        return messageType;
    }
}
