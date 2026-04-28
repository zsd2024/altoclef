package adris.altoclef.multiversion;

import net.minecraft.client.render.RenderLayer;

/**
 * 渲染层版本适配器类
 * 
 * 此类提供 Minecraft 不同版本间渲染层 API 的兼容层。
 * 主要处理 1.20.1 及以上版本新增的 GUI 覆盖层渲染功能。
 */
public class RenderLayerVer {


    /**
     * 获取 GUI 覆盖层渲染层
     * 
     * 在 Minecraft 1.20.1 及以上版本中，RenderLayer 类提供了 getGuiOverlay 方法。
     * 在早期版本中，此功能不存在，返回 null。
     * 
     * @return GUI 覆盖层渲染层，如果版本不支持则返回 null
     */
    public static RenderLayer getGuiOverlay() {
        //#if MC >= 12001
        return RenderLayer.getGuiOverlay();
        //#else
        //$$ return null;
        //#endif
    }

}
