package adris.altoclef.multiversion;

import net.minecraft.entity.damage.DamageSource;
import net.minecraft.world.World;

/**
 * 伤害来源版本适配器
 * 提供不同Minecraft版本间伤害来源API的兼容性处理
 */
public class DamageSourceVer {

    /**
     * 获取坠落伤害来源
     * 
     * @param world 世界对象
     * @return 坠落伤害来源实例
     */
    public static DamageSource getFallDamageSource(World world) {
        //#if MC >= 11904
        // 1.19.4及以上版本使用world.getDamageSources().fall()获取坠落伤害
        return world.getDamageSources().fall();
        //#else
        //$$ // 1.19.4以下版本直接使用DamageSource.FALL常量
        //$$ return DamageSource.FALL;
        //#endif
    }

}
