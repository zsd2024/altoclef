package adris.altoclef.util.helpers;

import net.minecraft.util.math.Vec3d;

/**
 * 数学帮助器 - 数学工具类
 * <p>
 * 我不是英国人。我发誓。我只是不想与Minecraft的`MathHelper`冲突。
 */
public interface MathsHelper {

    static Vec3d project(Vec3d vec, Vec3d onto, boolean assumeOntoNormalized) {
        if (!assumeOntoNormalized) {
            onto = onto.normalize();
        }
        return onto.multiply(vec.dotProduct(onto));
    }

    static Vec3d project(Vec3d vec, Vec3d onto) {
        return project(vec, onto, false);
    }

    static Vec3d projectOntoPlane(Vec3d vec, Vec3d normal, boolean assumeNormalNormalized) {
        Vec3d p = project(vec, normal, assumeNormalNormalized);
        return vec.subtract(p);
    }

    static Vec3d projectOntoPlane(Vec3d vec, Vec3d normal) {
        return projectOntoPlane(vec, normal, false);
    }
}
