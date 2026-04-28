package adris.altoclef.tasks.misc;

import adris.altoclef.AltoClef;
import adris.altoclef.tasksystem.Task;

/**
 * 睡过夜晚任务
 * 此任务负责让玩家通过睡觉跳过夜晚时间，直到白天开始
 */
public class SleepThroughNightTask extends Task {

    /**
     * 任务开始时调用的方法
     * 此任务不需要特殊初始化
     */
    @Override
    protected void onStart() {

    }

    /**
     * 每帧执行的任务逻辑
     * 返回一个放置床并留在床中的任务
     *
     * @return 放置床并设置重生点任务，并设置为留在床中
     */
    @Override
    protected Task onTick() {
        return new PlaceBedAndSetSpawnTask().stayInBed();
    }

    /**
     * 任务被中断时调用的方法
     * 此任务不需要特殊清理操作
     *
     * @param interruptTask 中断此任务的任务
     */
    @Override
    protected void onStop(Task interruptTask) {

    }

    /**
     * 检查给定任务是否与此任务相等
     *
     * @param other 要比较的任务
     * @return 如果任务是SleepThroughNightTask的实例则返回true，否则返回false
     */
    @Override
    protected boolean isEqual(Task other) {
        return other instanceof SleepThroughNightTask;
    }

    /**
     * 返回任务的调试字符串表示
     *
     * @return 调试字符串"睡过整个夜晚"
     */
    @Override
    protected String toDebugString() {
        return "睡过整个夜晚";
    }

    /**
     * 检查任务是否已完成
     * 当游戏时间处于白天（0-13000 ticks）时任务完成
     *
     * @return 如果当前是白天则返回true，否则返回false
     */
    @Override
    public boolean isFinished() {
        // 我们处于白天
        int time = (int) (AltoClef.getInstance().getWorld().getTimeOfDay() % 24000);
        return 0 <= time && time < 13000;
    }
}
