package adris.altoclef.mixins;

import net.minecraft.block.Portal;
import net.minecraft.world.dimension.PortalManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * 传送门管理器访问器
 * 用于访问 Minecraft 原版 PortalManager 类中的私有字段
 */
@Mixin(PortalManager.class)
public interface PortalManagerAccessor {

    /**
     * 获取传送门实例
     * @return Portal 传送门对象
     */
    @Accessor("portal")
    Portal accessPortal();

}
