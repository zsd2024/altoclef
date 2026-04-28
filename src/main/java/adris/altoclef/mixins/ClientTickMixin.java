package adris.altoclef.mixins;

import adris.altoclef.eventbus.EventBus;
import adris.altoclef.eventbus.events.ClientTickEvent;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// 已从玩家混入更改为客户端混入，希望这不会破坏任何功能。
/**
 * 客户端刻混入
 * 
 * 此混入类在 Minecraft 客户端主循环的每一刻（tick）执行时注入代码。
 * 通过监听 MinecraftClient 的 tick 方法，我们可以在每个客户端刻发布事件，
 * 这对于需要定期执行的任务（如状态检查、任务更新等）非常重要。
 */
@Mixin(MinecraftClient.class)
public final class ClientTickMixin {
    
    /**
     * 在客户端刻方法开始时注入
     * 
     * 此方法在 Minecraft 客户端主循环的每一刻开始时被调用。
     * 发布客户端刻事件，通知所有监听器客户端正在处理新的一刻。
     * 
     * @param ci 回调信息对象
     */
    @Inject(
            method = "tick",
            at = @At("HEAD")
    )
    private void clientTick(CallbackInfo ci) {
        EventBus.publish(new ClientTickEvent());
    }
}