package adris.altoclef.mixins;

import net.minecraft.network.ClientConnection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * 客户端连接访问器混入
 * 
 * 此混入接口允许访问 Minecraft 原版 ClientConnection 类中的私有字段。
 * 通过 @Accessor 注解，我们可以安全地获取客户端连接的内部状态，
 * 而无需直接修改原版代码。
 */
@Mixin(ClientConnection.class)
public interface ClientConnectionAccessor {
    
    /**
     * 获取客户端连接的刻计数器（ticks）
     * 
     * @return 当前连接已经运行的刻数
     */
    @Accessor("ticks")
    int getTicks();
}
