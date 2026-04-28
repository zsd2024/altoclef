package adris.altoclef.mixins;

import adris.altoclef.eventbus.EventBus;
import adris.altoclef.eventbus.events.PlayerCollidedWithEntityEvent;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 玩家与实体碰撞混入
 * 
 * 此混入类用于重定向玩家与实体碰撞的方法，
 * 在执行默认碰撞逻辑之前发布一个碰撞事件，
 * 以便其他系统可以监听和响应玩家与实体的碰撞。
 */
@Mixin(PlayerEntity.class)
public class PlayerCollidesWithEntityMixin {

    // 确定物品/经验球/其他物体在"拾取"范围内的碰撞
    @Redirect(
            method = "collideWithEntity",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;onPlayerCollision(Lnet/minecraft/entity/player/PlayerEntity;)V")
    )
    private void onCollideWithEntity(Entity self, PlayerEntity player) {
        // TODO: 使用更少硬编码的手动方式来强制客户端访问
        if (player instanceof ClientPlayerEntity) {
            EventBus.publish(new PlayerCollidedWithEntityEvent(player, self));
        }
        // 执行默认操作
        // TODO: 找到更干净的方式。首先重新阅读混入介绍文档
        self.onPlayerCollision(player);
    }
}
