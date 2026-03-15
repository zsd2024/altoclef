package adris.altoclef.butler;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.eventbus.EventBus;
import adris.altoclef.eventbus.events.ChatMessageEvent;
import adris.altoclef.eventbus.events.TaskFinishedEvent;
import adris.altoclef.ui.MessagePriority;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.message.MessageType;
import net.minecraft.world.World;

import java.util.Objects;

/**
 * 助手系统允许授权玩家向机器人发送指令以执行。
 * <p>
 * 这有效地使机器人功能像一个仆人或管家。
 * <p>
 * 授权定义在 "altoclef_butler_whitelist.txt" 和 "altoclef_butler_blacklist.txt" 中
 * 并取决于 "altoclef_settings.json" 中的 "useButlerWhitelist" 和 "useButlerBlacklist" 设置
 */
public class Butler {

    private static final String BUTLER_MESSAGE_START = "` "; // 助手消息开头标记

    private final AltoClef mod; // AltoClef 主模块引用

    private final WhisperChecker whisperChecker = new WhisperChecker(); // 私聊检查器

    private final UserAuth userAuth; // 用户认证系统

    private String currentUser = null; // 当前用户

    // 指令逻辑的实用变量
    private boolean commandInstantRan = false; // 指令是否立即执行
    private boolean commandFinished = false; // 指令是否完成

    public Butler(AltoClef mod) {
        this.mod = mod;
        userAuth = new UserAuth(mod);

        // 每当任务完成时撤销当前用户
        EventBus.subscribe(TaskFinishedEvent.class, evt -> {
            if (currentUser != null) {
                currentUser = null;
            }
        });

        // 接收系统事件
        EventBus.subscribe(ChatMessageEvent.class, evt -> {
            boolean debug = ButlerConfig.getInstance().whisperFormatDebug;
            String message = evt.messageContent();
            String sender = evt.senderName();
            MessageType messageType = evt.messageType();
            String receiver = mod.getPlayer().getName().getString();
            if (sender != null && !Objects.equals(sender, receiver) && shouldAccept(messageType)) {
                String wholeMessage = sender + " " + receiver + " " + message;
                if (debug) {
                    Debug.logMessage("接收到私聊: \"" + wholeMessage + "\".");
                }
                this.mod.getButler().receiveMessage(wholeMessage, receiver);
            }
        });
    }

    /**
 * 判断是否接受消息类型
 * @param messageType 消息类型
 * @return 是否接受该消息类型
 */
private static boolean shouldAccept(MessageType messageType) {
        //#if MC >= 11904
        return messageType.chat().style().isItalic()
                && messageType.chat().style().getColor() != null
                && Objects.equals(messageType.chat().style().getColor().getName(), "gray");
        //#else
        //$$ //it doesnt look like previous versions did any type of checking
        //$$ return true;
        //#endif
    }

    /**
 * 接收消息处理
 * @param msg 消息内容
 * @param receiver 接收者
 */
private void receiveMessage(String msg, String receiver) {
        // 格式: <USER> whispers to you: <MESSAGE>
        // 格式: <USER> whispers: <MESSAGE>
        WhisperChecker.MessageResult result = this.whisperChecker.receiveMessage(mod, receiver, msg);
        if (result != null) {
            this.receiveWhisper(result.from, result.message);
        } else if (ButlerConfig.getInstance().whisperFormatDebug) {
            Debug.logMessage("    未解析: 未找到消息格式。");
        }
    }

    /**
 * 接收私聊消息
 * @param username 用户名
 * @param message 消息内容
 */
private void receiveWhisper(String username, String message) {

        boolean debug = ButlerConfig.getInstance().whisperFormatDebug;
        // 忽略来自其他机器人的消息。
        if (message.startsWith(BUTLER_MESSAGE_START)) {
            if (debug) {
                Debug.logMessage("    拒绝: 消息被检测为来自另一个机器人。");
            }
            return;
        }

        if (userAuth.isUserAuthorized(username)) {
            executeWhisper(username, message);
        } else {
            if (debug) {
                Debug.logMessage("    拒绝: 用户 \"" + username + "\" 未授权。");
            }
            if (ButlerConfig.getInstance().sendAuthorizationResponse) {
                sendWhisper(username, ButlerConfig.getInstance().failedAuthorizationResposne.replace("{from}", username), MessagePriority.UNAUTHORIZED);
            }
        }
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    /**
     * 检查用户是否已授权
     * @param username 用户名
     * @return 是否授权
     */
    public boolean isUserAuthorized(String username) {
        return userAuth.isUserAuthorized(username);
    }

    /**
     * 记录日志消息
     * @param message 消息内容
     * @param priority 消息优先级
     */
    public void onLog(String message, MessagePriority priority) {
        if (currentUser != null) {
            sendWhisper(message, priority);
        }
    }

    /**
     * 记录警告日志消息
     * @param message 消息内容
     * @param priority 消息优先级
     */
    public void onLogWarning(String message, MessagePriority priority) {
        if (currentUser != null) {
            sendWhisper("[警告:] " + message, priority);
        }
    }

    /**
     * 系统时钟更新
     */
    public void tick() {
        // 暂无内容。
    }

    /**
     * 获取当前用户
     * @return 当前用户
     */
    public String getCurrentUser() {
        return currentUser;
    }

    /**
     * 检查是否存在当前用户
     * @return 是否存在当前用户
     */
    public boolean hasCurrentUser() {
        return currentUser != null;
    }

    /**
 * 执行私聊指令
 * @param username 用户名
 * @param message 指令消息
 */
private void executeWhisper(String username, String message) {
        String prevUser = currentUser;
        commandInstantRan = true;
        commandFinished = false;
        currentUser = username;
        sendWhisper("指令执行中: " + message, MessagePriority.TIMELY);

        String prefix = mod.getModSettings().getCommandPrefix();
        AltoClef.getCommandExecutor().execute(prefix + message, () -> {
            // 完成时
            sendWhisper("指令完成: " + message, MessagePriority.TIMELY);
            if (!commandInstantRan) {
                currentUser = null;
            }
            commandFinished = true;
        }, e -> {
            for (String msg : e.getMessage().split("\n")) {
                sendWhisper("任务失败: " + msg, MessagePriority.ASAP);
            }
            e.printStackTrace();
            currentUser = null;
            commandInstantRan = false;
        });
        commandInstantRan = false;
        // 仅在仍在运行时设置当前用户。
        if (commandFinished) {
            currentUser = prevUser;
        }
    }

    /**
 * 发送私聊消息
 * @param message 消息内容
 * @param priority 消息优先级
 */
private void sendWhisper(String message, MessagePriority priority) {
        if (currentUser != null) {
            sendWhisper(currentUser, message, priority);
        } else {
            Debug.logWarning("发送助手消息失败，因为没有用户存在: " + message);
        }
    }

    /**
     * 发送私聊消息给指定用户
     * @param username 用户名
     * @param message 消息内容
     * @param priority 消息优先级
     */
    private void sendWhisper(String username, String message, MessagePriority priority) {
      mod.getMessageSender().enqueueWhisper(username, BUTLER_MESSAGE_START + message, priority);
    }
}
