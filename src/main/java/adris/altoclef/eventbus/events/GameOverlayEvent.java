package adris.altoclef.eventbus.events;

/**
 * 游戏覆盖层事件
 * 当游戏需要在屏幕上显示覆盖消息时触发此事件
 */
public class GameOverlayEvent {
    /** 要显示的消息内容 */
    public String message;

    /**
     * 构造函数
     * @param message 要显示的消息内容
     */
    public GameOverlayEvent(String message) {
        this.message = message;
    }
}
