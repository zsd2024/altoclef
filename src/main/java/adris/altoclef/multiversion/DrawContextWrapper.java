package adris.altoclef.multiversion;

import adris.altoclef.mixins.DrawableHelperInvoker;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.util.math.MatrixStack;
import org.jetbrains.annotations.Nullable;

/**
 * 绘制上下文包装器
 * 封装不同Minecraft版本的绘制API差异，提供统一的绘制接口
 */
public class DrawContextWrapper {

    //#if MC >= 12001
    /**
     * 从DrawContext创建DrawContextWrapper实例（1.20.1及以上版本）
     * 
     * @param context DrawContext对象
     * @return 包装后的DrawContextWrapper实例，如果context为null则返回null
     */
    public static DrawContextWrapper of(DrawContext context) {
        if (context == null) return null;
        return new DrawContextWrapper(context);
    }
    /** DrawContext对象（1.20.1及以上版本） */
    private final DrawContext context;

    /**
     * 构造函数（1.20.1及以上版本）
     * 
     * @param context DrawContext对象
     */
    private DrawContextWrapper(DrawContext context) {
        this.context = context;
    }
    //#else
    //$$ /**
    //$$  * 从MatrixStack创建DrawContextWrapper实例（1.20.1以下版本）
    //$$  * 
    //$$  * @param matrices 矩阵栈对象
    //$$  * @return 包装后的DrawContextWrapper实例，如果matrices为null则返回null
    //$$  */
    //$$ public static DrawContextWrapper of(MatrixStack matrices) {
    //$$    if (matrices == null) return null;
    //$$    return new DrawContextWrapper(matrices);
    //$$ }
    //$$
    //$$ /** 矩阵栈对象（1.20.1以下版本） */
    //$$ private final MatrixStack matrices;
    //$$ /** DrawableHelper辅助对象（1.20.1以下版本） */
    //$$ private final DrawableHelper helper;
    //$$ /**
    //$$  * 构造函数（1.20.1以下版本）
    //$$  * 
    //$$  * @param matrices 矩阵栈对象
    //$$  */
    //$$ private DrawContextWrapper(MatrixStack matrices) {
    //$$        this.matrices = matrices;
    //$$        this.helper = new DrawableHelper(){};
    //$$ }
    //#endif

    /** 渲染层（仅用于1.20.1及以上版本） */
    private RenderLayer renderLayer = null;

    /**
     * 设置渲染层（仅用于1.20.1及以上版本）
     * 
     * @param renderLayer 渲染层对象
     */
    public void setRenderLayer(RenderLayer renderLayer) {
        this.renderLayer = renderLayer;
    }

    /**
     * 填充矩形区域
     * 
     * @param x1 左上角x坐标
     * @param y1 左上角y坐标
     * @param x2 右下角x坐标
     * @param y2 右下角y坐标
     * @param color 颜色值
     */
    public void fill(int x1, int y1, int x2, int y2, int color) {
        //#if MC >= 12001
        // 1.20.1及以上版本使用DrawContext的fill方法
        context.fill(renderLayer, x1, y1, x2, y2, color);
        //#else
        //$$ // 1.20.1以下版本使用DrawableHelper的静态fill方法
        //$$  DrawableHelper.fill(matrices, x1, y1, x2, y2, color);
        //#endif
    }

    /**
     * 绘制水平线
     * 
     * @param x1 起始x坐标
     * @param x2 结束x坐标
     * @param y y坐标
     * @param color 颜色值
     */
    public void drawHorizontalLine(int x1, int x2, int y, int color) {
        //#if MC >= 12001
        // 1.20.1及以上版本使用DrawContext的drawHorizontalLine方法
        context.drawHorizontalLine(renderLayer, x1, x2, y, color);
        //#else
        //$$ // 1.20.1以下版本通过DrawableHelperInvoker调用私有方法
        //$$ ((DrawableHelperInvoker) helper).invokeDrawHorizontalLine(matrices, x1, x2, y, color);
        //#endif
    }

    /**
     * 绘制垂直线
     * 
     * @param x x坐标
     * @param y1 起始y坐标
     * @param y2 结束y坐标
     * @param color 颜色值
     */
    public void drawVerticalLine(int x, int y1, int y2, int color) {
        //#if MC >= 12001
        // 1.20.1及以上版本使用DrawContext的drawVerticalLine方法
        context.drawVerticalLine(renderLayer, x, y1, y2, color);
        //#else
        //$$ // 1.20.1以下版本通过DrawableHelperInvoker调用私有方法
        //$$ ((DrawableHelperInvoker) helper).invokeDrawVerticalLine(matrices, x, y1, y2, color);
        //#endif
    }

    /**
     * 绘制文本
     * 
     * @param textRenderer 文本渲染器
     * @param text 要绘制的文本（可为null）
     * @param x x坐标
     * @param y y坐标
     * @param color 颜色值
     * @param shadow 是否使用阴影
     */
    public void drawText(TextRenderer textRenderer, @Nullable String text, int x, int y, int color, boolean shadow) {
        //#if MC >= 12001
        // 1.20.1及以上版本使用DrawContext的drawText方法
        context.drawText(textRenderer,text,x,y,color,shadow);
        //#else
        //$$ // 1.20.1以下版本根据是否需要阴影选择不同的绘制方法
        //$$ if (shadow) {
        //$$    textRenderer.drawWithShadow(matrices, text,x,y,color);
        //$$ } else {
        //$$    textRenderer.draw(matrices, text,x,y,color);
        //$$ }
        //#endif
    }

    /**
     * 获取矩阵栈
     * 
     * @return 矩阵栈对象
     */
    public MatrixStack getMatrices() {
        //#if MC >= 12001
        // 1.20.1及以上版本从DrawContext获取矩阵栈
        return context.getMatrices();
        //#else
        //$$ // 1.20.1以下版本直接返回存储的矩阵栈
        //$$ return matrices;
        //#endif
    }

    /**
     * 获取缩放后的窗口宽度
     * 
     * @return 缩放后的窗口宽度
     */
    public int getScaledWindowWidth() {
        //#if MC >= 12001
        // 1.20.1及以上版本从DrawContext获取缩放后的窗口宽度
        return context.getScaledWindowWidth();
        //#else
        //$$ // 1.20.1以下版本从MinecraftClient获取缩放后的窗口宽度
        //$$ return MinecraftClient.getInstance().getWindow().getScaledWidth();
        //#endif
    }

    /**
     * 获取缩放后的窗口高度
     * 
     * @return 缩放后的窗口高度
     */
    public int getScaledWindowHeight() {
        //#if MC >= 12001
        // 1.20.1及以上版本从DrawContext获取缩放后的窗口高度
        return context.getScaledWindowHeight();
        //#else
        //$$ // 1.20.1以下版本从MinecraftClient获取缩放后的窗口高度
        //$$ return MinecraftClient.getInstance().getWindow().getScaledHeight();
        //#endif
    }

}
