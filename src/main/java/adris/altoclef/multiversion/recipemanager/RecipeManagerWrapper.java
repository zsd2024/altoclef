package adris.altoclef.multiversion.recipemanager;

import net.minecraft.recipe.RecipeManager;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import java.util.Collection;

/**
 * 配方管理器包装器
 * 提供跨 Minecraft 版本的配方管理器操作兼容层
 */
public class RecipeManagerWrapper {

    private final RecipeManager recipeManager;

    /**
     * 从原始配方管理器创建包装器实例
     * 
     * @param recipeManager 原始配方管理器
     * @return 配方管理器包装器实例，如果输入为null则返回null
     */
    public static RecipeManagerWrapper of(RecipeManager recipeManager) {
        if (recipeManager == null) return null;

        return new RecipeManagerWrapper(recipeManager);
    }


    /**
     * 私有构造函数
     * 
     * @param recipeManager 原始配方管理器
     */
    private RecipeManagerWrapper(RecipeManager recipeManager) {
        this.recipeManager = recipeManager;
    }

    /**
     * 获取所有配方条目
     * 
     * @return 所有配方条目的集合
     */
    //#if MC>12001
    public Collection<WrappedRecipeEntry> values() {
        return recipeManager.values().stream().map(r -> new WrappedRecipeEntry(r.id(),r.value())).collect(Collectors.toSet());
    }
    //#else
    //$$ public Collection<WrappedRecipeEntry> values() {
    //$$    List<WrappedRecipeEntry> result = new ArrayList<>();
    //$$    for (Identifier id : recipeManager.keys().toList()) {
    //$$        result.add(new WrappedRecipeEntry(id, recipeManager.get(id).get()));
    //$$    }
    //$$
    //$$    return result;
    //$$ }
    //#endif



}
