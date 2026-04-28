package adris.altoclef.mixins;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * 可绘制帮助器调用器接口
 * 
 * 此接口用于通过Mixin的Invoker功能调用DrawContext类中的私有绘图方法，
 * 允许AltoClef在1.19.4及以下版本中绘制水平线和垂直线。
 */
@Mixin(DrawContext.class)
public interface DrawableHelperInvoker {

    //#if MC <= 11904
    //$$ /**
    //$$  * 调用DrawContext的私有drawHorizontalLine方法（仅适用于1.19.4及以下版本）
    //$$  * 绘制一条水平线
    //$$  * 
    //$$  * @param matrices 矩阵栈
    //$$  * @param x1 起始X坐标
    //$$  * @param x2 结束X坐标
    //$$  * @param y Y坐标
    //$$  * @param color 颜色（ARGB格式）
    //$$  */
    //$$ @Invoker("drawHorizontalLine")
    //$$ void invokeDrawHorizontalLine(MatrixStack matrices, int x1, int x2, int y, int color);
    //$$
    //$$ /**
    //$$  * 调用DrawContext的私有drawVerticalLine方法（仅适用于1.19.4及以下版本）
    //$$  * 绘制一条垂直线
    //$$  * 
    //$$  * @param matrices 矩阵栈
    //$$  * @param x X坐标
    //$$  * @param y1 起始Y坐标
    //$$  * @param y2 结束Y坐标
    //$$  * @param color 颜色（ARGB格式）
    //$$  */
    //$$ @Invoker("drawVerticalLine")
    //$$ void invokeDrawVerticalLine(MatrixStack matrices, int x, int y1, int y2, int color);
    //#endif
}
