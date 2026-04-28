package adris.altoclef.eventbus.events;

import adris.altoclef.multiversion.DrawContextWrapper;

/**
 * 客户端渲染事件
 * 在Minecraft客户端进行每一帧渲染时触发此事件
 */
public class ClientRenderEvent {
    /** 渲染上下文包装器，用于处理不同Minecraft版本的渲染API差异 */
    public DrawContextWrapper context;
    /** 渲染插值时间增量，用于平滑动画效果 */
    public float tickDelta;

    /**
     * 构造函数
     * @param context 渲染上下文包装器
     * @param tickDelta 渲染插值时间增量
     */
    public ClientRenderEvent(DrawContextWrapper context, float tickDelta) {
        this.context = context;
        this.tickDelta = tickDelta;
    }
}
