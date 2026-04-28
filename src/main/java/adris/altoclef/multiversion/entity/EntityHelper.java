package adris.altoclef.multiversion.entity;

import adris.altoclef.mixins.PortalManagerAccessor;
import net.minecraft.block.NetherPortalBlock;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.dimension.NetherPortal;

/**
 * 实体帮助器
 * 提供不同 Minecraft 版本之间实体相关功能的辅助方法
 */
public class EntityHelper {

    //#if MC >= 12100
    /**
     * 检查实体是否在地狱传送门中
     * 
     * @param entity 要检查的实体
     * @return 如果实体在地狱传送门中返回true，否则返回false
     */
    public static boolean isInNetherPortal(Entity entity) {
       return (entity.portalManager != null && ((PortalManagerAccessor)entity.portalManager).accessPortal() instanceof NetherPortalBlock && entity.portalManager.isInPortal())
               || entity.getPortalCooldown() > 0;
    }
    //#endif

    //#if MC <= 11605
    //$$ /**
    //$$  * 获取实体的眼睛位置
    //$$  * 
    //$$  * @param entity 实体实例
    //$$  * @return 眼睛位置的三维向量
    //$$  */
    //$$ public static Vec3d getEyePos(Entity entity) {
    //$$     return new Vec3d(entity.getX(), entity.getEyeY(), entity.getZ());
    //$$ }
    //$$
    //$$ /**
    //$$  * 获取实体所在的区块位置
    //$$  * 
    //$$  * @param entity 实体实例
    //$$  * @return 区块位置
    //$$  */
    //$$ public static ChunkPos getChunkPos(Entity entity) {
    //$$    return new ChunkPos(entity.getBlockPos());
    //$$ }
    //$$
    //$$ /**
    //$$  * 获取实体的X轴方块坐标
    //$$  * 
    //$$  * @param entity 实体实例
    //$$  * @return X轴方块坐标
    //$$  */
    //$$ public static int getBlockX(Entity entity) {
    //$$      return entity.getBlockPos().getX();
    //$$  }
    //$$
    //$$ /**
    //$$  * 获取实体的Y轴方块坐标
    //$$  * 
    //$$  * @param entity 实体实例
    //$$  * @return Y轴方块坐标
    //$$  */
    //$$ public static int getBlockY(Entity entity) {
    //$$      return entity.getBlockPos().getY();
    //$$  }
    //$$
    //$$ /**
    //$$  * 获取实体的Z轴方块坐标
    //$$  * 
    //$$  * @param entity 实体实例
    //$$  * @return Z轴方块坐标
    //$$  */
    //$$ public static int getBlockZ(Entity entity) {
    //$$      return entity.getBlockPos().getZ();
    //$$  }
    //#endif
}
