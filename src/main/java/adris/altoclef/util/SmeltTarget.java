package adris.altoclef.util;

import net.minecraft.item.Item;

import java.util.Objects;

/**
 * 熔炼目标工具类
 * 表示一个熔炼任务，包含目标物品、主要材料和可选材料
 */
public class SmeltTarget {

    /** 目标物品（熔炼后得到的物品） */
    private final ItemTarget item;
    /** 可选材料数组（可用于熔炼的额外材料） */
    private final Item[] optionalMaterials;
    /** 主要材料（用于熔炼的基础材料） */
    private ItemTarget material;

    /**
     * 构造熔炼目标对象
     * 
     * @param item 目标物品（熔炼后得到的物品）
     * @param material 主要材料（用于熔炼的基础材料）
     * @param optionalMaterials 可选材料数组（可用于熔炼的额外材料）
     */
    public SmeltTarget(ItemTarget item, ItemTarget material, Item... optionalMaterials) {
        this.item = item;
        this.material = material;
        this.material = new ItemTarget(material, this.item.getTargetCount());
        this.optionalMaterials = optionalMaterials;
    }

    /**
     * 获取目标物品（熔炼后得到的物品）
     * 
     * @return 目标物品
     */
    public ItemTarget getItem() {
        return item;
    }

    /**
     * 获取主要材料（用于熔炼的基础材料）
     * 
     * @return 主要材料
     */
    public ItemTarget getMaterial() {
        return material;
    }

    /**
     * 获取可选材料数组（可用于熔炼的额外材料）
     * 
     * @return 可选材料数组
     */
    public Item[] getOptionalMaterials() {
        return optionalMaterials;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SmeltTarget that = (SmeltTarget) o;
        return Objects.equals(material, that.material) && Objects.equals(item, that.item);
    }

    @Override
    public int hashCode() {
        return Objects.hash(material, item);
    }
}
