package adris.altoclef.util.helpers;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.function.Function;

/**
 * 扩展Java标准模板库(STL)的实用函数集合
 */
public interface StlHelper {
    /**
     * 根据获取Double值的函数创建比较器
     *
     * @param getValue 获取比较值的函数
     * @return 比较器实例
     */
    static <T> Comparator<T> compareValues(Function<T, Double> getValue) {
        return (left, right) -> (int) Math.signum(getValue.apply(left) - getValue.apply(right));
    }

    /**
     * 将集合转换为字符串表示形式
     *
     * @param thing 要转换的集合
     * @param toStringFunc 元素转字符串的函数
     * @return 集合的字符串表示，格式为"[元素1,元素2,...]"
     */
    static <T> String toString(Collection<T> thing, Function<T, String> toStringFunc) {
        StringBuilder result = new StringBuilder();
        result.append("[");
        int i = 0;
        for (T item : thing) {
            result.append(toStringFunc.apply(item));
            if (i != thing.size() - 1) {
                result.append(",");
            }
            ++i;
        }
        result.append("]");
        return result.toString();
    }

    /**
     * 将数组转换为字符串表示形式
     *
     * @param thing 要转换的数组
     * @param toStringFunc 元素转字符串的函数
     * @return 数组的字符串表示，格式为"[元素1,元素2,...]"，如果数组为null则返回"<null>"
     */
    static <T> String toString(T[] thing, Function<T, String> toStringFunc) {
        try {
            return toString(Arrays.asList(thing), toStringFunc);
        } catch (NullPointerException ignored) {
            return "<null>";
        }
    }
}