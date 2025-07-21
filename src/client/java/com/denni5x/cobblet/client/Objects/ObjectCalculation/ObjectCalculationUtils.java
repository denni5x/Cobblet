package com.denni5x.cobblet.client.Objects.ObjectCalculation;

import com.denni5x.cobblet.client.Objects.House.HouseRoofType;
import com.denni5x.cobblet.client.Objects.Theme.ThemeConfig;
import com.moulberry.axiom.collections.Position2ObjectMap;
import com.moulberry.axiom.render.regions.ChunkedBlockRegion;
import net.minecraft.block.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class ObjectCalculationUtils {
    public static int getCopyFromCoords(int min, int mod, int offset, int size) {
        int value = min + mod - offset;
        return value < min ? value + size : value;
    }

    public static int getPatternMod(int value, int min, int size) {
        int mod = value % size;
        return (mod < min) ? mod + size : mod;
    }

    public static void calcEdgesToGround(ChunkedBlockRegion chunkedBlockRegion, int minX, int minZ, int maxZ, int maxX, BlockPos.Mutable mutableBlockPos) {
        Position2ObjectMap<BlockState> newBlocks = new Position2ObjectMap<>((k) -> new BlockState[4096]);
        World level = MinecraftClient.getInstance().world;
        if (level == null) {
            return;
        }
        chunkedBlockRegion.forEachEntry((x, y, z, block) -> {
            if ((x == minX && z == minZ) || (x == minX && z == maxZ) || (x == maxX && z == minZ) || (x == maxX && z == maxZ)) {
                for (int yo = 1; yo < 256 && (chunkedBlockRegion.getBlockStateOrAir(x, y - yo, z).isAir() ||
                        chunkedBlockRegion.getBlockStateOrAir(x, y - yo, z).getBlock() instanceof StairsBlock ||
                        chunkedBlockRegion.getBlockStateOrAir(x, y - yo, z).getBlock() instanceof SlabBlock ||
                        chunkedBlockRegion.getBlockStateOrAir(x, y - yo, z).getBlock() instanceof FenceBlock ||
                        chunkedBlockRegion.getBlockStateOrAir(x, y - yo, z).getBlock() instanceof FenceGateBlock ||
                        chunkedBlockRegion.getBlockStateOrAir(x, y - yo, z).getBlock() instanceof WallBlock); ++yo) {
                    BlockState below = level.getBlockState(mutableBlockPos.set(x, y - yo, z));
                    if (below.getBlock() == Blocks.VOID_AIR || !below.isReplaceable()) {
                        break;
                    }
                    newBlocks.put(x, y - yo, z, block);
                }
            }
        });
        newBlocks.forEachEntry(chunkedBlockRegion::addBlock);
    }

    public static void calcFloorCeiling(ChunkedBlockRegion chunkedBlockRegion, ThemeConfig themeConfig, int minX, int maxX, int minZ, int maxZ, int minY, int maxY) {
        for (int x = minX + 1; x <= maxX - 1; x++) {
            for (int z = minZ + 1; z <= maxZ - 1; z++) {
                chunkedBlockRegion.addBlock(x, minY, z, themeConfig.fullWall());
                chunkedBlockRegion.addBlock(x, maxY, z, themeConfig.fullWall());
            }
        }
    }

    public static void roof(ChunkedBlockRegion chunkedBlockRegion, ThemeConfig themeConfig, int steppedGableType, HouseRoofType houseRoofType, Direction.Axis roofOrientation, int minX, int maxX, int minZ, int maxZ, int minY, int maxY) {
        BlockState fullRoof = themeConfig.fullRoof();
        BlockState slabRoof = themeConfig.slabRoof();
        BlockState stairRoof = themeConfig.stairRoof();
        BlockState stairRoofMirrored;

        BlockState fullEdge = themeConfig.fullEdge();
        BlockState slabEdge = themeConfig.slabEdge();
        BlockState stairEdge = themeConfig.stairEdge();

        if (houseRoofType == HouseRoofType.STEPPED_GABLE ||
                houseRoofType == HouseRoofType.GABLE ||
                houseRoofType == HouseRoofType.THICK_GABLE ||
                houseRoofType == HouseRoofType.LOW_ANGLE ||
                houseRoofType == HouseRoofType.HIGH_ANGLE) {
            stairRoof = (roofOrientation == Direction.Axis.Z) ? stairRoof.rotate(BlockRotation.CLOCKWISE_90) : stairRoof.rotate(BlockRotation.CLOCKWISE_180);
            stairRoofMirrored = (roofOrientation == Direction.Axis.Z) ? stairRoof.mirror(BlockMirror.FRONT_BACK) : stairRoof.mirror(BlockMirror.LEFT_RIGHT);
        } else {
            stairRoof = stairRoof.rotate(BlockRotation.CLOCKWISE_180);
            stairRoofMirrored = stairRoof.mirror(BlockMirror.LEFT_RIGHT);
        }

        switch (houseRoofType) {
            case STEPPED_GABLE ->
                    RoofCalculation.calcSteppedGable(chunkedBlockRegion, themeConfig, roofOrientation, steppedGableType, minX, maxX, minZ, maxZ, maxY, slabRoof, stairRoof, stairRoofMirrored);
            case GABLE ->
                    RoofCalculation.calcGable(chunkedBlockRegion, themeConfig, roofOrientation, minX, maxX, minZ, maxZ, maxY, slabRoof, stairRoof, stairRoofMirrored);
            case SQUARE_GABLE ->
                    RoofCalculation.calcSquareGable(chunkedBlockRegion, minX, maxX, minZ, maxZ, maxY, slabRoof, stairRoof, false);
            case THICK_GABLE ->
                    RoofCalculation.calcThickGable(chunkedBlockRegion, themeConfig, roofOrientation, minX, maxX, minZ, maxZ, maxY, slabRoof, stairRoof, stairRoofMirrored, fullRoof);
            case HIGH_ANGLE ->
                    RoofCalculation.calcHighAngle(chunkedBlockRegion, themeConfig, roofOrientation, minX, maxX, minZ, maxZ, maxY, slabRoof, stairRoof, stairRoofMirrored, fullRoof);
            case LOW_ANGLE ->
                    RoofCalculation.calcLowAngle(chunkedBlockRegion, themeConfig, roofOrientation, minX, maxX, minZ, maxZ, maxY, slabRoof, fullRoof);
            case SLABBED -> RoofCalculation.calcWalledRoof(chunkedBlockRegion, minX, maxX, minZ, maxZ, maxY, slabRoof);
            case WALL_PATTERN ->
                    TopWallObjectCalculation.topWall(themeConfig, chunkedBlockRegion, minX, maxX, maxY, minZ, maxZ);
            case WALLED ->
                    RoofCalculation.calcWalledRoof(chunkedBlockRegion, minX, maxX, minZ, maxZ, maxY, themeConfig.fullWallRoof());
            case THICK_WALLED ->
                    RoofCalculation.calcThickWalled(chunkedBlockRegion, minX, maxX, minZ, maxZ, maxY, slabEdge, fullEdge, stairEdge);
            case TOWER -> {
                RoofCalculation.calcThickWalled(chunkedBlockRegion, minX, maxX, minZ, maxZ, maxY, slabEdge, fullEdge, stairEdge);
                RoofCalculation.calcSquareGable(chunkedBlockRegion, minX - 1, maxX + 1, minZ - 1, maxZ + 1, maxY + 3, slabRoof, stairRoof, true);
            }
        }
    }
}
