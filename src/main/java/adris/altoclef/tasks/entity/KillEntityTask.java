package adris.altoclef.tasks.entity;

import adris.altoclef.AltoClef;
import net.minecraft.entity.Entity;

import java.util.Objects;
import java.util.Optional;

/**
 * 击杀单个实体任务 - 针对特定实体执行击杀操作
 */
public class KillEntityTask extends AbstractKillEntityTask {

    /** 目标实体 */
    private final Entity target;

    /**
     * 构造函数，使用默认战斗参数
     * @param entity 要击杀的目标实体
     */
    public KillEntityTask(Entity entity) {
        target = entity;
    }

    /**
     * 构造函数，指定自定义战斗参数
     * @param entity 要击杀的目标实体
     * @param maintainDistance 保持距离
     * @param combatGuardLowerRange 战斗守卫降低范围
     * @param combatGuardLowerFieldRadius 战斗守卫降低力场半径
     */
    public KillEntityTask(Entity entity, double maintainDistance, double combatGuardLowerRange, double combatGuardLowerFieldRadius) {
        super(maintainDistance, combatGuardLowerRange, combatGuardLowerFieldRadius);
        target = entity;
    }

    @Override
    protected Optional<Entity> getEntityTarget(AltoClef mod) {
        return Optional.of(target);
    }

    @Override
    protected boolean isSubEqual(AbstractDoToEntityTask other) {
        if (other instanceof KillEntityTask task) {
            return Objects.equals(task.target, target);
        }
        return false;
    }

    @Override
    protected String toDebugString() {
        return "正在击杀 " + target.getType().getTranslationKey();
    }
}

    public KillEntityTask(Entity entity, double maintainDistance, double combatGuardLowerRange, double combatGuardLowerFieldRadius) {
        super(maintainDistance, combatGuardLowerRange, combatGuardLowerFieldRadius);
        target = entity;
    }

    @Override
    protected Optional<Entity> getEntityTarget(AltoClef mod) {
        return Optional.of(target);
    }

    @Override
    protected boolean isSubEqual(AbstractDoToEntityTask other) {
        if (other instanceof KillEntityTask task) {
            return Objects.equals(task.target, target);
        }
        return false;
    }

    @Override
    protected String toDebugString() {
        return "Killing " + target.getType().getTranslationKey();
    }
}
