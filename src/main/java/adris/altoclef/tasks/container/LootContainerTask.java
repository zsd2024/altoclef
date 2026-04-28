package adris.altoclef.tasks.container;

import adris.altoclef.AltoClef;
import adris.altoclef.tasks.InteractWithBlockTask;
import adris.altoclef.tasks.slot.EnsureFreeInventorySlotTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.trackers.storage.ContainerType;
import adris.altoclef.util.helpers.ItemHelper;
import adris.altoclef.util.helpers.StorageHelper;
import adris.altoclef.util.slots.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;


/**
 * 掠夺容器任务 - 从指定容器中掠夺特定物品
 */
public class LootContainerTask extends Task {
    public final BlockPos chest; // 容器位置
    public final List<Item> targets = new ArrayList<>(); // 目标物品列表
    private final Predicate<ItemStack> check; // 物品检查条件
    private boolean weDoneHere = false; // 是否已完成

    public LootContainerTask(BlockPos chestPos, List<Item> items) {
        chest = chestPos;
        targets.addAll(items);
        check = x -> true;
    }

    public LootContainerTask(BlockPos chestPos, List<Item> items, Predicate<ItemStack> pred) {
        chest = chestPos;
        targets.addAll(items);
        check = pred;
    }

    @Override
    protected void onStart() {
        AltoClef mod = AltoClef.getInstance();

        mod.getBehaviour().push();
        // 保护目标物品，防止在掠夺过程中被丢弃
        for (Item item : targets) {
            if (!mod.getBehaviour().isProtected(item)) {
                mod.getBehaviour().addProtectedItems(item);
            }
        }
    }

    @Override
    protected Task onTick() {
        if (!ContainerType.screenHandlerMatches(ContainerType.CHEST)) {
            setDebugState("与容器交互");
            return new InteractWithBlockTask(chest);
        }
        AltoClef mod = AltoClef.getInstance();

        ItemStack cursor = StorageHelper.getItemStackInCursorSlot();
        if (!cursor.isEmpty()) {
            Optional<Slot> toFit = mod.getItemStorage().getSlotThatCanFitInPlayerInventory(cursor, false);
            if (toFit.isPresent()) {
                setDebugState("将光标物品放入背包");
                mod.getSlotHandler().clickSlot(toFit.get(), 0, SlotActionType.PICKUP);
                return null;
            } else {
                setDebugState("确保存储空间");
                return new EnsureFreeInventorySlotTask();
            }
        }
        Optional<Slot> optimal = getAMatchingSlot(mod);
        if (optimal.isEmpty()) {
            weDoneHere = true;
            return null;
        }
        setDebugState("掠夺物品: " + targets);
        mod.getSlotHandler().clickSlot(optimal.get(), 0, SlotActionType.PICKUP);
        return null;
    }

    @Override
    protected void onStop(Task task) {
        AltoClef mod = AltoClef.getInstance();

        ItemStack cursorStack = StorageHelper.getItemStackInCursorSlot();
        if (!cursorStack.isEmpty()) {
            Optional<Slot> moveTo = mod.getItemStorage().getSlotThatCanFitInPlayerInventory(cursorStack, false);
            moveTo.ifPresent(slot -> mod.getSlotHandler().clickSlot(slot, 0, SlotActionType.PICKUP));
            if (ItemHelper.canThrowAwayStack(mod, cursorStack)) {
                mod.getSlotHandler().clickSlot(Slot.UNDEFINED, 0, SlotActionType.PICKUP);
            }
            Optional<Slot> garbage = StorageHelper.getGarbageSlot(mod);
            // 如果光标物品是垃圾，尝试丢弃
            garbage.ifPresent(slot -> mod.getSlotHandler().clickSlot(slot, 0, SlotActionType.PICKUP));
            mod.getSlotHandler().clickSlot(Slot.UNDEFINED, 0, SlotActionType.PICKUP);
        } else {
            StorageHelper.closeScreen();
        }
        mod.getBehaviour().pop();
    }

    @Override
    protected boolean isEqual(Task other) {
        if (other instanceof LootContainerTask lootContainerTask) {
            return targets.equals(lootContainerTask.targets) &&
                chest.equals(lootContainerTask.chest);
        }
        return false;
    }

    private Optional<Slot> getAMatchingSlot(AltoClef mod) {
        for (Item item : targets) {
            List<Slot> slots = mod.getItemStorage().getSlotsWithItemContainer(item);
            if (!slots.isEmpty()) for (Slot slot : slots) {
                if (check.test(StorageHelper.getItemStackInSlot(slot))) return Optional.of(slot);
            }
        }
        return Optional.empty();
    }

    @Override
    public boolean isFinished() {
        return weDoneHere || (ContainerType.screenHandlerMatchesAny() &&
                getAMatchingSlot(AltoClef.getInstance()).isEmpty());
    }

    @Override
    protected String toDebugString() {
        return "掠夺容器";
    }
}
