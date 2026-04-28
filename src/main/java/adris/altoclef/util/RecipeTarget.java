package adris.altoclef.util;

import net.minecraft.item.Item;

import java.util.Objects;

/**
 * 配方目标工具类
 * 表示一个合成配方目标，包含配方、输出物品和目标数量
 */
public class RecipeTarget {

    /** 合成配方 */
    private final CraftingRecipe recipe;
    /** 输出物品 */
    private final Item item;
    /** 目标数量 */
    private final int targetCount;

    /**
     * 构造函数
     * @param item 输出物品
     * @param targetCount 目标数量
     * @param recipe 合成配方
     */
    public RecipeTarget(Item item, int targetCount, CraftingRecipe recipe) {
        this.item = item;
        this.targetCount = targetCount;
        this.recipe = recipe;
    }

    /**
     * 获取合成配方
     * @return 合成配方
     */
    public CraftingRecipe getRecipe() {
        return recipe;
    }

    /**
     * 获取输出物品
     * @return 输出物品
     */
    public Item getOutputItem() {
        return item;
    }

    /**
     * 获取目标数量
     * @return 目标数量
     */
    public int getTargetCount() {
        return targetCount;
    }

    @Override
    public String toString() {
        if (targetCount == 1)
            return "Recipe{"+item+"}";

        return "Recipe{" +
                item + " x " + targetCount +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RecipeTarget that = (RecipeTarget) o;
        return targetCount == that.targetCount && recipe.equals(that.recipe) && Objects.equals(item, that.item);
    }

    @Override
    public int hashCode() {
        return Objects.hash(recipe, item);
    }
}
