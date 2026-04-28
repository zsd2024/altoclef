package adris.altoclef.multiversion.versionedfields;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.world.World;
import sun.misc.Unsafe;

import java.lang.reflect.Field;

/**
 * 版本化字段帮助器
 * 
 * 此类提供了一种机制来处理不同 Minecraft 版本中可能不存在的字段。
 * 通过创建特殊的"不支持"对象，可以在运行时检测到不支持的功能并优雅地处理。
 */
@SuppressWarnings("restriction")
public class VersionedFieldHelper {

    /** Unsafe 实例，用于绕过构造函数直接创建对象实例 */
    private static final Unsafe unsafe;

    static {
        try {
            Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            unsafe = (Unsafe) theUnsafe.get(null);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 检查给定对象是否被支持
     * 
     * @param obj 要检查的对象
     * @return 如果对象被支持返回 true，否则返回 false
     */
    public static boolean isSupported(Object obj) {
        if (obj == null) return true;
        return !(obj instanceof UnsupportedBlock) && !(obj instanceof UnsupportedItem) && !(obj instanceof UnsupportedEntity);
    }


    /**
     * 使用 Unsafe 创建 UnsupportedBlock 实例而不调用构造函数
     * 
     * @return UnsupportedBlock 实例或 null（如果创建失败）
     */
    protected static UnsupportedBlock createUnsafeUnsupportedBlock() {
        try {
            // 实例化子类而不调用构造函数
            return (UnsupportedBlock) unsafe.allocateInstance(UnsupportedBlock.class);
        } catch (InstantiationException e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * 不支持的方块类
     * 
     * 当尝试在不支持的版本中使用某个方块时，会返回此类的实例。
     * 构造函数会抛出异常以防止意外使用。
     */
    protected static class UnsupportedBlock extends Block {
        public UnsupportedBlock(Settings settings) {
            super(settings);
            throw new IllegalStateException("不支持！");
        }
    }

    /**
     * 使用 Unsafe 创建 UnsupportedItem 实例而不调用构造函数
     * 
     * @return UnsupportedItem 实例或 null（如果创建失败）
     */
    protected static UnsupportedItem createUnsafeUnsupportedItem() {
        try {
            // 实例化子类而不调用构造函数
            return (UnsupportedItem) unsafe.allocateInstance(UnsupportedItem.class);
        } catch (InstantiationException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 不支持的物品类
     * 
     * 当尝试在不支持的版本中使用某个物品时，会返回此类的实例。
     * 构造函数会抛出异常以防止意外使用。
     */
    protected static class UnsupportedItem extends Item {
        public UnsupportedItem(Settings settings) {
            super(settings);
            throw new IllegalStateException("不支持！");
        }
    }


    /**
     * 获取不支持的实体类
     * 
     * @return UnsupportedEntity 类
     */
    protected static Class<? extends Entity> getUnsupportedEntityClass() {
        return UnsupportedEntity.class;
    }

    /**
     * 不支持的实体抽象类
     * 
     * 当尝试在不支持的版本中使用某个实体时，会返回此类的实例。
     * 构造函数会抛出异常以防止意外使用。
     */
    protected static abstract class UnsupportedEntity extends Entity {
        public UnsupportedEntity(EntityType<?> type, World world) {
            super(type, world);
            throw new IllegalStateException("不支持！");
        }
    }

}
