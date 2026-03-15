package adris.altoclef.tasks.resources;

import adris.altoclef.util.CraftingRecipe;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.helpers.ItemHelper;
import net.minecraft.item.Item;

import java.util.function.Function;

/**
 * 使用匹配羊毛制作任务抽象类
 * 用于处理与羊毛相关的合成任务，根据主要材料匹配相应的颜色
 */
public abstract class CraftWithMatchingWoolTask extends CraftWithMatchingMaterialsTask {

    // 获取主要材料的函数（通常是羊毛颜色）
    private final Function<ItemHelper.ColorfulItems, Item> getMajorityMaterial;
    // 获取目标物品的函数
    private final Function<ItemHelper.ColorfulItems, Item> getTargetItem;

    public CraftWithMatchingWoolTask(ItemTarget target, Function<ItemHelper.ColorfulItems, Item> getMajorityMaterial, Function<ItemHelper.ColorfulItems, Item> getTargetItem, CraftingRecipe recipe, boolean[] sameMask) {
        super(target, recipe, sameMask);
        this.getMajorityMaterial = getMajorityMaterial;
        this.getTargetItem = getTargetItem;
    }


    @Override
    protected Item getSpecificItemCorrespondingToMajorityResource(Item majority) {
        // 遍历所有彩色物品类型，找到匹配主要材料的颜色
        for (ItemHelper.ColorfulItems colorfulItem : ItemHelper.getColorfulItems()) {
            if (getMajorityMaterial.apply(colorfulItem) == majority) {
                // 返回对应颜色的目标物品
                return getTargetItem.apply(colorfulItem);
            }
        }
        return null;
    }
}
