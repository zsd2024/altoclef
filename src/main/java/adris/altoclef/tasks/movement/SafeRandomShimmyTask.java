package adris.altoclef.tasks.movement;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.helpers.LookHelper;
import adris.altoclef.util.time.TimerGame;
import baritone.Baritone;
import baritone.api.utils.input.Input;

/**
 * 安全随机摆动任务 - 在按住潜行键时随机移动
 * 用于从baritone无法正常工作的情况中逃脱。
 */
public class SafeRandomShimmyTask extends Task {

    private final TimerGame _lookTimer; // 看向计时器

    public SafeRandomShimmyTask(float randomLookInterval) {
        _lookTimer = new TimerGame(randomLookInterval);
    }

    public SafeRandomShimmyTask() {
        this(5);
    }

    @Override
    protected void onStart() {
        _lookTimer.reset();
    }

    @Override
    protected Task onTick() {

        if (_lookTimer.elapsed()) {
            Debug.logMessage("随机方向");
            _lookTimer.reset();
            LookHelper.randomOrientation(); // 随机改变朝向
        }

        Baritone baritone = AltoClef.getInstance().getClientBaritone();

        // 强制按住潜行、前进和左键
        baritone.getInputOverrideHandler().setInputForceState(Input.SNEAK, true);
        baritone.getInputOverrideHandler().setInputForceState(Input.MOVE_FORWARD, true);
        baritone.getInputOverrideHandler().setInputForceState(Input.CLICK_LEFT, true);
        return null;
    }

    @Override
    protected void onStop(Task interruptTask) {
        Baritone baritone = AltoClef.getInstance().getClientBaritone();

        // 释放按键状态
        baritone.getInputOverrideHandler().setInputForceState(Input.MOVE_FORWARD, false);
        baritone.getInputOverrideHandler().setInputForceState(Input.SNEAK, false);
        baritone.getInputOverrideHandler().setInputForceState(Input.CLICK_LEFT, false);
    }

    @Override
    protected boolean isEqual(Task other) {
        return other instanceof SafeRandomShimmyTask;
    }

    @Override
    protected String toDebugString() {
        return "摆动中";
    }
}
