package adris.altoclef.trackers.storage;

import adris.altoclef.trackers.Tracker;
import adris.altoclef.trackers.TrackerManager;
import adris.altoclef.util.helpers.ItemHelper;
import adris.altoclef.util.helpers.StorageHelper;
import adris.altoclef.util.slots.CraftingTableSlot;
import adris.altoclef.util.slots.CursorSlot;
import adris.altoclef.util.slots.PlayerSlot;
import adris.altoclef.util.slots.Slot;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * 物品栏子跟踪器 - 跟踪玩家的物品栏物品
 */
public class InventorySubTracker extends Tracker {

    private final HashMap<Item, List<Slot>> itemToSlotPlayer = new HashMap<>(); // 玩家物品到槽位映射
    private final HashMap<Item, List<Slot>> itemToSlotContainer = new HashMap<>(); // 容器物品到槽位映射
    private final HashMap<Item, Integer> itemCountsPlayer = new HashMap<>(); // 玩家物品数量
    private final HashMap<Item, Integer> itemCountsContainer = new HashMap<>(); // 容器物品数量

    private ScreenHandler _prevScreenHandler; // 上一屏幕处理器

    public InventorySubTracker(TrackerManager manager) {
        super(manager);
    }

    /**
     * 检查容器槽位是否应该被忽略
     * @param slot 槽位
     * @return 是否应该被忽略
     */
    private static boolean shouldIgnoreSlotForContainer(Slot slot) {
        // 重要说明!!!!
        // 计算容器槽位时忽略合成台输出槽。
        //
        // 为什么？
        // 因为我们不希望机器人认为如果物品在输出槽中，我们"拥有"这个物品。否则它会
        // 软锁，因为它会认为我们一切都好（我们得到了物品！）但实际上我们需要获取那个物品。
        //
        // 我们也不希望机器人认为如果物品在我们的盔甲/合成/盾牌槽中，我们"拥有"这个物品。这样处理起来很麻烦。
        if (slot instanceof CraftingTableSlot && slot.equals(CraftingTableSlot.OUTPUT_SLOT))
            return true;
        if (slot instanceof PlayerSlot) {
            // 忽略非正常的物品栏槽位
            int window = slot.getWindowSlot();
            return window < 9 || window > 44;
        }
        return false;
    }

    /**
     * 获取物品数量
     * @param playerInventory 是否包括玩家物品栏
     * @param containerInventory 是否包括容器物品栏
     * @param items 物品数组
     * @return 物品数量
     */
    public int getItemCount(boolean playerInventory, boolean containerInventory, Item... items) {
        ensureUpdated();
        int result = 0;
        ItemStack cursorStack = StorageHelper.getItemStackInCursorSlot();
        for (Item item : items) {
            if (playerInventory && cursorStack.getItem().equals(item))
                result += cursorStack.getCount();
            if (playerInventory)
                result += itemCountsPlayer.getOrDefault(item, 0);
            if (containerInventory)
                result += itemCountsContainer.getOrDefault(item, 0);
        }
        return result;
    }

    /**
     * 检查是否拥有物品
     * @param playerInventoryOnly 是否仅检查玩家物品栏
     * @param items 物品数组
     * @return 是否拥有物品
     */
    public boolean hasItem(boolean playerInventoryOnly, Item... items) {
        ensureUpdated();
        ItemStack cursorStack = StorageHelper.getItemStackInCursorSlot();
        for (Item item : items) {
            if (cursorStack.getItem().equals(item))
                return true;
            if (itemCountsPlayer.containsKey(item))
                return true;
            if (!playerInventoryOnly && itemCountsContainer.containsKey(item))
                return true;
        }
        return false;
    }

    /**
     * 获取包含物品的槽位列表
     * @param playerInventory 是否包括玩家物品栏
     * @param containerInventory 是否包括容器物品栏
     * @param items 物品数组
     * @return 槽位列表
     */
    public List<Slot> getSlotsWithItems(boolean playerInventory, boolean containerInventory, Item... items) {
        ensureUpdated();
        List<Slot> result = new ArrayList<>();
        ItemStack cursorStack = StorageHelper.getItemStackInCursorSlot();
        for (Item item : items) {
            if (playerInventory && cursorStack.getItem().equals(item))
                result.add(CursorSlot.SLOT);
            if (playerInventory)
                result.addAll(itemToSlotPlayer.getOrDefault(item, Collections.emptyList()));
            if (containerInventory)
                result.addAll(itemToSlotContainer.getOrDefault(item, Collections.emptyList()));
        }
        return result;
    }

