package adris.altoclef.multiversion;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Recipe;
import net.minecraft.world.World;

/**
 * 配方版本适配器类
 * 
 * 此类提供 Minecraft 不同版本间配方相关 API 的兼容层。
 * 主要处理 1.19.4 及以上版本与早期版本之间配方输出获取方法的差异。
 */
public class RecipeVer {


    /**
     * 获取配方的输出物品堆栈
     * 
     * 在 Minecraft 1.19.4 及以上版本中，配方的 getResult 方法需要 RegistryManager 参数。
     * 在早期版本中，getOutput 方法不接受任何参数。
     * 
     * @param recipe 要获取输出的配方对象
     * @param world 世界对象，用于获取注册表管理器
     * @return 配方的输出物品堆栈
     */
    public static ItemStack getOutput(Recipe<?> recipe, World world) {
        //#if MC >= 11904
        return recipe.getResult(world.getRegistryManager());
        //#else
        //$$ return recipe.getOutput();
        //#endif
    }


}
