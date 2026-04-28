package adris.altoclef.multiversion;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 多版本兼容性模式注解
 * 
 * 此注解用于标记那些在不同 Minecraft 版本中具有不同实现的方法。
 * 它与 ReplayMod 预处理器配合使用，允许同一方法根据 Minecraft 版本条件编译不同的代码块。
 * 注解本身在编译后会被丢弃（SOURCE 级别保留），仅用于源代码级别的处理。
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD)
public @interface Pattern {
}
