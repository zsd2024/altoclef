package adris.altoclef.ui;

import adris.altoclef.AltoClef;
import adris.altoclef.multiversion.DrawContextWrapper;
import adris.altoclef.tasksystem.Task;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.math.MatrixStack;

import java.awt.*;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

/**
 * 命令状态覆盖层
 * 负责渲染任务链和计时器的状态信息
 */
public class CommandStatusOverlay {

    // 日期时间格式化器（UTC时区）
    private final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss.SSS").withZone(ZoneId.from(ZoneOffset.of("+00:00")));
    // 游戏内计时器相关字段
    private long runningSince; // 开始运行的时间戳
    private long lastTime = 0; // 上次更新时间戳
    private long pausedTime = -1; // 暂停时的时间戳
    private boolean paused = false; // 是否已暂停

    /**
     * 渲染命令状态覆盖层
     * @param mod AltoClef实例
     * @param context 绘制上下文包装器
     */
    public void render(AltoClef mod, DrawContextWrapper context) {
        // 获取当前任务链中的任务列表
        List<Task> tasks = Collections.emptyList();
        if (mod.getTaskRunner().getCurrentTaskChain() != null) {
            tasks = mod.getTaskRunner().getCurrentTaskChain().getTasks();
        }
        // 如果之前已暂停但现在未暂停，恢复计时器
        if (paused && !mod.isPaused()) {
            runningSince = Instant.now().minusMillis(pausedTime).toEpochMilli();
            lastTime = Instant.now().toEpochMilli();
            paused = false;
        }

        MatrixStack matrixStack = context.getMatrices();

        matrixStack.push();

        // 绘制任务链
        drawTaskChain(context,MinecraftClient.getInstance().textRenderer, 10, 10,
                matrixStack, 10, tasks, mod);

        matrixStack.pop();
    }

    /**
     * 绘制任务链
     * @param context 绘制上下文包装器
     * @param renderer 文本渲染器
     * @param x X坐标
     * @param y Y坐标
     * @param matrices 矩阵栈
     * @param maxLines 最大显示行数
     * @param tasks 任务列表
     * @param mod AltoClef实例
     */
    private void drawTaskChain(DrawContextWrapper context, TextRenderer renderer, int x, int y, MatrixStack matrices, int maxLines, List<Task> tasks, AltoClef mod) {
        int whiteColor = 0xFFFFFFFF;

        // 缩放矩阵以支持更小的文本
        matrices.scale(0.5f,0.5f,0.5f);

        int fontHeight = renderer.fontHeight;
        int addX = 4; // X方向偏移量
        int addY = fontHeight + 2; // Y方向偏移量

        // 绘制状态报告
        context.drawText(renderer,mod.getTaskRunner().statusReport, x, y, Color.LIGHT_GRAY.getRGB(), true);
        y += addY;

        // 如果任务列表为空
        if (tasks.isEmpty()) {
            // 如果已暂停且有存储的任务
            if (mod.isPaused() && mod.getStoredTask() != null) {
                if (!paused) {
                    paused = true;
                    pausedTime = Instant.now().minusMillis(runningSince).toEpochMilli();
                }
                String realTime = DATE_TIME_FORMATTER.format(Instant.ofEpochMilli(pausedTime));
                context.drawText(renderer, "<" + realTime + ">", x, y, whiteColor, true);
                x += addX;
                y += addY;

                context.drawText(renderer, "（已暂停）", x, y, Color.LIGHT_GRAY.getRGB(), true);
                renderTask(mod.getStoredTask(), context, renderer, x+addX*2, y+addY);
                return;
            }
            // 如果任务运行器处于活动状态但没有任务
            if (mod.getTaskRunner().isActive()) {
                context.drawText(renderer, "（无任务运行）", x, y, whiteColor, true);
            }
            // 如果10秒内没有运行任何任务且应显示计时器，则重置计时器
            if (lastTime + 10000 < Instant.now().toEpochMilli() && mod.getModSettings().shouldShowTimer()) {
                runningSince = Instant.now().toEpochMilli(); // 重置计时器
            }
            return;
        }

        // 如果应显示计时器
        if (mod.getModSettings().shouldShowTimer()) {
            lastTime = Instant.now().toEpochMilli();

            String realTime = DATE_TIME_FORMATTER.format(Instant.now().minusMillis(runningSince));
            context.drawText(renderer, "<" + realTime + ">", x, y, whiteColor, true);
            x += addX;
            y += addY;
        }

        // 如果任务数量不超过最大显示行数
        if (tasks.size() <= maxLines) {
            for (Task task : tasks) {
                renderTask(task, context, renderer, x, y);

                x += addX;
                y += addY;
            }
            return;
        }

        // 如果任务数量超过最大显示行数，只显示开头和结尾的任务
        for (int i = 0; i < tasks.size(); ++i) {
            if (i == 1) {
                x += addX * 2;
                context.drawText(renderer, "...", x, y, whiteColor, true);

            } else if (i == 0 || i > tasks.size() - maxLines) {
                renderTask(tasks.get(i),context ,renderer, x, y);
            } else {
                continue;
            }

            x += addX;
            y += addY;
        }
    }


    /**
     * 渲染单个任务
     * @param task 任务对象
     * @param context 绘制上下文包装器
     * @param renderer 文本渲染器
     * @param x X坐标
     * @param y Y坐标
     */
    private void renderTask(Task task, DrawContextWrapper context, TextRenderer renderer, int x, int y) {
        String taskName = task.getClass().getSimpleName() + " ";
        // 绘制任务类名（灰色）
        context.drawText(renderer, taskName, x, y, new Color(128, 128, 128).getRGB(), true);

        // 绘制任务详细信息（白色）
        context.drawText(renderer, task.toString(), x + renderer.getWidth(taskName), y, new Color(255, 255, 255).getRGB(), true);
    }

    /**
     * 重置计时器
     */
    public void resetTimer() {
        runningSince = Instant.now().toEpochMilli(); // 重置计时器
        lastTime = 0;
        paused = false;
        pausedTime = -1;
    }
}
