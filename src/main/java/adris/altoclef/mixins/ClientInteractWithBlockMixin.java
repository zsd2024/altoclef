package adris.altoclef.mixins;

import adris.altoclef.eventbus.EventBus;
import adris.altoclef.eventbus.events.BlockInteractEvent;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 客户端与方块交互混入
 * 
 * 此混入类在玩家与方块交互时注入代码，用于发布方块交互事件。
 * 通过监听 interactBlock 方法的调用，我们可以捕获所有方块交互行为，
 * 并将其广播给事件总线，供 AltoClef 的其他模块处理。
 */
@Mixin(ClientPlayerInteractionManager.class)
public final class ClientInteractWithBlockMixin {
    
    /**
     * 在客户端方块交互方法执行前注入
     * 
     * 此方法在 Minecraft 原版的 interactBlock 方法开始执行时被调用。
     * 我们检查击中结果是否有效，如果有效则发布方块交互事件。
     * 
     * @param player 玩家实体
     * @param hand 使用的手（主手或副手）
     * @param hitResult 方块击中结果，包含被交互方块的位置和面信息
     * @param ci 回调信息对象（用于返回值的方法）
     */
    @Inject(
            method = "interactBlock",
            at = @At("HEAD")
    )

    //#if MC >= 11904
    private void onClientBlockInteract(ClientPlayerEntity player, Hand hand, BlockHitResult hitResult, CallbackInfoReturnable<ActionResult> ci) {
    //#else
    //$$ private void onClientBlockInteract(ClientPlayerEntity player, ClientWorld world, Hand hand, BlockHitResult hitResult, CallbackInfoReturnable<ActionResult> cir) {
    //#endif
        //Debug.logMessage("(客户端) 交互方块: " + (hitResult != null? hitResult.getBlockPos() : "(无)"));
        if (hitResult != null) {
            EventBus.publish(new BlockInteractEvent(hitResult));
        }

    }
}
