package adris.altoclef.multiversion;

import net.minecraft.entity.damage.DamageSource;

/**
 * 伤害来源包装器
 * 封装不同Minecraft版本的DamageSource API差异，提供统一的接口
 */
public class DamageSourceWrapper {

    /**
     * 从DamageSource创建DamageSourceWrapper实例
     * 
     * @param source 原始DamageSource对象
     * @return 包装后的DamageSourceWrapper实例，如果source为null则返回null
     */
    public static DamageSourceWrapper of(DamageSource source) {
        if (source == null) return null;

        return new DamageSourceWrapper(source);
    }

    /** 原始DamageSource对象 */
    private final DamageSource source;

    /**
     * 构造函数
     * 
     * @param source 原始DamageSource对象
     */
    private DamageSourceWrapper(DamageSource source) {
        this.source = source;
    }

    /**
     * 获取原始DamageSource对象
     * 
     * @return 原始DamageSource对象
     */
    public DamageSource getSource() {
        return source;
    }

    /**
     * 检查伤害是否能绕过盔甲
     * 
     * @return 如果伤害能绕过盔甲则返回true，否则返回false
     */
    public boolean bypassesArmor() {
        //#if MC >= 11904
        // 1.19.4及以上版本使用DamageTypeTags.BYPASSES_ARMOR标签检查
        return source.isIn(net.minecraft.registry.tag.DamageTypeTags.BYPASSES_ARMOR);
        //#else
        //$$ // 1.19.4以下版本直接调用bypassesArmor()方法
        //$$ return source.bypassesArmor();
        //#endif
    }

    /**
     * 检查伤害是否能绕过盾牌
     * 
     * @return 如果伤害能绕过盾牌则返回true，否则返回false
     */
    public boolean bypassesShield() {
        //#if MC >= 11904
        // 1.19.4及以上版本使用DamageTypeTags.BYPASSES_SHIELD标签检查
        return source.isIn(net.minecraft.registry.tag.DamageTypeTags.BYPASSES_SHIELD);
        //#else
        //$$ // 1.19.4以下版本使用isUnblockable()方法（旧版中称为"无法阻挡"）
        //$$ return source.isUnblockable();
        //#endif
    }

    /**
     * 检查伤害是否来自世界外（如虚空）
     * 
     * @return 如果伤害来自世界外则返回true，否则返回false
     */
    public boolean isOutOfWorld() {
        //#if MC >= 11904
        // 1.19.4及以上版本使用DamageTypes.OUT_OF_WORLD类型检查
        return source.isOf(net.minecraft.entity.damage.DamageTypes.OUT_OF_WORLD);
        //#else
        //$$ // 1.19.4以下版本直接调用isOutOfWorld()方法
        //$$ return source.isOutOfWorld();
        //#endif
    }

}
