package adris.altoclef.eventbus.events;

import net.minecraft.util.math.ChunkPos;

/**
 * 区块卸载事件
 * 当Minecraft世界中的一个区块从内存中卸载时触发此事件
 */
public class ChunkUnloadEvent {
    /** 被卸载区块的位置坐标 */
    public ChunkPos chunkPos;

    /**
     * 构造函数
     * @param chunkPos 被卸载区块的位置坐标
     */
    public ChunkUnloadEvent(ChunkPos chunkPos) {
        this.chunkPos = chunkPos;
    }
}
