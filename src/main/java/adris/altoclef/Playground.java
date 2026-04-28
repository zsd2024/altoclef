package adris.altoclef;

import adris.altoclef.tasks.CraftGenericManuallyTask;
import adris.altoclef.tasks.construction.PlaceBlockNearbyTask;
import adris.altoclef.tasks.construction.PlaceStructureBlockTask;
import adris.altoclef.tasks.construction.compound.ConstructIronGolemTask;
import adris.altoclef.tasks.construction.compound.ConstructNetherPortalObsidianTask;
import adris.altoclef.tasks.container.SmeltInFurnaceTask;
import adris.altoclef.tasks.container.StoreInAnyContainerTask;
import adris.altoclef.tasks.entity.KillEntityTask;
import adris.altoclef.tasks.entity.ShootArrowSimpleProjectileTask;
import adris.altoclef.tasks.examples.ExampleTask2;
import adris.altoclef.tasks.misc.EquipArmorTask;
import adris.altoclef.tasks.misc.PlaceBedAndSetSpawnTask;
import adris.altoclef.tasks.misc.RavageDesertTemplesTask;
import adris.altoclef.tasks.misc.RavageRuinedPortalsTask;
import adris.altoclef.tasks.movement.*;
import adris.altoclef.tasks.resources.CollectBlazeRodsTask;
import adris.altoclef.tasks.resources.CollectFlintTask;
import adris.altoclef.tasks.resources.CollectFoodTask;
import adris.altoclef.tasks.resources.TradeWithPiglinsTask;
import adris.altoclef.tasks.speedrun.KillEnderDragonTask;
import adris.altoclef.tasks.speedrun.KillEnderDragonWithBedsTask;
import adris.altoclef.tasks.speedrun.WaitForDragonAndPearlTask;
import adris.altoclef.util.*;
import adris.altoclef.util.helpers.WorldHelper;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.GhastEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.chunk.EmptyChunk;

import java.io.*;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

/**
 * 调试游乐场类
 * 
 * 用于测试各种功能的临时代码存放地。
 * 如solonovamax所建议，这些代码应该真正移到单元测试中，
 * 但在Minecraft中设置定时测试和测试世界可能具有挑战性，
 * 因此这是垃圾测试代码的临时存放地。
 */
@SuppressWarnings("EnhancedSwitchMigration")
public class Playground {

    /**
     * 空闲测试初始化函数
     * 在模组启动时调用，用于执行一次性测试代码
     * @param mod AltoClef模组实例
     */
    public static void IDLE_TEST_INIT_FUNCTION(AltoClef mod) {
        // 测试代码放在这里

        // 打印所有未编目的资源以及没有对应物品的资源
        /*
        Set<String> collectable = new HashSet<>(TaskCatalogue.resourceNames());
        Set<String> allItems = new HashSet<>();

        List<String> notCollected = new ArrayList<>();

        for (Identifier id : Registry.ITEM.getIds()) {
            Item item = Registry.ITEM.get(id);
            String name = ItemUtil.trimItemName(item.getTranslationKey());
            allItems.add(name);
            if (!collectable.contains(name)) {
                notCollected.add(name);
            }
        }

        List<String> notAnItem = new ArrayList<>();
        for (String cataloguedName : collectable) {
            if (!allItems.contains(cataloguedName)) {
                notAnItem.add(cataloguedName);
            }
        }

        notCollected.sort(String::compareTo);
        notAnItem.sort(String::compareTo);

        Function<List<String>, String> temp = (list) -> {
            StringBuilder result = new StringBuilder("");
            for (String name : list) {
                result.append(name).append("\n");
            }
            return result.toString();
        };

        Debug.logInternal("尚未收集:\n" + temp.apply(notCollected));
        Debug.logInternal("\n\n\n");
        Debug.logInternal("不是物品:\n" + temp.apply(notAnItem));
        */

        /* 打印所有已编目的资源

        List<String> resources = new ArrayList<>(TaskCatalogue.resourceNames());
        resources.sort(String::compareTo);
        StringBuilder result = new StringBuilder("所有资源:\n");
        for (String name : resources) {
            result.append(name).append("\n");
        }
        Debug.logInternal("我们有这些:\n" + result.toString());

         */
    }

    /**
     * 空闲测试Tick函数
     * 在游戏每帧调用，用于执行持续性测试代码
     * @param mod AltoClef模组实例
     */
    public static void IDLE_TEST_TICK_FUNCTION(AltoClef mod) {
        // 测试代码放在这里
    }

