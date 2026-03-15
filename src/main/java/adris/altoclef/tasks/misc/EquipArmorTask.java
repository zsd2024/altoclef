package adris.altoclef.tasks.misc;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.tasks.slot.MoveItemToSlotFromInventoryTask;
import adris.altoclef.tasks.squashed.CataloguedResourceTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.helpers.ItemHelper;
import adris.altoclef.util.helpers.StorageHelper;
import adris.altoclef.util.slots.PlayerSlot;
import adris.altoclef.util.slots.Slot;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.*;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * 装备盔甲任务
 * 该任务负责将指定的盔甲物品装备到玩家身上
 */
public class EquipArmorTask extends Task {

    // 要装备的物品目标数组
    private final ItemTarget[] toEquip;

    /**
     * 构造函数，指定要装备的物品目标
     * @param toEquip 要装备的物品目标数组
     */
    public EquipArmorTask(ItemTarget... toEquip) {
        this.toEquip = toEquip;
    }

    /**
     * 构造函数，指定要装备的物品
     * @param toEquip 要装备的物品数组
     */
    public EquipArmorTask(Item... toEquip) {
        this(Arrays.stream(toEquip).map(ItemTarget::new).toArray(ItemTarget[]::new));
    }

    @Override
    protected void onStart() {
        // 任务开始时不需要特殊处理
    }

    @Override
    protected Task onTick() {
        // 找出未装备的盔甲
        ItemTarget[] armorsNotEquipped = Arrays.stream(toEquip).filter(target -> !StorageHelper.itemTargetsMetInventory(target) && !StorageHelper.isArmorEquipped(target.getMatches())).toArray(ItemTarget[]::new);
        boolean armorMet = armorsNotEquipped.length == 0;
        if (!armorMet) {
            setDebugState("获取盔甲");
            return new CataloguedResourceTask(armorsNotEquipped);
        }

        setDebugState("装备盔甲");
        AltoClef mod = AltoClef.getInstance();

        // 现在装备
        for (ItemTarget targetArmor : toEquip) {
            Item[] targetArmorMatches = targetArmor.getMatches();
            if (Arrays.stream(targetArmorMatches).toList().contains(Items.SHIELD)) {
                ShieldItem shield = (ShieldItem) Objects.requireNonNull(targetArmor.getMatches())[0];
                if (shield == null) {
                    Debug.logWarning("物品 " + targetArmor + " 不是盔甲！不会装备。");
                } else {
                    if (!StorageHelper.isArmorEquipped(shield)) {
                        if (!(mod.getPlayer().currentScreenHandler instanceof PlayerScreenHandler)) {
                            ItemStack cursorStack = StorageHelper.getItemStackInCursorSlot();
                            if (!cursorStack.isEmpty()) {
                                Optional<Slot> moveTo = mod.getItemStorage().getSlotThatCanFitInPlayerInventory(cursorStack, false);
                                if (moveTo.isPresent()) {
                                    mod.getSlotHandler().clickSlot(moveTo.get(), 0, SlotActionType.PICKUP);
                                    return null;
                                }
                                if (ItemHelper.canThrowAwayStack(mod, cursorStack)) {
                                    mod.getSlotHandler().clickSlot(Slot.UNDEFINED, 0, SlotActionType.PICKUP);
                                    return null;
                                }
                                Optional<Slot> garbage = StorageHelper.getGarbageSlot(mod);
                                // 如果光标槽是垃圾，尝试丢弃
                                if (garbage.isPresent()) {
                                    mod.getSlotHandler().clickSlot(garbage.get(), 0, SlotActionType.PICKUP);
                                    return null;
                                }
                                mod.getSlotHandler().clickSlot(Slot.UNDEFINED, 0, SlotActionType.PICKUP);
                            } else {
                                StorageHelper.closeScreen();
                            }
                        }
                        Slot toMove = PlayerSlot.getEquipSlot(EquipmentSlot.OFFHAND);
                        if (toMove == null) {
                            Debug.logWarning("物品 " + shield.getTranslationKey() + " 的盔甲装备槽无效");
                        }
                        return new MoveItemToSlotFromInventoryTask(targetArmor, toMove);
                    }
                }
            } else {
                ArmorItem item = (ArmorItem) Objects.requireNonNull(targetArmor.getMatches())[0];
                if (item == null) {
                    Debug.logWarning("物品 " + targetArmor + " 不是盔甲！不会装备。");
                } else {
                    if (!StorageHelper.isArmorEquipped(item)) {
                        if (!(mod.getPlayer().currentScreenHandler instanceof PlayerScreenHandler)) {
                            ItemStack cursorStack = StorageHelper.getItemStackInCursorSlot();
                            if (!cursorStack.isEmpty()) {
                                Optional<Slot> moveTo = mod.getItemStorage().getSlotThatCanFitInPlayerInventory(cursorStack, false);
                                if (moveTo.isPresent()) {
                                    mod.getSlotHandler().clickSlot(moveTo.get(), 0, SlotActionType.PICKUP);
                                    return null;
                                }
                                if (ItemHelper.canThrowAwayStack(mod, cursorStack)) {
                                    mod.getSlotHandler().clickSlot(Slot.UNDEFINED, 0, SlotActionType.PICKUP);
                                    return null;
                                }
                                Optional<Slot> garbage = StorageHelper.getGarbageSlot(mod);
                                // 如果光标槽是垃圾，尝试丢弃
                                if (garbage.isPresent()) {
                                    mod.getSlotHandler().clickSlot(garbage.get(), 0, SlotActionType.PICKUP);
                                    return null;
                                }
                                mod.getSlotHandler().clickSlot(Slot.UNDEFINED, 0, SlotActionType.PICKUP);
                            } else {
                                StorageHelper.closeScreen();
                            }
                        }
                        Slot toMove = PlayerSlot.getEquipSlot(item.getSlotType());
                        if (toMove == null) {
                            Debug.logWarning("物品 " + item.getTranslationKey() + " 的盔甲装备槽无效: " + item.getSlotType());
                        }
                        return new MoveItemToSlotFromInventoryTask(targetArmor, toMove);
                    }
                }
            }
        }

        return null;
    }

    @Override
    public boolean isFinished() {
        return armorEquipped();
    }

    @Override
    protected void onStop(Task interruptTask) {
        // 任务停止时不需要特殊处理
    }

    @Override
    protected boolean isEqual(Task other) {
        if (other instanceof EquipArmorTask task) {
            return Arrays.equals(task.toEquip, toEquip);
        }
        return false;
    }

    @Override
    protected String toDebugString() {
        return "装备盔甲 " + ArrayUtils.toString(toEquip);
    }

    /**
     * 测试所有盔甲是否满足指定条件
     * @param armorSatisfies 判断盔甲是否满足条件的谓词
     * @return 如果所有盔甲都满足条件返回true
     */
    private boolean armorTestAll(Predicate<Item> armorSatisfies) {
        // 如果所有物品目标都有任何匹配项已装备...
        return Arrays.stream(toEquip).allMatch(
                target -> Arrays.stream(target.getMatches()).anyMatch(armorSatisfies)
        );
    }

    /**
     * 检查所有盔甲是否已装备
     * @return 如果所有盔甲都已装备返回true
     */
    public boolean armorEquipped() {
        return armorTestAll(item -> StorageHelper.isArmorEquipped(item));
    }

}
