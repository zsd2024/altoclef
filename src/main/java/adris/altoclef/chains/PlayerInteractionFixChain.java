package adris.altoclef.chains;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.tasksystem.TaskChain;
import adris.altoclef.tasksystem.TaskRunner;
import adris.altoclef.util.helpers.ItemHelper;
import adris.altoclef.util.helpers.LookHelper;
import adris.altoclef.util.helpers.StorageHelper;
import adris.altoclef.util.slots.PlayerSlot;
import adris.altoclef.util.slots.Slot;
import adris.altoclef.util.time.TimerGame;
import baritone.api.utils.Rotation;
import baritone.api.utils.input.Input;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.DeathScreen;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;

import java.util.Optional;

/**
 * 玩家交互修复链 - 处理玩家交互中的各种修复操作
 * 包括自动装备最佳工具、清理光标中的物品、自动关闭屏幕等
 */
public class PlayerInteractionFixChain extends TaskChain {
    private final TimerGame stackHeldTimeout = new TimerGame(1); // 物品持有超时计时器
    private final TimerGame generalDuctTapeSwapTimeout = new TimerGame(30); // 通用修复交换超时计时器
    private final TimerGame shiftDepressTimeout = new TimerGame(10); // Shift键按下超时计时器
    private final TimerGame betterToolTimer = new TimerGame(0); // 更好工具计时器
    private final TimerGame mouseMovingButScreenOpenTimeout = new TimerGame(1); // 鼠标移动但屏幕打开超时计时器
    private ItemStack lastHandStack = null; // 上一次光标中的物品栈

    private Screen lastScreen; // 上一个屏幕
    private Rotation lastLookRotation; // 上一次的视角旋转

    public PlayerInteractionFixChain(TaskRunner runner) {
        super(runner);
    }

    @Override
    protected void onStop() {
        // 停止时无需特殊处理
    }

    @Override
    public void onInterrupt(TaskChain other) {
        // 被其他链中断时无需特殊处理
    }

    @Override
    protected void onTick() {
        // 每个刻度的处理在getPriority()中实现
    }

