package adris.altoclef.tasks.slot;

import adris.altoclef.AltoClef;
import adris.altoclef.control.SlotHandler;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.slots.Slot;
import net.minecraft.screen.slot.SlotActionType;

/**
 * 点击槽位的任务
 */
public class ClickSlotTask extends Task {

    // 目标槽位
    private final Slot slot;
    // 鼠标按钮（0为左键，1为右键）
    private final int mouseButton;
    // 槽位操作类型
    private final SlotActionType type;

    // 是否已点击
    private boolean clicked = false;

    /**
     * 构造函数
     * @param slot 槽位
     * @param mouseButton 鼠标按钮
     * @param type 操作类型
     */
    public ClickSlotTask(Slot slot, int mouseButton, SlotActionType type) {
        this.slot = slot;
        this.mouseButton = mouseButton;
        this.type = type;
    }

    /**
     * 构造函数（指定槽位和操作类型，默认鼠标按钮）
     * @param slot 槽位
     * @param type 操作类型
     */
    public ClickSlotTask(Slot slot, SlotActionType type) {
        this(slot, 0, type);
    }

    /**
     * 构造函数（指定槽位和鼠标按钮，默认操作类型为拾取）
     * @param slot 槽位
     * @param mouseButton 鼠标按钮
     */
    public ClickSlotTask(Slot slot, int mouseButton) {
        this(slot, mouseButton, SlotActionType.PICKUP);
    }

    /**
     * 构造函数（仅指定槽位，默认操作类型为拾取，鼠标按钮为0）
     * @param slot 槽位
     */
    public ClickSlotTask(Slot slot) {
        this(slot, SlotActionType.PICKUP);
    }

    @Override
    protected void onStart() {
        clicked = false;
    }

    @Override
    protected Task onTick() {
        SlotHandler slotHandler = AltoClef.getInstance().getSlotHandler();

        if (slotHandler.canDoSlotAction()) {
            // 执行槽位点击操作
            slotHandler.clickSlot(slot, mouseButton, type);
            // 注册槽位操作
            slotHandler.registerSlotAction();
            clicked = true;
        }
        return null;
    }

    @Override
    protected void onStop(Task interruptTask) {
        // 任务停止时不需要特别处理
    }

    @Override
    protected boolean isEqual(Task obj) {
        if (obj instanceof ClickSlotTask task) {
            // 比较鼠标按钮、操作类型和槽位是否相同
            return task.mouseButton == mouseButton && task.type == type && task.slot.equals(slot);
        }
        return false;
    }

    @Override
    protected String toDebugString() {
        return "点击 " + slot.toString();
    }

    @Override
    public boolean isFinished() {
        // 当点击操作完成时任务完成
        return clicked;
    }
}
