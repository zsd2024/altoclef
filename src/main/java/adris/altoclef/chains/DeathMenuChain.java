package adris.altoclef.chains;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.mixins.DeathScreenAccessor;
import adris.altoclef.multiversion.ConnectScreenVer;
import adris.altoclef.multiversion.entity.PlayerVer;
import adris.altoclef.tasksystem.TaskChain;
import adris.altoclef.tasksystem.TaskRunner;
import adris.altoclef.util.time.TimerGame;
import adris.altoclef.util.time.TimerReal;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.*;
import net.minecraft.client.gui.screen.multiplayer.*;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.text.Text;

/**
 * 死亡菜单处理链 - 用于处理玩家死亡后的自动重生和重新连接逻辑
 * 此链负责监听死亡屏幕、执行自动重生、处理断线重连等操作
 */

public class DeathMenuChain extends TaskChain {

    // 有时我们可能会出错，所以可能需要重试处理死亡屏幕
    private final TimerReal deathRetryTimer = new TimerReal(8); // 重试死亡屏幕处理的定时器
    private final TimerGame reconnectTimer = new TimerGame(1); // 重新连接的定时器
    private final TimerGame waitOnDeathScreenBeforeRespawnTimer = new TimerGame(2); // 在死亡屏幕等待后重生的定时器
    private ServerInfo prevServerEntry = null; // 保存之前的服务器信息，用于重连
    private boolean reconnecting = false; // 标记是否正在重连
    private int deathCount = 0; // 死亡计数
    private Class<? extends Screen> prevScreen = null; // 保存前一个屏幕类型


    public DeathMenuChain(TaskRunner runner) {
        super(runner);
    }

    /**
     * 检查是否应该自动重生
     * @return 如果启用自动重生则返回true
     */
    private boolean shouldAutoRespawn() {
        return AltoClef.getInstance().getModSettings().isAutoRespawn();
    }

    /**
     * 检查是否应该自动重连
     * @return 如果启用自动重连则返回true
     */
    private boolean shouldAutoReconnect() {
        return AltoClef.getInstance().getModSettings().isAutoReconnect();
    }

    @Override
    protected void onStop() {
        // 当链停止时执行清理操作
    }

    @Override
    public void onInterrupt(TaskChain other) {
        // 当链被其他链中断时调用
    }

    @Override
    protected void onTick() {
        // 每个游戏刻度的处理逻辑在getPriority()中实现
    }

    @Override
    public float getPriority() {
        //MinecraftClient.getInstance().getCurrentServerEntry().address;
//        MinecraftClient.getInstance().
        Screen screen = MinecraftClient.getInstance().currentScreen;

        // 这可能修复只发生过一次的奇怪重生失败问题
        if (prevScreen == DeathScreen.class) {
            if (deathRetryTimer.elapsed()) {
                Debug.logMessage("(重生重试修复...)");
                deathRetryTimer.reset();
                prevScreen = null;
            }
        } else {
            deathRetryTimer.reset();
        }
        // 记录我们上次所在的服务器，以便重新连接
        if (AltoClef.inGame()) {
            prevServerEntry = MinecraftClient.getInstance().getCurrentServerEntry();
        }

        if (screen instanceof DeathScreen) {
            AltoClef mod = AltoClef.getInstance();

            if (waitOnDeathScreenBeforeRespawnTimer.elapsed()) {
                waitOnDeathScreenBeforeRespawnTimer.reset();
                if (shouldAutoRespawn()) {
                    deathCount++;
                    Debug.logMessage("正在重生... (这是第" + deathCount + "次死亡)");
                    assert MinecraftClient.getInstance().player != null;
                    Text screenMessage = ((DeathScreenAccessor) screen).getMessage();
                    String deathMessage = screenMessage != null ? screenMessage.getString() : "Unknown"; //"(not implemented yet)"; //screen.children().toString();
                    MinecraftClient.getInstance().player.requestRespawn();
                    MinecraftClient.getInstance().setScreen(null);
                    for (String i : mod.getModSettings().getDeathCommand().split(" & ")) {
                        String command = i.replace("{deathmessage}", deathMessage);
                        String prefix = mod.getModSettings().getCommandPrefix();
                        while (MinecraftClient.getInstance().player.isAlive()) ;
                        if (!command.isEmpty()) {
                            if (command.startsWith(prefix)) {
                                AltoClef.getCommandExecutor().execute(command, () -> {
                                }, Throwable::printStackTrace);
                            } else if (command.startsWith("/")) {
                                PlayerVer.sendChatCommand(MinecraftClient.getInstance().player, command.substring(1));
                            } else {
                                PlayerVer.sendChatMessage(MinecraftClient.getInstance().player, command);
                            }
                        }
                    }
                } else {
                    // 如果死亡且不自动重生，则取消用户任务
                    mod.cancelUserTask();
                }
            }
        } else {
            if (AltoClef.inGame()) {
                waitOnDeathScreenBeforeRespawnTimer.reset();
            }
            if (screen instanceof DisconnectedScreen) {
                if (shouldAutoReconnect()) {
                    Debug.logMessage("正在重连: 进入多人游戏屏幕");
                    reconnecting = true;
                    MinecraftClient.getInstance().setScreen(new MultiplayerScreen(new TitleScreen()));
                } else {
                    // 如果断开连接且不自动重连，则取消用户任务
                    AltoClef.getInstance().cancelUserTask();
                }
            } else if (screen instanceof MultiplayerScreen && reconnecting && reconnectTimer.elapsed()) {
                reconnectTimer.reset();
                Debug.logMessage("正在重连: 连接中 ");
                reconnecting = false;

                if (prevServerEntry == null) {
                    Debug.logWarning("重连服务器失败，未缓存服务器条目。");
                } else {
                    MinecraftClient client = MinecraftClient.getInstance();
                    ConnectScreenVer.connect(screen, client, ServerAddress.parse(prevServerEntry.address), prevServerEntry, false);
                    //ConnectScreen.connect(screen, client, ServerAddress.parse(_prevServerEntry.address), _prevServerEntry);
                    //client.setScreen(new ConnectScreen(screen, client, _prevServerEntry));
                }
            }
        }
        if (screen != null)
            prevScreen = screen.getClass();
        return Float.NEGATIVE_INFINITY;
    }

    @Override
    public boolean isActive() {
        return true;
    }

    @Override
    public String getName() {
        return "死亡菜单重生处理";
    }
}
