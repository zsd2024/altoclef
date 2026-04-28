package adris.altoclef.util.baritone;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.util.helpers.BaritoneHelper;
import adris.altoclef.util.helpers.ProjectileHelper;
import baritone.api.pathing.goals.Goal;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

/**
 * 躲避投射物目标工具类
 * 用于定义一个躲避投射物（如箭矢）的目标，确保机器人避开危险区域
 */
public class GoalDodgeProjectiles implements Goal {

    private static final double Y_SCALE = 0.3f;

    /** AltoClef主模块实例 */
    private final AltoClef mod;

    /** 水平方向的安全距离 */
    private final double distanceHorizontal;
    /** 垂直方向的安全距离 */
    private final double distanceVertical;

    /** 缓存的投射物列表 */
    private final List<CachedProjectile> cachedProjectiles = new ArrayList<>();

    /**
     * 构造函数
     * @param mod AltoClef主模块实例
     * @param distanceHorizontal 水平方向的安全距离
     * @param distanceVertical 垂直方向的安全距离
     */
    public GoalDodgeProjectiles(AltoClef mod, double distanceHorizontal, double distanceVertical) {
        this.mod = mod;
        this.distanceHorizontal = distanceHorizontal;
        this.distanceVertical = distanceVertical;
    }

    /**
     * 检查投射物是否无效
     * @param projectile 投射物对象
     * @return 如果投射物无效返回true，否则返回false
     */
    private static boolean isInvalidProjectile(CachedProjectile projectile) {
        //noinspection RedundantIfStatement
        if (projectile == null) return true;
        //if (projectile.getVelocity().lengthSquared() < 0.1) return false;
        return false;
    }

    @Override
    public boolean isInGoal(int x, int y, int z) {
        List<CachedProjectile> projectiles = getProjectiles();
        Vec3d p = new Vec3d(x, y, z);
        //Debug.logMessage("SIZE: " + projectiles.size());
        synchronized (BaritoneHelper.MINECRAFT_LOCK) {
            if (!projectiles.isEmpty()) {
                for (CachedProjectile projectile : projectiles) {
                    if (isInvalidProjectile(projectile)) continue;
                    try {
                        if (projectile.needsToRecache()) {
                            projectile.setCacheHit(ProjectileHelper.calculateArrowClosestApproach(projectile, p));
                        }
                        Vec3d hit = projectile.getCachedHit();
                        //Debug.logMessage("Hit Delta: " + p.subtract(hit));

                        if (isHitCloseEnough(hit, p)) return false;
                    } catch (Exception e) {
                        Debug.logWarning("检查目标时捕获到异常: " + e.getMessage());
                        /// ????? 不清楚为什么会在这里发生空指针异常。
                    }
                    //double sqFromMob = creepuh.squaredDistanceTo(x, y, z);
                    //if (sqFromMob < _distance*_distance) return false;
                }
            }
        }
        //Debug.logMessage("COMFY: " + p.subtract(MinecraftClient.getInstance().player.getPos()));
        return true;
    }

    @Override
    public double heuristic(int x, int y, int z) {
        Vec3d p = new Vec3d(x, y, z);
        // 成本越高越好（远离箭矢的总距离）
        double costFactor = 0;

        List<CachedProjectile> projectiles = getProjectiles();
        synchronized (BaritoneHelper.MINECRAFT_LOCK) {
            if (!projectiles.isEmpty()) {
                for (CachedProjectile projectile : projectiles) {
                    if (isInvalidProjectile(projectile)) continue;

                    if (projectile.needsToRecache()) {
                        projectile.setCacheHit(ProjectileHelper.calculateArrowClosestApproach(projectile, p));
                    }
                    Vec3d hit = projectile.getCachedHit();

                    double arrowPenalty = ProjectileHelper.getFlatDistanceSqr(projectile.position.x, projectile.position.z, projectile.velocity.x, projectile.velocity.z, p.x, p.z);
                    //double arrowCost = hit.squaredDistanceTo(p); //Math.pow(p.x - hit.x, 2) + Math.pow(p.z - hit.z, 2);

                    if (isHitCloseEnough(hit, p)) {
                        costFactor += arrowPenalty;
                    }
                }
            }
        }
        return -1 * costFactor;
    }

    /**
     * 检查命中点是否足够接近
     * @param hit 命中点
     * @param to 目标点
     * @return 如果足够接近返回true，否则返回false
     */
    private boolean isHitCloseEnough(Vec3d hit, Vec3d to) {
        Vec3d delta = to.subtract(hit);
        double horizontalSquared = delta.x * delta.x + delta.z * delta.z;
        double vertical = Math.abs(delta.y);
        return horizontalSquared < distanceHorizontal * distanceHorizontal && vertical < distanceVertical;
    }

    /**
     * 获取当前所有的投射物
     * @return 投射物列表
     */
    private List<CachedProjectile> getProjectiles() {
        return mod.getEntityTracker().getProjectiles();
    }
}
