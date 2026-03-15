package adris.altoclef;


import adris.altoclef.butler.Butler;
import adris.altoclef.chains.*;
import adris.altoclef.trackers.BlockScanner;
import adris.altoclef.commandsystem.CommandExecutor;
import adris.altoclef.commandsystem.TabCompleter;
import adris.altoclef.control.InputControls;
import adris.altoclef.control.PlayerExtraController;
import adris.altoclef.control.SlotHandler;
import adris.altoclef.eventbus.EventBus;
import adris.altoclef.eventbus.events.ClientRenderEvent;
import adris.altoclef.eventbus.events.ClientTickEvent;
import adris.altoclef.eventbus.events.SendChatEvent;
import adris.altoclef.eventbus.events.TitleScreenEntryEvent;
import adris.altoclef.multiversion.DrawContextWrapper;
import adris.altoclef.multiversion.RenderLayerVer;
import adris.altoclef.multiversion.versionedfields.Blocks;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.tasksystem.TaskRunner;
import adris.altoclef.trackers.*;
import adris.altoclef.trackers.storage.ContainerSubTracker;
import adris.altoclef.trackers.storage.ItemStorageTracker;
import adris.altoclef.ui.AltoClefTickChart;
import adris.altoclef.ui.CommandStatusOverlay;
import adris.altoclef.ui.MessagePriority;
import adris.altoclef.ui.MessageSender;
import adris.altoclef.util.helpers.InputHelper;
import adris.altoclef.util.helpers.StorageHelper;
import baritone.Baritone;
import baritone.altoclef.AltoClefSettings;
import baritone.api.BaritoneAPI;
import baritone.api.Settings;
import net.fabricmc.api.ModInitializer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import org.lwjgl.glfw.GLFW;

import java.util.*;
import java.util.function.Consumer;

/**
 * AltoClef 中心访问点 - 主要入口点和核心控制器
 */
public class AltoClef implements ModInitializer {

    // 静态访问 altoclef 的队列
    private static final Queue<Consumer<AltoClef>> _postInitQueue = new ArrayDeque<>();

    // 中心管理器
    private static CommandExecutor commandExecutor;  // 命令执行器
    private TaskRunner taskRunner;                   // 任务运行器
    private TrackerManager trackerManager;           // 跟踪器管理器
    private BotBehaviour botBehaviour;               // 机器人行为控制器
    private PlayerExtraController extraController;   // 玩家额外控制器
    // 任务链
    private UserTaskChain userTaskChain;             // 用户任务链
    private FoodChain foodChain;                     // 食物链
    private MobDefenseChain mobDefenseChain;         // 怪物防御链
    private MLGBucketFallChain mlgBucketChain;       // MLG水桶链
    // 跟踪器
    private ItemStorageTracker storageTracker;       // 物品存储跟踪器
    private ContainerSubTracker containerSubTracker; // 容器子跟踪器
    private EntityTracker entityTracker;             // 实体跟踪器
    private BlockScanner blockScanner;               // 区块扫描器
    private SimpleChunkTracker chunkTracker;         // 简单区块跟踪器
    private MiscBlockTracker miscBlockTracker;       // 杂项区块跟踪器
    private CraftingRecipeTracker craftingRecipeTracker; // 合成配方跟踪器
    // 渲染器
    private CommandStatusOverlay commandStatusOverlay;     // 命令状态覆盖层
    private AltoClefTickChart altoClefTickChart;         // AltoClef Tick图表
    // 设置
    private adris.altoclef.Settings settings;            // 设置
    // 杂项管理器/输入
    private MessageSender messageSender;                 // 消息发送器
    private InputControls inputControls;                 // 输入控制器
    private SlotHandler slotHandler;                     // 槽位处理器
    // 管家
    private Butler butler;                               // 管家功能
    // 暂停
    private boolean paused = false;                      // 是否暂停
    private Task storedTask;                             // 存储暂停时的任务

