package adris.altoclef.multiversion;

import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

/**
 * 文本版本适配器类
 * 
 * 此类提供 Minecraft 不同版本间文本 API 的兼容层。
 * 主要处理 1.19 及以上版本中引入的新的文本构建 API 与早期版本的 LiteralText 差异。
 */
public class TextVer {


    /**
     * 创建空的可变文本对象
     * 
     * 在 Minecraft 1.19 及以上版本中，使用 Text.empty() 静态方法创建空文本。
     * 在早期版本中，通过创建空字符串的 LiteralText 实例实现。
     * 
     * @return 空的可变文本对象
     */
    @Pattern
    public static MutableText empty() {
        //#if MC >= 11900
        return Text.empty();
        //#else
        //$$ return new net.minecraft.text.LiteralText("");
        //#endif
    }

    /**
     * 创建字面量可变文本对象
     * 
     * 在 Minecraft 1.19 及以上版本中，使用 Text.literal(str) 静态方法创建字面量文本。
     * 在早期版本中，通过创建 LiteralText 实例实现。
     * 
     * @param str 要转换为文本的字符串
     * @return 字面量可变文本对象
     */
    @Pattern
    public static MutableText literal(String str) {
        //#if MC >= 11900
        return Text.literal(str);
        //#else
        //$$ return new net.minecraft.text.LiteralText(str);
        //#endif
    }

}
