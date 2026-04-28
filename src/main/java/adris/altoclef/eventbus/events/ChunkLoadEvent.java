package adris.altoclef.eventbus.events;

import net.minecraft.world.chunk.WorldChunk;

/**
 * 区块加载事件
 * 当Minecraft世界中的一个区块被加载到内存时触发此事件
 */
public class ChunkLoadEvent {
    /** 被加载的区块对象 */
    public WorldChunk chunk;

    /**
     * 构造函数
     * @param chunk 被加载的区块对象
     */
    public ChunkLoadEvent(WorldChunk chunk) {
        this.chunk = chunk;
    }
}
