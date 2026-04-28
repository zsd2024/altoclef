package adris.altoclef.multiversion.entity;

import adris.altoclef.multiversion.Pattern;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

/**
 * 生物实体版本适配器
 * 提供不同 Minecraft 版本之间生物实体相关API的兼容层
 */
public class LivingEntityVer {

    // FIXME this should be possible with mappings, right?
    /**
     * 获取生物实体装备的物品
     * 
     * @param entity 生物实体实例
     * @return 装备物品的迭代器
     */
    @Pattern
    private static Iterable<ItemStack> getItemsEquipped(LivingEntity entity) {
        //#if MC >= 12005
        return entity.getEquippedItems();
        //#else
        //$$ return entity.getItemsEquipped();
        //#endif
    }

    /**
     * 检查物品是否适合破坏指定方块
     * 
     * @param item 物品实例
     * @param state 方块状态
     * @return 如果物品适合破坏该方块返回true，否则返回false
     */
    @Pattern
    private static boolean isSuitableFor(Item item, BlockState state) {
        //#if MC >= 12005
        return item.getDefaultStack().isSuitableFor(state);
        //#else
        //$$ return item.isSuitableFor(state);
        //#endif
    }
}
