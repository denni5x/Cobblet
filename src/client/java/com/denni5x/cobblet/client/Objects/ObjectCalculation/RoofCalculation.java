package com.denni5x.cobblet.client.Objects.ObjectCalculation;

import com.denni5x.cobblet.client.Objects.Theme.ThemeConfig;
import com.moulberry.axiom.block_maps.HDVoxelMap;
import com.moulberry.axiom.render.regions.ChunkedBlockRegion;
import net.minecraft.block.BlockState;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.WallBlock;
import net.minecraft.block.enums.BlockHalf;
import net.minecraft.block.enums.SlabType;
import net.minecraft.block.enums.StairShape;
import net.minecraft.state.property.Properties;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.NotNull;

public class RoofCalculation {

    public static void calcSquareGable(ChunkedBlockRegion chunkedBlockRegion, int minX, int maxX, int minZ, int maxZ, int maxY, @NotNull BlockState slab, @NotNull BlockState stair, boolean isTower) {
        BlockState stair90 = stair.rotate(BlockRotation.CLOCKWISE_90);
        BlockState stair180 = stair.rotate(BlockRotation.CLOCKWISE_180);
        BlockState stair270 = stair.rotate(BlockRotation.COUNTERCLOCKWISE_90);
        BlockState upperSlab = isTower ? slab : slab.with(Properties.SLAB_TYPE, SlabType.TOP);

        BlockState stairMinXMinZ = stair.with(Properties.BLOCK_HALF, BlockHalf.BOTTOM).with(Properties.STAIR_SHAPE,
                StairShape.OUTER_LEFT).with(Properties.HORIZONTAL_FACING, Direction.SOUTH);
        BlockState stairMaxXMinZ = stair.with(Properties.BLOCK_HALF, BlockHalf.BOTTOM).with(Properties.STAIR_SHAPE,
                StairShape.OUTER_LEFT).with(Properties.HORIZONTAL_FACING, Direction.WEST);
        BlockState stairMinXMaxZ = stair.with(Properties.BLOCK_HALF, BlockHalf.BOTTOM).with(Properties.STAIR_SHAPE,
                StairShape.OUTER_LEFT).with(Properties.HORIZONTAL_FACING, Direction.EAST);
        BlockState stairMaxXMaxZ = stair.with(Properties.BLOCK_HALF, BlockHalf.BOTTOM).with(Properties.STAIR_SHAPE,
                StairShape.OUTER_LEFT).with(Properties.HORIZONTAL_FACING, Direction.NORTH);

        int Y = maxY;
        for (int maxXIt = maxX + 1, minXIt = minX - 1; minXIt <= maxXIt; minXIt++, maxXIt--) {
            chunkedBlockRegion.addBlockIfNotPresent(minXIt, Y, minZ - 1, upperSlab);
            chunkedBlockRegion.addBlockIfNotPresent(maxXIt, Y, minZ - 1, upperSlab);
            chunkedBlockRegion.addBlockIfNotPresent(minXIt, Y, maxZ + 1, upperSlab);
            chunkedBlockRegion.addBlockIfNotPresent(maxXIt, Y, maxZ + 1, upperSlab);
        }
        for (int maxZIt = maxZ + 1, minZIt = minZ - 1; minZIt <= maxZIt; minZIt++, maxZIt--) {
            chunkedBlockRegion.addBlockIfNotPresent(minX - 1, Y, minZIt, upperSlab);
            chunkedBlockRegion.addBlockIfNotPresent(minX - 1, Y, maxZIt, upperSlab);
            chunkedBlockRegion.addBlockIfNotPresent(maxX + 1, Y, minZIt, upperSlab);
            chunkedBlockRegion.addBlockIfNotPresent(maxX + 1, Y, maxZIt, upperSlab);
        }
        if (!isTower) Y++;
        int offset = 0;
        int minXX = minX;
        int maxXX = maxX;
        int minZZ = minZ;
        int maxZZ = maxZ;
        while (maxX - offset >= minX + offset && maxZ - offset >= minZ + offset) {
            if (maxX - offset == minX + offset || maxZ - offset == minZ + offset) {
                stair = slab;
                stair90 = slab;
                stair180 = slab;
                stair270 = slab;
                stairMinXMinZ = slab;
                stairMaxXMinZ = slab;
                stairMinXMaxZ = slab;
                stairMaxXMaxZ = slab;
            }
            for (int maxXIt = maxX - offset, minXIt = minX + offset; minXIt <= maxXIt; minXIt++, maxXIt--) {
                chunkedBlockRegion.addBlockIfNotPresent(minXIt, Y, minZZ, minXIt == minXX ? stairMinXMinZ : stair);
                chunkedBlockRegion.addBlockIfNotPresent(maxXIt, Y, minZZ, maxXIt == maxXX ? stairMaxXMinZ : stair);
                chunkedBlockRegion.addBlockIfNotPresent(minXIt, Y, maxZZ, minXIt == minXX ? stairMinXMaxZ : stair180);
                chunkedBlockRegion.addBlockIfNotPresent(maxXIt, Y, maxZZ, maxXIt == maxXX ? stairMaxXMaxZ : stair180);
            }
            for (int maxZIt = maxZ - offset, minZIt = minZ + offset; minZIt <= maxZIt; minZIt++, maxZIt--) {
                chunkedBlockRegion.addBlockIfNotPresent(minXX, Y, minZIt, minZIt == minZZ ? stairMinXMinZ : stair270);
                chunkedBlockRegion.addBlockIfNotPresent(minXX, Y, maxZIt, maxZIt == maxZZ ? stairMinXMaxZ : stair270);
                chunkedBlockRegion.addBlockIfNotPresent(maxXX, Y, minZIt, minZIt == minZZ ? stairMaxXMinZ : stair90);
                chunkedBlockRegion.addBlockIfNotPresent(maxXX, Y, maxZIt, maxZIt == maxZZ ? stairMaxXMaxZ : stair90);
            }
            minZZ++;
            maxZZ--;
            minXX++;
            maxXX--;
            Y++;
            offset++;
        }
    }

