package adris.altoclef.mixins;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * 实体访问器接口
 * 提供对Minecraft实体类私有字段和方法的访问能力
 */
@Mixin(Entity.class)
public interface EntityAccessor {
    //#if MC <= 12006
    //$$ /**
    //$$  * 检查实体是否处于下界传送门中
    //$$  * 
    //$$  * @return 如果实体在下界传送门中返回true，否则返回false
    //$$  */
    //$$ @Accessor("inNetherPortal")
    //$$ boolean isInNetherPortal();
    //#endif

    /**
     * 获取传送门冷却时间
     * 
     * @return 传送门冷却时间（游戏刻）
     */
    @Accessor
    int getPortalCooldown();

    //#if MC <= 11605
    //$$ /**
    //$$  * 调用获取着陆位置的方法
    //$$  * 
    //$$  * @return 着陆位置的方块坐标
    //$$  */
    //$$ @Invoker("getLandingPos")
    //$$ BlockPos invokeGetLandingPos();
    //#endif

}
