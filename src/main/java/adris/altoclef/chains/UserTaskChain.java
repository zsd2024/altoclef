package adris.altoclef.chains;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.eventbus.EventBus;
import adris.altoclef.eventbus.events.TaskFinishedEvent;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.tasksystem.TaskRunner;
import adris.altoclef.util.time.Stopwatch;

/**
 * 用户任务链 - 运行用户定义任务的链，具有相同优先级
 * 这基本上替代了我们的旧任务运行器
 */
public class UserTaskChain extends SingleTaskChain {

    private final Stopwatch taskStopwatch = new Stopwatch(); // 任务计时器
    private Runnable currentOnFinish = null; // 当前完成回调

    private boolean runningIdleTask; // 标记是否正在运行空闲任务
    private boolean nextTaskIdleFlag; // 下一个任务空闲标志

    public UserTaskChain(TaskRunner runner) {
        super(runner);
    }

    /**
     * 美化打印时间持续时间
     * @param seconds 秒数
     * @return 格式化的时间字符串
     */
    private static String prettyPrintTimeDuration(double seconds) {
        int minutes = (int) (seconds / 60);
        int hours = minutes / 60;
        int days = hours / 24;

        String result = "";
        if (days != 0) {
            result += days + " 天 ";
        }
        if (hours != 0) {
            result += (hours % 24) + " 小时 ";
        }
        if (minutes != 0) {
            result += (minutes % 60) + " 分钟 ";
        }
        if (!result.isEmpty()) {
            result += "和 ";
        }
        result += String.format("%.3f", (seconds % 60));
        return result;
    }

    @Override
    protected void onTick() {

        // 如果未加载到世界中则暂停
        if (!AltoClef.inGame()) return;

        super.onTick();
    }

    /**
     * 取消当前任务
     * @param mod AltoClef实例
     */
    public void cancel(AltoClef mod) {
        if (mainTask != null && mainTask.isActive()) {
            stop();
            onTaskFinish(mod);
        }
        mod.getTaskRunner().disable();

        // FIXME 比较混乱，整个暂停逻辑可能应该移到这个类中
        mod.setStoredTask(null);
        mod.setPaused(false);
    }

    @Override
    public float getPriority() {
        return 50;
    }

    @Override
    public String getName() {
        return "用户任务";
    }

    /**
     * 运行任务
     * @param mod AltoClef实例
     * @param task 要运行的任务
     * @param onFinish 任务完成回调
     */
    public void runTask(AltoClef mod, Task task, Runnable onFinish) {
        runningIdleTask = nextTaskIdleFlag;
        nextTaskIdleFlag = false;

        currentOnFinish = onFinish;

        if (!runningIdleTask) {
            Debug.logMessage("用户任务设置: " + task.toString());
        }
        mod.getTaskRunner().enable();
        taskStopwatch.begin();
        setTask(task);

        if (mod.getModSettings().failedToLoad()) {
            Debug.logWarning("设置文件在某处加载失败。检查日志获取更多信息，或删除" +
                    "文件以重新加载工作设置。");
        }
    }

    @Override
    protected void onTaskFinish(AltoClef mod) {
        boolean shouldIdle = mod.getModSettings().shouldRunIdleCommandWhenNotActive();
        if (!shouldIdle) {
            // 停止
            mod.getTaskRunner().disable();
            // 额外重置。有时baritone会延迟，不能正确重置我们的按键
            mod.getClientBaritone().getInputOverrideHandler().clearAllKeys();
        }
        double seconds = taskStopwatch.time();
        Task oldTask = mainTask;
        mainTask = null;
        if (currentOnFinish != null) {
            currentOnFinish.run();
        }
        // 我们的 `onFinish` 可能触发了更多任务
        boolean actuallyDone = mainTask == null;
        if (actuallyDone) {
            if (!runningIdleTask) {
                Debug.logMessage("用户任务完成。耗时 %s 秒。", prettyPrintTimeDuration(seconds));
                EventBus.publish(new TaskFinishedEvent(seconds, oldTask));
            }
            if (shouldIdle) {
                AltoClef.getCommandExecutor().executeWithPrefix(mod.getModSettings().getIdleCommand());
                signalNextTaskToBeIdleTask();
                runningIdleTask = true;
            }
        }
    }

    /**
     * 检查是否正在运行空闲任务
     * @return 如果正在运行空闲任务则返回true
     */
    public boolean isRunningIdleTask() {
        return isActive() && runningIdleTask;
    }

    /**
     * 下一个任务将是一个空闲任务
     */
    public void signalNextTaskToBeIdleTask() {
        nextTaskIdleFlag = true;
    }
}
