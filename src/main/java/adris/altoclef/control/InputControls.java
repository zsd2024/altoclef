package adris.altoclef.control;

import baritone.api.utils.input.Input;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.KeyBinding;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

/**
 * 输入控制器
 * 有时我们想要触发一帧的"按下"操作，或执行其他输入强制操作。
 * <p>
 * 每次都跟踪按下状态和计时是很烦人的。
 * <p>
 * 出于某些原因，使用baritone的"Forcestate"并不总是有效，也许这是我的问题。
 * <p>
 * 但这个类将消除所有困惑。
 */
@SuppressWarnings("UnnecessaryDefault")
public class InputControls {

    // 需要释放的输入队列
    private final Queue<Input> toUnpress = new ArrayDeque<>();
    // 等待释放的输入集合（点击需要释放）
    private final Set<Input> _waitForRelease = new HashSet<>();

        /**
     * 将输入枚举转换为按键绑定
     * @param input 输入枚举
     * @return 对应的按键绑定
     */
    private static KeyBinding inputToKeyBinding(Input input) {
        GameOptions o = MinecraftClient.getInstance().options;
        return switch (input) {
            case MOVE_FORWARD -> o.forwardKey;        // 前进
            case MOVE_BACK -> o.backKey;              // 后退
            case MOVE_LEFT -> o.leftKey;              // 左移
            case MOVE_RIGHT -> o.rightKey;            // 右移
            case CLICK_LEFT -> o.attackKey;           // 左键点击
            case CLICK_RIGHT -> o.useKey;             // 右键点击
            case JUMP -> o.jumpKey;                   // 跳跃
            case SNEAK -> o.sneakKey;                 // 潜行
            case SPRINT -> o.sprintKey;               // 冲刺
            default -> throw new IllegalArgumentException("无效的按键输入/未考虑: " + input);
        };
    }

        /**
     * 尝试按下指定的输入（仅一帧）
     * @param input 要按下的输入
     */
    public void tryPress(Input input) {
        // 我们刚刚按下，所以让我们释放。
        if (_waitForRelease.contains(input)) {
            return;
        }
        inputToKeyBinding(input).setPressed(true);
        // 也需要确保游戏将输入注册为"按下"
        KeyBinding.onKeyPressed(inputToKeyBinding(input).getDefaultKey());
        toUnpress.add(input);
        _waitForRelease.add(input);
    }

        /**
     * 持续按下指定的输入
     * @param input 要持续按下的输入
     */
    public void hold(Input input) {
        if (!inputToKeyBinding(input).isPressed()) {
            KeyBinding.onKeyPressed(inputToKeyBinding(input).getDefaultKey());
        }
        inputToKeyBinding(input).setPressed(true);
    }

        /**
     * 释放指定的输入
     * @param input 要释放的输入
     */
    public void release(Input input) {
        inputToKeyBinding(input).setPressed(false);
    }

        /**
     * 检查指定输入是否被按下
     * @param input 要检查的输入
     * @return 如果输入被按下返回true，否则返回false
     */
    public boolean isHeldDown(Input input) {
        return inputToKeyBinding(input).isPressed();
    }

        /**
     * 强制设置玩家视角
     * @param yaw Y轴旋转角度（偏航角）
     * @param pitch X轴旋转角度（俯仰角）
     */
    public void forceLook(float yaw, float pitch) {
        if (MinecraftClient.getInstance().player != null) {
            MinecraftClient.getInstance().player.setYaw(yaw);
            MinecraftClient.getInstance().player.setPitch(pitch);
        }
    }

        // 在用户为当前帧调用输入命令之前
    public void onTickPre() {
        while (!toUnpress.isEmpty()) {
            inputToKeyBinding(toUnpress.remove()).setPressed(false);
        }
    }

        // 在用户为当前帧调用输入命令之后
    public void onTickPost() {
        _waitForRelease.clear();
    }
}