    /**
     * 临时测试函数
     * 根据传入的参数执行不同的测试任务
     * @param mod AltoClef模组实例
     * @param arg 测试参数
     */
    public static void TEMP_TEST_FUNCTION(AltoClef mod, String arg) {
        //mod.runUserTask();
        Debug.logMessage("正在运行测试...");

        switch (arg) {
            case "":
                // 未指定测试
                Debug.logWarning("请指定一个测试 (例如: stacked, bed, terminate)");
                break;
            case "pickup":
                mod.runUserTask(new PickupDroppedItemTask(new ItemTarget(Items.RAW_IRON, 3), true));
                break;
            case "chunk": {
                // 我们可能会错过一个很远的区块...
                BlockPos p = new BlockPos(100000, 3, 100000);
                Debug.logMessage("已加载? " + (!(mod.getWorld().getChunk(p) instanceof EmptyChunk)));
                break;
            }
            case "structure":
                mod.runUserTask(new PlaceStructureBlockTask(new BlockPos(10, 6, 10)));
                break;
            case "place": {
                //BlockPos targetPos = new BlockPos(0, 6, 0);
                //mod.runUserTask(new PlaceSignTask(targetPos, "Hello"));
                //Direction direction = Direction.WEST;
                //mod.runUserTask(new InteractItemWithBlockTask(TaskCatalogue.getItemTarget("lava_bucket", 1), direction, targetPos, false));
                mod.runUserTask(new PlaceBlockNearbyTask(Blocks.CRAFTING_TABLE, Blocks.FURNACE));
                //mod.runUserTask(new PlaceStructureBlockTask(new BlockPos(472, 24, -324)));
                break;
            }
            case "stacked":
                // 它只需要:
                // 24 (盔甲) + 3*3 (镐) + 2 = 35 钻石
                // 2*3 (镐) + 1 = 7 木棍
                // 4 木板
                /*
                mod.runUserTask(TaskCatalogue.getSquashedItemTask(
                        new ItemTarget("diamond_chestplate", 1),
                        new ItemTarget("diamond_leggings", 1),
                        new ItemTarget("diamond_helmet", 1),
                        new ItemTarget("diamond_boots", 1),
                        new ItemTarget("diamond_pickaxe", 3),
                        new ItemTarget("diamond_sword", 1),
                        new ItemTarget("crafting_table", 1)
                ));
                 */
                mod.runUserTask(new EquipArmorTask(Items.DIAMOND_CHESTPLATE, Items.DIAMOND_LEGGINGS, Items.DIAMOND_HELMET, Items.DIAMOND_BOOTS));
                break;
            case "stacked2":
                mod.runUserTask(new EquipArmorTask(Items.DIAMOND_CHESTPLATE));
                break;
            case "ravage":
                mod.runUserTask(new RavageRuinedPortalsTask());
                break;
            case "temples":
                mod.runUserTask(new RavageDesertTemplesTask());
                break;
            case "smelt":
                ItemTarget target = new ItemTarget("iron_ingot", 4);
                ItemTarget material = new ItemTarget("iron_ore", 4);
                mod.runUserTask(new SmeltInFurnaceTask(new SmeltTarget(target, material)));
                break;
            case "iron":
                mod.runUserTask(new ConstructIronGolemTask());
                break;
            case "avoid":
                // 测试方块破坏谓词
                mod.getBehaviour().avoidBlockBreaking((BlockPos b) -> (-1000 < b.getX() && b.getX() < 1000)
                        && (-1000 < b.getY() && b.getY() < 1000)
                        && (-1000 < b.getZ() && b.getZ() < 1000));
                Debug.logMessage("测试避免破坏从 -1000, -1000, -1000 到 1000, 1000, 1000 的区域");
                break;
            case "portal":
                //mod.runUserTask(new EnterNetherPortalTask(new ConstructNetherPortalBucketTask(), Dimension.NETHER));
                mod.runUserTask(new EnterNetherPortalTask(new ConstructNetherPortalObsidianTask(), WorldHelper.getCurrentDimension() == Dimension.OVERWORLD ? Dimension.NETHER : Dimension.OVERWORLD));
                break;
            case "kill":
                List<ZombieEntity> zombs = mod.getEntityTracker().getTrackedEntities(ZombieEntity.class);
                if (zombs.size() == 0) {
                    Debug.logWarning("未找到僵尸。");
                } else {
                    LivingEntity entity = zombs.get(0);
                    mod.runUserTask(new KillEntityTask(entity));
                }
                break;
            case "craft":
                // 测试卸装备
                new Thread(() -> {
                    for (int i = 3; i > 0; --i) {
                        Debug.logMessage(i + "...");
                        sleepSec(1);
                    }

                    Item[] c = new Item[]{Items.COBBLESTONE};
                    Item[] s = new Item[]{Items.STICK};
                    CraftingRecipe recipe = CraftingRecipe.newShapedRecipe("test pickaxe", new Item[][]{c, c, c, null, s, null, null, s, null}, 1);

                    mod.runUserTask(new CraftGenericManuallyTask(new RecipeTarget(Items.STONE_PICKAXE, 1, recipe)));
                    /*
                    Item toEquip = Items.BUCKET;//Items.AIR;
                    Slot target = PlayerInventorySlot.getEquipSlot(EquipmentSlot.MAINHAND);

                    InventoryTracker t = mod.getItemStorage();

                    // 已经装备
                    if (t.getItemStackInSlot(target).getItem() == toEquip) {
                        Debug.logMessage("已经装备。");
                    } else {
                        List<Integer> itemSlots = t.getInventorySlotsWithItem(toEquip);
                        if (itemSlots.size() != 0) {
                            int slot = itemSlots.get(0);
                            t.swapItems(Slot.getFromInventory(slot), target);
                            Debug.logMessage("通过交换装备");
                        } else {
                            Debug.logWarning("无法装备物品 " + toEquip.getTranslationKey());
                        }
                    }
                     */
                }).start();
                //mod.getItemStorage().equipItem(Items.AIR);
                break;
            case "food":
                mod.runUserTask(new CollectFoodTask(20));
                break;
            case "temple":
                mod.runUserTask(new LocateDesertTempleTask());
                break;
            case "blaze":
                mod.runUserTask(new CollectBlazeRodsTask(7));
                break;
            case "flint":
                mod.runUserTask(new CollectFlintTask(5));
                break;
            case "unobtainable":
                String fname = "unobtainables.txt";
                try {
                    int unobtainable = 0;
                    int total = 0;
                    File f = new File(fname);
                    FileWriter fw = new FileWriter(f);
                    for (Identifier id : Registries.ITEM.getIds()) {
                        Item item = Registries.ITEM.get(id);
                        if (!TaskCatalogue.isObtainable(item)) {
                            ++unobtainable;
                            fw.write(item.getTranslationKey() + "\n");
                        }
                        total++;
                    }
                    fw.flush();
                    fw.close();
                    Debug.logMessage(unobtainable + " / " + total + " 个不可获得的物品。已将物品列表写入 \"" + f.getAbsolutePath() + "\"。");
                } catch (IOException e) {
                    Debug.logWarning(e.toString());
                }
                break;
            case "piglin":
                mod.runUserTask(new TradeWithPiglinsTask(32, new ItemTarget(Items.ENDER_PEARL, 12)));
                break;
            case "stronghold":
                mod.runUserTask(new GoToStrongholdPortalTask(12));
                break;
            case "bed":
                mod.runUserTask(new PlaceBedAndSetSpawnTask());
                break;
            case "dragon":
                mod.runUserTask(new KillEnderDragonWithBedsTask());
                break;
            case "dragon-pearl":
                mod.runUserTask(new ThrowEnderPearlSimpleProjectileTask(new BlockPos(0, 60, 0)));
                break;
            case "dragon-old":
                mod.runUserTask(new KillEnderDragonTask());
                break;
            case "chest":
                mod.runUserTask(new StoreInAnyContainerTask(true, new ItemTarget(Items.DIAMOND, 3)));
                break;
            case "example":
                mod.runUserTask(new ExampleTask2());
                break;
            case "netherite":
                mod.runUserTask(TaskCatalogue.getSquashedItemTask(
                        new ItemTarget("netherite_pickaxe", 1),
                        new ItemTarget("netherite_sword", 1),
                        new ItemTarget("netherite_helmet", 1),
                        new ItemTarget("netherite_chestplate", 1),
                        new ItemTarget("netherite_leggings", 1),
                        new ItemTarget("netherite_boots", 1)));
                break;
            case "arrow":

                List<GhastEntity> ghasts = mod.getEntityTracker().getTrackedEntities(GhastEntity.class);

                if (ghasts.size() == 0) {
                    Debug.logWarning("未找到恶魂。");
                    break;
                }

                GhastEntity ghast = ghasts.get(0);
                mod.runUserTask(new ShootArrowSimpleProjectileTask(ghast));
                break;
            default:
                mod.logWarning("未找到测试: \"" + arg + "\"。");
                break;
        }
    }

    /**
     * 睡眠指定秒数
     * @param seconds 秒数
     */
    private static void sleepSec(double seconds) {
        try {
            Thread.sleep((int) (1000 * seconds));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


}
