package adris.altoclef.mixins;

import net.minecraft.entity.projectile.PersistentProjectileEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * 持久投射物实体访问器
 * 
 * 此混入接口为PersistentProjectileEntity类提供对inGround字段的访问能力，
 * 用于判断投射物是否已经插入地面。
 */
@Mixin(PersistentProjectileEntity.class)
public interface PersistentProjectileEntityAccessor {
    /**
     * 获取投射物是否已插入地面的状态
     * 
     * @return 如果投射物已插入地面则返回true，否则返回false
     */
    @Accessor("inGround")
    boolean isInGround();
}
