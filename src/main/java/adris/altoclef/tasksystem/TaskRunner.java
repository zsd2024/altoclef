package adris.altoclef.tasksystem;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;

import java.util.ArrayList;

/**
 * 任务执行器 - 负责管理和执行所有任务链
 * 根据优先级选择并运行最高优先级的活动任务链
 */
public class TaskRunner {

    /** 所有注册的任务链列表 */
    private final ArrayList<TaskChain> chains = new ArrayList<>();
    /** AltoClef主模块实例 */
    private final AltoClef mod;
    /** 标记任务执行器是否处于活动状态 */
    private boolean active;

    /** 缓存当前正在执行的任务链 */
    private TaskChain cachedCurrentTaskChain = null;

    /** 状态报告字符串 */
    public String statusReport = " (没有运行的任务链) ";

    /**
     * 构造函数
     * @param mod AltoClef主模块实例
     */
    public TaskRunner(AltoClef mod) {
        this.mod = mod;
        active = false;
    }

    /**
     * 任务执行器主循环方法
     * 选择并执行最高优先级的活动任务链
     */
    public void tick() {
        if (!active || !AltoClef.inGame()) {
            statusReport = " (没有运行的任务链) ";
            return;
        }

        // 获取最高优先级的任务链并运行
        TaskChain maxChain = null;
        float maxPriority = Float.NEGATIVE_INFINITY;
        for (TaskChain chain : chains) {
            if (!chain.isActive()) continue;
            float priority = chain.getPriority();
            if (priority > maxPriority) {
                maxPriority = priority;
                maxChain = chain;
            }
        }
        if (cachedCurrentTaskChain != null && maxChain != cachedCurrentTaskChain) {
            cachedCurrentTaskChain.onInterrupt(maxChain);
        }
        cachedCurrentTaskChain = maxChain;
        if (maxChain != null) {
            statusReport = "任务链: "+maxChain.getName() + ", 优先级: "+maxPriority;
            maxChain.tick();
        } else {
            statusReport = " (没有运行的任务链) ";
        }
    }

    /**
     * 添加任务链到执行器
     * @param chain 要添加的任务链
     */
    public void addTaskChain(TaskChain chain) {
        chains.add(chain);
    }

    /**
     * 启用任务执行器
     */
    public void enable() {
        if (!active) {
            mod.getBehaviour().push();
            mod.getBehaviour().setPauseOnLostFocus(false);
        }
        active = true;
    }

    /**
     * 禁用任务执行器
     */
    public void disable() {
        if (active) {
            mod.getBehaviour().pop();
            Debug.logMessage("已停止");
        }
        for (TaskChain chain : chains) {
            chain.stop();
        }
        active = false;
    }

    /**
     * 检查任务执行器是否处于活动状态
     * @return 如果任务执行器处于活动状态则返回true，否则返回false
     */
    public boolean isActive() {
        return active;
    }

    /**
     * 获取当前正在执行的任务链
     * @return 当前正在执行的任务链，如果没有则返回null
     */
    public TaskChain getCurrentTaskChain() {
        return cachedCurrentTaskChain;
    }

    // 有点笨拙，说实话
    /**
     * 获取AltoClef主模块实例
     * @return AltoClef主模块实例
     */
    public AltoClef getMod() {
        return mod;
    }
}
