package com.denni5x.cobblet.client.Raterization;

import com.moulberry.axiom.rasterization.CylinderRasterization;
import com.moulberry.axiom.rasterization.RasterizationHelper;
import com.moulberry.axiom.render.regions.ChunkedBlockRegion;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class TubeRasterization {
    private static final Quaternionf EMPTY_QUATERNION = new Quaternionf();
    private static final int[] CARDINAL_OFFSETS = new int[]{0, 0, -1, 0, 0, 1, 0, -1, 0, 0, 1, 0, -1, 0, 0, 1, 0, 0};

    public static void tube(ChunkedBlockRegion region, BlockState block, BlockPos center, int diameterX, int height, int diameterZ, boolean hollow, int thickness, @Nullable Quaternionf quaternionf) {
        if (thickness * 2 < diameterX && thickness * 2 < diameterZ) {
            if (diameterX > 0 && height > 0 && diameterZ > 0) {
                if (quaternionf == null) {
                    quaternionf = EMPTY_QUATERNION;
                }

                float radiusX = (float) (diameterX - 1) / 2.0F;
                float radiusY = (float) (height - 1) / 2.0F;
                float radiusZ = (float) (diameterZ - 1) / 2.0F;
                int ceilRadiusX = (int) Math.ceil(radiusX);
                int ceilRadiusY = (int) Math.floor(radiusY);
                int ceilRadiusZ = (int) Math.ceil(radiusZ);
                int maxRadiusX = 0;
                int maxRadiusY = 0;
                int maxRadiusZ = 0;
                Vector3f vector3f = new Vector3f();

                for (int x = -1; x <= 1; x += 2) {
                    for (int y = -1; y <= 1; y += 2) {
                        for (int z = -1; z <= 1; z += 2) {
                            vector3f.set((float) (ceilRadiusX * x), (float) (ceilRadiusY * y), (float) (ceilRadiusZ * z));
                            quaternionf.transformInverse(vector3f);
                            maxRadiusX = Math.max(maxRadiusX, (int) Math.ceil(Math.abs(vector3f.x)));
                            maxRadiusY = Math.max(maxRadiusY, (int) Math.ceil(Math.abs(vector3f.y)));
                            maxRadiusZ = Math.max(maxRadiusZ, (int) Math.ceil(Math.abs(vector3f.z)));
                        }
                    }
                }

                float invRadiusSqX = RasterizationHelper.calcInvRadiusSq(radiusX);
                float invRadiusSqZ = RasterizationHelper.calcInvRadiusSq(radiusZ);
                float invInnerRadiusSqX = RasterizationHelper.calcInvRadiusSq(radiusX - (float) thickness);
                float invInnerRadiusSqZ = RasterizationHelper.calcInvRadiusSq(radiusZ - (float) thickness);
                int centerX = center.getX();
                int centerY = center.getY();
                int centerZ = center.getZ();
                float offsetX = -(radiusX % 1.0F);
                float offsetY = -(radiusY % 1.0F);
                float offsetZ = -(radiusZ % 1.0F);

                for (int x = -maxRadiusX; x <= maxRadiusX; ++x) {
                    for (int y = -maxRadiusY; y <= maxRadiusY; ++y) {
                        for (int z = -maxRadiusZ; z <= maxRadiusZ; ++z) {
                            vector3f.set((float) x, (float) y, (float) z);
                            quaternionf.transform(vector3f);
                            float rx = vector3f.x + offsetX;
                            float ry = vector3f.y + offsetY;
                            float rz = vector3f.z + offsetZ;
                            if (rx * rx * invRadiusSqX + rz * rz * invRadiusSqZ <= 1.0F && rx * rx * invInnerRadiusSqX + rz * rz * invInnerRadiusSqZ > 1.0F && Math.abs(ry) <= radiusY + 0.5F) {
                                if (!hollow) {
                                    region.addBlock(x + centerX, y + centerY, z + centerZ, block);
                                } else {
                                    for (int i = 0; i < 18; i += 3) {
                                        int nx = x + CARDINAL_OFFSETS[i];
                                        int ny = y + CARDINAL_OFFSETS[i + 1];
                                        int nz = z + CARDINAL_OFFSETS[i + 2];
                                        vector3f.set((float) nx, (float) ny, (float) nz);
                                        quaternionf.transform(vector3f);
                                        rx = vector3f.x + offsetX;
                                        ry = vector3f.y + offsetY;
                                        rz = vector3f.z + offsetZ;
                                        if (rx * rx * invRadiusSqX + rz * rz * invRadiusSqZ > 1.0F || rx * rx * invInnerRadiusSqX + rz * rz * invInnerRadiusSqZ <= 1.0F || Math.abs(ry) > radiusY + 0.5F) {
                                            region.addBlock(x + centerX, y + centerY, z + centerZ, block);
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

            }
        } else {
            CylinderRasterization.cylinder(region, block, center, diameterX, height, diameterZ, false, quaternionf);
        }
    }
}
