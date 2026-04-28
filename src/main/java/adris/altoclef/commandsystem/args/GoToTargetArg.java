package adris.altoclef.commandsystem.args;

import adris.altoclef.commandsystem.exception.BadCommandSyntaxException;
import adris.altoclef.commandsystem.exception.CommandException;
import adris.altoclef.commandsystem.GotoTarget;
import adris.altoclef.commandsystem.StringReader;
import adris.altoclef.commandsystem.exception.CommandNotFinishedException;
import adris.altoclef.util.Dimension;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * 目标位置参数类
 * 用于解析和处理命令中的目标位置参数，支持坐标和维度信息
 */
public class GoToTargetArg extends Arg<GotoTarget> {

    /**
     * 构造函数
     * @param name 参数名称
     */
    public GoToTargetArg(String name) {
        super(name);
    }

    /**
     * 构造函数
     * @param name 参数名称
     * @param defaultValue 默认值
     * @param showDefault 是否显示默认值
     */
    public GoToTargetArg(String name, GotoTarget defaultValue, boolean showDefault) {
        super(name, defaultValue, showDefault);
    }

    /**
     * 解析目标位置参数
     * @param reader 字符串读取器
     * @return 解析后的目标位置对象
     * @throws CommandException 当解析失败时抛出命令异常
     */
    public static GotoTarget parse(StringReader reader) throws CommandException {
        List<Integer> numbers = new ArrayList<>();

        Integer num;
        while (numbers.size() < 3 && (num = Arg.parseIfSupplied(reader, IntArg::parse)) != null) {
            numbers.add(num);
        }

        Dimension dimension = Arg.parseIfSupplied(reader, r -> EnumArg.parse(r, Dimension.class));

        if (numbers.isEmpty() && dimension == null) {
            throw new CommandNotFinishedException("需要提供坐标和/或维度信息");
        }

        int x = 0;
        int y = 0;
        int z = 0;

        GotoTarget.GotoTargetCoordType coordType;
        switch (numbers.size()) {
            case 0 -> coordType = GotoTarget.GotoTargetCoordType.NONE;
            case 1 -> {
                y = numbers.getFirst();
                coordType = GotoTarget.GotoTargetCoordType.Y;
            }
            case 2 -> {
                x = numbers.get(0);
                z = numbers.get(1);
                coordType = GotoTarget.GotoTargetCoordType.XZ;
            }
            case 3 -> {
                x = numbers.get(0);
                y = numbers.get(1);
                z = numbers.get(2);
                coordType = GotoTarget.GotoTargetCoordType.XYZ;
            }
            default -> {
                throw new BadCommandSyntaxException("传递给坐标的整数数量意外: " + numbers.size());
            }
        }
        return new GotoTarget(x, y, z, dimension, coordType);
    }

    /**
     * 获取坐标建议
     * @param client Minecraft客户端实例
     * @param numbers 已解析的数字列表
     * @return 坐标建议列表
     */
    private static List<String> getCoordsSuggestion(MinecraftClient client, List<Integer> numbers) {
        HitResult hit = client.crosshairTarget;

        BlockPos pos;
        if (hit instanceof BlockHitResult blockHit) {
            pos = blockHit.getBlockPos();
        } else {
            pos = client.player.getBlockPos();
        }

        // 选择下一个要建议的轴
        if (numbers.isEmpty()) {
            return List.of(
                    pos.getX() + " " + pos.getY() + " " + pos.getZ(),
                    pos.getX() + " " + pos.getZ(),
                    pos.getY()+""

            );
        }


        return List.of();
    }

    @Override
    public Class<GotoTarget> getType() {
        return GotoTarget.class;
    }

    @Override
    protected StringParser<GotoTarget> getParser() {
        return GoToTargetArg::parse;
    }

    @Override
    public Stream<String> getSuggestions(StringReader reader) {
        List<Integer> numbers = new ArrayList<>();

        Integer num;
        while (numbers.size() < 3 && (num = Arg.parseIfSupplied(reader, IntArg::parse)) != null) {
            numbers.add(num);
        }

        if (numbers.isEmpty()) {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client == null || client.player == null) {
                return Stream.empty();
            }
            List<String> suggestion = getCoordsSuggestion(client, numbers);

            return Stream.concat(suggestion.stream(), EnumArg.suggestions(Dimension.class));
        }

        return EnumArg.suggestions(Dimension.class);
    }

    @Override
    public String getTypeName() {
        return "GoTo Target";
    }

}
