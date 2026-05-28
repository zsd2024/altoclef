package adris.altoclef.util;

import java.util.Objects;

/**
 * 配对工具类
 * 用于存储和操作一对值（左值和右值）
 * 
 * @param <L> 左值的类型
 * @param <R> 右值的类型
 */
public class Pair<L, R> {
    /** 左值 */
    private L left;
    /** 右值 */
    private R right;

    /**
     * 构造函数
     * @param left 左值
     * @param right 右值
     */
    public Pair(L left, R right) {
        this.left = left;
        this.right = right;
    }

    /**
     * 获取左值
     * @return 左值
     */
    public L getLeft() {
        return this.left;
    }

    /**
     * 获取右值
     * @return 右值
     */
    public R getRight() {
        return this.right;
    }

    /**
     * 设置左值
     * @param value 新的左值
     */
    public void setLeft(L value) {
        this.left = value;
    }

    /**
     * 设置右值
     * @param value 新的右值
     */
    public void setRight(R value) {
        this.right = value;
    }

    @Override
    public String toString() {
        return "Pair{" +
                "left=" + left +
                ", right=" + right +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Pair<?, ?> pair)) return false;
        return Objects.equals(left, pair.left) && Objects.equals(right, pair.right);
    }

    @Override
    public int hashCode() {
        return Objects.hash(left, right);
    }
}
