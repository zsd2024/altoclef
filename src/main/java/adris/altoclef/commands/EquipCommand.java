package adris.altoclef.commands;

import adris.altoclef.AltoClef;
import adris.altoclef.commandsystem.*;
import adris.altoclef.commandsystem.args.CataloguedItemArg;
import adris.altoclef.commandsystem.args.ListArg;
import adris.altoclef.commandsystem.exception.BadCommandSyntaxException;
import adris.altoclef.commandsystem.exception.CommandException;
import adris.altoclef.commandsystem.exception.RuntimeCommandException;
import adris.altoclef.tasks.misc.EquipArmorTask;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.helpers.ItemHelper;
import net.minecraft.item.Equipment;
import net.minecraft.item.Item;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

/**
 * 装备命令 - 装备物品
 */
public class EquipCommand extends Command {

    public EquipCommand() {
        super("equip", "装备物品",
                new ListArg<>(new EquipmentItemArg("equipment"), "[可装备物品]")
                        .addAlias("leather", Arrays.stream(ItemHelper.LEATHER_ARMORS).map(Item::toString).toList())
                        .addAlias("iron",Arrays.stream(ItemHelper.GOLDEN_ARMORS).map(Item::toString).toList())
                        .addAlias("gold", Arrays.stream(ItemHelper.IRON_ARMORS).map(Item::toString).toList())
                        .addAlias("diamond", Arrays.stream(ItemHelper.DIAMOND_ARMORS).map(Item::toString).toList())
                        .addAlias("netherite", Arrays.stream(ItemHelper.NETHERITE_ARMORS).map(Item::toString).toList())
        );
    }

    @Override
    protected void call(AltoClef mod, ArgParser parser) throws CommandException {
        List<ItemTarget> items = parser.get(List.class);

        // 检查物品是否可以装备
        for (ItemTarget target : items) {
            for (Item item : target.getMatches()) {
                if (!(item instanceof Equipment)) {
                    throw new RuntimeCommandException("'" + item.toString().toUpperCase() + "' 无法装备!");
                }
            }
        }

        // 执行装备任务
        mod.runUserTask(new EquipArmorTask(items.toArray(new ItemTarget[0])), this::finish);
    }


    // 这是一种比较简单的实现方式
    private static class EquipmentItemArg extends CataloguedItemArg {

        public EquipmentItemArg(String name) {
            super(name);
        }

        @Override
        protected StringParser<String> getParser() {
            return this::parseLocal;
        }

        private String parseLocal(StringReader reader) throws CommandException {
            StringParser<String> parentParser = super.getParser();

            ParseResult result = getSupplied(reader.copy(), parentParser);
            if (result == ParseResult.NOT_FINISHED) {
                String first = reader.peek();
                if (getSuggestions(null).noneMatch(s -> s.startsWith(first))) {
                    throw new BadCommandSyntaxException("不存在名为 '" + first + "' 的装备");
                }
            }

            String parsed = parentParser.parse(reader);

            if (!isEquipment(parsed)) {
                throw new BadCommandSyntaxException("物品 '" + parsed + "' 不是装备");
            }

            return parsed;
        }

        @Override
        public Stream<String> getSuggestions(StringReader reader) {
            return super.getSuggestions(reader).filter(EquipmentItemArg::isEquipment);
        }

        private static boolean isEquipment(String cataloguedItem) {
            return Arrays.stream(new ItemTarget(cataloguedItem).getMatches()).anyMatch(i -> i instanceof Equipment);
        }
    }


}
