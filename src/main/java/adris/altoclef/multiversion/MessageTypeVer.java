package adris.altoclef.multiversion;

import net.minecraft.network.message.MessageType;

/**
 * 消息类型版本适配器
 * 
 * 此类提供不同 Minecraft 版本间消息类型参数获取的兼容性处理。
 * 由于 Minecraft 1.19.4 及以上版本引入了 MessageType.Parameters，
 * 而早期版本没有此概念，因此需要版本特定的实现。
 */
public class MessageTypeVer {

    //#if MC >= 11904
    /**
     * 从消息参数中获取消息类型
     * 
     * @param parameters 消息参数对象（Minecraft 1.19.4+）
     * @return 消息类型
     */
    public static MessageType getMessageType(MessageType.Parameters parameters) {
    //#else
    //$$ /**
    //$$  * 从消息参数中获取消息类型
    //$$  * 
    //$$  * @param obj 消息参数对象（旧版本使用 Object 类型）
    //$$  * @return 消息类型
    //$$  * @throws IllegalStateException 在不支持消息参数的版本中抛出异常
    //$$  */
    //$$ public static MessageType getMessageType(Object obj) {
    //#endif

        //#if MC >= 12005
        // Minecraft 1.20.5+ 版本中，type() 返回一个 RegistryEntry，需要调用 value() 获取实际值
        return parameters.type().value();
        //#elseif MC >= 11904
        //$$ // Minecraft 1.19.4-1.20.4 版本中，直接返回 type()
        //$$ return parameters.type();
        //#else
        //$$ // 1.19.4 以下版本不支持消息参数，抛出异常
        //$$ throw new IllegalStateException("无法从此版本获取消息类型参数，因为该版本不存在此功能！");
        //#endif
    }
}