    public static void calcThickWalled(ChunkedBlockRegion chunkedBlockRegion, int minX, int maxX, int minZ, int maxZ, int maxY, @NotNull BlockState slab, @NotNull BlockState full, @NotNull BlockState stair) {
        BlockState stair90 = stair.rotate(BlockRotation.CLOCKWISE_90);
        BlockState stair180 = stair.rotate(BlockRotation.CLOCKWISE_180);
        BlockState stair270 = stair.rotate(BlockRotation.COUNTERCLOCKWISE_90);

        BlockState stairMinXMinZ = stair.with(Properties.BLOCK_HALF, BlockHalf.BOTTOM).with(Properties.STAIR_SHAPE,
                StairShape.OUTER_LEFT).with(Properties.HORIZONTAL_FACING, Direction.SOUTH);
        BlockState stairMaxXMinZ = stair.with(Properties.BLOCK_HALF, BlockHalf.BOTTOM).with(Properties.STAIR_SHAPE,
                StairShape.OUTER_LEFT).with(Properties.HORIZONTAL_FACING, Direction.WEST);
        BlockState stairMinXMaxZ = stair.with(Properties.BLOCK_HALF, BlockHalf.BOTTOM).with(Properties.STAIR_SHAPE,
                StairShape.OUTER_LEFT).with(Properties.HORIZONTAL_FACING, Direction.EAST);
        BlockState stairMaxXMaxZ = stair.with(Properties.BLOCK_HALF, BlockHalf.BOTTOM).with(Properties.STAIR_SHAPE,
                StairShape.OUTER_LEFT).with(Properties.HORIZONTAL_FACING, Direction.NORTH);

        int Y = maxY;
        for (int iteration = 0; iteration <= 2; iteration++) {
            chunkedBlockRegion.addBlockIfNotPresent(minX, Y, minZ, full);
            chunkedBlockRegion.addBlockIfNotPresent(minX - 1, Y, minZ, iteration == 2 ? stair90 : full);
            chunkedBlockRegion.addBlockIfNotPresent(minX, Y, minZ - 1, iteration == 2 ? stair180 : full);
            chunkedBlockRegion.addBlockIfNotPresent(minX - 1, Y, minZ - 1, iteration == 2 ? stairMinXMinZ : full);

            chunkedBlockRegion.addBlockIfNotPresent(maxX, Y, maxZ, full);
            chunkedBlockRegion.addBlockIfNotPresent(maxX + 1, Y, maxZ, iteration == 2 ? stair270 : full);
            chunkedBlockRegion.addBlockIfNotPresent(maxX, Y, maxZ + 1, iteration == 2 ? stair : full);
            chunkedBlockRegion.addBlockIfNotPresent(maxX + 1, Y, maxZ + 1, iteration == 2 ? stairMaxXMaxZ : full);

            chunkedBlockRegion.addBlockIfNotPresent(minX, Y, maxZ, full);
            chunkedBlockRegion.addBlockIfNotPresent(minX - 1, Y, maxZ, iteration == 2 ? stair90 : full);
            chunkedBlockRegion.addBlockIfNotPresent(minX, Y, maxZ + 1, iteration == 2 ? stair : full);
            chunkedBlockRegion.addBlockIfNotPresent(minX - 1, Y, maxZ + 1, iteration == 2 ? stairMinXMaxZ : full);

            chunkedBlockRegion.addBlockIfNotPresent(maxX, Y, minZ, full);
            chunkedBlockRegion.addBlockIfNotPresent(maxX, Y, minZ - 1, iteration == 2 ? stair180 : full);
            chunkedBlockRegion.addBlockIfNotPresent(maxX + 1, Y, minZ, iteration == 2 ? stair270 : full);
            chunkedBlockRegion.addBlockIfNotPresent(maxX + 1, Y, minZ - 1, iteration == 2 ? stairMaxXMinZ : full);

            boolean placeExtrudedWall = false;
            for (int maxXIt = maxX - 1, minXIt = minX + 1; minXIt <= maxXIt; minXIt++, maxXIt--) {
                if (placeExtrudedWall) {
                    chunkedBlockRegion.addBlockIfNotPresent(minXIt, Y, minZ, full);
                    chunkedBlockRegion.addBlockIfNotPresent(minXIt, Y, minZ - 1, iteration == 2 ? stair180 : full);

                    chunkedBlockRegion.addBlockIfNotPresent(maxXIt, Y, minZ, full);
                    chunkedBlockRegion.addBlockIfNotPresent(maxXIt, Y, minZ - 1, iteration == 2 ? stair180 : full);

                    chunkedBlockRegion.addBlockIfNotPresent(minXIt, Y, maxZ, full);
                    chunkedBlockRegion.addBlockIfNotPresent(minXIt, Y, maxZ + 1, iteration == 2 ? stair : full);

                    chunkedBlockRegion.addBlockIfNotPresent(maxXIt, Y, maxZ, full);
                    chunkedBlockRegion.addBlockIfNotPresent(maxXIt, Y, maxZ + 1, iteration == 2 ? stair : full);
                } else {
                    if (iteration == 2) {
                        chunkedBlockRegion.addBlockIfNotPresent(minXIt, Y, minZ, slab);
                        chunkedBlockRegion.addBlockIfNotPresent(maxXIt, Y, minZ, slab);
                        chunkedBlockRegion.addBlockIfNotPresent(minXIt, Y, maxZ, slab);
                        chunkedBlockRegion.addBlockIfNotPresent(maxXIt, Y, maxZ, slab);
                    }
                }
                placeExtrudedWall = !placeExtrudedWall;
            }

            placeExtrudedWall = false;
            for (int maxZIt = maxZ - 1, minZIt = minZ + 1; minZIt <= maxZIt; minZIt++, maxZIt--) {
                if (placeExtrudedWall) {
                    chunkedBlockRegion.addBlockIfNotPresent(minX, Y, minZIt, full);
                    chunkedBlockRegion.addBlockIfNotPresent(minX - 1, Y, minZIt, iteration == 2 ? stair90 : full);

                    chunkedBlockRegion.addBlockIfNotPresent(minX, Y, maxZIt, full);
                    chunkedBlockRegion.addBlockIfNotPresent(minX - 1, Y, maxZIt, iteration == 2 ? stair90 : full);

                    chunkedBlockRegion.addBlockIfNotPresent(maxX, Y, minZIt, full);
                    chunkedBlockRegion.addBlockIfNotPresent(maxX + 1, Y, minZIt, iteration == 2 ? stair270 : full);

                    chunkedBlockRegion.addBlockIfNotPresent(maxX, Y, maxZIt, full);
                    chunkedBlockRegion.addBlockIfNotPresent(maxX + 1, Y, maxZIt, iteration == 2 ? stair270 : full);
                } else {
                    if (iteration == 2) {
                        chunkedBlockRegion.addBlockIfNotPresent(minX, Y, minZIt, slab);
                        chunkedBlockRegion.addBlockIfNotPresent(minX, Y, maxZIt, slab);
                        chunkedBlockRegion.addBlockIfNotPresent(maxX, Y, minZIt, slab);
                        chunkedBlockRegion.addBlockIfNotPresent(maxX, Y, maxZIt, slab);
                    }
                }
                placeExtrudedWall = !placeExtrudedWall;
            }
            Y++;
        }
    }