    private static AltoClef instance;                    // 单例实例

    // 是否在游戏中（在服务器/世界中游玩）
    public static boolean inGame() {
        return MinecraftClient.getInstance().player != null && MinecraftClient.getInstance().getNetworkHandler() != null;
    }

    /**
     * 执行命令（例如 `@get`/`@gamer`）
     */
    public static CommandExecutor getCommandExecutor() {
        return commandExecutor;
    }

    @Override
    public void onInitialize() {
        // 此代码在Minecraft处于mod加载就绪状态时立即运行
        // 但是，某些内容（如资源）可能仍未初始化
        // 因此，这里只会进行基本初始化，不会加载任何内容
        EventBus.subscribe(TitleScreenEntryEvent.class, evt -> onInitializeLoad());

        if (instance != null) {
            throw new IllegalStateException("AltoClef 已经加载！");
        }
        instance = this;
    }

    public void onInitializeLoad() {
        // 此代码应当在Minecraft加载完其他所有内容后运行
        // 这是实际的启动点，由mixin控制

        initializeBaritoneSettings();

        // 中心管理器
        commandExecutor = new CommandExecutor(this);
        taskRunner = new TaskRunner(this);
        trackerManager = new TrackerManager(this);
        botBehaviour = new BotBehaviour(this);
        extraController = new PlayerExtraController(this);

        // 任务链
        userTaskChain = new UserTaskChain(taskRunner);
        mobDefenseChain = new MobDefenseChain(taskRunner);
        new DeathMenuChain(taskRunner);
        new PlayerInteractionFixChain(taskRunner);
        mlgBucketChain = new MLGBucketFallChain(taskRunner);
        new UnstuckChain(taskRunner);
        new PreEquipItemChain(taskRunner);
        new WorldSurvivalChain(taskRunner);
        foodChain = new FoodChain(taskRunner);

        // 跟踪器
        storageTracker = new ItemStorageTracker(this, trackerManager, container -> containerSubTracker = container);
        entityTracker = new EntityTracker(trackerManager);
        blockScanner = new BlockScanner(this);
        chunkTracker = new SimpleChunkTracker(this);
        miscBlockTracker = new MiscBlockTracker(this);
        craftingRecipeTracker = new CraftingRecipeTracker(trackerManager);

        // 渲染器
        commandStatusOverlay = new CommandStatusOverlay();
        altoClefTickChart = new AltoClefTickChart(MinecraftClient.getInstance().textRenderer);

        // 杂项管理器
        messageSender = new MessageSender();
        inputControls = new InputControls();
        slotHandler = new SlotHandler(this);

        butler = new Butler(this);

        initializeCommands();

        // 加载设置
        adris.altoclef.Settings.load(newSettings -> {
            settings = newSettings;
            // Baritone的`acceptableThrowawayItems`应该与我们自己的匹配
            List<Item> baritoneCanPlace = Arrays.stream(settings.getThrowawayItems(true))
                    .filter(item -> item != Items.SOUL_SAND && item != Items.MAGMA_BLOCK && item != Items.SAND && item
                            != Items.GRAVEL).toList();
            getClientBaritoneSettings().acceptableThrowawayItems.value.addAll(baritoneCanPlace);
            // 如果我们应该运行空闲命令...
            if ((!getUserTaskChain().isActive() || getUserTaskChain().isRunningIdleTask()) && getModSettings().shouldRunIdleCommandWhenNotActive()) {
                getUserTaskChain().signalNextTaskToBeIdleTask();
                getCommandExecutor().executeWithPrefix(getModSettings().getIdleCommand());
            }
            // 不要在我们明确保护的位置破坏或放置方块
            getExtraBaritoneSettings().avoidBlockBreak(blockPos -> settings.isPositionExplicitlyProtected(blockPos));
            getExtraBaritoneSettings().avoidBlockPlace(blockPos -> settings.isPositionExplicitlyProtected(blockPos));
            getExtraBaritoneSettings().getForceSaveToolPredicates().add((state, item) -> StorageHelper.shouldSaveStack(this, state.getBlock(), item));
        });

        // 接收+取消聊天
        EventBus.subscribe(SendChatEvent.class, evt -> {
            String line = evt.message;
            if (getCommandExecutor().isClientCommand(line)) {
                evt.cancel();
                getCommandExecutor().execute(line);
            }
        });

        // 与客户端同步Tick
        EventBus.subscribe(ClientTickEvent.class, evt -> {
            long nanos = System.nanoTime();
            onClientTick();
            altoClefTickChart.pushTickNanos(System.nanoTime()-nanos);
        });

        // 渲染
        EventBus.subscribe(ClientRenderEvent.class, evt -> onClientRenderOverlay(evt.context));

        // 测试场
        Playground.IDLE_TEST_INIT_FUNCTION(this);

        // 任务
        TaskCatalogue.init();

        getClientBaritone().getGameEventHandler().registerEventListener(new TabCompleter());

        // 外部mod初始化
        runEnqueuedPostInits();
    }

