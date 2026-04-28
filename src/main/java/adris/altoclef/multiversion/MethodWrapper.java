package adris.altoclef.multiversion;

import net.minecraft.entity.DamageUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.block.spawner.MobSpawnerLogic;
import net.minecraft.world.World;

//#if MC >= 11701
import net.minecraft.util.math.random.CheckedRandom;
import net.minecraft.util.math.random.Random;
//#endif

/**
 * 方法包装器
 * 
 * 此类封装了不同 Minecraft 版本间方法签名变化的兼容性处理，
 * 提供统一的接口来调用版本特定的方法实现。
 */
public class MethodWrapper {



    /**
     * 获取刷怪笼渲染的实体
     * 
     * 不同 Minecraft 版本中 MobSpawnerLogic.getRenderedEntity 方法的参数不同：
     * - 1.20.3+：需要 World 和 BlockPos 参数
     * - 1.19.4-1.20.2：需要 World、Random 和 BlockPos 参数
     * - 1.17.1-1.19.3：只需要 World 参数
     * - 1.17.1 以下：不需要任何参数
     * 
     * @param logic 刷怪笼逻辑对象
     * @param world 世界对象
     * @param pos 方块位置
     * @return 渲染的实体
     */
    public static Entity getRenderedEntity(MobSpawnerLogic logic, World world, BlockPos pos) {
        //#if MC>12002
        // Minecraft 1.20.3+ 版本
        return logic.getRenderedEntity(world, pos);
        //#elseif MC >= 11904
        //$$ // Minecraft 1.19.4-1.20.2 版本，需要创建随机数生成器
        //$$ return logic.getRenderedEntity(world,Random.create() ,pos);
        //#elseif MC >= 11701
        //$$ // Minecraft 1.17.1-1.19.3 版本
        //$$ return logic.getRenderedEntity(world);
        //#else
        //$$ // Minecraft 1.17.1 以下版本
        //$$ return logic.getRenderedEntity();
        //#endif
    }

    /**
     * 计算护甲剩余伤害（双精度版本）
     * 
     * 此方法将双精度参数转换为单精度并调用对应的单精度版本。
     * 
     * @param armorWearer 护甲穿戴者
     * @param damage 原始伤害值
     * @param source 伤害来源
     * @param armor 护甲值
     * @param armorToughness 护甲韧性
     * @return 剩余伤害值
     */
    public static float getDamageLeft(LivingEntity armorWearer, double damage, DamageSource source, double armor, double armorToughness) {
        return getDamageLeft(armorWearer, (float)damage,source,(float)armor,(float)armorToughness);
    }

    /**
     * 计算护甲剩余伤害（单精度版本）
     * 
     * 不同 Minecraft 版本中 DamageUtil.getDamageLeft 方法的参数不同：
     * - 1.21.0+：需要 LivingEntity、damage、source、armor、armorToughness 参数
     * - 1.20.5-1.20.6：需要 damage、source、armor、armorToughness 参数
     * - 1.20.4 以下：只需要 damage、armor、armorToughness 参数
     * 
     * @param armorWearer 护甲穿戴者
     * @param damage 原始伤害值
     * @param source 伤害来源
     * @param armor 护甲值
     * @param armorToughness 护甲韧性
     * @return 剩余伤害值
     */
    public static float getDamageLeft(LivingEntity armorWearer, float damage, DamageSource source, float armor, float armorToughness) {
        //#if MC >= 12100
        // Minecraft 1.21.0+ 版本，包含实体和伤害来源参数
        return DamageUtil.getDamageLeft(armorWearer, damage, source, armor, armorToughness);
        //#elseif MC>=12005
        //$$ // Minecraft 1.20.5-1.20.6 版本，包含伤害来源但不包含实体参数
        //$$ return DamageUtil.getDamageLeft(damage, source, armor, armorToughness);
        //#else
        //$$ // Minecraft 1.20.4 以下版本，只有基础参数
        //$$ return DamageUtil.getDamageLeft(damage,armor,armorToughness);
        //#endif
    }



}

    public static float getDamageLeft(LivingEntity armorWearer, double damage, DamageSource source, double armor, double armorToughness) {
        return getDamageLeft(armorWearer, (float)damage,source,(float)armor,(float)armorToughness);
    }

    public static float getDamageLeft(LivingEntity armorWearer, float damage, DamageSource source, float armor, float armorToughness) {
        //#if MC >= 12100
        return DamageUtil.getDamageLeft(armorWearer, damage, source, armor, armorToughness);
        //#elseif MC>=12005
        //$$ return DamageUtil.getDamageLeft(damage, source, armor, armorToughness);
        //#else
        //$$ return DamageUtil.getDamageLeft(damage,armor,armorToughness);
        //#endif
    }



}