    public static void calcWalledRoof(ChunkedBlockRegion chunkedBlockRegion, int minX, int maxX, int minZ, int maxZ, int maxY, @NotNull BlockState block) {
        BlockState wallNorthSouth = block, wallEastWest = block, maxXmaxZ = block, minXmaxZ = block, maxXminZ = block, minXminZ = block;
        if (block.getBlock() instanceof WallBlock) {
            wallNorthSouth = block.cycle(WallBlock.NORTH_SHAPE).cycle(WallBlock.SOUTH_SHAPE);
            wallEastWest = block.cycle(WallBlock.EAST_SHAPE).cycle(WallBlock.WEST_SHAPE);
            minXminZ = block.cycle(WallBlock.SOUTH_SHAPE).cycle(WallBlock.EAST_SHAPE);
            maxXminZ = block.cycle(WallBlock.SOUTH_SHAPE).cycle(WallBlock.WEST_SHAPE);
            minXmaxZ = block.cycle(WallBlock.NORTH_SHAPE).cycle(WallBlock.EAST_SHAPE);
            maxXmaxZ = block.cycle(WallBlock.NORTH_SHAPE).cycle(WallBlock.WEST_SHAPE);
        }
        int y = maxY + 1;
        chunkedBlockRegion.addBlockIfNotPresent(maxX, y, maxZ, maxXmaxZ);
        chunkedBlockRegion.addBlockIfNotPresent(minX, y, maxZ, minXmaxZ);
        chunkedBlockRegion.addBlockIfNotPresent(maxX, y, minZ, maxXminZ);
        chunkedBlockRegion.addBlockIfNotPresent(minX, y, minZ, minXminZ);
        for (int x = minX + 1; x < maxX; x++) {
            chunkedBlockRegion.addBlockIfNotPresent(x, y, minZ, wallEastWest);
            chunkedBlockRegion.addBlockIfNotPresent(x, y, maxZ, wallEastWest);

        }
        for (int z = minZ + 1; z < maxZ; z++) {
            chunkedBlockRegion.addBlockIfNotPresent(minX, y, z, wallNorthSouth);
            chunkedBlockRegion.addBlockIfNotPresent(maxX, y, z, wallNorthSouth);
        }
    }

