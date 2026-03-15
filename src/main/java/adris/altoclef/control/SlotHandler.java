package adris.altoclef.control;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.helpers.ItemHelper;
import adris.altoclef.util.helpers.StorageHelper;
import adris.altoclef.util.slots.CursorSlot;
import adris.altoclef.util.slots.PlayerSlot;
import adris.altoclef.util.slots.Slot;
import adris.altoclef.util.time.TimerGame;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.*;
import net.minecraft.screen.slot.SlotActionType;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * 物品栏处理器
 * 负责处理Minecraft中的物品栏操作，包括物品移动、装备、交换等核心功能
 */


public class SlotHandler {

    private final AltoClef mod;

    // 物品栏操作计时器，用于控制物品操作的速度
    private final TimerGame slotActionTimer = new TimerGame(0);
    // 一次性覆盖计时器标志
    private boolean overrideTimerOnce = false;

        /**
     * 构造函数，初始化物品栏处理器
     * @param mod AltoClef主模块实例
     */
    public SlotHandler(AltoClef mod) {
        this.mod = mod;
    }

        /**
     * 强制允许下一次物品栏操作（覆盖计时器限制）
     */
    private void forceAllowNextSlotAction() {
        overrideTimerOnce = true;
    }

        /**
     * 检查是否可以执行物品栏操作（受计时器限制）
     * @return 如果可以执行操作则返回true，否则返回false
     */
    public boolean canDoSlotAction() {
        if (overrideTimerOnce) {
            overrideTimerOnce = false;
            return true;
        }
        slotActionTimer.setInterval(mod.getModSettings().getContainerItemMoveDelay());
        return slotActionTimer.elapsed();
    }

        /**
     * 注册物品栏操作，更新计时器状态
     */
    public void registerSlotAction() {
        mod.getItemStorage().registerSlotAction();
        slotActionTimer.reset();
    }


        /**
     * 点击指定槽位
     * @param slot 槽位对象
     * @param mouseButton 鼠标按键（0为左键，1为右键）
     * @param type 槽位操作类型
     */
    public void clickSlot(Slot slot, int mouseButton, SlotActionType type) {
        if (!canDoSlotAction()) return;

        if (slot.getWindowSlot() == -1) {
            clickSlot(PlayerSlot.UNDEFINED, 0, SlotActionType.PICKUP);
            return;
        }
        // NOT THE CASE! We may have something in the cursor slot to place.
        //if (getItemStackInSlot(slot).isEmpty()) return getItemStackInSlot(slot);

        clickWindowSlot(slot.getWindowSlot(), mouseButton, type);
    }

        /**
     * 强制点击槽位（忽略计时器限制）
     * @param slot 槽位对象
     * @param mouseButton 鼠标按键
     * @param type 槽位操作类型
     */
    private void clickSlotForce(Slot slot, int mouseButton, SlotActionType type) {
        forceAllowNextSlotAction();
        clickSlot(slot, mouseButton, type);
    }

