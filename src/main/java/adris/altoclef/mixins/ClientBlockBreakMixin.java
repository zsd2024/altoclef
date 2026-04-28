package adris.altoclef.mixins;

import adris.altoclef.eventbus.EventBus;
import adris.altoclef.eventbus.events.BlockBreakingCancelEvent;
import adris.altoclef.eventbus.events.BlockBreakingEvent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 客户端方块破坏混入类，用于监听方块破坏事件
 */
@Mixin(ClientPlayerInteractionManager.class)
public final class ClientBlockBreakMixin {

    /**
     * 方块破坏取消帧计数器
     * 由于Baritone每两帧会触发一次方块破坏取消，因此需要2帧的要求
     */
    @Unique
    private static int _breakCancelFrames;

    /**
     * 注入方块破坏进度更新方法，发布方块破坏事件
     * 
     * @param pos 方块位置
     * @param direction 破坏方向
     * @param ci 回调信息返回值
     */
    @Inject(
            method = "updateBlockBreakingProgress",
            at = @At("HEAD")
    )
    private void onBreakUpdate(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> ci) {
        ClientBlockBreakAccessor breakAccessor = (ClientBlockBreakAccessor) (MinecraftClient.getInstance().interactionManager);
        if (breakAccessor != null) {
            _breakCancelFrames = 2;
            EventBus.publish(new BlockBreakingEvent(pos, breakAccessor.getCurrentBreakingProgress()));
        }
    }

    /**
     * 注入取消方块破坏方法，发布方块破坏取消事件
     * 
     * @param ci 回调信息
     */
    @Inject(
            method = "cancelBlockBreaking",
            at = @At("HEAD")
    )
    private void cancelBlockBreaking(CallbackInfo ci) {
        if (_breakCancelFrames-- == 0) {
            EventBus.publish(new BlockBreakingCancelEvent());
        }
    }
}