    // 客户端Tick
    private void onClientTick() {
        runEnqueuedPostInits();

        inputControls.onTickPre();

        // 取消快捷键
        if (InputHelper.isKeyPressed(GLFW.GLFW_KEY_LEFT_CONTROL) && InputHelper.isKeyPressed(GLFW.GLFW_KEY_K)) {
            stopTasks();
        }

        // TODO: 这应该放在这里吗？
        storageTracker.setDirty();
        containerSubTracker.onServerTick();
        miscBlockTracker.tick();
        trackerManager.tick();
        blockScanner.tick();
        taskRunner.tick();

        messageSender.tick();

        inputControls.onTickPost();
    }

    public void stopTasks() {
        if (userTaskChain != null) {
            userTaskChain.cancel(this);
        }
        if (taskRunner.getCurrentTaskChain() != null) {
            taskRunner.getCurrentTaskChain().stop();
        }
        commandStatusOverlay.resetTimer();
    }

    /// GETTERS AND SETTERS

    private void onClientRenderOverlay(DrawContextWrapper context) {
        context.setRenderLayer(RenderLayerVer.getGuiOverlay());
        if (settings.shouldShowTaskChain()) {
            commandStatusOverlay.render(this, context);
        }

        if (settings.shouldShowDebugTickMs()) {
            altoClefTickChart.render(this, context, 1, context.getScaledWindowWidth() / 2 - 124);
        }
    }

