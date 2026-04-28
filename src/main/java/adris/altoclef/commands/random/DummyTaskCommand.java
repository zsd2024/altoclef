package adris.altoclef.commands.random;

import adris.altoclef.AltoClef;
import adris.altoclef.commandsystem.ArgParser;
import adris.altoclef.commandsystem.Command;
import adris.altoclef.commandsystem.exception.CommandException;
import adris.altoclef.tasksystem.Task;

/**
 * 虚拟任务命令
 * 创建一个不执行任何操作的虚拟任务，用于测试目的
 */
public class DummyTaskCommand extends Command {
    /**
     * 构造函数
     * 初始化命令名称为"dummy"，描述为"不执行任何操作"
     */
    public DummyTaskCommand() {
        super("dummy", "不执行任何操作");
    }

    /**
     * 执行命令逻辑
     * 
     * @param mod AltoClef主模块实例
     * @param parser 命令参数解析器
     * @throws CommandException 命令执行异常
     */
    @Override
    protected void call(AltoClef mod, ArgParser parser) throws CommandException {
        mod.runUserTask(new DummyTask(), this::finish);
    }

    /**
     * 虚拟任务内部类
     * 实现一个空的任务，所有方法都不执行任何操作
     */
    private class DummyTask extends Task {

        /**
         * 任务开始时调用
         */
        @Override
        protected void onStart() {

        }

        /**
         * 每个游戏刻调用
         * 
         * @return 返回null表示任务已完成
         */
        @Override
        protected Task onTick() {
            return null;
        }

        /**
         * 任务停止时调用
         * 
         * @param interruptTask 中断当前任务的新任务
         */
        @Override
        protected void onStop(Task interruptTask) {

        }

        /**
         * 比较任务是否相等
         * 
         * @param other 要比较的其他任务
         * @return 始终返回false，表示任务不相等
         */
        @Override
        protected boolean isEqual(Task other) {
            return false;
        }

        /**
         * 获取任务的调试字符串
         * 
         * @return 返回null
         */
        @Override
        protected String toDebugString() {
            return null;
        }
    }
}
