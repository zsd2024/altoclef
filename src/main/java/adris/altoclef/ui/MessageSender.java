package adris.altoclef.ui;

import adris.altoclef.Debug;
import adris.altoclef.multiversion.entity.PlayerVer;
import adris.altoclef.util.time.BaseTimer;
import adris.altoclef.util.time.TimerReal;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;

import java.util.Comparator;
import java.util.PriorityQueue;

/**
 * 消息发送器
 * 由于服务器会踢出频繁发送消息的客户端，因此我们采用延迟队列的方式发送消息。
 */
public class MessageSender {

    // 快速发送限制：在短暂暂停前可以快速发送的消息数量
    private static final int FAST_LIMIT = 6;
    // 慢速发送限制：在长时间暂停前可以发送的批次数量
    private static final int SLOW_LIMIT = 3;

    // 消息优先队列，按优先级和索引排序
    private final PriorityQueue<BaseMessage> whisperQueue = new PriorityQueue<>(
            Comparator.comparingInt((BaseMessage msg) -> msg.priority.getImportance())
                    .thenComparingInt(msg -> msg.index)
    );

    // 发送定时器
    private final BaseTimer fastSendTimer = new TimerReal(0.3f); // 快速发送间隔（秒）
    private final BaseTimer bigSendTimer = new TimerReal(3.5);   // 批次发送间隔（秒）
    private final BaseTimer bigBigSendTimer = new TimerReal(10); // 长时间发送间隔（秒）

    private int messageCounter = 0; // 消息计数器，用于保持发送顺序

    private int fastCount; // 快速发送计数
    private int slowCount; // 慢速发送计数

    /**
     * 游戏刻度更新方法
     * 检查是否可以发送消息，如果可以则从队列中取出最高优先级的消息进行发送
     */
    public void tick() {
        if (canSendMessage()) {
            if (!whisperQueue.isEmpty()) {
                BaseMessage msg = whisperQueue.poll();
                assert msg != null;
                sendChatUpdateTimers(msg);
            }
        }
    }

    /**
     * 将私信消息加入队列
     * @param username 接收用户名
     * @param message 消息内容
     * @param priority 消息优先级
     */
    public void enqueueWhisper(String username, String message, MessagePriority priority) {
        whisperQueue.add(new Whisper(username, message, priority, messageCounter++));
    }

    /**
     * 将聊天消息加入队列
     * @param message 消息内容
     * @param priority 消息优先级
     */
    public void enqueueChat(String message, MessagePriority priority) {
        whisperQueue.add(new ChatMessage(message, priority, messageCounter++));
    }

    /**
     * 检查是否可以发送消息
     * 所有定时器都必须已过期才能发送消息
     * @return 是否可以发送消息
     */
    private boolean canSendMessage() {
        return bigBigSendTimer.elapsed() && bigSendTimer.elapsed() && fastSendTimer.elapsed();
    }

    /**
     * 发送聊天消息并更新定时器
     * @param message 消息对象
     */
    private void sendChatUpdateTimers(BaseMessage message) {
        sendChatInstant(message.getChatInput(), message instanceof Whisper);
        fastSendTimer.reset();
        fastCount++;
        // 如果达到快速发送限制，重置批次定时器
        if (fastCount >= FAST_LIMIT) {
            bigSendTimer.reset();
            fastCount = 0;
            slowCount++;
            // 如果达到慢速发送限制，重置长时间定时器
            if (slowCount >= SLOW_LIMIT) {
                bigBigSendTimer.reset();
                slowCount = 0;
            }
        }
    }

    /**
     * 立即发送聊天消息
     * @param message 消息内容
     * @param command 是否为命令（私信）
     */
    private void sendChatInstant(String message, boolean command) {
        if (MinecraftClient.getInstance().player == null) {
            Debug.logError("无法发送聊天消息，因为客户端未加载。");
            return;
        }

        ClientPlayNetworkHandler networkHandler =  MinecraftClient.getInstance().getNetworkHandler();
        assert networkHandler != null;

        if (command) {
            // 发送命令（私信）
            PlayerVer.sendChatCommand(MinecraftClient.getInstance().player, message);
        } else {
            // 发送普通聊天消息
            PlayerVer.sendChatMessage(MinecraftClient.getInstance().player, message);
        }
    }

    /**
     * 基础消息类
     * 所有消息类型的基类
     */
    private static abstract class BaseMessage {
        public MessagePriority priority; // 消息优先级
        public int index; // 消息索引（用于保持顺序）

        /**
         * 构造函数
         * @param priority 消息优先级
         * @param index 消息索引
         */
        public BaseMessage(MessagePriority priority, int index) {
            this.priority = priority;
            this.index = index;
        }

        /**
         * 获取聊天输入内容
         * @return 聊天输入字符串
         */
        public abstract String getChatInput();
    }

    /**
     * 私信消息类
     */
    private static class Whisper extends BaseMessage {
        public String username; // 接收用户名
        public String message;  // 消息内容

        /**
         * 构造函数
         * @param username 接收用户名
         * @param message 消息内容
         * @param priority 消息优先级
         * @param index 消息索引
         */
        public Whisper(String username, String message, MessagePriority priority, int index) {
            super(priority, index);
            this.username = username;
            this.message = message;
        }

        @Override
        public String getChatInput() {
            // 返回私信命令格式：msg 用户名 消息
            return "msg " + username + " " + message;
        }
    }

    /**
     * 聊天消息类
     */
    private static class ChatMessage extends BaseMessage {

        public String message; // 消息内容

        /**
         * 构造函数
         * @param message 消息内容
         * @param priority 消息优先级
         * @param index 消息索引
         */
        public ChatMessage(String message, MessagePriority priority, int index) {
            super(priority, index);
            this.message = message;
        }

        @Override
        public String getChatInput() {
            // 返回普通聊天消息
            return message;
        }
    }
}
