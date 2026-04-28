package adris.altoclef.multiversion;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;


/**
 * 附魔助手版本适配器
 * 提供不同Minecraft版本间附魔API的兼容性处理
 */
public class EnchantmentHelperVer {

    /**
     * 检查物品是否具有绑定诅咒
     * 
     * @param stack 物品堆栈
     * @return 如果物品具有绑定诅咒则返回true，否则返回false
     */
    @Pattern
    public boolean hasBindingCurse(ItemStack stack) {
        //#if MC >= 12100
        // 1.21.0及以上版本使用EnchantmentEffectComponentTypes.PREVENT_ARMOR_CHANGE组件类型检查
        return EnchantmentHelper.hasAnyEnchantmentsWith(stack, net.minecraft.component.EnchantmentEffectComponentTypes.PREVENT_ARMOR_CHANGE);
        //#else
        //$$ // 1.21.0以下版本直接调用hasBindingCurse()方法
        //$$ return EnchantmentHelper.hasBindingCurse(stack);
        //#endif
    }

}
