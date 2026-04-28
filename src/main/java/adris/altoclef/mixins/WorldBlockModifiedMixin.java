package adris.altoclef.mixins;

import adris.altoclef.eventbus.EventBus;
import adris.altoclef.eventbus.events.BlockPlaceEvent;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 世界方块修改混入
 * 用于检测和跟踪方块放置事件
 */
@Mixin(World.class)
public class WorldBlockModifiedMixin {

    /**
     * 检查指定位置的方块状态是否为有效的固体方块
     * @param state 方块状态
     * @param pos 方块位置
     * @return 如果方块是非空气且为固体方块则返回true
     */
    @Unique
    private static boolean hasBlock(BlockState state, BlockPos pos) {
        return !state.isAir() && state.isSolidBlock(MinecraftClient.getInstance().world, pos);
    }

    /**
     * 在方块发生变化时触发
     * 当检测到从空气变为固体方块时，发布方块放置事件
     * @param pos 方块位置
     * @param oldBlock 变化前的方块状态
     * @param newBlock 变化后的方块状态
     * @param ci 回调信息
     */
    @Inject(
            method = "onBlockChanged",
            at = @At("HEAD")
    )
    public void onBlockWasChanged(BlockPos pos, BlockState oldBlock, BlockState newBlock, CallbackInfo ci) {
        if (!hasBlock(oldBlock, pos) && hasBlock(newBlock, pos)) {
            BlockPlaceEvent evt = new BlockPlaceEvent(pos, newBlock);
            EventBus.publish(evt);
        }
    }

}
