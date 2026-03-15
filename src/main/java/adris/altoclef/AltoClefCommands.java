package adris.altoclef;

import adris.altoclef.commands.*;
import adris.altoclef.commands.random.ScanCommand;
import adris.altoclef.commands.random.DummyTaskCommand;
import adris.altoclef.commandsystem.exception.CommandException;

/**
 * 初始化AltoClef内置命令系统。
 */
public class AltoClefCommands {

    public static void init() throws CommandException {
        // 在此处列出命令
        AltoClef.getCommandExecutor().registerNewCommand(
                new HelpCommand(),            // 帮助命令
                new GetCommand(),             // 获取命令
                new ListCommand(),            // 列表命令
                new EquipCommand(),           // 装备命令
                new DepositCommand(),         // 存储命令
                new StashCommand(),           // 藏匿命令
                new GotoCommand(),            // 前往命令
                new IdleCommand(),            // 空闲命令
                new HeroCommand(),            // 英雄命令
                new CoordsCommand(),          // 坐标命令
                new StatusCommand(),          // 状态命令
                new InventoryCommand(),       // 背包命令
                new LocateStructureCommand(), // 定位结构命令
                new StopCommand(),            // 停止命令
                new PauseCommand(),           // 暂停命令
                new UnPauseCommand(),         // 恢复命令
                new SetGammaCommand(),        // 设置伽马值命令
                new TestCommand(),            // 测试命令
                new FoodCommand(),            // 食物命令
                new MeatCommand(),            // 肉类命令
                new ReloadSettingsCommand(),  // 重载设置命令
                new GamerCommand(),           // 游戏者命令
                new MarvionCommand(),         // Marvion命令
                new DummyTaskCommand(),       // 哑任务命令
                new FollowCommand(),          // 跟随命令
                new ScanCommand(),            // 扫描命令
                new GiveCommand()             // 给予命令
        );
    }
}