    public static void calcThickGable(ChunkedBlockRegion chunkedBlockRegion, ThemeConfig themeConfig, Direction.Axis roofOrientation, int minX, int maxX, int minZ, int maxZ, int maxY, BlockState slabRoofBlock, BlockState roofStairBlock, BlockState roofStairBlockMirrored, BlockState fullRoof) {
        float width = roofOrientation == Direction.Axis.Z ? Math.abs(minX - maxX) + 1 : Math.abs(minZ - maxZ) + 1;
        int x, z, iterations = 0, xMirrored, zMirrored;
        boolean hasSingleRidge = width % 2 > 0;
        width = hasSingleRidge ? width + 1 : width;
        if (roofOrientation == Direction.Axis.Z) {
            x = minX - 1;
            xMirrored = maxX + 1;
            while (iterations <= width) {
                for (z = minZ - 1; z <= maxZ + 1; z++) {
                    int y = (int) Math.floor((double) iterations / 2) + maxY;
                    chunkedBlockRegion.addBlock(x, y, z, fullRoof);
                    if (hasSingleRidge && iterations == width) {
                        chunkedBlockRegion.addBlock(x, y + 1, z, slabRoofBlock);
                    } else {
                        chunkedBlockRegion.addBlock(x, y + 1, z, roofStairBlock);
                        chunkedBlockRegion.addBlock(xMirrored, y, z, fullRoof);
                        chunkedBlockRegion.addBlock(xMirrored, y + 1, z, roofStairBlockMirrored);
                    }
                    if ((z == minZ || z == maxZ) && y > maxY) {
                        for (int gableEndWall = x + 1; gableEndWall < xMirrored; gableEndWall++) {
                            chunkedBlockRegion.addBlock(gableEndWall, y, z, themeConfig.fullWall());
                            chunkedBlockRegion.addBlock(gableEndWall, y + 1, z, themeConfig.fullWall());
                        }
                    }
                }
                x += 1;
                xMirrored -= 1;
                iterations += 2;
            }
        } else {
            z = minZ - 1;
            zMirrored = maxZ + 1;
            while (iterations <= width) {
                for (x = minX - 1; x <= maxX + 1; x++) {
                    int y = (int) Math.floor((double) iterations / 2) + maxY;
                    chunkedBlockRegion.addBlock(x, y, z, fullRoof);
                    if (hasSingleRidge && iterations == width) {
                        chunkedBlockRegion.addBlock(x, y + 1, z, slabRoofBlock);
                    } else {
                        chunkedBlockRegion.addBlock(x, y + 1, z, roofStairBlock);
                        chunkedBlockRegion.addBlock(x, y, zMirrored, fullRoof);
                        chunkedBlockRegion.addBlock(x, y + 1, zMirrored, roofStairBlockMirrored);
                    }
                    if ((x == minX || x == maxX) && y > maxY) {
                        for (int gableEndWall = z + 1; gableEndWall < zMirrored; gableEndWall++) {
                            chunkedBlockRegion.addBlock(x, y, gableEndWall, themeConfig.fullWall());
                            chunkedBlockRegion.addBlock(x, y + 1, gableEndWall, themeConfig.fullWall());
                        }
                    }
                }
                z += 1;
                zMirrored -= 1;
                iterations += 2;
            }
        }
    }

