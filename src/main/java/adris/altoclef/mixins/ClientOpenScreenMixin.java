package adris.altoclef.mixins;

import adris.altoclef.eventbus.EventBus;
import adris.altoclef.eventbus.events.ScreenOpenEvent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 客户端打开屏幕混入
 * 
 * 此混入类在 Minecraft 客户端打开或关闭 GUI 屏幕时注入代码。
 * 通过监听 setScreen 方法，我们可以在屏幕打开前和打开后发布相应的事件，
 * 用于跟踪 GUI 状态变化，这对于自动化任务（如自动合成、容器操作等）非常重要。
 */
@Mixin(MinecraftClient.class)
public final class ClientOpenScreenMixin {
    
    /**
     * 在屏幕打开方法开始时注入
     * 
     * 此方法在 Minecraft 原版的 setScreen 方法开始执行时被调用。
     * 发布屏幕打开开始事件，标识屏幕即将被设置。
     * 
     * @param screen 要打开的屏幕对象，可能为 null（表示关闭当前屏幕）
     * @param ci 回调信息对象
     */
    @Inject(
            method = "setScreen",
            at = @At("HEAD")
    )
    private void onScreenOpenBegin(@Nullable Screen screen, CallbackInfo ci) {
        EventBus.publish(new ScreenOpenEvent(screen, true));
    }

    /**
     * 在屏幕打开方法结束时注入
     * 
     * 此方法在 Minecraft 原版的 setScreen 方法执行完毕后被调用。
     * 发布屏幕打开结束事件，标识屏幕已经成功设置。
     * 
     * @param screen 已打开的屏幕对象，可能为 null（表示已关闭屏幕）
     * @param ci 回调信息对象
     */
    @Inject(
            method = "setScreen",
            at = @At("TAIL")
    )
    private void onScreenOpenEnd(@Nullable Screen screen, CallbackInfo ci) {
        EventBus.publish(new ScreenOpenEvent(screen, false));
    }
}
