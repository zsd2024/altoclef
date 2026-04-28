package adris.altoclef.eventbus.events;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;

/**
 * 方块已破坏事件
 * 当游戏中的方块被破坏时触发此事件
 */
public class BlockBrokenEvent {
    /** 被破坏方块的位置 */
    public BlockPos blockPos;
    /** 被破坏方块的状态 */
    public BlockState blockState;
    /** 破坏方块的玩家实体 */
    public PlayerEntity player;
}
