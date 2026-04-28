package adris.altoclef.mixins;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 本地玩家混入
 * 
 * 此混入类用于拦截本地玩家的俯仰角和偏航角获取方法，
 * 确保返回的是父类的原始值，防止其他混入或代码修改这些值。
 */
@Mixin(ClientPlayerEntity.class)
public abstract class MixinLocalPlayer extends AbstractClientPlayerEntity {

    /**
     * 构造函数
     * 
     * @param world 客户端世界
     * @param profile 游戏档案
     */
    public MixinLocalPlayer(ClientWorld world, GameProfile profile) {
        super(world, profile);
    }

    /**
     * 注入到getPitch方法，在方法返回时执行
     * 设置回调返回值为父类的俯仰角值
     * 
     * @param tickDelta 渲染刻差值
     * @param cir 回调信息返回对象
     */
    @Inject(method = "getPitch", at = @At("RETURN"), cancellable = true)
    public void getPitch(float tickDelta, CallbackInfoReturnable<Float> cir) {
        cir.setReturnValue(super.getPitch(tickDelta));
    }

    /**
     * 注入到getYaw方法，在方法返回时执行
     * 设置回调返回值为父类的偏航角值
     * 
     * @param tickDelta 渲染刻差值
     * @param cir 回调信息返回对象
     */
    @Inject(method = "getYaw", at = @At("RETURN"), cancellable = true)
    public void getYaw(float tickDelta, CallbackInfoReturnable<Float> cir) {
        cir.setReturnValue(super.getYaw(tickDelta));
    }
}