        /**
     * 点击窗口槽位的内部实现
     * @param windowSlot 窗口槽位索引
     * @param mouseButton 鼠标按键
     * @param type 槽位操作类型
     */
    private void clickWindowSlot(int windowSlot, int mouseButton, SlotActionType type) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) {
            return;
        }
        registerSlotAction();
        int syncId = player.currentScreenHandler.syncId;

        try {
            mod.getController().clickSlot(syncId, windowSlot, mouseButton, type, player);
        } catch (Exception e) {
            Debug.logWarning("槽位点击错误（已忽略）");
            e.printStackTrace();
        }
    }

        /**
     * 强制将指定物品装备到副手
     * @param toEquip 要装备的物品
     */
    public void forceEquipItemToOffhand(Item toEquip) {
        if (StorageHelper.getItemStackInSlot(PlayerSlot.OFFHAND_SLOT).getItem() == toEquip) {
            return;
        }
        List<Slot> currentItemSlot = mod.getItemStorage().getSlotsWithItemPlayerInventory(false,
                toEquip);
        for (Slot CurrentItemSlot : currentItemSlot) {
            if (!Slot.isCursor(CurrentItemSlot)) {
                mod.getSlotHandler().clickSlot(CurrentItemSlot, 0, SlotActionType.PICKUP);
            } else {
                mod.getSlotHandler().clickSlot(PlayerSlot.OFFHAND_SLOT, 0, SlotActionType.PICKUP);
            }
        }
    }

        /**
     * 强制装备指定物品到主手
     * @param toEquip 要装备的物品
     * @return 如果成功装备返回true，否则返回false
     */
    public boolean forceEquipItem(Item toEquip) {

        // 已经装备了
        if (StorageHelper.getItemStackInSlot(PlayerSlot.getEquipSlot()).getItem() == toEquip) return true;

        // 总是装备到第二个槽位。第一个和最后一个被baritone占用。
        mod.getPlayer().getInventory().selectedSlot = 1;

        // 如果我们的物品在光标中，直接将其移动到快捷栏。
        boolean inCursor = StorageHelper.getItemStackInSlot(CursorSlot.SLOT).getItem() == toEquip;

        List<Slot> itemSlots = mod.getItemStorage().getSlotsWithItemScreen(toEquip);
        if (!itemSlots.isEmpty()) {
            for (Slot ItemSlots : itemSlots) {
                int hotbar = 1;
                //_mod.getPlayer().getInventory().swapSlotWithHotbar();
                clickSlotForce(Objects.requireNonNull(ItemSlots), inCursor ? 0 : hotbar, inCursor ? SlotActionType.PICKUP : SlotActionType.SWAP);
                //registerSlotAction();
            }
            return true;
        }
        return false;
    }

        /**
     * 强制卸下攻击工具（如剑、镐等）
     * @return 如果成功卸下返回true，否则返回false
     */
    public boolean forceDeequipHitTool() {
        return forceDeequip(stack -> stack.getItem() instanceof ToolItem);
    }

        /**
     * 强制卸下可右键点击的物品（如弓、盾牌、药水等）
     */
    public void forceDeequipRightClickableItem() {
        forceDeequip(stack -> {
                    Item item = stack.getItem();
                    return item instanceof BucketItem // 水桶、岩浆桶、牛奶、鱼
                            || item instanceof EnderEyeItem
                            || item == Items.BOW
                            || item == Items.CROSSBOW
                            || item == Items.FLINT_AND_STEEL || item == Items.FIRE_CHARGE
                            || item == Items.ENDER_PEARL
                            || item instanceof FireworkRocketItem
                            || item instanceof SpawnEggItem
                            || item == Items.END_CRYSTAL
                            || item == Items.EXPERIENCE_BOTTLE
                            || item instanceof PotionItem // 也包括喷溅型/滞留型药水
                            || item == Items.TRIDENT
                            || item == Items.WRITABLE_BOOK
                            || item == Items.WRITTEN_BOOK
                            || item instanceof FishingRodItem
                            || item instanceof OnAStickItem
                            || item == Items.COMPASS
                            || item instanceof EmptyMapItem
                            || item instanceof Equipment
                            || item == Items.LEAD
                            || item == Items.SHIELD;
                }
        );
    }

        /**
     * 尝试卸下我们不希望装备的任何物品。
     *
     * @param isBad: 判断物品是否不良/不应该装备
     * @return 是否成功卸下，或者根本就没有装备该物品。
     */
    public boolean forceDeequip(Predicate<ItemStack> isBad) {
        ItemStack equip = StorageHelper.getItemStackInSlot(PlayerSlot.getEquipSlot());
        ItemStack cursor = StorageHelper.getItemStackInSlot(CursorSlot.SLOT);
        if (isBad.test(cursor)) {
            // 丢弃光标槽位或移动
            Optional<Slot> fittableSlots = mod.getItemStorage().getSlotThatCanFitInPlayerInventory(equip, false);
            if (fittableSlots.isEmpty()) {
                // 尝试与第一个非不良槽位交换物品。
                for (Slot slot : Slot.getCurrentScreenSlots()) {
                    if (!isBad.test(StorageHelper.getItemStackInSlot(slot))) {
                        clickSlotForce(slot, 0, SlotActionType.PICKUP);
                        return false;
                    }
                }
                if (ItemHelper.canThrowAwayStack(mod, cursor)) {
                    clickSlotForce(PlayerSlot.UNDEFINED, 0, SlotActionType.PICKUP);
                    return true;
                }
                // 无法丢弃 :(
                return false;
            } else {
                // 放入空/可用槽位。
                clickSlotForce(fittableSlots.get(), 0, SlotActionType.PICKUP);
                return true;
            }
        } else if (isBad.test(equip)) {
            // 拾取物品
            clickSlotForce(PlayerSlot.getEquipSlot(), 0, SlotActionType.PICKUP);
            return false;
        } else if (equip.isEmpty() && !cursor.isEmpty()) {
            // 光标是好的且装备槽为空，所以完成填充。
            clickSlotForce(PlayerSlot.getEquipSlot(), 0, SlotActionType.PICKUP);
            return true;
        }
        // 我们已经卸下了
        return true;
    }

        /**
     * 强制将指定槽位的物品装备到主手槽位
     * @param slot 要装备的槽位
     */
    public void forceEquipSlot(Slot slot) {
        Slot target = PlayerSlot.getEquipSlot();
        clickSlotForce(slot, target.getInventorySlot(), SlotActionType.SWAP);
    }

        /**
     * 强制装备匹配的物品
     * @param matches 要匹配的物品数组
     * @param unInterruptable 是否不可中断（即使机器人在吃东西时也强制装备）
     * @return 如果成功装备返回true，否则返回false
     */
    public boolean forceEquipItem(Item[] matches, boolean unInterruptable) {
        return forceEquipItem(new ItemTarget(matches, 1), unInterruptable);
    }

        /**
     * 强制装备目标物品
     * @param toEquip 要装备的目标物品
     * @param unInterruptable 是否不可中断（即使机器人在吃东西时也强制装备）
     * @return 如果成功装备返回true，否则返回false
     */
    public boolean forceEquipItem(ItemTarget toEquip, boolean unInterruptable) {
        if (toEquip == null) return false;

        // 如果机器人尝试进食
        if (mod.getFoodChain().needsToEat() && !unInterruptable) { //除非我们真的需要强制装备该物品
            return false; //暂时不装备该物品
        }

        Slot target = PlayerSlot.getEquipSlot();
        // 已经装备了
        if (toEquip.matches(StorageHelper.getItemStackInSlot(target).getItem())) return true;

        for (Item item : toEquip.getMatches()) {
            if (mod.getItemStorage().hasItem(item)) {
                if (forceEquipItem(item)) return true;
            }
        }
        return false;
    }

        // 默认情况下，如果机器人正在进食，则不强制装备。
    public boolean forceEquipItem(Item... toEquip) {
        return forceEquipItem(toEquip, false);
    }

        /**
     * 刷新玩家物品栏（通过点击每个槽位来更新物品栏状态）
     */
    public void refreshInventory() {
        if (MinecraftClient.getInstance().player == null)
            return;
        for (int i = 0; i < MinecraftClient.getInstance().player.getInventory().main.size(); ++i) {
            Slot slot = Slot.getFromCurrentScreenInventory(i);
            clickSlotForce(slot, 0, SlotActionType.PICKUP);
            clickSlotForce(slot, 0, SlotActionType.PICKUP);
        }
    }
}
