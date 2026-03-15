package adris.altoclef.butler;

import adris.altoclef.util.helpers.ConfigHelper;

/**
 * 助手配置类，管理助手系统的各种设置
 */
public class ButlerConfig {

    private static ButlerConfig _instance = new ButlerConfig(); // 配置实例

    static {
        ConfigHelper.loadConfig("configs/butler.json", ButlerConfig::new, ButlerConfig.class, newConfig -> _instance = newConfig);
    }

    /**
     * 如果为真，将使用黑名单拒绝用户使用您的玩家作为助手
     */
    public boolean useButlerBlacklist = true;
    /**
     * 如果为真，将使用白名单仅接受来自白名单的用户。
     */
    public boolean useButlerWhitelist = false;
    /**
     * 服务器有不同消息插件会改变消息显示方式。
     * 与其尝试实现所有插件并引入重大安全风险，
     * 您可以定义助手将监听的自定义私聊格式。
     * <p>
     * 在花括号中是三个特殊部分：
     * <p>
     * {from}: 消息发送者
     * {to}: 消息接收者，如果这不是您的用户名，助手将忽略。
     * {message}: 消息内容。
     * <p>
     * <p>
     * 警告: 助手只会接受非聊天消息作为指令，但不要设置得太宽松，
     * 否则您可能会面临机器人未授权控制的风险。基本上，确保只有私聊消息可以
     * 创建以下消息。
     */
    public String[] whisperFormats = new String[]{
            "{from} {to} {message}"
    };
    /**
     * 如果设置为真，将打印关于已解析的私聊信息和
     * 解析失败的私聊信息。
     * <p>
     * 如果您需要帮助设置私聊格式，请启用此功能。
     */
    public boolean whisperFormatDebug = true;
    /**
     * 确定是否应向尝试使用助手的未授权实体发送失败消息
     * <p>
     * 如果您需要保持隐蔽，请禁用此功能。
     */
    public boolean sendAuthorizationResponse = true;
    /**
     * 由于未授权而执行失败时发送的响应
     * {from}: 触发失败授权响应的玩家用户名
     */
    public String failedAuthorizationResposne = "抱歉 {from} 但您未被授权！";
    /**
     * 使用此选项选择消息中是否需要前缀
     * <p>
     * 如果您希望可以发送普通消息而不是助手指令，请禁用此功能。
     */
    public boolean requirePrefixMsg = false; // 是否要求消息前缀

    /**
     * 获取配置实例
     * @return 配置实例
     */
    public static ButlerConfig getInstance() {
        return _instance;
    }
}