    public static void calcHighAngle(ChunkedBlockRegion chunkedBlockRegion, ThemeConfig themeConfig, Direction.Axis roofOrientation, int minX, int maxX, int minZ, int maxZ, int maxY, BlockState slabRoofBlock, BlockState roofStairBlock, BlockState roofStairBlockMirrored, BlockState fullRoof) {
        float width = roofOrientation == Direction.Axis.Z ? Math.abs(minX - maxX) + 1 : Math.abs(minZ - maxZ) + 1;
        int x, z, iterations = 0, xMirrored, zMirrored;
        boolean hasSingleRidge = width % 2 > 0;
        width = hasSingleRidge ? width + 1 : width;
        if (roofOrientation == Direction.Axis.Z) {
            x = minX - 1;
            xMirrored = maxX + 1;
            while (iterations <= width) {
                for (z = minZ - 1; z <= maxZ + 1; z++) {
                    int y = iterations + maxY;
                    chunkedBlockRegion.addBlock(x, y, z, fullRoof);
                    if (hasSingleRidge && iterations == width) {
                        chunkedBlockRegion.addBlock(x, y + 1, z, slabRoofBlock);
                    } else {
                        chunkedBlockRegion.addBlock(x, y + 1, z, roofStairBlock);
                        chunkedBlockRegion.addBlock(xMirrored, y, z, fullRoof);
                        chunkedBlockRegion.addBlock(xMirrored, y + 1, z, roofStairBlockMirrored);
                    }
                    y = y > maxY ? y : y + 1;
                    if ((z == minZ || z == maxZ) && y > maxY) {
                        for (int gableEndWall = x + 1; gableEndWall < xMirrored; gableEndWall++) {
                            chunkedBlockRegion.addBlock(gableEndWall, y, z, themeConfig.fullWall());
                            chunkedBlockRegion.addBlock(gableEndWall, y + 1, z, themeConfig.fullWall());
                        }
                    }
                }
                x += 1;
                xMirrored -= 1;
                iterations += 2;
            }
        } else {
            z = minZ - 1;
            zMirrored = maxZ + 1;
            while (iterations <= width) {
                for (x = minX - 1; x <= maxX + 1; x++) {
                    int y = iterations + maxY;
                    chunkedBlockRegion.addBlock(x, y, z, fullRoof);
                    if (hasSingleRidge && iterations == width) {
                        chunkedBlockRegion.addBlock(x, y + 1, z, slabRoofBlock);
                    } else {
                        chunkedBlockRegion.addBlock(x, y + 1, z, roofStairBlock);
                        chunkedBlockRegion.addBlock(x, y, zMirrored, fullRoof);
                        chunkedBlockRegion.addBlock(x, y + 1, zMirrored, roofStairBlockMirrored);
                    }
                    y = y > maxY ? y : y + 1;
                    if ((x == minX || x == maxX) && y > maxY) {
                        for (int gableEndWall = z + 1; gableEndWall < zMirrored; gableEndWall++) {
                            chunkedBlockRegion.addBlock(x, y, gableEndWall, themeConfig.fullWall());
                            chunkedBlockRegion.addBlock(x, y + 1, gableEndWall, themeConfig.fullWall());
                        }
                    }
                }
                z += 1;
                zMirrored -= 1;
                iterations += 2;
            }
        }
    }

