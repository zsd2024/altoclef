package adris.altoclef.tasks.speedrun.beatgame.prioritytask.tasks;

import adris.altoclef.AltoClef;
import adris.altoclef.tasks.speedrun.beatgame.prioritytask.prioritycalculators.PriorityCalculator;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.Pair;

import java.util.function.Function;

/**
 * 动作优先级任务 - 创建优先级任务的最通用方式（基本上其他所有子类都可以用这个替换）。
 * 给定优先级计算器和任务提供器，返回一个任务和优先级
 * （有点像旧的GatherResource，我猜）
 */
public class ActionPriorityTask extends PriorityTask {


    private final TaskAndPriorityProvider taskAndPriorityProvider; // 任务和优先级提供器

    // 仅用于生成调试字符串
    private Task lastTask = null;

    public ActionPriorityTask(TaskProvider taskProvider, PriorityCalculator priorityCalculator) {
        this(taskProvider, priorityCalculator, a -> true, false, true, false);
    }

    public ActionPriorityTask(TaskProvider taskProvider, PriorityCalculator priorityCalculator, Function<AltoClef, Boolean> canCall) {
        this((mod -> new Pair<>(taskProvider.getTask(mod), priorityCalculator.getPriority())), canCall);
    }

    public ActionPriorityTask(TaskAndPriorityProvider taskAndPriorityProvider) {
        this(taskAndPriorityProvider, a -> true);
    }

    public ActionPriorityTask(TaskAndPriorityProvider taskAndPriorityProvider, Function<AltoClef, Boolean> canCall) {
        this(taskAndPriorityProvider, canCall, false, true, false);
    }

    public ActionPriorityTask(TaskProvider taskProvider, PriorityCalculator priorityCalculator, Function<AltoClef, Boolean> canCall, boolean shouldForce, boolean canCache, boolean bypassForceCooldown) {
       this((mod -> new Pair<>(taskProvider.getTask(mod), priorityCalculator.getPriority())), canCall, shouldForce, canCache, bypassForceCooldown);
    }

    public ActionPriorityTask(TaskAndPriorityProvider taskAndPriorityProvider, Function<AltoClef, Boolean> canCall, boolean shouldForce, boolean canCache, boolean bypassForceCooldown) {
        super(canCall, shouldForce, canCache, bypassForceCooldown);
        this.taskAndPriorityProvider = taskAndPriorityProvider;
    }


    @Override
    public Task getTask(AltoClef mod) {
        // 获取任务和优先级对，返回任务部分
        lastTask = getTaskAndPriority(mod).getLeft();
        return lastTask;
    }

    @Override
    public String getDebugString() {
        return "执行一个动作: "+lastTask;
    }

    @Override
    protected double getPriority(AltoClef mod) {
        // 获取任务和优先级对，返回优先级部分
        return getTaskAndPriority(mod).getRight();
    }

    /**
     * 获取任务和优先级
     * @param mod AltoClef实例
     * @return 任务和优先级的配对
     */
    private Pair<Task, Double> getTaskAndPriority(AltoClef mod) {
        Pair<Task, Double> pair = taskAndPriorityProvider.getTaskAndPriority(mod);
        if (pair == null) {
            pair = new Pair<>(null, 0d);
        }

        if (pair.getRight() <= 0 || pair.getLeft() == null) {
            pair.setLeft(null);
            pair.setRight(Double.NEGATIVE_INFINITY);
        }

        return pair;
    }


    /**
     * 任务提供器接口
     */
    public interface TaskProvider {
        Task getTask(AltoClef mod);
    }


    /**
     * 任务和优先级提供器接口
     */
    public interface TaskAndPriorityProvider {
        Pair<Task, Double> getTaskAndPriority(AltoClef mod);
    }


}
