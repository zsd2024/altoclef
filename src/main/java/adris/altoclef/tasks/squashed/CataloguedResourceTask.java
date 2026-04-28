package adris.altoclef.tasks.squashed;

import adris.altoclef.AltoClef;
import adris.altoclef.TaskCatalogue;
import adris.altoclef.tasks.ResourceTask;
import adris.altoclef.tasks.container.CraftInTableTask;
import adris.altoclef.tasks.container.UpgradeInSmithingTableTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.helpers.StorageHelper;
import org.apache.commons.lang3.ArrayUtils;

import java.util.*;

/**
 * 目录资源任务
 * 该类用于管理和执行一系列已注册的任务，通过任务合并优化资源获取流程
 */
public class CataloguedResourceTask extends ResourceTask {

    /**
     * 任务合并器，用于优化同类任务的执行
     */
    private final TaskSquasher squasher;
    
    /**
     * 目标物品列表
     */
    private final ItemTarget[] targets;
    
    /**
     * 需要完成的任务列表
     */
    private final List<ResourceTask> tasksToComplete;

    /**
     * 构造函数
     * 
     * @param squash 是否启用任务合并优化
     * @param targets 目标物品列表
     */
    public CataloguedResourceTask(boolean squash, ItemTarget... targets) {
        super(targets);
        squasher = new TaskSquasher();
        this.targets = targets;
        tasksToComplete = new ArrayList<>(targets.length);

        for (ItemTarget target : targets) {
            if (target != null) {
                tasksToComplete.add(TaskCatalogue.getItemTask(target));
            }
        }

        if (squash) {
            squashTasks(tasksToComplete);
        }
    }

    /**
     * 默认构造函数，默认启用任务合并优化
     * 
     * @param targets 目标物品列表
     */
    public CataloguedResourceTask(ItemTarget... targets) {
        this(true, targets);
    }

    @Override
    protected void onResourceStart(AltoClef mod) {

    }

    @Override
    protected Task onResourceTick(AltoClef mod) {
        for (ResourceTask task : tasksToComplete) {
            for (ItemTarget target : task.getItemTargets()) {
                // 如果未能满足此任务的目标，则执行该任务
                if (!StorageHelper.itemTargetsMetInventory(target)) return task;
            }
        }
        return null;
    }

    @Override
    public boolean isFinished() {
        for (ResourceTask task : tasksToComplete) {
            for (ItemTarget target : task.getItemTargets()) {
                if (!StorageHelper.itemTargetsMetInventory(target)) return false;
            }
        }
        // 所有目标均已满足
        return true;
    }

    @Override
    protected boolean shouldAvoidPickingUp(AltoClef mod) {
        // 无用
        return false;
    }

    @Override
    protected void onResourceStop(AltoClef mod, Task interruptTask) {

    }

    @Override
    protected boolean isEqualResource(ResourceTask other) {
        if (other instanceof CataloguedResourceTask task) {
            return Arrays.equals(task.targets, targets);
        }
        return false;
    }

    @Override
    protected String toDebugStringName() {
        return "获取目录资源: " + ArrayUtils.toString(targets);
    }

    /**
     * 合并任务列表以优化执行
     * 
     * @param tasks 待合并的任务列表
     */
    private void squashTasks(List<ResourceTask> tasks) {
        squasher.addTasks(tasks);
        tasks.clear();
        tasks.addAll(squasher.getSquashed());
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    /**
     * 任务合并器内部类
     * 负责将相同类型的任务进行合并优化
     */
    static class TaskSquasher {

        /**
         * 任务类型到合并器的映射表
         */
        private final Map<Class, adris.altoclef.tasks.squashed.TypeSquasher> _squashMap = new HashMap<>();

        /**
         * 无法合并的任务列表
         */
        private final List<ResourceTask> _unSquashableTasks = new ArrayList<>();

        /**
         * 构造函数，初始化支持的任务类型合并器
         */
        public TaskSquasher() {
            _squashMap.put(CraftInTableTask.class, new CraftSquasher());
            _squashMap.put(UpgradeInSmithingTableTask.class, new SmithingSquasher());
            //_squashMap.put(MineAndCollectTask.class)
        }

        /**
         * 添加单个任务到合并器
         * 
         * @param t 待添加的任务
         */
        public void addTask(ResourceTask t) {
            Class type = t.getClass();
            if (_squashMap.containsKey(type)) {
                _squashMap.get(type).add(t);
            } else {
                //Debug.logMessage("无法合并的任务: " + type + ": " + t);
                _unSquashableTasks.add(t);
            }
        }

        /**
         * 添加多个任务到合并器
         * 
         * @param tasks 待添加的任务列表
         */
        public void addTasks(List<ResourceTask> tasks) {
            for (ResourceTask task : tasks) {
                addTask(task);
            }
        }

        /**
         * 获取合并后的任务列表
         * 
         * @return 合并优化后的任务列表
         */
        public List<ResourceTask> getSquashed() {
            List<ResourceTask> result = new ArrayList<>();

            for (Class type : _squashMap.keySet()) {
                result.addAll(_squashMap.get(type).getSquashed());
            }
            result.addAll(_unSquashableTasks);

            return result;
        }
    }


}
