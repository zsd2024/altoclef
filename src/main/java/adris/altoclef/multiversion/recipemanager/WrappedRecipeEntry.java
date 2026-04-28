package adris.altoclef.multiversion.recipemanager;

import net.minecraft.recipe.Recipe;
//#if MC>12001
import net.minecraft.recipe.RecipeEntry;
//#endif
import net.minecraft.util.Identifier;

/**
 * 包装的配方条目
 * 用于处理 Minecraft 1.20.2 及以上版本中 RecipeEntry 类型与旧版本 Recipe 类型的兼容性问题
 */
public record WrappedRecipeEntry(Identifier id, Recipe<?> value) {

    //#if MC>12001
    /**
     * 将包装的配方转换为 Minecraft 1.20.2+ 的 RecipeEntry 格式
     * @return RecipeEntry 对象，包含配方ID和配方值
     */
    public RecipeEntry<?> asRecipe() {
        return new RecipeEntry<Recipe<?>>(id, value);
    }
    //#else
    //$$ /**
    //$$  * 将包装的配方转换为旧版本的 Recipe 格式
    //$$  * @return 直接返回配方值
    //$$  */
    //$$ public Recipe<?> asRecipe(){
    //$$     return value;
    //$$ }
    //#endif

}