    public static void calcLowAngle(ChunkedBlockRegion chunkedBlockRegion, ThemeConfig themeConfig, Direction.Axis roofOrientation, int minX, int maxX, int minZ, int maxZ, int maxY, @NotNull BlockState slabRoofBlock, BlockState fullRoof) {
        float width = roofOrientation == Direction.Axis.Z ? Math.abs(minX - maxX) + 1 : Math.abs(minZ - maxZ) + 1;
        float roofHeight;
        int x, z, iterations = 0, xMirrored, zMirrored;
        roofHeight = (float) Math.ceil(width / 2);
        BlockState upperSlab = slabRoofBlock.cycle(SlabBlock.TYPE).cycle(SlabBlock.TYPE);
        if (roofOrientation == Direction.Axis.Z) {
            x = minX - 1;
            xMirrored = maxX + 1;
            while (iterations <= roofHeight) {
                for (z = minZ - 1; z <= maxZ + 1; z++) {
                    int y = (int) Math.floor((double) iterations / 2) + maxY;
                    if ((z == minZ || z == maxZ) && iterations % 2 != 0 && x < maxX) {
                        chunkedBlockRegion.addBlock(x, y, z, fullRoof);
                        chunkedBlockRegion.addBlock(xMirrored, y, z, fullRoof);
                    } else {
                        chunkedBlockRegion.addBlock(x, y, z, (iterations % 2 != 0) ? upperSlab : slabRoofBlock);
                        chunkedBlockRegion.addBlock(xMirrored, y, z, (iterations % 2 != 0) ? upperSlab : slabRoofBlock);
                    }
                    if ((z == minZ || z == maxZ) && y > maxY) {
                        for (int gableEndWall = x + 1; gableEndWall < xMirrored; gableEndWall++) {
                            chunkedBlockRegion.addBlock(gableEndWall, y, z, themeConfig.fullWall());
                        }
                    }
                }
                x += 1;
                xMirrored -= 1;
                iterations += 1;
            }
        } else {
            z = minZ - 1;
            zMirrored = maxZ + 1;
            while (iterations <= roofHeight) {
                for (x = minX - 1; x <= maxX + 1; x++) {
                    int y = (int) Math.floor((double) iterations / 2) + maxY;
                    if ((x == minX || x == maxX) && iterations % 2 != 0 && z < maxZ) {
                        chunkedBlockRegion.addBlock(x, y, z, fullRoof);
                        chunkedBlockRegion.addBlock(x, y, zMirrored, fullRoof);
                    } else {
                        chunkedBlockRegion.addBlock(x, y, z, (iterations % 2 != 0) ? upperSlab : slabRoofBlock);
                        chunkedBlockRegion.addBlock(x, y, zMirrored, (iterations % 2 != 0) ? upperSlab : slabRoofBlock);
                    }
                    if ((x == minX || x == maxX) && y > maxY) {
                        for (int gableEndWall = z + 1; gableEndWall < zMirrored; gableEndWall++) {
                            chunkedBlockRegion.addBlock(x, y, gableEndWall, themeConfig.fullWall());
                        }
                    }
                }
                z += 1;
                zMirrored -= 1;
                iterations += 1;
            }
        }
    }

