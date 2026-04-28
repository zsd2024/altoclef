package adris.altoclef.mixins;

import adris.altoclef.eventbus.EventBus;
import adris.altoclef.eventbus.events.SendChatEvent;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


/**
 * 聊天输入混入
 * 监听玩家发送聊天消息的事件，并允许通过事件系统取消消息发送
 */
@Mixin(ClientPlayNetworkHandler.class)
public final class ChatInputMixin {
    /**
     * 在发送聊天消息时注入代码
     * 允许通过事件系统拦截和取消聊天消息的发送
     * 
     * @param content 聊天消息内容
     * @param ci 回调信息，可用于取消操作
     */
    @Inject(
            method = "sendChatMessage",
            at = @At("HEAD"),
            cancellable = true
    )
    private void sendChatMessage(String content, CallbackInfo ci) {
        // 创建发送聊天事件
        SendChatEvent event = new SendChatEvent(content);
        // 发布事件到事件总线
        EventBus.publish(event);
        // 如果事件被取消，则阻止原版聊天消息发送
        if (event.isCancelled()) {
            ci.cancel();
        }
    }
}