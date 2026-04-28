package adris.altoclef.mixins;

import adris.altoclef.eventbus.EventBus;
import adris.altoclef.eventbus.events.GameOverlayEvent;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 游戏覆盖层混入类
 * 监听游戏内覆盖消息的设置，并发布相应事件
 */
@Mixin(InGameHud.class)
public class GameOverlayMixin {

    /**
     * 在设置覆盖消息时注入代码
     * 将消息内容转换为字符串并发布游戏覆盖层事件
     * 
     * @param message 要显示的消息文本
     * @param tinted 是否应用色调
     * @param ci 回调信息对象
     */
    @Inject(
            method = "setOverlayMessage",
            at = @At("HEAD")
    )
    public void onSetOverlayMessage(Text message, boolean tinted, CallbackInfo ci) {
        String text = message.getString();
        EventBus.publish(new GameOverlayEvent(text));
    }
}
