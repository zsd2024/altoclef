package adris.altoclef.mixins;

import net.minecraft.screen.AbstractFurnaceScreenHandler;
import net.minecraft.screen.PropertyDelegate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * 抽象熔炉屏幕处理器访问器
 * 用于访问 Minecraft 原版 AbstractFurnaceScreenHandler 类中的私有字段
 */
@Mixin(AbstractFurnaceScreenHandler.class)
public interface AbstractFurnaceScreenHandlerAccessor {
    /**
     * 获取熔炉屏幕处理器的属性委托对象
     * 属性委托包含了熔炉的进度、燃料等状态信息
     *
     * @return 熔炉属性委托对象
     */
    @Accessor("propertyDelegate")
    PropertyDelegate getPropertyDelegate();
}
