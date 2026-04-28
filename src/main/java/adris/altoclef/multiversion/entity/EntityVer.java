package adris.altoclef.multiversion.entity;

import adris.altoclef.multiversion.Pattern;
import net.fabricmc.loader.impl.lib.sat4j.core.Vec;
import net.minecraft.entity.Entity;
import adris.altoclef.mixins.EntityAccessor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;

/**
 * 实体版本适配器
 * 提供不同 Minecraft 版本之间实体相关API的兼容层
 */
public class EntityVer {

    /**
     * 检查实体是否在地狱传送门中
     * 
     * @param entity 要检查的实体
     * @return 如果实体在地狱传送门中返回true，否则返回false
     */
    @Pattern
    public boolean isInNetherPortal(Entity entity) {
        //#if MC <= 12006
        //$$ return ((EntityAccessor)entity).isInNetherPortal();
        //#else
        return adris.altoclef.multiversion.entity.EntityHelper.isInNetherPortal(entity);
        //#endif
    }

    /**
     * 获取实体的传送门冷却时间
     * 
     * @param entity 实体实例
     * @return 传送门冷却时间
     */
    @Pattern
    public int getPortalCooldown(Entity entity) {
        //#if MC >= 12001
        return entity.getPortalCooldown();
        //#else
        //$$ return ((EntityAccessor) entity).getPortalCooldown();
        //#endif
    }

    /**
     * 获取实体的着陆位置
     * 
     * @param entity 实体实例
     * @return 着陆位置的方块坐标
     */
    @Pattern
    public BlockPos getLandingPos(Entity entity) {
        //#if MC >= 11701
        return entity.getSteppingPos();
        //#else
        //$$ return ((adris.altoclef.mixins.EntityAccessor) entity).invokeGetLandingPos();
        //#endif
    }

    /**
     * 获取实体的俯仰角（pitch）
     * 
     * @param player 实体实例
     * @return 俯仰角度值
     */
    @Pattern
    private static float getPitch(Entity player) {
        //#if MC >= 11701
        return player.getPitch();
        //#else
        //$$ return player.pitch;
        //#endif
    }

    /**
     * 获取实体的偏航角（yaw）
     * 
     * @param player 实体实例
     * @return 偏航角度值
     */
    @Pattern
    private static float getYaw(Entity player) {
        //#if MC >= 11701
        return player.getYaw();
        //#else
        //$$ return player.yaw;
        //#endif
    }

    /**
     * 设置实体的俯仰角（pitch）
     * 
     * @param player 实体实例
     * @param value 俯仰角度值
     */
    @Pattern
    private static void setPitch(Entity player, float value) {
        //#if MC >= 11701
        player.setPitch(value);
        //#else
        //$$ player.pitch = value;
        //#endif
    }

    /**
     * 设置实体的偏航角（yaw）
     * 
     * @param player 实体实例
     * @param value 偏航角度值
     */
    @Pattern
    private static void setYaw(Entity player, float value) {
        //#if MC >= 11701
        player.setYaw(value);
        //#else
        //$$ player.yaw = value;
        //#endif
    }

    /**
     * 获取实体的眼睛位置
     * 
     * @param entity 实体实例
     * @return 眼睛位置的三维向量
     */
    @Pattern
    private static Vec3d getEyePos(Entity entity) {
        //#if MC >= 11701
        return entity.getEyePos();
        //#else
        //$$ return adris.altoclef.multiversion.entity.EntityHelper.getEyePos(entity);
        //#endif
    }

    /**
     * 获取实体所在的区块位置
     * 
     * @param entity 实体实例
     * @return 区块位置
     */
    @Pattern
    private static ChunkPos getChunkPos(Entity entity) {
        //#if MC >= 11701
        return entity.getChunkPos();
        //#else
        //$$ return adris.altoclef.multiversion.entity.EntityHelper.getChunkPos(entity);
        //#endif
    }

    /**
     * 获取实体的X轴方块坐标
     * 
     * @param entity 实体实例
     * @return X轴方块坐标
     */
    @Pattern
    private static int getBlockX(Entity entity) {
        //#if MC >= 11701
        return entity.getBlockX();
        //#else
        //$$ return adris.altoclef.multiversion.entity.EntityHelper.getBlockX(entity);
        //#endif
    }

    /**
     * 获取实体的Y轴方块坐标
     * 
     * @param entity 实体实例
     * @return Y轴方块坐标
     */
    @Pattern
    private static int getBlockY(Entity entity) {
        //#if MC >= 11701
        return entity.getBlockY();
        //#else
        //$$ return adris.altoclef.multiversion.entity.EntityHelper.getBlockY(entity);
        //#endif
    }

    /**
     * 获取实体的Z轴方块坐标
     * 
     * @param entity 实体实例
     * @return Z轴方块坐标
     */
    @Pattern
    private static int getBlockZ(Entity entity) {
        //#if MC >= 11701
        return entity.getBlockZ();
        //#else
        //$$ return adris.altoclef.multiversion.entity.EntityHelper.getBlockZ(entity);
        //#endif
    }
}
