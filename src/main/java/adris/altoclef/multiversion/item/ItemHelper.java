package adris.altoclef.multiviversion.item;

import adris.altoclef.mixins.AxeItemAccessor;
import adris.altoclef.mixins.MiningToolItemAccessor;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.AxeItem;
import net.minecraft.item.Item;
import net.minecraft.item.MiningToolItem;
import net.minecraft.item.PickaxeItem;

import java.util.Set;

/**
 * 物品帮助器
 * 提供跨 Minecraft 版本的物品操作兼容层，主要用于1.16.5及更早版本
 */
public class ItemHelper {


    //#if MC <= 11605
    /**
     * 检查物品是否适合挖掘指定方块状态（仅适用于1.16.5及更早版本）
     * 
     * @param item 物品
     * @param state 方块状态
     * @return 如果物品适合挖掘该方块返回true，否则返回false
     */
    //$$ public static boolean isSuitableFor(Item item, BlockState state){
    //$$     if (item instanceof PickaxeItem pickaxe) {
    //$$         return pickaxe.isSuitableFor(state);
    //$$     }
    //$$
    //$$     if (item instanceof MiningToolItem) {
    //$$         boolean isInEffectiveBlocks = ((MiningToolItemAccessor)item).getEffectiveBlocks().contains(state.getBlock());
    //$$
    //$$         if (item instanceof AxeItem) {
    //$$             return isInEffectiveBlocks || ((AxeItemAccessor)item).getEffectiveMaterials().contains(state.getMaterial());
    //$$         }
    //$$         return isInEffectiveBlocks;
    //$$     }
    //$$
    //$$     return item.isSuitableFor(state);
    //$$ }
    //$$
    //#endif

}
