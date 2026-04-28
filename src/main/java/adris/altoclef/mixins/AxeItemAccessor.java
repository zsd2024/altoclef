package adris.altoclef.mixins;

import net.minecraft.item.AxeItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Set;

/**
 * 斧头物品访问器
 * 用于访问 Minecraft 原版 AxeItem 类中的私有字段
 * 主要用于获取斧头有效的材料类型集合（适用于 Minecraft 1.16.5 及以下版本）
 */
@Mixin(AxeItem.class)
public interface AxeItemAccessor {

    //#if MC <= 11605
    /**
     * 获取斧头能够有效挖掘的材料类型集合
     * 在 Minecraft 1.16.5 及以下版本中，斧头对特定材料有加成效果
     *
     * @return 有效的材料类型集合
     */
    //$$ @Accessor("field_23139")
    //$$ Set<net.minecraft.block.Material> getEffectiveMaterials();
    //#endif

}
