package com.denni5x.cobblet.client.Objects.ObjectCalculation;

import com.denni5x.cobblet.client.Objects.Theme.ThemeConfig;
import com.moulberry.axiom.render.regions.ChunkedBlockRegion;
import net.minecraft.block.BlockState;

import java.util.Objects;

public class TopWallObjectCalculation {
    private static final int[] patternOffsetTopWallXAxis = new int[]{0, 0, 0};
    private static final int[] patternOffsetTopWallZAxis = new int[]{0, 0, 0};
    private static final int[] patternSizeTopWall = new int[]{0, 0, 0};
    private static final int[] patternSizeTopWall1R = new int[]{0, 0, 0};
    private static final int[] positionOffsetTopWall = new int[]{0, 0, 0};

    public static ChunkedBlockRegion topWall(ThemeConfig themeConfig, ChunkedBlockRegion chunkedBlockRegion, int minXHouse, int maxXHouse, int maxYHouse, int minZHouse, int maxZHouse) {
        if (themeConfig.topWallPattern() == null) {
            return chunkedBlockRegion;
        }

        int patternCounterX = 0;
        int patternCounterY = 0;
        int patternCounterZ = 0;

        ChunkedBlockRegion copyFrom = themeConfig.topWallPattern().transformedBlockRegions().getBlocks(0, false, false);
        ChunkedBlockRegion copyFrom1R = themeConfig.topWallPattern().transformedBlockRegions().getBlocks(1, true, false);
        ChunkedBlockRegion copyFrom2R = themeConfig.topWallPattern().transformedBlockRegions().getBlocks(2, true, false);
        ChunkedBlockRegion copyFrom3R = themeConfig.topWallPattern().transformedBlockRegions().getBlocks(3, false, false);

        int minXCB = Objects.requireNonNull(copyFrom.min()).getX();
        int minYCB = Objects.requireNonNull(copyFrom.min()).getY();
        int minZCB = Objects.requireNonNull(copyFrom.min()).getZ();
        int maxXCB = Objects.requireNonNull(copyFrom.max()).getX();
        int maxYCB = Objects.requireNonNull(copyFrom.max()).getY();
        int maxZCB = Objects.requireNonNull(copyFrom.max()).getZ();

        int minXCB1R = Objects.requireNonNull(copyFrom1R.min()).getX();
        int minYCB1R = Objects.requireNonNull(copyFrom1R.min()).getY();
        int minZCB1R = Objects.requireNonNull(copyFrom1R.min()).getZ();
        int maxXCB1R = Objects.requireNonNull(copyFrom1R.max()).getX();
        int maxYCB1R = Objects.requireNonNull(copyFrom1R.max()).getY();
        int maxZCB1R = Objects.requireNonNull(copyFrom1R.max()).getZ();

        int minXCB2R = Objects.requireNonNull(copyFrom2R.min()).getX();
        int minYCB2R = Objects.requireNonNull(copyFrom2R.min()).getY();
        int minZCB2R = Objects.requireNonNull(copyFrom2R.min()).getZ();

        int minXCB3R = Objects.requireNonNull(copyFrom3R.min()).getX();
        int minYCB3R = Objects.requireNonNull(copyFrom3R.min()).getY();
        int minZCB3R = Objects.requireNonNull(copyFrom3R.min()).getZ();

        patternSizeTopWall[0] = maxXCB - minXCB + 1;
        patternSizeTopWall[1] = maxYCB - minYCB + 1;
        patternSizeTopWall[2] = maxZCB - minZCB + 1;

        patternSizeTopWall1R[0] = maxXCB1R - minXCB1R + 1;
        patternSizeTopWall1R[1] = maxYCB1R - minYCB1R + 1;
        patternSizeTopWall1R[2] = maxZCB1R - minZCB1R + 1;

        positionOffsetTopWall[0] = themeConfig.topWallPattern().positionOffset()[0];
        positionOffsetTopWall[1] = themeConfig.topWallPattern().positionOffset()[1];
        positionOffsetTopWall[2] = themeConfig.topWallPattern().positionOffset()[2];

        ChunkedBlockRegion copyFromEdge = themeConfig.topWallEdgePattern().transformedBlockRegions().getBlocks(0, false, false);
        int minXEdge = Objects.requireNonNull(copyFromEdge.min()).getX();
        int minZEdge = Objects.requireNonNull(copyFromEdge.min()).getZ();
        int maxXEdge = Objects.requireNonNull(copyFromEdge.max()).getX();
        int maxZEdge = Objects.requireNonNull(copyFromEdge.max()).getZ();

        int edgeSizeX = maxXEdge - minXEdge + 1;
        int edgeSizeZ = maxZEdge - minZEdge + 1;

        int edgeSizeXOffset = (int) Math.ceil((float) edgeSizeX / 2);
        int edgeSizeYOffset = (int) Math.ceil((float) edgeSizeZ / 2);

        int minY = maxYHouse + 1 + positionOffsetTopWall[1];
        int maxY = minY + patternSizeTopWall[1] + positionOffsetTopWall[1];

        for (int y = minY; y <= maxY; y++, patternCounterY++) {
            for (int x = minXHouse + edgeSizeX - edgeSizeXOffset; x <= maxXHouse - edgeSizeX + edgeSizeXOffset; x++, patternCounterX++) {
                int minZ = minZHouse + positionOffsetTopWall[2];
                int maxZ = minZ + patternSizeTopWall[2] - 1;

                for (int z = minZ; z <= maxZ; z++, patternCounterZ++) {
                    int modX = ObjectCalculationUtils.getPatternMod(patternCounterX, minXCB, patternSizeTopWall[0]);
                    int modY = ObjectCalculationUtils.getPatternMod(patternCounterY, minYCB, patternSizeTopWall[1]);
                    int modZ = ObjectCalculationUtils.getPatternMod(patternCounterZ, minZCB, patternSizeTopWall[2]);

                    int copyFromX = ObjectCalculationUtils.getCopyFromCoords(minXCB, modX, patternOffsetTopWallXAxis[0],
                            patternSizeTopWall[0]);
                    int copyFromY = ObjectCalculationUtils.getCopyFromCoords(minYCB, modY, patternOffsetTopWallXAxis[1],
                            patternSizeTopWall[1]);
                    int copyFromZ = ObjectCalculationUtils.getCopyFromCoords(minZCB, modZ, patternOffsetTopWallXAxis[2],
                            patternSizeTopWall[2]);

                    BlockState blockStateMinZ = copyFrom.getBlockStateOrAir(copyFromX, copyFromY, copyFromZ);
                    if (!blockStateMinZ.isAir()) {
                        chunkedBlockRegion.addBlock(x, y, z, blockStateMinZ);
                    }
                }
                patternCounterZ = 0;

                minZ = maxZHouse - positionOffsetTopWall[2] - 1;
                maxZ = minZ + patternSizeTopWall[2] - 1;

                for (int z = minZ; z <= maxZ; z++, patternCounterZ++) {
                    int modX = ObjectCalculationUtils.getPatternMod(patternCounterX, minXCB2R, patternSizeTopWall[0]);
                    int modY = ObjectCalculationUtils.getPatternMod(patternCounterY, minYCB2R, patternSizeTopWall[1]);
                    int modZ = ObjectCalculationUtils.getPatternMod(patternCounterZ, minZCB2R, patternSizeTopWall[2]);

                    int copyFromX = ObjectCalculationUtils.getCopyFromCoords(minXCB2R, modX, patternOffsetTopWallXAxis[0],
                            patternSizeTopWall[0]);
                    int copyFromY = ObjectCalculationUtils.getCopyFromCoords(minYCB2R, modY, patternOffsetTopWallXAxis[1],
                            patternSizeTopWall[1]);
                    int copyFromZ = ObjectCalculationUtils.getCopyFromCoords(minZCB2R, modZ, patternOffsetTopWallXAxis[2],
                            patternSizeTopWall[2]);

                    BlockState blockStateMinZ = copyFrom2R.getBlockStateOrAir(copyFromX, copyFromY, copyFromZ);
                    if (!blockStateMinZ.isAir()) {
                        chunkedBlockRegion.addBlock(x, y, z, blockStateMinZ);
                    }
                }
                patternCounterZ = 0;
            }
            patternCounterX = 0;

            for (int z = minZHouse + edgeSizeZ - edgeSizeYOffset; z <= maxZHouse - edgeSizeZ + edgeSizeYOffset; z++, patternCounterZ++) {
                int minX = minXHouse + positionOffsetTopWall[0];
                int maxX = minX + patternSizeTopWall1R[0] - 1;

                for (int x = minX; x <= maxX; x++, patternCounterX++) {
                    int modX = ObjectCalculationUtils.getPatternMod(patternCounterX, minXCB1R, patternSizeTopWall1R[0]);
                    int modY = ObjectCalculationUtils.getPatternMod(patternCounterY, minYCB1R, patternSizeTopWall1R[1]);
                    int modZ = ObjectCalculationUtils.getPatternMod(patternCounterZ, minZCB1R, patternSizeTopWall1R[2]);

                    int copyFromX = ObjectCalculationUtils.getCopyFromCoords(minXCB1R, modX, patternOffsetTopWallZAxis[2],
                            patternSizeTopWall1R[0]);
                    int copyFromY = ObjectCalculationUtils.getCopyFromCoords(minYCB1R, modY, patternOffsetTopWallZAxis[1],
                            patternSizeTopWall1R[1]);
                    int copyFromZ = ObjectCalculationUtils.getCopyFromCoords(minZCB1R, modZ, patternOffsetTopWallZAxis[0],
                            patternSizeTopWall1R[2]);

                    BlockState blockStateMinZ = copyFrom1R.getBlockStateOrAir(copyFromX, copyFromY, copyFromZ);
                    if (!blockStateMinZ.isAir()) {
                        chunkedBlockRegion.addBlock(x, y, z, blockStateMinZ);
                    }
                }
                patternCounterX = 0;

                minX = maxXHouse - positionOffsetTopWall[0] - 1;
                maxX = minX + patternSizeTopWall1R[0] - 1;

                for (int x = minX; x <= maxX; x++, patternCounterX++) {
                    int modX = ObjectCalculationUtils.getPatternMod(patternCounterX, minXCB3R, patternSizeTopWall1R[0]);
                    int modY = ObjectCalculationUtils.getPatternMod(patternCounterY, minYCB3R, patternSizeTopWall1R[1]);
                    int modZ = ObjectCalculationUtils.getPatternMod(patternCounterZ, minZCB3R, patternSizeTopWall1R[2]);

                    int copyFromX = ObjectCalculationUtils.getCopyFromCoords(minXCB3R, modX, patternOffsetTopWallZAxis[2],
                            patternSizeTopWall1R[0]);
                    int copyFromY = ObjectCalculationUtils.getCopyFromCoords(minYCB3R, modY, patternOffsetTopWallZAxis[1],
                            patternSizeTopWall1R[1]);
                    int copyFromZ = ObjectCalculationUtils.getCopyFromCoords(minZCB3R, modZ, patternOffsetTopWallZAxis[0],
                            patternSizeTopWall1R[2]);

                    BlockState blockStateMinZ = copyFrom3R.getBlockStateOrAir(copyFromX, copyFromY, copyFromZ);
                    if (!blockStateMinZ.isAir()) {
                        chunkedBlockRegion.addBlock(x, y, z, blockStateMinZ);
                    }
                }
                patternCounterX = 0;
            }
            patternCounterZ = 0;
        }

        int positionOffsetEdgeX = themeConfig.topWallEdgePattern().positionOffset()[0];
        int positionOffsetEdgeY = themeConfig.topWallEdgePattern().positionOffset()[1];
        int positionOffsetEdgeZ = themeConfig.topWallEdgePattern().positionOffset()[2];

        ChunkedBlockRegion edgePattern0 = themeConfig.topWallEdgePattern().
                transformedBlockRegions().getBlocks(0, false, false);
        edgePattern0.forEachEntry((x, y, z, state) -> chunkedBlockRegion.addBlock(
                x + maxXHouse - positionOffsetEdgeX,
                y + minY + positionOffsetEdgeY,
                z + minZHouse + positionOffsetEdgeZ, state)
        );

        ChunkedBlockRegion edgePattern1 = themeConfig.topWallEdgePattern().
                transformedBlockRegions().getBlocks(1, false, false);
        edgePattern1.forEachEntry((x, y, z, state) -> chunkedBlockRegion.addBlock(
                x + minXHouse + positionOffsetEdgeZ,
                y + minY + positionOffsetEdgeY,
                z + minZHouse + positionOffsetEdgeX, state)
        );

        ChunkedBlockRegion edgePattern2 = themeConfig.topWallEdgePattern().
                transformedBlockRegions().getBlocks(2, false, false);
        edgePattern2.forEachEntry((x, y, z, state) -> chunkedBlockRegion.addBlock(
                x + minXHouse + positionOffsetEdgeX,
                y + minY + positionOffsetEdgeY,
                z + maxZHouse - positionOffsetEdgeZ, state)
        );

        ChunkedBlockRegion edgePattern3 = themeConfig.topWallEdgePattern().
                transformedBlockRegions().getBlocks(3, false, false);
        edgePattern3.forEachEntry((x, y, z, state) -> chunkedBlockRegion.addBlock(
                x + maxXHouse - positionOffsetEdgeZ,
                y + minY + positionOffsetEdgeY,
                z + maxZHouse - positionOffsetEdgeX, state)
        );
        return chunkedBlockRegion;
    }
}
