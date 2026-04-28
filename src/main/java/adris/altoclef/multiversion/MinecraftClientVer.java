package adris.altoclef.multiversion;

import net.minecraft.client.MinecraftClient;

/**
 * Minecraft客户端版本适配器
 * 
 * 此类提供不同 Minecraft 版本间客户端相关方法的兼容性处理。
 * 主要处理 Minecraft 1.21.0 及以上版本 API 变化带来的差异。
 */
public class MinecraftClientVer {


    /**
     * 获取客户端渲染的时间增量（tick delta）
     * 
     * Minecraft 1.21.0 版本重构了渲染计时器系统，
     * 将 getTickDelta() 方法移至 RenderTickCounter 类中。
     * 
     * @param client Minecraft 客户端实例
     * @return 时间增量值
     */
    @Pattern
    private static float getTickDelta(MinecraftClient client) {
        //#if MC >= 12100
        // Minecraft 1.21.0+ 版本，通过 RenderTickCounter 获取
        return client.getRenderTickCounter().getTickDelta(true);
        //#else
        //$$ // Minecraft 1.21.0 以下版本，直接调用客户端方法
        //$$ return client.getTickDelta();
        //#endif
    }

}
