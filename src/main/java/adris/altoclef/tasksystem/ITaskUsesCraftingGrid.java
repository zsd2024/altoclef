package adris.altoclef.tasksystem;

/**
 * 某些任务（主要是打开容器的任务）如果不确保2x2合成网格为空就会出错。
 * 实现此接口的任务要求实现者保持合成网格为空。
 * <p>
 * 此接口允许任务声明在执行前必须清空特定槽位。
 */
public interface ITaskUsesCraftingGrid {

}
