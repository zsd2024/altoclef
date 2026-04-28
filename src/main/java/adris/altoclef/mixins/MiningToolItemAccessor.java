package adris.altoclef.mixins;

import net.minecraft.block.Block;
import net.minecraft.item.MiningToolItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Set;

/**
 * 挖掘工具物品访问器
 * 
 * 此混入接口为MiningToolItem类提供对有效方块集合的访问能力。
 * 主要用于Minecraft 1.16.5及以下版本，允许获取挖掘工具可以有效挖掘的方块集合。
 */
@Mixin(MiningToolItem.class)
public interface MiningToolItemAccessor {

    //#if MC <= 11605
    /**
     * 获取此挖掘工具可以有效挖掘的方块集合
     * 
     * @return 有效方块的集合
     */
    //$$ @Accessor("effectiveBlocks")
    //$$ Set<Block> getEffectiveBlocks();
    //#endif

}
