package adris.altoclef.multiversion;

import net.minecraft.client.MinecraftClient;

/**
 * 游戏内HUD版本适配器
 * 用于处理不同 Minecraft 版本中调试HUD显示状态获取方式的差异
 * 在 1.20.2+ 版本中，通过 inGameHud.getDebugHud().shouldShowDebugHud() 获取
 * 在 1.20.1 及更早版本中，通过 options.debugEnabled 获取
 */
public class InGameHudVer {

    /**
     * 检查是否应该显示调试HUD
     * 
     * @return 如果应该显示调试HUD则返回true，否则返回false
     */
    public static boolean shouldShowDebugHud() {
        //#if MC > 12001
        return MinecraftClient.getInstance().inGameHud.getDebugHud().shouldShowDebugHud();
        //#else
        //$$ return MinecraftClient.getInstance().options.debugEnabled;
        //#endif
    }
}
