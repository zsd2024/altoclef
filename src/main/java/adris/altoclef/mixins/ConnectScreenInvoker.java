package adris.altoclef.mixins;

import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * 连接屏幕调用器接口
 * 
 * 此接口用于通过Mixin的Invoker功能调用ConnectScreen类中的私有connect方法，
 * 允许AltoClef以编程方式连接到指定的服务器地址和端口。
 */
@Mixin(ConnectScreen.class)
public interface ConnectScreenInvoker {

    //#if MC <= 11605
    //$$ /**
    //$$  * 调用ConnectScreen的私有connect方法（仅适用于1.16.5及以下版本）
    //$$  * 
    //$$  * @param address 服务器地址
    //$$  * @param port 服务器端口
    //$$  */
    //$$ @Invoker("connect")
    //$$ void invokeConnect(String address, int port);
    //#endif
}
