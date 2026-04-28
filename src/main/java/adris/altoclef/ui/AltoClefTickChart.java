package adris.altoclef.ui;

import adris.altoclef.AltoClef;
import adris.altoclef.multiversion.InGameHudVer;
import adris.altoclef.multiversion.DrawContextWrapper;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * AltoClef刻度图表
 * 显示AltoClef占用的游戏刻时间图表（与Minecraft的`TickChart`非常相似）
 */
public class AltoClefTickChart {


    // 文本渲染器
    protected final TextRenderer textRenderer;
    // 存储纳秒时间的列表
    protected final List<Long> list = new ArrayList<>();

    /**
     * 构造函数
     * @param textRenderer 文本渲染器
     */
    public AltoClefTickChart(TextRenderer textRenderer) {
        this.textRenderer = textRenderer;
    }


    /**
     * 推入刻度纳秒时间
     * @param nanoTime 纳秒时间
     */
    public void pushTickNanos(long nanoTime) {
        list.add(nanoTime);
    }

    /**
     * 渲染图表
     * @param mod AltoClef实例
     * @param context 绘制上下文包装器
     * @param x X坐标
     * @param width 宽度
     */
    public void render(AltoClef mod, DrawContextWrapper context, int x, int width) {
        // 如果显示调试HUD或任务运行器未激活，则不渲染
        if (InGameHudVer.shouldShowDebugHud() || !mod.getTaskRunner().isActive()) return;

        int height = context.getScaledWindowHeight();
        // 绘制背景
        context.fill(x, height - 37, x + width, height, 0x90505050);

        long m = Integer.MAX_VALUE;
        long n = Integer.MIN_VALUE;


        // 保持列表大小不超过宽度-1
        while (list.size() >= width-1) {
            list.remove(0);
        }

        // 遍历列表绘制每个刻度条
        for (int i = 0; i < list.size(); ++i) {
            int p = x + i + 1;

            long r = this.get(i);
            m = Math.min(m, r);
            n = Math.max(n, r);

            this.drawTotalBar(context, p, height, i);
        }

        // 绘制边框线
        context.drawHorizontalLine(x, x + width - 1, height - 37, 0xFFDDDDDD);
        context.drawHorizontalLine(x, x + width - 1, height - 1, 0xFFDDDDDD);
        context.drawVerticalLine(x, height - 37, height, 0xFFDDDDDD);
        context.drawVerticalLine(x + width - 1, height - 37, height, 0xFFDDDDDD);


        // 绘制"50 ms"文本
        this.drawBorderedText(context, "50 毫秒", x + 1, height - 37 + 1);
    }


    /**
     * 绘制总刻度条
     * @param context 绘制上下文包装器
     * @param x X坐标
     * @param y Y坐标
     * @param index 索引
     */
    protected void drawTotalBar(DrawContextWrapper context, int x, int y, int index) {
        long l = list.get(index);
        int i = this.getHeight(l);
        int j = this.getColor(l);
        context.fill(x, y - i, x + 1, y, j);
    }

    /**
     * 获取指定索引的值
     * @param index 索引
     * @return 纳秒时间值
     */
    protected long get(int index) {
        return list.get(index);
    }


    /**
     * 绘制带边框的文本
     * @param context 绘制上下文包装器
     * @param string 文本字符串
     * @param x X坐标
     * @param y Y坐标
     */
    protected void drawBorderedText(DrawContextWrapper context, String string, int x, int y) {
        MatrixStack matrixStack = context.getMatrices();
        matrixStack.push();
        matrixStack.scale(0.5f,0.5f,1);

        // 绘制文本背景
        context.fill(x*2, y*2, x*2 + this.textRenderer.getWidth(string) + 2, y*2 + this.textRenderer.fontHeight+1, 0x90505050);
        // 绘制文本
        context.drawText(this.textRenderer, string, (x + 1)*2, (y + 1)*2, 0xE9E9E9, false);

        matrixStack.pop();
    }



    /**
     * 根据值计算高度
     * @param value 纳秒值
     * @return 高度（像素）
     */
    protected int getHeight(double value) {
        return (int)Math.round(nanosToMillis(value) * 37 / 50d);
    }


    /**
     * 根据值获取颜色
     * @param value 纳秒值
     * @return ARGB颜色值
     */
    protected int getColor(long value) {
        float maxMs = 50f;
        double ms = nanosToMillis(value);

        // 如果超过最大毫秒数，返回白色
        if (ms > maxMs) {
            return 0xFFFFFFFF;
        }

        // 根据比例返回渐变颜色（绿色->黄色->红色）
        return getColor(ms/maxMs, 0xFF00FF00, 0xFFFFC800, 0xFFFF0000);
    }

    /**
     * 根据值在三种颜色之间进行插值
     * @param value 0-1之间的值
     * @param minColor 最小值颜色（绿色）
     * @param medianColor 中间值颜色（黄色）
     * @param maxColor 最大值颜色（红色）
     * @return 插值后的ARGB颜色值
     */
    protected int getColor(double value, int minColor, int medianColor, int maxColor) {
        if (value < 0.5) {
            // 在最小值和中间值之间插值
            return lerp((float)((value) / (0.5)), minColor, medianColor);
        }
        // 在中间值和最大值之间插值
        return lerp((float)((value - 0.5) / 0.5), medianColor, maxColor);
    }

    /**
     * 在两个ARGB颜色之间进行线性插值
     * @param delta 插值因子（0-1）
     * @param start 起始颜色
     * @param end 结束颜色
     * @return 插值后的ARGB颜色值
     */
    private static int lerp(float delta, int start, int end) {
        int i = (int) MathHelper.lerp(delta, ColorHelper.Argb.getAlpha(start), ColorHelper.Argb.getAlpha(end));
        int j = (int) MathHelper.lerp(delta, ColorHelper.Argb.getRed(start), ColorHelper.Argb.getRed(end));
        int k = (int) MathHelper.lerp(delta, ColorHelper.Argb.getGreen(start), ColorHelper.Argb.getGreen(end));
        int l = (int) MathHelper.lerp(delta, ColorHelper.Argb.getBlue(start), ColorHelper.Argb.getBlue(end));
        return ColorHelper.Argb.getArgb(i, j, k, l);
    }

    /**
     * 将纳秒转换为毫秒
     * @param nanos 纳秒值
     * @return 毫秒值
     */
    private static double nanosToMillis(double nanos) {
        return nanos / 1_000_000.0;
    }

}
