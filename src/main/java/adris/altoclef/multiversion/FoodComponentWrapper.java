package adris.altoclef.multiversion;

//#if MC >= 12005
import net.minecraft.component.type.FoodComponent;
//#else
//$$ import net.minecraft.item.FoodComponent;
//#endif

/**
 * 食物组件包装器
 * 用于统一不同 Minecraft 版本中食物组件的 API 差异
 * 在 1.20.5+ 版本中，食物组件使用 nutrition() 和 saturation() 方法
 * 在 1.20.4 及更早版本中，食物组件使用 getHunger() 和 getSaturationModifier() 方法
 */
public class FoodComponentWrapper {

    /**
     * 从原始食物组件创建包装器实例
     * 
     * @param component 原始食物组件，如果为 null 则返回 null
     * @return 包装后的食物组件实例
     */
    public static FoodComponentWrapper of(FoodComponent component) {
        if (component == null) return null;

        return new FoodComponentWrapper(component);
    }

    private final FoodComponent component;

    /**
     * 私有构造函数，防止外部直接实例化
     * 
     * @param component 原始食物组件
     */
    private FoodComponentWrapper(FoodComponent component) {
        this.component = component;
    }

    /**
     * 获取食物提供的饥饿值
     * 
     * @return 食物的饥饿值
     */
    public int getHunger() {
        //#if MC >= 12005
        return component.nutrition();
        //#else
        //$$ return component.getHunger();
        //#endif
    }

    /**
     * 获取食物的饱和度修正值
     * 
     * @return 食物的饱和度修正值
     */
    public float getSaturationModifier() {
        //#if MC >= 12005
        return component.saturation();
        //#else
        //$$ return component.getSaturationModifier();
        //#endif
    }
}
