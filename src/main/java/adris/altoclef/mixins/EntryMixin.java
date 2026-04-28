package adris.altoclef.mixins;

import adris.altoclef.Debug;
import adris.altoclef.eventbus.EventBus;
import adris.altoclef.eventbus.events.TitleScreenEntryEvent;
import net.minecraft.client.gui.screen.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 主菜单屏幕混入类
 * 在进入主菜单时触发全局初始化事件
 */
@Mixin(TitleScreen.class)
public class EntryMixin {

    /**
     * 初始化标志位，确保全局初始化只执行一次
     */
    @Unique
    private static boolean _initialized = false;

    /**
     * 在主菜单初始化方法开始时注入代码
     * 发布标题屏幕进入事件并记录全局初始化日志
     * 
     * @param info 回调信息对象
     */
    @Inject(at = @At("HEAD"), method = "init()V")
    private void init(CallbackInfo info) {
        if (!_initialized) {
            _initialized = true;
            Debug.logMessage("全局初始化");
            EventBus.publish(new TitleScreenEntryEvent());
        }
    }
}

