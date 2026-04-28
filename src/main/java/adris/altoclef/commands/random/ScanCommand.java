package adris.altoclef.commands.random;

import adris.altoclef.AltoClef;
import adris.altoclef.trackers.BlockScanner;
import adris.altoclef.commandsystem.ArgParser;
import adris.altoclef.commandsystem.Command;
import adris.altoclef.commandsystem.exception.CommandException;
import adris.altoclef.commandsystem.args.StringArg;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;

import java.lang.reflect.Field;

/**
 * 扫描命令
 * 用于定位最近的指定方块
 */
public class ScanCommand extends Command {

    /**
     * 构造函数
     * 初始化命令名称为"scan"，描述为"定位最近的方块"，并定义一个字符串参数"block"(默认值为"DIRT")
     * 
     * @throws CommandException 命令初始化异常
     */
    public ScanCommand() throws CommandException {
        super("scan", "定位最近的方块",
                new StringArg("block", "DIRT")
        );
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
        // 获取用户输入的方块名称
        String blockStr = parser.get(String.class);

        // 获取Minecraft所有方块的字段
        Field[] declaredFields = Blocks.class.getDeclaredFields();
        Block block = null;

        // 遍历所有方块字段，查找匹配的方块
        for (Field field : declaredFields) {
            field.setAccessible(true);
            try {
                if (field.getName().equalsIgnoreCase(blockStr)) {
                    block = (Block) field.get(Blocks.class);
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
            field.setAccessible(false);
        }

        // 如果未找到指定方块，记录警告日志并返回
        if (block == null) {
            mod.logWarning("未找到名为: " + blockStr + " 的方块 :(");
            return;
        }

        // 使用方块扫描器获取最近的指定方块位置并记录日志
        BlockScanner blockScanner = mod.getBlockScanner();
        mod.log(blockScanner.getNearestBlock(block,mod.getPlayer().getPos())+"");
    }

}