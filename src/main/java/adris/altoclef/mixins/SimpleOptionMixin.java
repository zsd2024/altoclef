package adris.altoclef.mixins;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.SimpleOption;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;
import java.util.Optional;

/**
 * 简单选项混入
 * 用于拦截和修改游戏选项的设置行为
 */
@Mixin(SimpleOption.class)
public class SimpleOptionMixin<T> {

    /**
     * 当前选项的值
     */
    @Shadow
    T value;

    /**
     * 在设置选项值时进行拦截
     * 特别处理伽马值（亮度）的设置，防止其被重置
     * @param value 要设置的新值
     * @param ci 回调信息，用于取消原始方法的执行
     */
    @Inject(method = "setValue",at = @At("HEAD"), cancellable = true)
    public void inject(T value, CallbackInfo ci) {
        if (MinecraftClient.getInstance() == null || MinecraftClient.getInstance().options == null) return;
        if (((Object)this) == MinecraftClient.getInstance().options.getGamma()) {
            this.value = value;
            ci.cancel();
        }
    }

}