    private void initializeBaritoneSettings() {
        getExtraBaritoneSettings().canWalkOnEndPortal(false);
        getClientBaritoneSettings().freeLook.value = false;
        getClientBaritoneSettings().overshootTraverse.value = false;
        getClientBaritoneSettings().allowOvershootDiagonalDescend.value = true;
        getClientBaritoneSettings().allowInventory.value = true;
        getClientBaritoneSettings().allowParkour.value = false;
        getClientBaritoneSettings().allowParkourAscend.value = false;
        getClientBaritoneSettings().allowParkourPlace.value = false;
        getClientBaritoneSettings().allowDiagonalDescend.value = false;
        getClientBaritoneSettings().allowDiagonalAscend.value = false;
        getClientBaritoneSettings().blocksToAvoid.value = new LinkedList<>(List.of(Blocks.FLOWERING_AZALEA, Blocks.AZALEA,
                Blocks.POWDER_SNOW, Blocks.BIG_DRIPLEAF, Blocks.BIG_DRIPLEAF_STEM, Blocks.CAVE_VINES,
                Blocks.CAVE_VINES_PLANT, Blocks.TWISTING_VINES, Blocks.TWISTING_VINES_PLANT, Blocks.SWEET_BERRY_BUSH,
                Blocks.WARPED_ROOTS, Blocks.VINE, Blocks.SHORT_GRASS, Blocks.FERN, Blocks.TALL_GRASS, Blocks.LARGE_FERN,
                Blocks.SMALL_AMETHYST_BUD, Blocks.MEDIUM_AMETHYST_BUD, Blocks.LARGE_AMETHYST_BUD,
                Blocks.AMETHYST_CLUSTER, Blocks.SCULK, Blocks.SCULK_VEIN));

        // 不要尝试破坏下界传送门方块
        getClientBaritoneSettings().blocksToAvoidBreaking.value.add(Blocks.NETHER_PORTAL);
        getClientBaritoneSettings().blocksToDisallowBreaking.value.add(Blocks.NETHER_PORTAL);

        // 让baritone移动物品到快捷栏来使用它们
        // 减少一些远距离渲染以节省FPS
        getClientBaritoneSettings().fadePath.value = true;
        // 不要让baritone扫描掉落物品，我们自己处理
        getClientBaritoneSettings().mineScanDroppedItems.value = false;
        // 不要让baritone等待掉落物，我们自己处理
        getClientBaritoneSettings().mineDropLoiterDurationMSThanksLouca.value = 0L;

        // 水桶放置将由我们专门处理
        getExtraBaritoneSettings().configurePlaceBucketButDontFall(true);

        // 为了渲染平滑
        getClientBaritoneSettings().randomLooking.value = 0.0;
        getClientBaritoneSettings().randomLooking113.value = 0.0;

        // 给baritone更多时间来计算路径。有时候它们可能非常远
        // 原值: 2000L
        getClientBaritoneSettings().failureTimeoutMS.reset();
        // 原值: 5000L
        getClientBaritoneSettings().planAheadFailureTimeoutMS.reset();
        // 原值 100
        getClientBaritoneSettings().movementTimeoutTicks.reset();
    }

