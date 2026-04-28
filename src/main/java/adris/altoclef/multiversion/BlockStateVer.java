package adris.altoclef.multiversion;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;

/**
 * 方块状态版本适配器
 * 提供不同 Minecraft 版本间方块状态相关 API 的兼容层
 */
public class BlockStateVer {


    /**
     * 检查方块状态是否为固体
     * 
     * @param state 方块状态
     * @return 如果方块是固体则返回 true，否则返回 false
     */
    @Pattern
    private static boolean isSolid(BlockState state) {
        //#if MC >= 12001
        // Minecraft 1.20.1 及以上版本使用直接的 isSolid() 方法
        return state.isSolid();
        //#else
        //$$ // Minecraft 1.20.1 以下版本通过材质判断是否为固体
        //$$ return state.getMaterial().isSolid();
        //#endif
    }

    /**
     * 检查方块状态是否可被替换
     * 
     * @param state 方块状态
     * @return 如果方块可被替换则返回 true，否则返回 false
     */
    @Pattern
    private static boolean isReplaceable(BlockState state) {
        //#if MC >= 11904
        // Minecraft 1.19.4 及以上版本使用直接的 isReplaceable() 方法
        return state.isReplaceable();
        //#else
        //$$ // Minecraft 1.19.4 以下版本通过材质判断是否可被替换
        //$$ return state.getMaterial().isReplaceable();
        //#endif
    }

    /**
     * 获取方块硬度
     * 
     * @param state 方块状态
     * @return 方块的硬度值
     */
    @Pattern
    private static float getHardness(BlockState state) {
        //#if MC >= 11701
        // Minecraft 1.17.1 及以上版本直接从方块获取硬度
        return state.getBlock().getHardness();
        //#else
        //$$ // Minecraft 1.17.1 以下版本需要通过方块状态的 getHardness 方法获取
        //$$ return state.getHardness(null, null);
        //#endif
    }

}
