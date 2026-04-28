package adris.altoclef.mixins;

import adris.altoclef.eventbus.EventBus;
import adris.altoclef.eventbus.events.ClientRenderEvent;
import adris.altoclef.multiversion.DrawContextWrapper;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 客户端UI混入类
 * 
 * 此混入类用于在游戏内HUD渲染完成后注入自定义渲染事件，
 * 允许AltoClef在游戏UI之上绘制额外的信息或界面元素。
 */
@Mixin(InGameHud.class)
public final class ClientUIMixin {
    /**
     * 在InGameHud的render方法末尾注入客户端渲染事件
     * 
     * @param context 渲染上下文（1.21+版本）
     * @param tickCounter 渲染帧计数器（1.21+版本）
     * @param ci 回调信息
     */
    @Inject(
            method = "render",
            at = @At("TAIL")
    )
    //#if MC >= 12100
    private void clientRender(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        EventBus.publish(new ClientRenderEvent(DrawContextWrapper.of(context), tickCounter.getTickDelta(true)));
    }
    //#else
    //#if MC >= 12001
    //$$ /**
    //$$  * 在InGameHud的render方法末尾注入客户端渲染事件（1.20.1-1.20.x版本）
    //$$  * 
    //$$  * @param obj 渲染上下文
    //$$  * @param tickDelta 渲染帧增量
    //$$  * @param ci 回调信息
    //$$  */
    //$$ private void clientRender(DrawContext obj, float tickDelta, CallbackInfo ci) {
    //#else
    //$$ /**
    //$$  * 在InGameHud的render方法末尾注入客户端渲染事件（1.19.4及以下版本）
    //$$  * 
    //$$  * @param obj 矩阵栈
    //$$  * @param tickDelta 渲染帧增量
    //$$  * @param ci 回调信息
    //$$  */
    //$$ private void clientRender(MatrixStack obj, float tickDelta, CallbackInfo ci) {
    //#endif
    //$$    EventBus.publish(new ClientRenderEvent(DrawContextWrapper.of(obj), tickDelta));
    //$$ }
    //#endif


}