    @Override
    public float getPriority() {
        if (!AltoClef.inGame()) return Float.NEGATIVE_INFINITY;

        AltoClef mod = AltoClef.getInstance();

        if (mod.getUserTaskChain().isActive() && betterToolTimer.elapsed()) {
            // 如果我们没有使用正确的工具，则装备合适的工具
            betterToolTimer.reset();
            if (mod.getControllerExtras().isBreakingBlock()) {
                BlockState state = mod.getWorld().getBlockState(mod.getControllerExtras().getBreakingBlockPos());
                Optional<Slot> bestToolSlot = StorageHelper.getBestToolSlot(mod, state);
                Slot currentEquipped = PlayerSlot.getEquipSlot();

                // 如果baritone正在运行，只接受工具栏外的工具！
                // Baritone会处理工具栏内的工具
                if (bestToolSlot.isPresent() && !bestToolSlot.get().equals(currentEquipped)) {
                    // 仅当物品类严格不同时才装备（否则我们会频繁交换）
                    if (StorageHelper.getItemStackInSlot(currentEquipped).getItem() != StorageHelper.getItemStackInSlot(bestToolSlot.get()).getItem()) {
                        boolean isAllowedToManage = (!mod.getClientBaritone().getPathingBehavior().isPathing() ||
                                bestToolSlot.get().getInventorySlot() >= 9) && !mod.getFoodChain().isTryingToEat();
                        if (isAllowedToManage) {
                            Debug.logMessage("在库存中找到更好的工具，正在装备。");
                            ItemStack bestToolItemStack = StorageHelper.getItemStackInSlot(bestToolSlot.get());
                            Item bestToolItem = bestToolItemStack.getItem();
                            mod.getSlotHandler().forceEquipItem(bestToolItem);
                        }
                    }
                }
            }
        }

        // 释放shift键（由于某种原因它会卡住???）
        if (mod.getInputControls().isHeldDown(Input.SNEAK)) {
            if (shiftDepressTimeout.elapsed()) {
                mod.getInputControls().release(Input.SNEAK);
            }
        } else {
            shiftDepressTimeout.reset();
        }

        // 刷新库存
        if (generalDuctTapeSwapTimeout.elapsed()) {
            if (!mod.getControllerExtras().isBreakingBlock()) {
                Debug.logMessage("已刷新库存...");
                mod.getSlotHandler().refreshInventory();
                generalDuctTapeSwapTimeout.reset();
                return Float.NEGATIVE_INFINITY;
            }
        }

        ItemStack currentStack = StorageHelper.getItemStackInCursorSlot();

        if (currentStack != null && !currentStack.isEmpty()) {
            //noinspection PointlessNullCheck
            if (lastHandStack == null || !ItemStack.areEqual(currentStack, lastHandStack)) {
                // 我们在光标中持有了一个新物品！
                stackHeldTimeout.reset();
                lastHandStack = currentStack.copy();
            }
        } else {
            stackHeldTimeout.reset();
            lastHandStack = null;
        }

        // 如果我们在手中持有一段时间...
        if (lastHandStack != null && stackHeldTimeout.elapsed()) {
            Optional<Slot> moveTo = mod.getItemStorage().getSlotThatCanFitInPlayerInventory(lastHandStack, false);
            if (moveTo.isPresent()) {
                mod.getSlotHandler().clickSlot(moveTo.get(), 0, SlotActionType.PICKUP);
                return Float.NEGATIVE_INFINITY;
            }
            if (ItemHelper.canThrowAwayStack(mod, StorageHelper.getItemStackInCursorSlot())) {
                mod.getSlotHandler().clickSlot(Slot.UNDEFINED, 0, SlotActionType.PICKUP);
                return Float.NEGATIVE_INFINITY;
            }
            Optional<Slot> garbage = StorageHelper.getGarbageSlot(mod);
            // 如果光标槽是垃圾，尝试丢弃
            if (garbage.isPresent()) {
                mod.getSlotHandler().clickSlot(garbage.get(), 0, SlotActionType.PICKUP);
                return Float.NEGATIVE_INFINITY;
            }
            mod.getSlotHandler().clickSlot(Slot.UNDEFINED, 0, SlotActionType.PICKUP);
            return Float.NEGATIVE_INFINITY;
        }

        if (shouldCloseOpenScreen()) {
            //Debug.logMessage("由于我们改变了视角，关闭了屏幕。");
            ItemStack cursorStack = StorageHelper.getItemStackInCursorSlot();
            if (!cursorStack.isEmpty()) {
                Optional<Slot> moveTo = mod.getItemStorage().getSlotThatCanFitInPlayerInventory(cursorStack, false);
                if (moveTo.isPresent()) {
                    mod.getSlotHandler().clickSlot(moveTo.get(), 0, SlotActionType.PICKUP);
                    return Float.NEGATIVE_INFINITY;
                }
                if (ItemHelper.canThrowAwayStack(mod, cursorStack)) {
                    mod.getSlotHandler().clickSlot(Slot.UNDEFINED, 0, SlotActionType.PICKUP);
                    return Float.NEGATIVE_INFINITY;
                }
                Optional<Slot> garbage = StorageHelper.getGarbageSlot(mod);
                // 如果光标槽是垃圾，尝试丢弃
                if (garbage.isPresent()) {
                    mod.getSlotHandler().clickSlot(garbage.get(), 0, SlotActionType.PICKUP);
                    return Float.NEGATIVE_INFINITY;
                }
                mod.getSlotHandler().clickSlot(Slot.UNDEFINED, 0, SlotActionType.PICKUP);
            } else {
                StorageHelper.closeScreen();
            }
            return Float.NEGATIVE_INFINITY;
        }

        return Float.NEGATIVE_INFINITY;
    }

    /**
     * 检查是否应该关闭打开的屏幕
     * @return 如果应该关闭屏幕则返回true
     */
    private boolean shouldCloseOpenScreen() {
        if (!AltoClef.getInstance().getModSettings().shouldCloseScreenWhenLookingOrMining())
            return false;

        // 只有在相同屏幕打开一段时间后才检查视角
        Screen openScreen = MinecraftClient.getInstance().currentScreen;
        if (openScreen != lastScreen) {
            mouseMovingButScreenOpenTimeout.reset();
        }
        // 我们在播放器屏幕/我们不想退出的屏幕
        if (openScreen == null || openScreen instanceof ChatScreen || openScreen instanceof GameMenuScreen || openScreen instanceof DeathScreen) {
            mouseMovingButScreenOpenTimeout.reset();
            return false;
        }
        // 检查旋转变化
        Rotation look = LookHelper.getLookRotation();
        if (lastLookRotation != null && mouseMovingButScreenOpenTimeout.elapsed()) {
            Rotation delta = look.subtract(lastLookRotation);
            if (Math.abs(delta.getYaw()) > 0.1f || Math.abs(delta.getPitch()) > 0.1f) {
                lastLookRotation = look;
                return true;
            }
            // 不要更新我们的最后视角旋转，只是因为我们想要测量长期旋转
        } else {
            lastLookRotation = look;
        }
        lastScreen = openScreen;
        return false;
    }

    @Override
    public boolean isActive() {
        return true;
    }

    @Override
    public String getName() {
        return "手持物品栈修复链";
    }
}
