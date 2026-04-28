package adris.altoclef.eventbus.events;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

/**
 * 玩家与实体碰撞事件
 * 当玩家与其他实体发生碰撞时触发此事件
 */
public class PlayerCollidedWithEntityEvent {
    /** 发生碰撞的玩家实体 */
    public PlayerEntity player;
    /** 与玩家碰撞的其他实体 */
    public Entity other;

    /**
     * 构造函数
     * @param player 发生碰撞的玩家实体
     * @param other 与玩家碰撞的其他实体
     */
    public PlayerCollidedWithEntityEvent(PlayerEntity player, Entity other) {
        this.player = player;
        this.other = other;
    }
}