    /**
     * 获取物品栏堆栈列表
     * @param includeCursor 是否包含光标
     * @return 物品堆栈列表
     */
    public List<ItemStack> getInventoryStacks(boolean includeCursor) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null || player.getInventory() == null)
            return Collections.emptyList();
        PlayerInventory inv = player.getInventory();
        // 36 玩家 + 1 副手 + 4 盔甲
        List<ItemStack> result = new ArrayList<>(41 + (includeCursor ? 1 : 0));
        if (includeCursor) {
            result.add(StorageHelper.getItemStackInCursorSlot());
        }
        result.addAll(inv.main);
        result.addAll(inv.armor);
        result.addAll(inv.offHand);
        return result;
    }

    /**
     * 获取可以容纳物品的槽位列表
     * @param list 槽位列表
     * @param item 要容纳的物品
     * @param acceptPartial 是否接受部分容纳
     * @return 可以容纳物品的槽位列表
     */
    private List<Slot> getSlotsThatCanFit(HashMap<Item, List<Slot>> list, ItemStack item, boolean acceptPartial) {
        List<Slot> result = new ArrayList<>();
        // 首先添加可填充的槽位
        for (Slot toCheckStackable : list.getOrDefault(item.getItem(), Collections.emptyList())) {
            // 忽略光标槽位。
            if (Slot.isCursor(toCheckStackable))
                continue;
            ItemStack stackToAddTo = StorageHelper.getItemStackInSlot(toCheckStackable);
            // 我们必须有一些剩余空间，然后决定是否关心是否有足够的空间
            if (!stackToAddTo.isEmpty() && ItemHelper.canStackTogether(item, stackToAddTo)) {
                int roomLeft = stackToAddTo.getMaxCount() - stackToAddTo.getCount();
                if (acceptPartial || roomLeft > item.getCount()) {
                    result.add(toCheckStackable);
                }
            }
        }
        // 然后添加可以插入物品的空气槽位
        if (MinecraftClient.getInstance().player != null) {
            ScreenHandler handler = MinecraftClient.getInstance().player.currentScreenHandler;
            for (Slot airSlot : list.getOrDefault(Items.AIR, Collections.emptyList())) {
                // 忽略光标槽位
                if (airSlot.equals(CursorSlot.SLOT))
                    continue;
                int windowCheck = airSlot.getWindowSlot();
                // 特殊情况：盔甲/盾牌，如果我们的物品栏未打开，我们希望忽略这些。
                if (windowCheck < handler.slots.size() && handler.getSlot(windowCheck).canInsert(item)) {
                    result.add(airSlot);
                }
            }
        }
        return result;
    }

    /**
     * 获取可以容纳物品的槽位列表
     * @param includePlayer 是否包括玩家物品栏
     * @param includeContainer 是否包括容器
     * @param item 物品
     * @param acceptPartial 是否接受部分容纳
     * @return 可以容纳物品的槽位列表
     */
    public List<Slot> getSlotsThatCanFit(boolean includePlayer, boolean includeContainer, ItemStack item, boolean acceptPartial) {
        ensureUpdated();
        final List<Slot> result = new ArrayList<>();
        if (includePlayer)
            result.addAll(getSlotsThatCanFit(itemToSlotPlayer, item, acceptPartial));
        if (includeContainer)
            result.addAll(getSlotsThatCanFit(itemToSlotContainer, item, acceptPartial));
        return result;
    }

    /**
     * 检查是否有空槽位
     * @param playerInventoryOnly 是否仅检查玩家物品栏
     * @return 是否有空槽位
     */
    public boolean hasEmptySlot(boolean playerInventoryOnly) {
        return hasItem(playerInventoryOnly, Items.AIR);
    }

    /**
     * 注册物品
     * @param stack 物品堆栈
     * @param slot 槽位
     * @param isSlotPlayerInventory 是否是玩家物品栏槽位
     */
    private void registerItem(ItemStack stack, Slot slot, boolean isSlotPlayerInventory) {
        Item item = stack.getItem();
        int count = stack.getCount();
        if (stack.isEmpty()) {
            // 如果我们的光标槽位是空的，忽略它，因为我们不想将其视为有效槽位。
            item = Items.AIR;
            count = 0;
        }

        if (isSlotPlayerInventory) {
            itemCountsPlayer.put(item, itemCountsPlayer.getOrDefault(item, 0) + count);
        } else {
            itemCountsContainer.put(item, itemCountsContainer.getOrDefault(item, 0) + count);
        }

        if (slot != null) {
            HashMap<Item, List<Slot>> toAdd = isSlotPlayerInventory ? itemToSlotPlayer : itemToSlotContainer;
            if (!toAdd.containsKey(item))
                toAdd.put(item, new ArrayList<>());
            toAdd.get(item).add(slot);
        }
    }

    @Override
    protected void updateState() {
        _prevScreenHandler = MinecraftClient.getInstance().player != null ? MinecraftClient.getInstance().player.currentScreenHandler : null;

        itemToSlotPlayer.clear();
        itemToSlotContainer.clear();
        itemCountsPlayer.clear();
        itemCountsContainer.clear();
        if (MinecraftClient.getInstance().player == null)
            return;
        ScreenHandler handler = MinecraftClient.getInstance().player.currentScreenHandler;
        if (handler == null)
            return;
        for (Slot slot : Slot.getCurrentScreenSlots()) {
            // 忽略光标槽位，这是单独处理的。
            if (slot.equals(CursorSlot.SLOT))
                continue;
            ItemStack stack = StorageHelper.getItemStackInSlot(slot);
            // 如果我们在容器与玩家物品栏中，分别添加。

            if (!shouldIgnoreSlotForContainer(slot)) {
                registerItem(stack, slot, slot.isSlotInPlayerInventory());
            }
        }
    }

    @Override
    protected void reset() {
        itemToSlotPlayer.clear();
        itemToSlotContainer.clear();
        itemCountsPlayer.clear();
        itemCountsContainer.clear();
    }

    @Override
    protected boolean isDirty() {
        ScreenHandler handler = MinecraftClient.getInstance().player != null ? MinecraftClient.getInstance().player.currentScreenHandler : null;
        return super.isDirty() || handler != _prevScreenHandler;
    }
}
