package adris.altoclef.tasks.squashed;

import adris.altoclef.tasks.ResourceTask;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 类型合并器抽象基类
 * 为特定类型的任务提供合并优化功能
 * 
 * @param <T> 资源任务的具体类型
 */
public abstract class TypeSquasher<T extends ResourceTask> {

    /**
     * 待合并的任务列表
     */
    private final List<T> _tasks = new ArrayList<>();

    /**
     * 添加任务到合并列表
     * 
     * @param task 待添加的任务
     */
    void add(T task) {
        _tasks.add(task);
    }

    /**
     * 获取合并后的任务列表
     * 
     * @return 合并优化后的任务列表
     */
    public List<ResourceTask> getSquashed() {
        if (_tasks.isEmpty()) {
            // 列表为空，不执行任何逻辑
            return Collections.emptyList();
        }
        return getSquashed(_tasks);
    }

    /**
     * 抽象方法：实现具体的任务合并逻辑
     * 
     * @param tasks 待合并的任务列表
     * @return 合并后的任务列表
     */
    protected abstract List<ResourceTask> getSquashed(List<T> tasks);
}
