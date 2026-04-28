package adris.altoclef.multiversion;

import net.minecraft.client.MinecraftClient;

/**
 * 选项版本适配器
 * 
 * 此类提供不同 Minecraft 版本间游戏选项设置的兼容性处理。
 * Minecraft 1.19.4 及以上版本将选项字段改为私有并通过 getter 方法访问，
 * 而早期版本可以直接访问公共字段。
 */
public class OptionsVer {


    /**
     * 设置游戏伽马值（亮度）
     * 
     * @param value 伽马值（0.0-1.0 范围，值越大越亮）
     */
    public static void setGamma(double value) {
        //#if MC >= 11904
        // Minecraft 1.19.4+ 版本，通过 getter 方法设置值
        MinecraftClient.getInstance().options.getGamma().setValue(value);
        //#else
        //$$ // Minecraft 1.19.4 以下版本，直接设置公共字段
        //$$ MinecraftClient.getInstance().options.gamma = value;
        //#endif
    }

    /**
     * 设置自动跳跃选项
     * 
     * @param value true 启用自动跳跃，false 禁用自动跳跃
     */
    public static void setAutoJump(boolean value) {
        //#if MC >= 11904
        // Minecraft 1.19.4+ 版本，通过 getter 方法设置值
        MinecraftClient.getInstance().options.getAutoJump().setValue(value);
        //#else
        //$$ // Minecraft 1.19.4 以下版本，直接设置公共字段
        //$$ MinecraftClient.getInstance().options.autoJump = value;
        //#endif
    }

}
