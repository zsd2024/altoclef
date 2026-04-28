package adris.altoclef.tasks.entity;

import net.minecraft.entity.Entity;

import java.util.function.Predicate;

/**
 * 击杀多个实体任务 - 查找并击杀指定类型的所有实体
 */
public class KillEntitiesTask extends DoToClosestEntityTask {

    /**
     * 构造函数，指定实体筛选条件和目标实体类型
     * @param shouldKill 判断是否应该击杀该实体的谓词条件
     * @param entities 目标实体类型数组
     */
    public KillEntitiesTask(Predicate<Entity> shouldKill, Class<?>... entities) {
        super(KillEntityTask::new, shouldKill, entities);
    }

    /**
     * 构造函数，仅指定目标实体类型（击杀所有匹配类型的实体）
     * @param entities 目标实体类型数组
     */
    public KillEntitiesTask(Class<?>... entities) {
        super(KillEntityTask::new, entities);
    }
}

    public KillEntitiesTask(Class<?>... entities) {
        super(KillEntityTask::new, entities);
    }
}
