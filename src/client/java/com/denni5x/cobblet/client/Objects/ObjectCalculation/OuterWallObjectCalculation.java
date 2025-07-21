package com.denni5x.cobblet.client.Objects.ObjectCalculation;

import com.denni5x.cobblet.client.Objects.Theme.ThemeConfig;
import com.moulberry.axiom.render.regions.ChunkedBlockRegion;
import net.minecraft.block.BlockState;

import java.util.Objects;

public class OuterWallObjectCalculation {

    public static void outerWalls(ThemeConfig themeConfig, ChunkedBlockRegion chunkedBlockRegion,
                                  int minY, int maxY, int minX, int maxX, int minZ, int maxZ,
                                  int[] offsetX, int[] offsetZ, int[] size, int[] size1R) {
        if (themeConfig.fullWallPattern() == null) {
            return;
        }
        ChunkedBlockRegion copyFrom = themeConfig.fullWallPattern().transformedBlockRegions().getBlocks(0, false, false);
        ChunkedBlockRegion copyFrom1R = themeConfig.fullWallPattern().transformedBlockRegions().getBlocks(1, false, false);

        int minXCB = Objects.requireNonNull(copyFrom.min()).getX();
        int minYCB = Objects.requireNonNull(copyFrom.min()).getY();
        int minZCB = Objects.requireNonNull(copyFrom.min()).getZ();
        int maxXCB = Objects.requireNonNull(copyFrom.max()).getX();
        int maxYCB = Objects.requireNonNull(copyFrom.max()).getY();
        int maxZCB = Objects.requireNonNull(copyFrom.max()).getZ();
        size[0] = maxXCB - minXCB + 1;
        size[1] = maxYCB - minYCB + 1;
        size[2] = maxZCB - minZCB + 1;

        int minXCB1R = Objects.requireNonNull(copyFrom1R.min()).getX();
        int minYCB1R = Objects.requireNonNull(copyFrom1R.min()).getY();
        int minZCB1R = Objects.requireNonNull(copyFrom1R.min()).getZ();
        int maxXCB1R = Objects.requireNonNull(copyFrom1R.max()).getX();
        int maxYCB1R = Objects.requireNonNull(copyFrom1R.max()).getY();
        int maxZCB1R = Objects.requireNonNull(copyFrom1R.max()).getZ();
        size1R[0] = maxXCB1R - minXCB1R + 1;
        size1R[1] = maxYCB1R - minYCB1R + 1;
        size1R[2] = maxZCB1R - minZCB1R + 1;

        for (int y = minY; y <= maxY; y++) {

            for (int x = minX; x <= maxX; x++) {
                int modX = ObjectCalculationUtils.getPatternMod(x, minXCB, size[0]);
                int modY = ObjectCalculationUtils.getPatternMod(y, minYCB, size[1]);
                int modMinZ = ObjectCalculationUtils.getPatternMod(minZ, minZCB, size[2]);
                int modMaxZ = ObjectCalculationUtils.getPatternMod(maxZ, minZCB, size[2]);

                int copyFromX = ObjectCalculationUtils.getCopyFromCoords(minXCB, modX, offsetX[0], size[0]);
                int copyFromY = ObjectCalculationUtils.getCopyFromCoords(minYCB, modY, offsetX[1], size[1]);
                int copyFromZMin = ObjectCalculationUtils.getCopyFromCoords(minZCB, modMinZ, offsetX[2], size[2]);
                int copyFromZMax = ObjectCalculationUtils.getCopyFromCoords(minZCB, modMaxZ, offsetX[2], size[2]);

                BlockState blockStateMinZ = copyFrom.getBlockStateOrAir(copyFromX, copyFromY, copyFromZMin);
                if (!blockStateMinZ.isAir()) {
                    chunkedBlockRegion.addBlock(x, y, minZ, blockStateMinZ);
                }
                BlockState blockStateMaxZ = copyFrom.getBlockStateOrAir(copyFromX, copyFromY, copyFromZMax);
                if (!blockStateMaxZ.isAir()) {
                    chunkedBlockRegion.addBlock(x, y, maxZ, blockStateMaxZ);
                }
            }

            for (int z = minZ; z <= maxZ; z++) {
                int modMinX = ObjectCalculationUtils.getPatternMod(minX, minXCB1R, size1R[0]);
                int modMaxX = ObjectCalculationUtils.getPatternMod(maxX, minXCB1R, size1R[0]);
                int modY = ObjectCalculationUtils.getPatternMod(y, minYCB1R, size1R[1]);
                int modZ = ObjectCalculationUtils.getPatternMod(z, minZCB1R, size1R[2]);

                int copyFromXMin = ObjectCalculationUtils.getCopyFromCoords(minXCB1R, modMinX, offsetZ[2], size1R[0]);
                int copyFromXMax = ObjectCalculationUtils.getCopyFromCoords(minXCB1R, modMaxX, offsetZ[2], size1R[0]);
                int copyFromY = ObjectCalculationUtils.getCopyFromCoords(minYCB1R, modY, offsetZ[1], size1R[1]);
                int copyFromZ = ObjectCalculationUtils.getCopyFromCoords(minZCB1R, modZ, offsetZ[0], size1R[2]);

                BlockState blockStateMinZ = copyFrom1R.getBlockStateOrAir(copyFromXMin, copyFromY, copyFromZ);
                if (!blockStateMinZ.isAir()) {
                    chunkedBlockRegion.addBlock(minX, y, z, blockStateMinZ);
                }
                BlockState blockStateMaxZ = copyFrom1R.getBlockStateOrAir(copyFromXMax, copyFromY, copyFromZ);
                if (!blockStateMaxZ.isAir()) {
                    chunkedBlockRegion.addBlock(maxX, y, z, blockStateMaxZ);
                }
            }
            chunkedBlockRegion.addBlock(minX, y, minZ, themeConfig.fullEdge());
            chunkedBlockRegion.addBlock(minX, y, maxZ, themeConfig.fullEdge());
            chunkedBlockRegion.addBlock(maxX, y, minZ, themeConfig.fullEdge());
            chunkedBlockRegion.addBlock(maxX, y, maxZ, themeConfig.fullEdge());
        }
    }
}
