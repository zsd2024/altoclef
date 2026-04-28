package adris.altoclef.multivoclef.multiversion;

import net.minecraft.util.Identifier;

/**
 * 标识符版本适配器
 * 用于处理不同 Minecraft 版本中标识符创建方式的差异
 * 在 1.21.0+ 版本中，使用 Identifier.of() 静态方法创建标识符
 * 在 1.20.6 及更早版本中，使用 new Identifier() 构造函数创建标识符
 */
public class IdentifierVer {

    /**
     * 根据字符串创建标识符实例
     * 这是一个私有方法，仅在内部使用
     * 
     * @param str 标识符字符串
     * @return 创建的标识符实例
     */
    @Pattern
    private static Identifier newCreation(String str) {
        //#if MC >= 12100
        return Identifier.of(str);
        //#else
        //$$ return new Identifier(str);
        //#endif
    }
}
