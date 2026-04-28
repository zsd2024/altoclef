package adris.altoclef.eventbus.events;

import net.minecraft.client.gui.screen.Screen;

/**
 * 屏幕打开事件
 * 当游戏界面屏幕被打开或即将打开时触发此事件
 */
public class ScreenOpenEvent {
    /** 被打开的屏幕对象 */
    public Screen screen;
    /** 是否在屏幕实际打开之前触发（true表示预打开，false表示已打开） */
    public boolean preOpen;

    /**
     * 构造函数
     * @param screen 被打开的屏幕对象
     * @param preOpen 是否在屏幕实际打开之前触发
     */
    public ScreenOpenEvent(Screen screen, boolean preOpen) {
        this.screen = screen;
        this.preOpen = preOpen;
    }
}
