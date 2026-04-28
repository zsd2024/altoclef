package adris.altoclef.tasksystem;

/**
 * 允许任务声明其父任务不能中断自身，且此任务必须继续执行。
 */
public interface ITaskCanForce {

    /**
     * @param interruptingCandidate 尝试中断当前任务的任务。
     * @return 即使父任务决定不应继续，是否仍应强制任务继续执行。
     */
    boolean shouldForce(Task interruptingCandidate);
}
