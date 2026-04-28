package adris.altoclef.mixins;

import adris.altoclef.eventbus.EventBus;
import adris.altoclef.eventbus.events.BlockBrokenEvent;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 玩家修改方块混入
 * 监听玩家破坏方块的事件，并发布自定义的 BlockBrokenEvent 事件
 */
@Mixin(Block.class)
public class BlockModifiedByPlayerMixin {

    /**
     * 在方块被破坏时注入代码
     * 根据 Minecraft 版本不同，回调参数类型有所差异
     * 
     * @param world 方块所在的世界
     * @param pos 方块位置
     * @param state 方块状态
     * @param player 破坏方块的玩家
     * @param cir 回调信息返回值（Minecraft 1.20.3+）
     * @param ci 回调信息（Minecraft 1.20.2 及以下）
     */
    @Inject(
            method = "onBreak",
            at = @At("HEAD")
    )
    //#if MC>12002
    public void onBlockBroken(World world, BlockPos pos, BlockState state, PlayerEntity player, CallbackInfoReturnable<BlockState> cir) {
    //#else
    //$$ public void onBlockBroken(World world, BlockPos pos, BlockState state, PlayerEntity player, CallbackInfo ci) {
    //#endif
        // 确保事件只在玩家所在的世界中触发
        if (player.getWorld() == world) {
            // 创建方块破坏事件
            BlockBrokenEvent evt = new BlockBrokenEvent();
            evt.blockPos = pos;
            evt.blockState = state;
            evt.player = player;
            // 发布事件到事件总线
            EventBus.publish(evt);
        }
    }

}
