package adris.altoclef.tasks.speedrun.beatgame.prioritytask.tasks;

import adris.altoclef.AltoClef;
import adris.altoclef.TaskCatalogue;
import adris.altoclef.tasks.speedrun.beatgame.prioritytask.prioritycalculators.ItemPriorityCalculator;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.ItemTarget;

import java.util.Arrays;
import java.util.function.Function;

/**
 * 资源优先级任务 - 表示给定优先级计算器和物品目标的TaskCatalogue.getItemTask的包装器
 */
public class ResourcePriorityTask extends PriorityTask {


    private final ItemPriorityCalculator priorityCalculator; // 优先级计算器
    private final ItemTarget[] collect; // 收集目标
    private boolean collected = false; // 是否已收集完成
    private Task task = null; // 任务

    public ResourcePriorityTask(ItemPriorityCalculator priorityCalculator, Function<AltoClef, Boolean> canCall,Task task, ItemTarget... collect) {
        this(priorityCalculator, canCall, false, true, false, collect);
        this.task = task;

    }

    public ResourcePriorityTask(ItemPriorityCalculator priorityCalculator, Function<AltoClef, Boolean> canCall, ItemTarget... collect) {
        this(priorityCalculator, canCall, false, true, false, collect);
    }


    public ResourcePriorityTask(ItemPriorityCalculator priorityCalculator, Function<AltoClef, Boolean> canCall, boolean shouldForce, boolean canCache, boolean bypassForceCooldown, ItemTarget... collect) {
        super(canCall, shouldForce, canCache, bypassForceCooldown);

        this.collect = collect;
        this.priorityCalculator = priorityCalculator;
    }

    @Override
    public Task getTask(AltoClef mod) {
        if (this.task != null) return task; // 如果有自定义任务，返回自定义任务

        return TaskCatalogue.getSquashedItemTask(collect); // 否则返回整理后的物品任务
    }

    @Override
    public String getDebugString() {
        return "收集资源: "+ Arrays.toString(collect);
    }

    @Override
    public double getPriority(AltoClef mod) {
        if (collected) return Double.NEGATIVE_INFINITY; // 如果已收集完成，返回负无穷

        int count = 0;
        for (ItemTarget target : collect) {
            count += mod.getItemStorage().getItemCount(target.getMatches()); // 计算收集目标的总数量
        }

        if (count >= priorityCalculator.maxCount) {
            collected= true; // 如果达到最大数量，标记为已收集完成
        }

        return priorityCalculator.getPriority(count); // 返回优先级
    }


    /**
     * 检查是否已收集完成
     * @return 是否已收集完成
     */
    public boolean isCollected() {
        return collected;
    }
}
