package adris.altoclef.multiversion;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;

/**
 * 连接屏幕版本适配器
 * 提供不同 Minecraft 版本间服务器连接屏幕相关 API 的兼容层
 */
public class ConnectScreenVer {


    /**
     * 连接到服务器
     * 
     * @param screen 当前屏幕
     * @param client Minecraft 客户端实例
     * @param address 服务器地址
     * @param info 服务器信息
     * @param quickPlay 是否快速游戏（某些版本特有）
     */
    // 由于 1.19.4 版本缺少 quickPlay 参数，模式匹配存在一些奇怪的问题
    public static void connect(Screen screen, MinecraftClient client, ServerAddress address, ServerInfo info, boolean quickPlay) {
        //#if MC >= 12005
        // Minecraft 1.20.5 及以上版本需要额外的参数
        ConnectScreen.connect(screen, client, address, info, quickPlay,null);
        //#elseif MC >= 12001
        //$$ // Minecraft 1.20.1 到 1.20.4 版本支持 quickPlay 参数
        //$$ ConnectScreen.connect(screen, client, address, info, quickPlay);
        //#elseif MC >= 11701
        //$$ // Minecraft 1.17.1 到 1.20.0 版本不支持 quickPlay 参数
        //$$ ConnectScreen.connect(screen, client, address, info);
        //#else
        //$$ // Minecraft 1.17.1 以下版本使用旧的构造函数
        //$$ new ConnectScreen(screen,client, address.getAddress(), address.getPort());
        //#endif
    }

}
