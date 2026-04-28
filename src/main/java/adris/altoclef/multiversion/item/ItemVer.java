package adris.altoclef.multiversion.item;

import adris.altoclef.multiversion.FoodComponentWrapper;
import adris.altoclef.multiversion.Pattern;
import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

/**
 * 物品版本适配器
 * 提供跨 Minecraft 版本的物品操作兼容层
 */
public class ItemVer {

    /**
     * 获取物品的食物组件
     * 
     * @param item 物品
     * @return 食物组件包装器
     */
    public static FoodComponentWrapper getFoodComponent(Item item) {
        //#if MC >=12005
        return FoodComponentWrapper.of(item.getComponents().get(net.minecraft.component.DataComponentTypes.FOOD));
        //#else
        //$$ return FoodComponentWrapper.of(item.getFoodComponent());
        //#endif
    }

    /**
     * 检查物品堆栈是否为食物
     * 
     * @param stack 物品堆栈
     * @return 如果是食物返回true，否则返回false
     */
    public static boolean isFood(ItemStack stack) {
        return isFood(stack.getItem());
    }

    /**
     * 检查物品堆栈是否有自定义名称
     * 
     * @param stack 物品堆栈
     * @return 如果有自定义名称返回true，否则返回false
     */
    public static boolean hasCustomName(ItemStack stack) {
        //#if MC >= 12005
        return stack.contains(net.minecraft.component.DataComponentTypes.CUSTOM_NAME);
        //#else
        //$$ return stack.hasCustomName();
        //#endif
    }

    /**
     * 检查物品是否为食物
     * 
     * @param item 物品
     * @return 如果是食物返回true，否则返回false
     */
    public static boolean isFood(Item item) {
        //#if MC >=12005
        return item.getComponents().contains(net.minecraft.component.DataComponentTypes.FOOD);
        //#else
        //$$ return item.isFood();
        //#endif
    }

    /**
     * 检查物品是否适合挖掘指定方块状态
     * 
     * @param item 物品
     * @param state 方块状态
     * @return 如果物品适合挖掘该方块返回true，否则返回false
     */
    @Pattern
    private static boolean isSuitableFor(Item item, BlockState state) {
        //#if MC >= 11701
        return item.getDefaultStack().isSuitableFor(state);
        //#else
        //$$ return adris.altoclef.multiversion.item.ItemHelper.isSuitableFor(item, state);
        //#endif
    }

    // 这种实现方式令人难以置信...
    /**
     * 获取原金物品（1.17+）或金矿石物品（1.16.5及更早版本）
     * 
     * @return 原金物品或金矿石物品
     */
    @Pattern
    private static Item RAW_GOLD() {
        //#if MC >= 11701
        return Items.RAW_GOLD;
        //#else
        //$$ return Items.GOLD_ORE;
        //#endif
    }

    /**
     * 获取原铁物品（1.17+）或铁矿石物品（1.16.5及更早版本）
     * 
     * @return 原铁物品或铁矿石物品
     */
    @Pattern
    private static Item RAW_IRON() {
        //#if MC >= 11701
        return Items.RAW_IRON;
        //#else
        //$$ return Items.IRON_ORE;
        //#endif
    }


}
