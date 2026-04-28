package adris.altoclef.multiversion;

import net.minecraft.item.ToolItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.item.ToolMaterials;

/**
 * 工具材料版本适配器
 * 提供跨 Minecraft 版本的工具挖掘等级获取功能
 */
public class ToolMaterialVer {

    /**
     * 获取工具物品的挖掘等级
     * 
     * @param item 工具物品
     * @return 挖掘等级（0-4）
     */
    public static int getMiningLevel(ToolItem item) {
        return getMiningLevel(item.getMaterial());
    }

    /**
     * 获取工具材料的挖掘等级
     * 
     * @param material 工具材料
     * @return 挖掘等级（0-4）
     *         - 0: 木制/金制工具
     *         - 1: 石制工具  
     *         - 2: 铁制工具
     *         - 3: 钻石工具
     *         - 4: 下界合金工具
     * @throws IllegalStateException 当遇到未知的工具材料时抛出异常
     */
    public static int getMiningLevel(ToolMaterial material) {
        if (material.equals(ToolMaterials.WOOD) || material.equals(ToolMaterials.GOLD)) {
            return 0;
        } else if (material.equals(ToolMaterials.STONE)) {
            return 1;
        } else if (material.equals(ToolMaterials.IRON)) {
            return 2;
        } else if (material.equals(ToolMaterials.DIAMOND)) {
            return 3;
        } else if (material.equals(ToolMaterials.NETHERITE)) {
            return 4;
        }
        throw new IllegalStateException("意外的工具材料值: " + material);
    }

}
