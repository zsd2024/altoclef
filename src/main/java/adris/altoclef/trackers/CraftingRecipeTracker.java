package adris.altoclef.trackers;

import adris.altoclef.AltoClef;
import adris.altoclef.multiversion.recipemanager.RecipeManagerWrapper;
import adris.altoclef.multiversion.recipemanager.WrappedRecipeEntry;
import adris.altoclef.util.RecipeTarget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

// TODO 移除那些丑陋的 "ensureUpdate" 语句，实际上我们只需要在加入世界时更新
public class CraftingRecipeTracker extends Tracker{


    private final HashMap<Item, List<adris.altoclef.util.CraftingRecipe>> itemRecipeMap = new HashMap<>(); // 物品配方映射
    private final HashMap<adris.altoclef.util.CraftingRecipe, ItemStack> recipeResultMap = new HashMap<>(); // 配方结果映射

    private boolean shouldRebuild; // 是否需要重建

    public CraftingRecipeTracker(TrackerManager manager) {
        super(manager);
        shouldRebuild = true;
    }

    /**
     * 获取指定物品的配方列表
     * @param item 物品
     * @return 配方列表
     */
    public List<adris.altoclef.util.CraftingRecipe> getRecipeForItem(Item item) {
        ensureUpdated();

        if (!hasRecipeForItem(item)) {
            mod.logWarning("尝试访问未知物品的配方: "+item);
            return null;
        }

        return itemRecipeMap.get(item);
    }

    /**
     * 获取指定物品的第一个配方
     * @param item 物品
     * @return 第一个配方
     */
    public adris.altoclef.util.CraftingRecipe getFirstRecipeForItem(Item item) {
        ensureUpdated();

        if (!hasRecipeForItem(item)) {
            mod.logWarning("尝试访问未知物品的配方: "+item);
            return null;
        }

        return itemRecipeMap.get(item).get(0);
    }

    /**
     * 获取指定物品的配方目标列表
     * @param item 物品
     * @param targetCount 目标数量
     * @return 配方目标列表
     */
    public List<RecipeTarget> getRecipeTarget(Item item, int targetCount) {
        ensureUpdated();

        List<RecipeTarget> targets = new ArrayList<>();
        for (adris.altoclef.util.CraftingRecipe recipe : getRecipeForItem(item)) {
            targets.add(new RecipeTarget(item, targetCount, recipe));
        }

        return targets;
    }

    /**
     * 获取指定物品的第一个配方目标
     * @param item 物品
     * @param targetCount 目标数量
     * @return 第一个配方目标
     */
    public RecipeTarget getFirstRecipeTarget(Item item, int targetCount) {
        ensureUpdated();

        return new RecipeTarget(item, targetCount, getFirstRecipeForItem(item));
    }

    /**
     * 检查是否有所需物品的配方
     * @param item 物品
     * @return 是否有配方
     */
    public boolean hasRecipeForItem(Item item) {
        ensureUpdated();
        return itemRecipeMap.containsKey(item);
    }

    /**
     * 获取配方的产出物品
     * @param recipe 配方
     * @return 产出物品
     */
    public ItemStack getRecipeResult(adris.altoclef.util.CraftingRecipe recipe) {
        ensureUpdated();

        if (!hasRecipe(recipe)) {
            mod.logWarning("尝试获取未知配方的产出: "+recipe);
            return null;
        }
        ItemStack result = recipeResultMap.get(recipe);

        return new ItemStack(result.getItem(), result.getCount());
    }

    /**
     * 检查是否有指定配方
     * @param recipe 配方
     * @return 是否有配方
     */
    public boolean hasRecipe(adris.altoclef.util.CraftingRecipe recipe) {
        ensureUpdated();
        return recipeResultMap.containsKey(recipe);
    }


    @Override
    protected void updateState() {
        if (!shouldRebuild) return;

        // 进入游戏后重建
        if (!AltoClef.inGame()) return;

        ClientPlayNetworkHandler networkHandler =  MinecraftClient.getInstance().getNetworkHandler();
        if (networkHandler == null) return;

        RecipeManagerWrapper recipeManager = RecipeManagerWrapper.of(networkHandler.getRecipeManager());

        for (WrappedRecipeEntry recipe : recipeManager.values()) {
            if (!(recipe.value() instanceof net.minecraft.recipe.CraftingRecipe craftingRecipe)) continue;

            // 暂时未实现，因为不需要（希望如此 xd）
            if (craftingRecipe instanceof SpecialCraftingRecipe) continue;

            // 参数不应被使用，我们只需传递null
            ItemStack result = new ItemStack(craftingRecipe.getResult(null).getItem(), craftingRecipe.getResult(null).getCount());

            Item[][] altoclefRecipeItems = getShapedCraftingRecipe(craftingRecipe.getIngredients());

            adris.altoclef.util.CraftingRecipe altoclefRecipe = adris.altoclef.util.CraftingRecipe.newShapedRecipe(altoclefRecipeItems, result.getCount());

            if (itemRecipeMap.containsKey(result.getItem())) {
                itemRecipeMap.get(result.getItem()).add(altoclefRecipe);
            } else {
                List<adris.altoclef.util.CraftingRecipe> recipes = new ArrayList<>();
                recipes.add(altoclefRecipe);

                itemRecipeMap.put(result.getItem(), recipes);
            }

            recipeResultMap.put(altoclefRecipe, result);
        }

        itemRecipeMap.replaceAll((k,v) -> Collections.unmodifiableList(v));

        shouldRebuild = false;
    }

    // TODO 针对小配方进行调整
    // 总是按照形状排列，但这对于无序配方不重要
    // 数组的第二维度用于不同类型物品（例如原木）
    private static Item[][] getShapedCraftingRecipe(List<Ingredient> ingredients) {
        Item[][] result = new Item[9][];
        int x = 0;

        for (Ingredient ingredient : ingredients) {
            ItemStack[] stacks = ingredient.getMatchingStacks();
            Item[] items = new Item[stacks.length];

            for (int i = 0; i < stacks.length; i++) {
                ItemStack stack = stacks[i];
                if (stack.getCount() > 1) {
                    throw new IllegalStateException("配方需要在一个槽位中放置多个物品... 呃... 见鬼 (材料: " + ingredient + ")");
                }

                items[i] = stack.getItem();
            }

            if (stacks.length != 0) {
                // FIXME 这样做很愚蠢，但是TaskCatalogue是以这种方式设置的，所以需要重写来允许多个资源 :')
                result[x] = new Item[]{items[0]};
            } else {
                result[x] = null;
            }

            x++;
        }


        return result;
    }

    @Override
    protected void reset() {
       shouldRebuild = true;
       itemRecipeMap.clear();
       recipeResultMap.clear();
    }

    @Override
    protected boolean isDirty() {
        return shouldRebuild;
    }
}
