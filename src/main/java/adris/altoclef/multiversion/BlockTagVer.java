package adris.altoclef.multiversion;

import net.minecraft.block.Block;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.Registries;

/**
 * 方块标签版本适配器
 * 提供不同 Minecraft 版本间方块标签相关 API 的兼容层
 */
public class BlockTagVer {


    /**
     * 检查方块是否为羊毛
     * 
     * @param block 要检查的方块
     * @return 如果方块是羊毛则返回 true，否则返回 false
     */
    public static boolean isWool(Block block) {
        //#if MC >= 11802
        // Minecraft 1.18.2 及以上版本使用注册表和标签流进行检查
        return Registries.BLOCK.getKey(block).map(e -> Registries.BLOCK.entryOf(e).streamTags().anyMatch(t -> t == BlockTags.WOOL)).orElse(false);
        //#else
        //$$ // Minecraft 1.18.2 以下版本直接使用 BlockTags.WOOL.contains() 方法
        //$$ return BlockTags.WOOL.contains(block);
        //#endif
    }

}