    // 在此处列出所有命令源
    private void initializeCommands() {
        try {
            // 这里创建命令。如果需要更多命令，可以自由初始化新的命令列表
            AltoClefCommands.init();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // TODO 重构代码库以使用此方法，而不是到处传递参数
    /**
     * @return 此类的实例，如果尚未初始化则返回null
     */
    public static AltoClef getInstance() {
        return instance;
    }

    /**
     * 运行最高优先级的任务链
     * (任务链运行任务树)
     */
    public TaskRunner getTaskRunner() {
        return taskRunner;
    }

    /**
     * 用户任务链 (运行您的命令。例如：获取钻石，通关游戏)
     */
    public UserTaskChain getUserTaskChain() {
        return userTaskChain;
    }

    /**
     * 控制机器人行为，比如是否临时"保护"某些方块或物品
     */
    public BotBehaviour getBehaviour() {
        return botBehaviour;
    }

    /**
     * 控制任务，用于暂停和恢复机器人
     */
    public boolean isPaused() {
        return paused;
    }

    public void setPaused(boolean pausing) {
        this.paused = pausing;
    }

    /**
     * 存储暂停前正在进行的任务
     */
    public void setStoredTask(Task currentTask) {
        this.storedTask = currentTask;
    }

    /**
     * 获取暂停前正在进行的任务
     */
    public Task getStoredTask() {
        return storedTask;
    }

    /**
     * 跟踪物品在你的背包和存储容器中的状态
     */
    public ItemStorageTracker getItemStorage() {
        return storageTracker;
    }

    /**
     * 跟踪已加载的实体
     */
    public EntityTracker getEntityTracker() {
        return entityTracker;
    }

    /**
     * 管理所有可用配方的列表
     */
    public CraftingRecipeTracker getCraftingRecipeTracker() {
        return craftingRecipeTracker;
    }

    /**
     * 跟踪方块及其位置 - BlockTracker的更好版本
     */
    public BlockScanner getBlockScanner() {
        return blockScanner;
    }

    /**
     * 跟踪区块是否已加载/可见
     */
    public SimpleChunkTracker getChunkTracker() {
        return chunkTracker;
    }

    /**
     * 跟踪随机区块事物，比如我们上次使用的下界传送门
     */
    public MiscBlockTracker getMiscBlockTracker() {
        return miscBlockTracker;
    }

    /**
     * Baritone访问 (实际上可以是静态的)
     */
    public Baritone getClientBaritone() {
        if (getPlayer() == null) {
            return (Baritone) BaritoneAPI.getProvider().getPrimaryBaritone();
        }
        return (Baritone) BaritoneAPI.getProvider().getBaritoneForPlayer(getPlayer());
    }

    /**
     * Baritone设置访问 (实际上可以是静态的)
     */
    public Settings getClientBaritoneSettings() {
        return Baritone.settings();
    }

    /**
     * AltoClef专用的Baritone设置 (实际上可以是静态的)
     */
    public AltoClefSettings getExtraBaritoneSettings() {
        return AltoClefSettings.getInstance();
    }

    /**
     * AltoClef设置
     */
    public adris.altoclef.Settings getModSettings() {
        return settings;
    }

    /**
     * 管家控制器。跟踪用户并让您接收用户消息
     */
    public Butler getButler() {
        return butler;
    }

    /**
     * 发送聊天消息 (避免自动踢出)
     */
    public MessageSender getMessageSender() {
        return messageSender;
    }

    /**
     * 执行背包/容器槽位操作
     */
    public SlotHandler getSlotHandler() {
        return slotHandler;
    }

    /**
     * Minecraft玩家客户端访问 (实际上可以是静态的)
     */
    public ClientPlayerEntity getPlayer() {
        return MinecraftClient.getInstance().player;
    }

    /**
     * Minecraft世界访问 (实际上可以是静态的)
     */
    public ClientWorld getWorld() {
        return MinecraftClient.getInstance().world;
    }

    /**
     * Minecraft客户端交互控制器访问 (实际上可以是静态的)
     */
    public ClientPlayerInteractionManager getController() {
        return MinecraftClient.getInstance().interactionManager;
    }

    /**
     * ClientPlayerInteractionManager中不存在的额外控制。这真的应该设为静态或与某些其他组件合并。
     */
    public PlayerExtraController getControllerExtras() {
        return extraController;
    }

    /**
     * 手动控制输入操作 (例如：跳跃，攻击)
     */
    public InputControls getInputControls() {
        return inputControls;
    }

    /**
     * 运行用户任务
     */
    public void runUserTask(Task task) {
        runUserTask(task, () -> {
        });
    }

    /**
     * 运行用户任务
     */
    public void runUserTask(Task task, Runnable onFinish) {
        userTaskChain.runTask(this, task, onFinish);
    }

    /**
     * 取消当前运行的用户任务
     */
    public void cancelUserTask() {
        userTaskChain.cancel(this);
    }

    /**
     * 接管控制权以进食
     */
    public FoodChain getFoodChain() {
        return foodChain;
    }

    /**
     * 接管控制权以防御怪物
     */
    public MobDefenseChain getMobDefenseChain() {
        return mobDefenseChain;
    }

    /**
     * 接管控制权以执行水桶自救
     */
    public MLGBucketFallChain getMLGBucketChain() {
        return mlgBucketChain;
    }

    public void log(String message) {
        log(message, MessagePriority.TIMELY);
    }

    /**
     * 记录到控制台，并向使用机器人作为管家的任何玩家发送消息
     */
    public void log(String message, MessagePriority priority) {
        Debug.logMessage(message);
    }

    public void logWarning(String message) {
        logWarning(message, MessagePriority.TIMELY);
    }

    /**
     * 记录警告到控制台，并向使用机器人作为管家的任何玩家发出警报
     */
    public void logWarning(String message, MessagePriority priority) {
        Debug.logWarning(message);
    }

    private void runEnqueuedPostInits() {
        synchronized (_postInitQueue) {
            while (!_postInitQueue.isEmpty()) {
                _postInitQueue.poll().accept(this);
            }
        }
    }

}
