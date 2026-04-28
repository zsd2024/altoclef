package adris.altoclef.util.slots;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.*;
import org.apache.commons.lang3.NotImplementedException;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Predicate;

/**
 * 槽位屏幕映射类
 * 用于根据当前打开的屏幕类型，将槽位索引映射到对应的槽位对象
 */
@SuppressWarnings("rawtypes")
public class SlotScreenMapping {

    // 映射条目列表，顺序很重要：第一个predicate返回true的条目会被选中
    private static final List<SlotScreenMappingEntry> _classList = List.of(
            e(CraftingTableSlot.class, screen -> screen instanceof CraftingScreen, CraftingTableSlot::new),
            e(FurnaceSlot.class, screen -> screen instanceof AbstractFurnaceScreen, FurnaceSlot::new),
            e(SmokerSlot.class, screen -> screen instanceof AbstractFurnaceScreen, SmokerSlot::new),
            e(BlastFurnaceSlot.class, screen -> screen instanceof AbstractFurnaceScreen, BlastFurnaceSlot::new),
            e(SmithingTableSlot.class, screen -> screen instanceof SmithingScreen, SmithingTableSlot::new),
            e(BrewingStandSlot.class, screen -> screen instanceof BrewingStandScreen, BrewingStandSlot::new),
            e(ChestSlot.class, screen -> screen instanceof GenericContainerScreen, ChestSlot::new),
            e(PlayerSlot.class, screen -> true, PlayerSlot::new), // 顺序很重要，这个必须放在倒数第二位！
            e(CursorSlot.class, screen -> true, (slot, inv) -> CursorSlot.SLOT) // 顺序很重要，这个必须放在最后！
    );

    /**
     * 检查指定槽位类型的屏幕是否已打开
     *
     * @param slotType 槽位类型类
     * @return 如果该类型的屏幕已打开则返回true，否则返回false
     * @throws NotImplementedException 如果槽位类型未在映射中注册
     */
    @SuppressWarnings("unchecked")
    public static boolean isScreenOpen(Class slotType) {
        Screen screen = MinecraftClient.getInstance().currentScreen;
        if (!_classList.isEmpty()) {
            for (SlotScreenMappingEntry entry : _classList) {
                if (slotType == entry.type || slotType.isAssignableFrom(entry.type)) {
                    return entry.inScreen.test(screen);
                }
            }
        }
        throw new NotImplementedException("槽位类型类未在SlotScreenMapping中注册: " + slotType + ". 请注册! (当前屏幕 = " + screen + ")");
    }

    /**
     * 根据当前屏幕获取对应的槽位对象
     *
     * @param slot      槽位索引
     * @param inventory 是否为库存槽位
     * @return 对应的槽位对象
     * @throws NotImplementedException 如果没有找到匹配的屏幕类型（理论上不应该发生）
     */
    public static Slot getFromScreen(int slot, boolean inventory) {
        Screen screen = MinecraftClient.getInstance().currentScreen;
        if (!_classList.isEmpty()) {
            for (SlotScreenMappingEntry entry : _classList) {
                if (entry.inScreen.test(screen)) {
                    return entry.getSlot.apply(slot, inventory);
                }
            }
        }
        throw new NotImplementedException("我们不应该到达这里，_classList应该在底部包含一个始终返回true的predicate（用于PlayerSlot和CursorSlot）");
    }


    /**
     * 创建槽位屏幕映射条目
     *
     * @param type     槽位类型
     * @param inScreen 屏幕类型判断谓词
     * @param getSlot  槽位创建函数
     * @return 槽位屏幕映射条目
     */
    private static SlotScreenMappingEntry e(Class type, Predicate<Screen> inScreen, BiFunction<Integer, Boolean, Slot> getSlot) {
        return new SlotScreenMappingEntry(type, inScreen, getSlot);
    }

    /**
     * 槽位屏幕映射条目内部类
     * 存储槽位类型、屏幕判断谓词和槽位创建函数
     */
    static class SlotScreenMappingEntry {
        public Class type;
        public Predicate<Screen> inScreen;
        public BiFunction<Integer, Boolean, Slot> getSlot;

        public SlotScreenMappingEntry(Class type, Predicate<Screen> inScreen, BiFunction<Integer, Boolean, Slot> getSlot) {
            this.type = type;
            this.inScreen = inScreen;
            this.getSlot = getSlot;
        }
    }
}
