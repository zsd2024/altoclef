package adris.altoclef.multiversion.versionedfields;

import net.minecraft.entity.Entity;

/**
 * 版本化实体字段
 * 此辅助类实现了在某些 Minecraft 版本中尚未支持的实体类型
 */
public class Entities {

    /**
     * 表示不受支持的实体类占位符
     */
    public static final Class<? extends Entity> UNSUPPORTED;
    
    /**
     * 监守者实体类 - 在 Minecraft 1.19.4 及以上版本中受支持
     */
    public static final Class<? extends Entity> WARDEN;
    
    /**
     * 发光鱿鱼实体类 - 在 Minecraft 1.17.1 及以上版本中受支持
     */
    public static final Class<? extends Entity> GLOW_SQUID;

    static {
        UNSUPPORTED = VersionedFieldHelper.getUnsupportedEntityClass();

        //#if MC >= 11904
        // 支持监守者实体（Minecraft 1.19.4+）
        WARDEN = net.minecraft.entity.mob.WardenEntity.class;
        //#else
        //$$ // 监守者实体在低版本中不受支持
        //$$ WARDEN = UNSUPPORTED;
        //#endif

        //#if MC >= 11701
        // 支持发光鱿鱼实体（Minecraft 1.17.1+）
        GLOW_SQUID = net.minecraft.entity.passive.GlowSquidEntity.class;
        //#else
        //$$ // 发光鱿鱼实体在低版本中不受支持
        //$$ GLOW_SQUID = UNSUPPORTED;
        //#endif
    }

}
