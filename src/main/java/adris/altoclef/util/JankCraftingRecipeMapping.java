package adris.altoclef.util;

import adris.altoclef.multiversion.RecipeVer;
import adris.altoclef.multiversion.recipemanager.RecipeManagerWrapper;
import adris.altoclef.multiversion.recipemanager.WrappedRecipeEntry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 简易合成配方映射工具类
 * 用于工作台/背包配方书中，根据给定的合成配方找出对应的标识符
 */
public class JankCraftingRecipeMapping {
    /** 存储物品到配方条目的映射关系 */
    private static final HashMap<Item, List<WrappedRecipeEntry>> recipeMapping = new HashMap<>();

    /**
     * 重新加载配方映射
     * 从Minecraft客户端获取当前可用的配方并建立映射关系
     */
    private static void reloadRecipeMapping() {
        MinecraftClient client = MinecraftClient.getInstance();

        // 检查网络处理器是否可用
        if (client.getNetworkHandler() != null) {
            RecipeManagerWrapper recipes = RecipeManagerWrapper.of(client.getNetworkHandler().getRecipeManager());
            ClientWorld world = client.world;

            // 检查配方管理器是否可用
            if (recipes != null) {
                for (WrappedRecipeEntry recipe : recipes.values()) {
                    assert world != null;
                    Recipe<?> value = recipe.value();
                    Item output = RecipeVer.getOutput(value,world).getItem();
                    recipeMapping.computeIfAbsent(output, k -> new ArrayList<>()).add(recipe);
                }
            }
        }
    }

    /**
     * 从Minecraft合成配方中检索给定输出物品的映射配方
     *
     * @param recipe 要检查的合成配方
     * @param output 配方的输出物品
     * @return 如果找到映射的配方条目则返回Optional包含该条目，否则返回空Optional
     */
    public static Optional<WrappedRecipeEntry> getMinecraftMappedRecipe(CraftingRecipe recipe, Item output) {
        reloadRecipeMapping();
        // 检查输出物品是否存在于配方映射中
        if (recipeMapping.containsKey(output)) {
            // 遍历所有映射到输出物品的配方
            for (WrappedRecipeEntry checkRecipe : recipeMapping.get(output)) {
                // 创建需要满足的物品目标列表
                List<ItemTarget> toSatisfy = Arrays.stream(recipe.getSlots())
                        .filter(itemTarget -> itemTarget != null && !itemTarget.isEmpty())
                        .collect(Collectors.toList());
                // 检查配方是否有原料
                if (!checkRecipe.value().getIngredients().isEmpty()) {
                    // 遍历配方的原料
                    for (Ingredient ingredient : checkRecipe.value().getIngredients()) {
                        // 跳过空原料
                        if (ingredient.isEmpty()) {
                            continue;
                        }
                        // 遍历需要满足的物品
                        outer:
                        for (int i = 0; i < toSatisfy.size(); ++i) {
                            ItemTarget target = toSatisfy.get(i);
                            // 检查原料的匹配堆栈中是否有与物品目标匹配的物品
                            for (ItemStack stack : ingredient.getMatchingStacks()) {
                                if (target.matches(stack.getItem())) {
                                    toSatisfy.remove(i);
                                    break outer;
                                }
                            }
                        }
                    }
                }
                // 检查是否所有物品目标都已满足
                if (toSatisfy.isEmpty()) {
                    return Optional.of(checkRecipe);
                }
            }
        }
        return Optional.empty();
    }
}
            }
        }
    }

    /**
     * Retrieves the mapped recipe for a given output item from the Minecraft crafting recipe.
     *
     * @param recipe The crafting recipe to check against.
     * @param output The output item of the recipe.
     * @return An Optional containing the mapped recipe entry if found, or an empty Optional if not found.
     */
    public static Optional<WrappedRecipeEntry> getMinecraftMappedRecipe(CraftingRecipe recipe, Item output) {
        reloadRecipeMapping();
        // Check if the output item is present in the recipe mapping
        if (recipeMapping.containsKey(output)) {
            // Iterate through all the recipes mapped to the output item
            for (WrappedRecipeEntry checkRecipe : recipeMapping.get(output)) {
                // Create a list of item targets to satisfy
                List<ItemTarget> toSatisfy = Arrays.stream(recipe.getSlots())
                        .filter(itemTarget -> itemTarget != null && !itemTarget.isEmpty())
                        .collect(Collectors.toList());
                // Check if the recipe has ingredients
                if (!checkRecipe.value().getIngredients().isEmpty()) {
                    // Iterate through the ingredients of the recipe
                    for (Ingredient ingredient : checkRecipe.value().getIngredients()) {
                        // Skip empty ingredients
                        if (ingredient.isEmpty()) {
                            continue;
                        }
                        // Iterate through the items to satisfy
                        outer:
                        for (int i = 0; i < toSatisfy.size(); ++i) {
                            ItemTarget target = toSatisfy.get(i);
                            // Check if any of the ingredient's matching stacks matches the item target
                            for (ItemStack stack : ingredient.getMatchingStacks()) {
                                if (target.matches(stack.getItem())) {
                                    toSatisfy.remove(i);
                                    break outer;
                                }
                            }
                        }
                    }
                }
                // Check if all the item targets have been satisfied
                if (toSatisfy.isEmpty()) {
                    return Optional.of(checkRecipe);
                }
            }
        }
        return Optional.empty();
    }
}
