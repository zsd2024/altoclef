package adris.altoclef.butler;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.util.time.TimerGame;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 私聊检查器类，用于解析和验证私聊消息格式
 */
public class WhisperChecker {

    private static final TimerGame _repeatTimer = new TimerGame(0.1); // 重复消息计时器

    private static String _lastMessage = null; // 上一条消息

    /**
     * 尝试解析私聊消息
     * @param ourUsername 我们的用户名
     * @param whisperFormat 私聊格式
     * @param message 消息内容
     * @return 解析结果
     */
    // 这个没有正确工作，所以我不使用复杂正则表达式重写了它 -miran
    public static MessageResult tryParse(String ourUsername, String whisperFormat, String message) {
        List<String> parts = new ArrayList<>(List.of("{from}", "{to}", "{message}")); // 定义格式部分

        // 按在whisperFormat中出现的顺序排序。
        parts.sort(Comparator.comparingInt(whisperFormat::indexOf));
        parts.removeIf(part -> !whisperFormat.contains(part));

        ArrayList<String> messageParts = new ArrayList<>(Arrays.stream(message.split(" ")).toList());
        MessageResult result = new MessageResult(); // 创建解析结果
        for (int i = 0; i < parts.size(); i++) {
            String part = parts.get(i);
            if (messageParts.isEmpty()) return null;

            if (part.equals("{from}")) {
                result.from = messageParts.remove(0); // 发送者
            } else if (part.equals("{to}")) {
                String toUser = messageParts.remove(0);
                if (!toUser.equals(ourUsername)) {
                    Debug.logInternal("拒绝消息，因为它发送给 " + toUser + " 而不是 " + ourUsername);
                    return null;
                }
            } else if (part.equals("{message}")) {
                List<String> messageList = messageParts.subList(0,messageParts.size()-(parts.size()-i-1));

                StringBuilder msg = new StringBuilder(messageList.get(0));

                for (int j = 1; j < messageList.size(); j++) {
                    msg.append(" ").append(messageList.get(j));
                }

                result.message = msg.toString(); // 消息内容
            } else {
                throw new IllegalArgumentException("未知部分: "+part);
            }

        }

        return result;
    }

    /**
     * 接收消息并尝试解析
     * @param mod AltoClef 主模块
     * @param ourUsername 我们的用户名
     * @param msg 消息内容
     * @return 解析结果
     */
    public MessageResult receiveMessage(AltoClef mod, String ourUsername, String msg) {
        String foundMiddlePart = "";
        int index = -1;

        boolean duplicate = (msg.equals(_lastMessage));
        if (duplicate && !_repeatTimer.elapsed()) {
            _repeatTimer.reset();
            // 这可能是一个实际重复。我不知道为什么会出现这些，但确实如此。
            return null;
        }

        _lastMessage = msg;

        for (String format : ButlerConfig.getInstance().whisperFormats) {
            MessageResult check = tryParse(ourUsername, format, msg);
            if (check != null) {
                String user = check.from;
                String message = check.message;
                if (user == null || message == null) break;
                return check;
            }
        }

        return null;
    }

    /**
     * 消息解析结果类
     */
    public static class MessageResult {
        public String from; // 发送者
        public String message; // 消息内容

        @Override
        /**
         * 返回结果的字符串表示
         * @return 字符串表示
         */
        public String toString() {
            return "MessageResult{" +
                    "from='" + from + '\'' +
                    ", message='" + message + '\'' +
                    '}';
        }
    }
}
