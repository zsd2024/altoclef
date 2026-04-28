package adris.altoclef.mixins;

import adris.altoclef.eventbus.EventBus;
import adris.altoclef.eventbus.events.ChatMessageEvent;
import adris.altoclef.multiversion.MessageTypeVer;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.network.message.MessageHandler;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SignedMessage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


/**
 * 聊天读取混入类，用于监听聊天消息事件
 */
@Mixin(MessageHandler.class)
public final class ChatReadMixin {
    /**
     * 注入聊天消息处理方法，在接收到聊天消息时发布事件
     * 
     * @param message 签名消息
     * @param sender 发送者游戏档案
     * @param params 消息类型参数
     * @param ci 回调信息
     */
    @Inject(
            method = "onChatMessage",
            at = @At("HEAD")
    )
    private void onChatMessage(SignedMessage message, GameProfile sender, MessageType.Parameters params, CallbackInfo ci) {
        ChatMessageEvent evt = new ChatMessageEvent(message.getContent().getString(), sender.getName(), MessageTypeVer.getMessageType(params));
        EventBus.publish(evt);
    }
}