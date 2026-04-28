package adris.altoclef.multiversion;

import net.minecraft.item.ItemStack;
import net.minecraft.recipe.CraftingRecipe;

/**
 * 合成配方版本适配器
 * 提供不同 Minecraft 版本间合成配方相关 API 的兼容层
 */
public class CraftingRecipeVer {


    /**
     * 获取合成配方的输出物品
     * 
     * @param craftingRecipe 合成配方
     * @return 合成结果物品堆栈
     */
    @Pattern
    private static ItemStack getOutput(CraftingRecipe craftingRecipe) {
        //#if MC >= 11904
        // Minecraft 1.19.4 及以上版本使用 getResult() 方法
        return craftingRecipe.getResult(null);
        //#else
        //$$ // Minecraft 1.19.4 以下版本使用 getOutput() 方法
        //$$ return craftingRecipe.getOutput();
        //#endif
    }

}
