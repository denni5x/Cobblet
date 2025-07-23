package com.denni5x.cobblet.client.Objects.ObjectCalculation;

import com.denni5x.cobblet.client.Objects.Theme.ThemeConfig;
import com.moulberry.axiom.render.regions.ChunkedBlockRegion;
import net.minecraft.block.BlockState;

import java.util.Objects;

public class InnerFloorCalculation {
    public static void innerFloor(ThemeConfig themeConfig, ChunkedBlockRegion chunkedBlockRegion,
                                  int minX, int maxX, int minY, int maxY, int minZ, int maxZ,
                                  int[] offset, int[] size) {
        if (themeConfig.innerFloorPattern() == null) {
            return;
        }
        ChunkedBlockRegion copyFrom = themeConfig.innerFloorPattern().transformedBlockRegions().getBlocks(0, false, false);

        int minXCB = Objects.requireNonNull(copyFrom.min()).getX();
        int minYCB = Objects.requireNonNull(copyFrom.min()).getY();
        int minZCB = Objects.requireNonNull(copyFrom.min()).getZ();
        int maxXCB = Objects.requireNonNull(copyFrom.max()).getX();
        int maxYCB = Objects.requireNonNull(copyFrom.max()).getY();
        int maxZCB = Objects.requireNonNull(copyFrom.max()).getZ();
        size[0] = maxXCB - minXCB + 1;
        size[1] = maxYCB - minYCB + 1;
        size[2] = maxZCB - minZCB + 1;

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    int modX = ObjectCalculationUtils.getPatternMod(x, minXCB, size[0]);
                    int modY = ObjectCalculationUtils.getPatternMod(y, minYCB, size[1]);
                    int modZ = ObjectCalculationUtils.getPatternMod(z, minZCB, size[2]);

                    int copyFromX = ObjectCalculationUtils.getCopyFromCoords(minXCB, modX, offset[0], size[0]);
                    int copyFromY = ObjectCalculationUtils.getCopyFromCoords(minYCB, modY, offset[1], size[1]);
                    int copyFromZ = ObjectCalculationUtils.getCopyFromCoords(minZCB, modZ, offset[2], size[2]);

                    BlockState blockState = copyFrom.getBlockStateOrAir(copyFromX, copyFromY, copyFromZ);
                    if (!blockState.isAir()) {
                        chunkedBlockRegion.addBlock(x, y, z, blockState);
                    }
                }
            }
        }
    }
}
