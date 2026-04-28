package adris.altoclef.eventbus.events;

import net.minecraft.util.hit.BlockHitResult;

/**
 * 方块交互事件
 * 当玩家与方块进行交互时触发此事件（例如右键点击方块）
 */
public class BlockInteractEvent {
    /** 方块交互的命中结果信息 */
    public BlockHitResult hitResult;

    /**
     * 构造函数
     * @param hitResult 方块交互的命中结果
     */
    public BlockInteractEvent(BlockHitResult hitResult) {
        this.hitResult = hitResult;
    }
}
