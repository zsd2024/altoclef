package adris.altoclef.util.helpers;

import adris.altoclef.AltoClef;
import adris.altoclef.multiversion.DamageSourceWrapper;
import adris.altoclef.multiversion.MethodWrapper;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.DamageUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.*;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

/**
 * 实体帮助器类
 * 提供解释实体状态的辅助函数
 */
public class EntityHelper {
    public static final double ENTITY_GRAVITY = 0.08; // 每秒重力值

    /**
     * 检查实体是否对玩家生气（敌对且能看见玩家）
     * @param mod AltoClef实例
     * @param mob 实体对象
     * @return 如果实体对玩家生气返回true，否则返回false
     */
    public static boolean isAngryAtPlayer(AltoClef mod, Entity mob) {
        boolean hostile = isProbablyHostileToPlayer(mod, mob);
        if (mob instanceof LivingEntity entity) {
            return hostile && entity.canSee(mod.getPlayer());
        }
        return hostile;
    }

    /**
     * 检查实体是否可能对玩家敌对
     * @param mod AltoClef实例
     * @param entity 实体对象
     * @return 如果实体可能对玩家敌对返回true，否则返回false
     */
    public static boolean isProbablyHostileToPlayer(AltoClef mod, Entity entity) {
        if (entity instanceof MobEntity mob) {
            if (mob instanceof SlimeEntity slime) {
                return slime.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE) > 0;
            }
            if (mob instanceof PiglinEntity piglin) {
                return piglin.isAttacking() && !isTradingPiglin(mob) && piglin.isAdult();
            }
            if (mob instanceof EndermanEntity enderman) {
                return enderman.isAngry();
            }
            if (mob instanceof ZombifiedPiglinEntity zombifiedPiglin) {
                return zombifiedPiglin.isAttacking();
            }

            return mob.isAttacking() || mob instanceof HostileEntity;
        }

        return false;
    }

    /**
     * 检查猪灵是否正在交易
     * @param entity 实体对象
     * @return 如果猪灵正在交易返回true，否则返回false
     */
    public static boolean isTradingPiglin(Entity entity) {
        if (entity instanceof PiglinEntity pig) {
            if (pig.getHandItems() != null) {
                for (ItemStack stack : pig.getHandItems()) {
                    if (stack.getItem().equals(Items.GOLD_INGOT)) {
                        // 我们正在与这个猪灵交易，忽略它。
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * 计算玩家受到伤害后的实际伤害值
     * 如果玩家受到此伤害，玩家的生命值将减去计算结果值。
     * @param player 玩家实体
     * @param src 伤害来源
     * @param damageAmount 原始伤害值
     * @return 实际伤害值
     */
    public static double calculateResultingPlayerDamage(PlayerEntity player, DamageSource src, double damageAmount) {
        // 复制自`PlayerEntity.applyDamage`的逻辑
        DamageSourceWrapper source = DamageSourceWrapper.of(src);

        if (player.isInvulnerableTo(src))
            return 0;

        // 护甲基础计算
        if (!source.bypassesArmor()) {
            damageAmount = MethodWrapper.getDamageLeft(player, damageAmount,src,player.getArmor(),player.getAttributeValue(EntityAttributes.GENERIC_ARMOR_TOUGHNESS));
        }

        // 附魔和药水效果
        if (!source.bypassesShield()) {
            float k;
            if (player.hasStatusEffect(StatusEffects.RESISTANCE) && source.isOutOfWorld()) {
                //noinspection ConstantConditions
                k = (player.getStatusEffect(StatusEffects.RESISTANCE).getAmplifier() + 1) * 5;
                float j = 25 - k;
                double f = damageAmount * (double) j;
                double g = damageAmount;
                damageAmount = Math.max(f / 25.0F, 0.0F);
            }

            if (damageAmount <= 0.0) {
                damageAmount = 0.0;
            } else {
                //#if MC >= 12100
                k = EnchantmentHelper.getProtectionAmount(null, player, src);
                //#else
                //$$ k = EnchantmentHelper.getProtectionAmount(player.getArmorItems(), src);
                //#endif
                if (k > 0) {
                    damageAmount = DamageUtil.getInflictedDamage((float) damageAmount, (float) k);
                }
            }
        }

        // 吸收效果
        damageAmount = Math.max(damageAmount - player.getAbsorptionAmount(), 0.0F);
        return damageAmount;
    }
}
