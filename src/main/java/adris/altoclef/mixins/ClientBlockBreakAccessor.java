package adris.altoclef.mixins;

import net.minecraft.client.network.ClientPlayerInteractionManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * 客户端方块破坏访问器接口，用于获取当前方块破坏进度
 */
@Mixin(ClientPlayerInteractionManager.class)
public interface ClientBlockBreakAccessor {
    /**
     * 获取当前方块破坏进度
     * 
     * @return 方块破坏进度（0.0-1.0）
     */
    @Accessor("currentBreakingProgress")
    float getCurrentBreakingProgress();
}