    public static void calcSteppedGable(ChunkedBlockRegion chunkedBlockRegion, ThemeConfig themeConfig, Direction.Axis roofOrientation, int steppedGableType, int minX, int maxX, int minZ, int maxZ, int maxY, BlockState slabRoof, BlockState roofStair, BlockState roofStairMirrored) {
        float width = roofOrientation == Direction.Axis.Z ? Math.abs(minX - maxX) + 1 : Math.abs(minZ - maxZ) + 1;
        HDVoxelMap.HDVoxelBaseBlocks wallBlocks = HDVoxelMap.getAssociatedBlocks(themeConfig.fullWall().getBlock());
        BlockState steppedRoofBlock;
        BlockState steppedRoofBlockMirrored;
        switch (steppedGableType) {
            case 0 -> steppedRoofBlock = wallBlocks.slab().getDefaultState();
            case 1 ->
                    steppedRoofBlock = roofOrientation == Direction.Axis.Z ? wallBlocks.stair().getDefaultState().rotate(BlockRotation.CLOCKWISE_180) : wallBlocks.stair().getDefaultState().rotate(BlockRotation.CLOCKWISE_90);
            case 2 -> steppedRoofBlock = wallBlocks.full().getDefaultState();
            default -> steppedRoofBlock = wallBlocks.stair().getDefaultState();
        }
        steppedRoofBlockMirrored = steppedRoofBlock;
        float roofHeight;
        int iterations = 1, x, y, z, xMirrored, zMirrored;
        boolean hasSingleRidge = width % 2 > 0;
        roofHeight = (float) Math.ceil(width / 2);
        y = maxY + 1;
        if (roofOrientation == Direction.Axis.Z) {
            if (steppedGableType == 1)
                steppedRoofBlockMirrored = steppedRoofBlockMirrored.mirror(BlockMirror.LEFT_RIGHT);
            xMirrored = maxX;
            x = minX;
            while (iterations <= roofHeight) {
                for (z = minZ; z <= maxZ; z++) {
                    if (hasSingleRidge && iterations == roofHeight) {
                        chunkedBlockRegion.addBlock(x, y, z, slabRoof);
                    } else {
                        chunkedBlockRegion.addBlock(x, y, z, roofStair);
                        chunkedBlockRegion.addBlock(xMirrored, y, z, roofStairMirrored);
                    }
                }
                for (int xX = x; xX <= xMirrored; xX++) {
                    chunkedBlockRegion.addBlock(xX, y, minZ, themeConfig.fullWall());
                    chunkedBlockRegion.addBlock(xX, y, maxZ, themeConfig.fullWall());
                }
                chunkedBlockRegion.addBlock(x, y + 1, maxZ, steppedRoofBlock);
                chunkedBlockRegion.addBlock(x, y + 1, minZ, steppedRoofBlockMirrored);
                chunkedBlockRegion.addBlock(xMirrored, y + 1, maxZ, steppedRoofBlock);
                chunkedBlockRegion.addBlock(xMirrored, y + 1, minZ, steppedRoofBlockMirrored);
                x += 1;
                y += 1;
                xMirrored -= 1;
                iterations++;
            }
        } else {
            if (steppedGableType == 1)
                steppedRoofBlockMirrored = steppedRoofBlockMirrored.mirror(BlockMirror.FRONT_BACK);
            zMirrored = maxZ;
            z = minZ;
            while (iterations <= roofHeight) {
                for (x = minX; x <= maxX; x++) {
                    if (hasSingleRidge && iterations == roofHeight) {
                        chunkedBlockRegion.addBlock(x, y, z, slabRoof);
                    } else {
                        chunkedBlockRegion.addBlock(x, y, z, roofStair);
                        chunkedBlockRegion.addBlock(x, y, zMirrored, roofStairMirrored);
                    }
                }
                for (int zZ = z; zZ <= zMirrored; zZ++) {
                    chunkedBlockRegion.addBlock(maxX, y, zZ, themeConfig.fullWall());
                    chunkedBlockRegion.addBlock(minX, y, zZ, themeConfig.fullWall());
                }
                chunkedBlockRegion.addBlock(maxX, y + 1, z, steppedRoofBlock);
                chunkedBlockRegion.addBlock(minX, y + 1, z, steppedRoofBlockMirrored);
                chunkedBlockRegion.addBlock(maxX, y + 1, zMirrored, steppedRoofBlock);
                chunkedBlockRegion.addBlock(minX, y + 1, zMirrored, steppedRoofBlockMirrored);
                z += 1;
                y += 1;
                zMirrored -= 1;
                iterations++;
            }
        }
    }

