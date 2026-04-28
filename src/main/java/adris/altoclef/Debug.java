package adris.altoclef;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

/**
 * 调试工具类
 * 
 * 提供日志记录和调试信息输出功能，支持不同级别的日志（调试、警告、错误）
 * 可以在游戏内聊天窗口和控制台同时输出日志信息
 */
public class Debug {

    // 日志级别常量
    private static final int DEBUG_LOG_LEVEL = 0;  // 调试日志级别
    private static final int WARN_LOG_LEVEL = 1;   // 警告日志级别
    private static final int ERROR_LOG_LEVEL = 2;  // 错误日志级别

    /**
     * 记录内部日志消息到控制台
     * @param message 日志消息内容
     */
    public static void logInternal(String message) {
        if (canLog(DEBUG_LOG_LEVEL)) {
            System.out.println("ALTO CLEF: " + message);
        }
    }

    /**
     * 使用格式化字符串记录内部日志消息
     * @param format 格式化字符串
     * @param args 格式化参数
     */
    public static void logInternal(String format, Object... args) {
        logInternal(String.format(format, args));
    }

    /**
     * 获取日志前缀
     * @return 日志前缀字符串
     */
    private static String getLogPrefix() {
        AltoClef altoClef = AltoClef.getInstance();
        if (altoClef != null) {
            return altoClef.getModSettings().getChatLogPrefix();
        }
        return "[Alto Clef] ";
    }

    /**
     * 记录消息到游戏聊天窗口或控制台
     * @param message 消息内容
     * @param prefix 是否添加前缀
     */
    public static void logMessage(String message, boolean prefix) {
        if (MinecraftClient.getInstance() != null && MinecraftClient.getInstance().player != null) {
            if (prefix) {
                message = "\u00A72\u00A7l\u00A7o" + getLogPrefix() + "\u00A7r" + message;
            }
            MinecraftClient.getInstance().player.sendMessage(Text.of(message), false);

        } else {
            logInternal(message);
        }
    }

    /**
     * 记录带前缀的消息到游戏聊天窗口
     * @param message 消息内容
     */
    public static void logMessage(String message) {
        logMessage(message, true);
    }

    /**
     * 使用格式化字符串记录消息
     * @param format 格式化字符串
     * @param args 格式化参数
     */
    public static void logMessage(String format, Object... args) {
        logMessage(String.format(format, args));
    }

    /**
     * 记录警告消息
     * @param message 警告消息内容
     */
    public static void logWarning(String message) {
        if (canLog(WARN_LOG_LEVEL)) {
            System.out.println("ALTO CLEF: 警告: " + message);
        }

        AltoClef altoClef = AltoClef.getInstance();
        if (altoClef != null && !altoClef.getModSettings().shouldHideAllWarningLogs()) {
            if (MinecraftClient.getInstance() != null && MinecraftClient.getInstance().player != null) {
                String msg = "\u00A72\u00A7l\u00A7o" + getLogPrefix() + "\u00A7c" + message + "\u00A7r";
                MinecraftClient.getInstance().player.sendMessage(Text.of(msg), false);

            }
        }
    }

    /**
     * 使用格式化字符串记录警告消息
     * @param format 格式化字符串
     * @param args 格式化参数
     */
    public static void logWarning(String format, Object... args) {
        logWarning(String.format(format, args));
    }

    /**
     * 记录错误消息，包含堆栈跟踪信息
     * @param message 错误消息内容
     */
    public static void logError(String message) {
        String stacktrace = getStack(2);

        if (canLog(ERROR_LOG_LEVEL)) {
            System.err.println(message);
            System.err.println("位置:");
            System.err.println(stacktrace);
        }

        if (MinecraftClient.getInstance() != null && MinecraftClient.getInstance().player != null) {
            String msg = "\u00A72\u00A7l\u00A7c" + getLogPrefix() + "[错误] " + message + "\n位置:\n" + stacktrace + "\u00A7r";
            MinecraftClient.getInstance().player.sendMessage(Text.of(msg), false);
        }
    }

    /**
     * 使用格式化字符串记录错误消息
     * @param format 格式化字符串
     * @param args 格式化参数
     */
    public static void logError(String format, Object... args) {
        logError(String.format(format, args));
    }

    /**
     * 记录当前堆栈跟踪信息
     */
    public static void logStack() {
        logInternal("堆栈跟踪: \n" + getStack(2));
    }

    /**
     * 获取堆栈跟踪信息
     * @param toSkip 跳过的堆栈帧数量
     * @return 堆栈跟踪字符串
     */
    private static String getStack(int toSkip) {
        StringBuilder stacktrace = new StringBuilder();
        for (StackTraceElement ste : Thread.currentThread().getStackTrace()) {
            if (toSkip-- <= 0) {
                stacktrace.append(ste.toString()).append("\n");
            }
        }
        return stacktrace.toString();
    }

    /**
     * 检查是否可以记录指定级别的日志
     * @param level 日志级别
     * @return 如果可以记录则返回true，否则返回false
     */
    private static boolean canLog(int level) {
        if (AltoClef.getInstance() == null || AltoClef.getInstance().getModSettings() == null) return true;

        String enabledLogLevel = AltoClef.getInstance().getModSettings().getLogLevel();

        return switch (enabledLogLevel) {
            case "NONE" -> false;
            case "ALL" -> true;
            case "NORMAL" -> level == WARN_LOG_LEVEL || level == ERROR_LOG_LEVEL;
            case "WARN" -> level == WARN_LOG_LEVEL;
            case "ERROR" -> level == ERROR_LOG_LEVEL;
            default ->
                    // 无效的日志级别，切换到默认值(NORMAL)
                    level != DEBUG_LOG_LEVEL;
        };

    }

}
