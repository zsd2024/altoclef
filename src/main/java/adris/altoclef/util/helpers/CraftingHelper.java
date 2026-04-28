package adris.altoclef.util.helpers;

import adris.altoclef.AltoClef;
import adris.altoclef.util.CraftingRecipe;
import adris.altoclef.util.ItemTarget;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.*;

/**
 * 合成帮助器类
 * 提供与物品合成相关的实用方法
 */
public class CraftingHelper {

    private CraftingHelper() {
    }


/**
 * 检查机器人是否能仅通过库存中的物品合成指定物品
 * （合成可能包含多个步骤，例如将原木变成木棍）
 * @param mod AltoClef实例
 * @param item 要合成的物品
 * @return 如果可以合成返回true，否则返回false
 */
public static boolean canCraftItemNow(AltoClef mod, Item item) {
        List<ItemStack> inventoryItems = new ArrayList<>();
        for (ItemStack stack : mod.getItemStorage().getItemStacksPlayerInventory(true)) {
            inventoryItems.add(new ItemStack(stack.getItem(), stack.getCount()));
        }

        for (CraftingRecipe recipe : mod.getCraftingRecipeTracker().getRecipeForItem(item)) {
            if (canCraftItemNow(mod, new ArrayList<>(inventoryItems), recipe, new HashSet<>())) {
                return true;
            }
        }

        return false;
    }

    /**
     * 递归检查是否可以使用当前库存合成指定配方
     * @param mod AltoClef实例
     * @param inventoryStacks 当前库存物品列表
     * @param recipe 要检查的合成配方
     * @param alreadyChecked 已检查过的物品集合（防止循环依赖）
     * @return 如果可以合成返回true，否则返回false
     */
    private static boolean canCraftItemNow(AltoClef mod, List<ItemStack> inventoryStacks, CraftingRecipe recipe, HashSet<Item> alreadyChecked) {
        Item recipeResult = mod.getCraftingRecipeTracker().getRecipeResult(recipe).getItem();

        if (alreadyChecked.contains(recipeResult)) return false;
        alreadyChecked.add(recipeResult);

        ItemTarget[] targets = recipe.getSlots();

        itemTargetLoop:
        for (ItemTarget itemTarget : targets) {
            if (itemTarget == ItemTarget.EMPTY) {
                continue;
            }

            for (Item item : itemTarget.getMatches()) {

                for (ItemStack inventoryStack : inventoryStacks) {

                    // 我们可以使用库存中的物品来合成！
                    // 减少可用物品数量，继续循环
                    if (inventoryStack.getItem() == item && inventoryStack.getCount() >= itemTarget.getTargetCount()) {
                        inventoryStack.setCount(inventoryStack.getCount() - itemTarget.getTargetCount());
                        continue itemTargetLoop;
                    }
                }
            }

            // 我们没有找到可以直接使用的库存物品
            // 现在尝试递归调用我们需要的物品的配方（如果找到的话）

            // FIXME 这里没有考虑数量，但我认为没有配方需要在特定槽位放置多个物品，所以应该没问题
            for (Item item : itemTarget.getMatches()) {
                if (!mod.getCraftingRecipeTracker().hasRecipeForItem(item)) continue;

                for (CraftingRecipe newRecipe : mod.getCraftingRecipeTracker().getRecipeForItem(item)) {
                    List<ItemStack> inventoryStacksCopy = new ArrayList<>(inventoryStacks);
                    if (canCraftItemNow(mod, inventoryStacksCopy, newRecipe, new HashSet<>(alreadyChecked))) {

                        // 这是我们现在剩下的库存
                        inventoryStacks = inventoryStacksCopy;

                        // 我们合成了某些东西，将其添加到可用物品中（减去我们使用的那个）
                        ItemStack result = mod.getCraftingRecipeTracker().getRecipeResult(newRecipe);
                        result.setCount(result.getCount() - 1);
                        inventoryStacks.add(result);

                        continue itemTargetLoop;
                    }
                }
            }

            // 我们无法获取该物品
            return false;
        }

        return true;
    }

            for (Item item : itemTarget.getMatches()) {

                for (ItemStack inventoryStack : inventoryStacks) {

                    // we can use something from inventory to craft it!
                    // reduce the amount of items available, continue the loop
                    if (inventoryStack.getItem() == item && inventoryStack.getCount() >= itemTarget.getTargetCount()) {
                        inventoryStack.setCount(inventoryStack.getCount() - itemTarget.getTargetCount());
                        continue itemTargetLoop;
                    }
                }

            }

            // we didn't find and item in the inventory that we could use right away
            // now try to recursively call for recipes of the items we need if we find something

            // FIXME this doesnt take counts into consideration, but I dont even think there is a recipe that needs more then one item on a specific slot, so we should be fine
            for (Item item : itemTarget.getMatches()) {
                if (!mod.getCraftingRecipeTracker().hasRecipeForItem(item)) continue;

                for (CraftingRecipe newRecipe : mod.getCraftingRecipeTracker().getRecipeForItem(item)) {
                    List<ItemStack> inventoryStacksCopy = new ArrayList<>(inventoryStacks);
                    if (canCraftItemNow(mod, inventoryStacksCopy, newRecipe, new HashSet<>(alreadyChecked))) {

                        // this is the inventory we are now left with
                        inventoryStacks = inventoryStacksCopy;

                        // we crafted something, add it to the available items minus the one we used
                        ItemStack result = mod.getCraftingRecipeTracker().getRecipeResult(newRecipe);
                        result.setCount(result.getCount() - 1);
                        inventoryStacks.add(result);

                        continue itemTargetLoop;
                    }
                }
            }

            // we cannot get the item
            return false;
        }

        return true;
    }

}