    public static void calcGable(ChunkedBlockRegion chunkedBlockRegion, ThemeConfig themeConfig, Direction.Axis roofOrientation, int minX, int maxX, int minZ, int maxZ, int maxY, BlockState slabRoofBlock, BlockState roofStairBlock, BlockState roofStairBlockMirrored) {
        float width = roofOrientation == Direction.Axis.Z ? Math.abs(minX - maxX) + 1 : Math.abs(minZ - maxZ) + 1;
        float roofHeight;
        int iterations = 0, x, y, z, xMirrored, zMirrored;
        boolean hasSingleRidge = width % 2 > 0;
        roofHeight = (float) Math.ceil(width / 2);
        if (roofOrientation == Direction.Axis.Z) {
            xMirrored = maxX + 1;
            x = minX - 1;
            y = maxY;
            while (iterations <= roofHeight) {
                for (z = minZ - 1; z <= maxZ + 1; z++) {
                    if (hasSingleRidge && iterations == roofHeight) {
                        chunkedBlockRegion.addBlock(x, y, z, slabRoofBlock);
                    } else {
                        chunkedBlockRegion.addBlock(x, y, z, roofStairBlock);
                        chunkedBlockRegion.addBlock(xMirrored, y, z, roofStairBlockMirrored);
                    }
                    if ((z == minZ || z == maxZ) && y > maxY) {
                        for (int gableEndWall = x + 1; gableEndWall < xMirrored; gableEndWall++) {
                            chunkedBlockRegion.addBlock(gableEndWall, y, z, themeConfig.fullWall());
                        }
                    }
                }
                x += 1;
                y += 1;
                xMirrored -= 1;
                iterations++;
            }
        } else {
            zMirrored = maxZ + 1;
            z = minZ - 1;
            y = maxY;
            while (iterations <= roofHeight) {
                for (x = minX - 1; x <= maxX + 1; x++) {
                    if (hasSingleRidge && iterations == roofHeight) {
                        chunkedBlockRegion.addBlock(x, y, z, slabRoofBlock);
                    } else {
                        chunkedBlockRegion.addBlock(x, y, z, roofStairBlock);
                        chunkedBlockRegion.addBlock(x, y, zMirrored, roofStairBlockMirrored);
                    }
                    if ((x == minX || x == maxX) && y > maxY) {
                        for (int gableEndWall = z + 1; gableEndWall < zMirrored; gableEndWall++) {
                            chunkedBlockRegion.addBlock(x, y, gableEndWall, themeConfig.fullWall());
                        }
                    }
                }
                z += 1;
                y += 1;
                zMirrored -= 1;
                iterations++;
            }
        }
    }
}
