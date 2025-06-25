package com.denni5x.cobblet.client.Objects.House;

import com.denni5x.cobblet.client.CobbletTool;
import com.denni5x.cobblet.client.Objects.CobbletObject;
import com.denni5x.cobblet.client.Objects.Theme.Theme;
import com.denni5x.cobblet.client.Objects.Theme.ThemeConfig;
import com.denni5x.cobblet.client.Objects.Theme.ThemeRecord;
import com.moulberry.axiom.block_maps.HDVoxelMap;
import com.moulberry.axiom.collections.Position2ObjectMap;
import com.moulberry.axiom.exceptions.FaultyImplementationError;
import com.moulberry.axiom.gizmo.Gizmo;
import com.moulberry.axiom.render.regions.ChunkedBlockRegion;
import net.minecraft.block.*;
import net.minecraft.block.enums.BlockHalf;
import net.minecraft.block.enums.SlabType;
import net.minecraft.block.enums.StairShape;
import net.minecraft.client.MinecraftClient;
import net.minecraft.state.property.Properties;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public class House extends CobbletObject {
    private final int[] patternOffsetFullWallXAxis = new int[]{0, 0, 0};
    private final int[] patternOffsetFullWallZAxis = new int[]{0, 0, 0};
    private final int[] patternSizeFullWall = new int[]{0, 0, 0};
    private final int[] patternSizeFullWall1R = new int[]{0, 0, 0};

    private final int[] patternOffsetFloor = new int[]{0, 0, 0};
    private final int[] patternSizeFloor = new int[]{0, 0, 0};

    private final int[] patternOffsetTopWallXAxis = new int[]{0, 0, 0};
    private final int[] patternOffsetTopWallZAxis = new int[]{0, 0, 0};
    private final int[] patternSizeTopWall = new int[]{0, 0, 0};
    private final int[] patternSizeTopWall1R = new int[]{0, 0, 0};
    private final int[] positionOffsetTopWall = new int[]{0, 0, 0};

    private Direction.Axis roofOrientation;
    private Theme theme;
    private ThemeConfig themeConfig;
    private HouseRoofType houseRoofType;
    private int steppedGableType;

    public House(int[] position, int[] size, CobbletTool cobbletTool, boolean isRecord, Theme gladeTheme, HouseRoofType houseRoofType, int steppedGableType, Direction.Axis roofOrientation) {
        super(cobbletTool, isRecord);
        super.position = position;
        super.size = size;
        this.roofOrientation = roofOrientation;
        this.houseRoofType = houseRoofType;
        this.steppedGableType = steppedGableType;
        this.theme = gladeTheme;
        if (isRecord) return;
        this.themeConfig = ThemeRecord.getHouseConfig(this.theme);
        setupGizmos(new ArrayList<>(Arrays.asList(new BlockPos(position[0], position[1], position[2]), new BlockPos(size[0] + position[0], size[1] + position[1], size[2] + position[2]))));
    }

    public House(int[] position, int[] size, CobbletTool cobbletTool, boolean isRecord) {
        this(position, size, cobbletTool, isRecord, Theme.BRICKS, HouseRoofType.STEPPED_GABLE, 0, Direction.Axis.X);
    }

    public House(int[] size, CobbletTool cobbletTool, boolean isRecord) {
        this(new int[]{0, 0, 0}, size, cobbletTool, isRecord);
    }

    public House copy() {
        return new House(this.position.clone(), this.size.clone(), super.cobbletTool, super.isRecord, this.theme, this.houseRoofType, this.steppedGableType, this.roofOrientation);
    }

    public void updateSettings(Theme gladeTheme, HouseRoofType houseRoofType, int steppedGableType, Direction.Axis roofOrientation) {
        this.roofOrientation = roofOrientation;
        this.theme = gladeTheme;
        this.themeConfig = ThemeRecord.getHouseConfig(gladeTheme);
        this.houseRoofType = houseRoofType;
        this.steppedGableType = steppedGableType;
        if (super.isRecord) return;
        this.updateGizmoState(new ArrayList<>(Arrays.asList(this.wallPos1Gizmo.getTargetPosition(), this.wallPos2Gizmo.getTargetPosition())));
    }

    public void updateSettings(Direction.Axis roofOrientation) {
        this.roofOrientation = roofOrientation;
        if (!super.isRecord) this.calcBlocks();
    }

    public void setupGizmos(@NotNull ArrayList<BlockPos> blockPositions) {
        BlockPos first = blockPositions.getFirst();
        BlockPos second = blockPositions.getLast();
        float offsetX = first.getX() <= second.getX() ? -0.5F : 0.5F;
        float offsetY = first.getY() <= second.getY() ? -0.5F : 0.5F;
        float offsetZ = first.getZ() <= second.getZ() ? -0.5F : 0.5F;
        float centerX = (float) (first.getX() + second.getX()) / 2.0F;
        float centerY = (float) (first.getY() + second.getY()) / 2.0F;
        float centerZ = (float) (first.getZ() + second.getZ()) / 2.0F;
        this.wallPos1Gizmo = new Gizmo(first, new Vec3d(offsetX, offsetY, offsetZ));
        this.wallPos2Gizmo = new Gizmo(second, new Vec3d((-offsetX), (-offsetY), (-offsetZ)));
        this.mainMoveGizmo = new Gizmo(BlockPos.ofFloored(centerX, centerY, centerZ), new Vec3d((double) centerX - Math.floor(centerX), (double) centerY - Math.floor(centerY), (double) centerZ - Math.floor(centerZ)));
        boolean firstXGreater = this.wallPos1Gizmo.getInterpPosition().getX() > this.wallPos2Gizmo.getInterpPosition().getX();
        boolean firstYGreater = this.wallPos1Gizmo.getInterpPosition().getY() > this.wallPos2Gizmo.getInterpPosition().getY();
        boolean firstZGreater = this.wallPos1Gizmo.getInterpPosition().getZ() > this.wallPos2Gizmo.getInterpPosition().getZ();
        this.wallPos1Gizmo.setAxisDirections(firstXGreater, firstYGreater, firstZGreater);
        this.wallPos2Gizmo.setAxisDirections(!firstXGreater, !firstYGreater, !firstZGreater);
        this.wallPos1Gizmo.enableAxes = false;
        this.wallPos1Gizmo.enableScale = false;
        this.wallPos2Gizmo.enableAxes = false;
        this.wallPos2Gizmo.enableScale = false;
        this.mainMoveGizmo.enableAxes = true;
        this.mainMoveGizmo.enableScale = false;
        this.calcBlocks();
    }

    public void calcBlocks() {
        this.chunkedBlockRegion.clear();
        BlockPos.Mutable mutableBlockPos = new BlockPos.Mutable();
        BlockPos pos1 = this.wallPos1Gizmo.getTargetPosition();
        BlockPos pos2 = this.wallPos2Gizmo.getTargetPosition();
        int minX = Math.min(pos1.getX(), pos2.getX());
        int minY = Math.min(pos1.getY(), pos2.getY());
        int minZ = Math.min(pos1.getZ(), pos2.getZ());
        int maxX = Math.max(pos1.getX(), pos2.getX());
        int maxY = Math.max(pos1.getY(), pos2.getY());
        int maxZ = Math.max(pos1.getZ(), pos2.getZ());

        calcWalls(minY, maxY, minX, maxX, minZ, maxZ);
        calcFloorCeiling(minX, maxX, minZ, maxZ, minY, maxY);
        calcRoof(minX, maxX, minZ, maxZ, minY, maxY);
//        calcEdgesToGround(minX, minZ, maxZ, maxX, mutableBlockPos);
        calcInnerFloors(minX + 1, maxX - 1, minY + 1, maxY - 1, minZ + 1, maxZ - 1);
    }

    private void calcInnerFloors(int minX, int maxX, int minY, int maxY, int minZ, int maxZ) {
        if (this.themeConfig.innerFloorPattern() == null) {
            return;
        }
        ChunkedBlockRegion copyFrom = this.themeConfig.innerFloorPattern().transformedBlockRegions().getBlocks(0, false, false);

        int minXCB = Objects.requireNonNull(copyFrom.min()).getX();
        int minYCB = Objects.requireNonNull(copyFrom.min()).getY();
        int minZCB = Objects.requireNonNull(copyFrom.min()).getZ();
        int maxXCB = Objects.requireNonNull(copyFrom.max()).getX();
        int maxYCB = Objects.requireNonNull(copyFrom.max()).getY();
        int maxZCB = Objects.requireNonNull(copyFrom.max()).getZ();
        this.patternSizeFloor[0] = maxXCB - minXCB + 1;
        this.patternSizeFloor[1] = maxYCB - minYCB + 1;
        this.patternSizeFloor[2] = maxZCB - minZCB + 1;

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    int modX = getPatternMod(x, minXCB, this.patternSizeFloor[0]);
                    int modY = getPatternMod(y, minYCB, this.patternSizeFloor[1]);
                    int modZ = getPatternMod(z, minZCB, this.patternSizeFloor[2]);

                    int copyFromX = getCopyFromCoords(minXCB, modX, this.patternOffsetFloor[0], this.patternSizeFloor[0]);
                    int copyFromY = getCopyFromCoords(minYCB, modY, this.patternOffsetFloor[1], this.patternSizeFloor[1]);
                    int copyFromZ = getCopyFromCoords(minZCB, modZ, this.patternOffsetFloor[2], this.patternSizeFloor[2]);

                    BlockState blockState = copyFrom.getBlockStateOrAir(copyFromX, copyFromY, copyFromZ);
                    if (!blockState.isAir()) {
                        this.chunkedBlockRegion.addBlock(x, y, z, blockState);
                    }
                }
            }
        }
    }

    private void calcRoof(int minX, int maxX, int minZ, int maxZ, int minY, int maxY) {
        BlockState fullRoof = this.themeConfig.fullRoof();
        BlockState slabRoof = this.themeConfig.slabRoof();
        BlockState stairRoof = this.themeConfig.stairRoof();
        BlockState stairRoofMirrored;

        BlockState fullEdge = this.themeConfig.fullEdge();
        BlockState slabEdge = this.themeConfig.slabEdge();
        BlockState stairEdge = this.themeConfig.stairEdge();

        if (this.houseRoofType == HouseRoofType.STEPPED_GABLE || this.houseRoofType == HouseRoofType.GABLE || this.houseRoofType == HouseRoofType.THICK_GABLE || this.houseRoofType == HouseRoofType.LOW_ANGLE || this.houseRoofType == HouseRoofType.HIGH_ANGLE) {
            stairRoof = (this.roofOrientation == Direction.Axis.Z) ? stairRoof.rotate(BlockRotation.CLOCKWISE_90) : stairRoof.rotate(BlockRotation.CLOCKWISE_180);
            stairRoofMirrored = (this.roofOrientation == Direction.Axis.Z) ? stairRoof.mirror(BlockMirror.FRONT_BACK) : stairRoof.mirror(BlockMirror.LEFT_RIGHT);
        } else {
            stairRoof = stairRoof.rotate(BlockRotation.CLOCKWISE_180);
            stairRoofMirrored = stairRoof.mirror(BlockMirror.LEFT_RIGHT);
        }

        switch (this.houseRoofType) {
            case STEPPED_GABLE ->
                    calcSteppedGable(minX, maxX, minZ, maxZ, maxY, slabRoof, stairRoof, stairRoofMirrored);
            case GABLE -> calcGable(minX, maxX, minZ, maxZ, maxY, slabRoof, stairRoof, stairRoofMirrored);
            case SQUARE_GABLE -> calcSquareGable(minX, maxX, minZ, maxZ, maxY, slabRoof, stairRoof, false);
            case THICK_GABLE ->
                    calcThickGable(minX, maxX, minZ, maxZ, maxY, slabRoof, stairRoof, stairRoofMirrored, fullRoof);
            case HIGH_ANGLE ->
                    calcHighAngle(minX, maxX, minZ, maxZ, maxY, slabRoof, stairRoof, stairRoofMirrored, fullRoof);
            case LOW_ANGLE -> calcLowAngle(minX, maxX, minZ, maxZ, maxY, slabRoof, fullRoof);
            case SLABBED -> calcWalledRoof(minX, maxX, minZ, maxZ, maxY, slabRoof);
            case WALL_PATTERN -> calcWallTopPattern(minX, maxX, maxY, minZ, maxZ);
            case WALLED -> calcWalledRoof(minX, maxX, minZ, maxZ, maxY, this.themeConfig.fullWallRoof());
            case THICK_WALLED -> calcThickWalled(minX, maxX, minZ, maxZ, maxY, slabEdge, fullEdge, stairEdge);
            case TOWER -> {
                calcThickWalled(minX, maxX, minZ, maxZ, maxY, slabEdge, fullEdge, stairEdge);
                calcSquareGable(minX - 1, maxX + 1, minZ - 1, maxZ + 1, maxY + 3, slabRoof, stairRoof, true);
            }
        }
    }

    private void calcWallTopPattern(int minXHouse, int maxXHouse, int maxYHouse, int minZHouse, int maxZHouse) {
        if (this.themeConfig.topWallPattern() == null) {
            return;
        }
        int patternCounterX = 0;
        int patternCounterY = 0;
        int patternCounterZ = 0;

        ChunkedBlockRegion copyFrom = this.themeConfig.topWallPattern().transformedBlockRegions().getBlocks(0, false, false);
        ChunkedBlockRegion copyFrom1R = this.themeConfig.topWallPattern().transformedBlockRegions().getBlocks(1, true, false);
        ChunkedBlockRegion copyFrom2R = this.themeConfig.topWallPattern().transformedBlockRegions().getBlocks(2, true, false);
        ChunkedBlockRegion copyFrom3R = this.themeConfig.topWallPattern().transformedBlockRegions().getBlocks(3, false, false);

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

        this.patternSizeTopWall[0] = maxXCB - minXCB + 1;
        this.patternSizeTopWall[1] = maxYCB - minYCB + 1;
        this.patternSizeTopWall[2] = maxZCB - minZCB + 1;

        this.patternSizeTopWall1R[0] = maxXCB1R - minXCB1R + 1;
        this.patternSizeTopWall1R[1] = maxYCB1R - minYCB1R + 1;
        this.patternSizeTopWall1R[2] = maxZCB1R - minZCB1R + 1;

        this.positionOffsetTopWall[0] = this.themeConfig.topWallPattern().positionOffset()[0];
        this.positionOffsetTopWall[1] = this.themeConfig.topWallPattern().positionOffset()[1];
        this.positionOffsetTopWall[2] = this.themeConfig.topWallPattern().positionOffset()[2];

        ChunkedBlockRegion copyFromEdge = this.themeConfig.topWallEdgePattern().transformedBlockRegions().getBlocks(0, false, false);
        int minXEdge = Objects.requireNonNull(copyFromEdge.min()).getX();
        int minZEdge = Objects.requireNonNull(copyFromEdge.min()).getZ();
        int maxXEdge = Objects.requireNonNull(copyFromEdge.max()).getX();
        int maxZEdge = Objects.requireNonNull(copyFromEdge.max()).getZ();

        int edgeSizeX = maxXEdge - minXEdge + 1;
        int edgeSizeZ = maxZEdge - minZEdge + 1;

        int edgeSizeXOffset = (int) Math.ceil((float) edgeSizeX / 2);
        int edgeSizeYOffset = (int) Math.ceil((float) edgeSizeZ / 2);

        int minY = maxYHouse + 1 + this.positionOffsetTopWall[1];
        int maxY = minY + this.patternSizeTopWall[1] + this.positionOffsetTopWall[1];

        for (int y = minY; y <= maxY; y++, patternCounterY++) {
            for (int x = minXHouse + edgeSizeX - edgeSizeXOffset; x <= maxXHouse - edgeSizeX + edgeSizeXOffset; x++, patternCounterX++) {
                int minZ = minZHouse + this.positionOffsetTopWall[2];
                int maxZ = minZ + this.patternSizeTopWall[2] - 1;

                for (int z = minZ; z <= maxZ; z++, patternCounterZ++) {
                    int modX = getPatternMod(patternCounterX, minXCB, this.patternSizeTopWall[0]);
                    int modY = getPatternMod(patternCounterY, minYCB, this.patternSizeTopWall[1]);
                    int modZ = getPatternMod(patternCounterZ, minZCB, this.patternSizeTopWall[2]);

                    int copyFromX = getCopyFromCoords(minXCB, modX, this.patternOffsetTopWallXAxis[0],
                            this.patternSizeTopWall[0]);
                    int copyFromY = getCopyFromCoords(minYCB, modY, this.patternOffsetTopWallXAxis[1],
                            this.patternSizeTopWall[1]);
                    int copyFromZ = getCopyFromCoords(minZCB, modZ, this.patternOffsetTopWallXAxis[2],
                            this.patternSizeTopWall[2]);

                    BlockState blockStateMinZ = copyFrom.getBlockStateOrAir(copyFromX, copyFromY, copyFromZ);
                    if (!blockStateMinZ.isAir()) {
                        this.chunkedBlockRegion.addBlock(x, y, z, blockStateMinZ);
                    }
                }
                patternCounterZ = 0;

                minZ = maxZHouse - this.positionOffsetTopWall[2] - 1;
                maxZ = minZ + this.patternSizeTopWall[2] - 1;

                for (int z = minZ; z <= maxZ; z++, patternCounterZ++) {
                    int modX = getPatternMod(patternCounterX, minXCB2R, this.patternSizeTopWall[0]);
                    int modY = getPatternMod(patternCounterY, minYCB2R, this.patternSizeTopWall[1]);
                    int modZ = getPatternMod(patternCounterZ, minZCB2R, this.patternSizeTopWall[2]);

                    int copyFromX = getCopyFromCoords(minXCB2R, modX, this.patternOffsetTopWallXAxis[0],
                            this.patternSizeTopWall[0]);
                    int copyFromY = getCopyFromCoords(minYCB2R, modY, this.patternOffsetTopWallXAxis[1],
                            this.patternSizeTopWall[1]);
                    int copyFromZ = getCopyFromCoords(minZCB2R, modZ, this.patternOffsetTopWallXAxis[2],
                            this.patternSizeTopWall[2]);

                    BlockState blockStateMinZ = copyFrom2R.getBlockStateOrAir(copyFromX, copyFromY, copyFromZ);
                    if (!blockStateMinZ.isAir()) {
                        this.chunkedBlockRegion.addBlock(x, y, z, blockStateMinZ);
                    }
                }
                patternCounterZ = 0;
            }
            patternCounterX = 0;

            for (int z = minZHouse + edgeSizeZ - edgeSizeYOffset; z <= maxZHouse - edgeSizeZ + edgeSizeYOffset; z++, patternCounterZ++) {
                int minX = minXHouse + this.positionOffsetTopWall[0];
                int maxX = minX + this.patternSizeTopWall1R[0] - 1;

                for (int x = minX; x <= maxX; x++, patternCounterX++) {
                    int modX = getPatternMod(patternCounterX, minXCB1R, this.patternSizeTopWall1R[0]);
                    int modY = getPatternMod(patternCounterY, minYCB1R, this.patternSizeTopWall1R[1]);
                    int modZ = getPatternMod(patternCounterZ, minZCB1R, this.patternSizeTopWall1R[2]);

                    int copyFromX = getCopyFromCoords(minXCB1R, modX, this.patternOffsetTopWallZAxis[2],
                            this.patternSizeTopWall1R[0]);
                    int copyFromY = getCopyFromCoords(minYCB1R, modY, this.patternOffsetTopWallZAxis[1],
                            this.patternSizeTopWall1R[1]);
                    int copyFromZ = getCopyFromCoords(minZCB1R, modZ, this.patternOffsetTopWallZAxis[0],
                            this.patternSizeTopWall1R[2]);

                    BlockState blockStateMinZ = copyFrom1R.getBlockStateOrAir(copyFromX, copyFromY, copyFromZ);
                    if (!blockStateMinZ.isAir()) {
                        this.chunkedBlockRegion.addBlock(x, y, z, blockStateMinZ);
                    }
                }
                patternCounterX = 0;

                minX = maxXHouse - this.positionOffsetTopWall[0] - 1;
                maxX = minX + this.patternSizeTopWall1R[0] - 1;

                for (int x = minX; x <= maxX; x++, patternCounterX++) {
                    int modX = getPatternMod(patternCounterX, minXCB3R, this.patternSizeTopWall1R[0]);
                    int modY = getPatternMod(patternCounterY, minYCB3R, this.patternSizeTopWall1R[1]);
                    int modZ = getPatternMod(patternCounterZ, minZCB3R, this.patternSizeTopWall1R[2]);

                    int copyFromX = getCopyFromCoords(minXCB3R, modX, this.patternOffsetTopWallZAxis[2],
                            this.patternSizeTopWall1R[0]);
                    int copyFromY = getCopyFromCoords(minYCB3R, modY, this.patternOffsetTopWallZAxis[1],
                            this.patternSizeTopWall1R[1]);
                    int copyFromZ = getCopyFromCoords(minZCB3R, modZ, this.patternOffsetTopWallZAxis[0],
                            this.patternSizeTopWall1R[2]);

                    BlockState blockStateMinZ = copyFrom3R.getBlockStateOrAir(copyFromX, copyFromY, copyFromZ);
                    if (!blockStateMinZ.isAir()) {
                        this.chunkedBlockRegion.addBlock(x, y, z, blockStateMinZ);
                    }
                }
                patternCounterX = 0;
            }
            patternCounterZ = 0;
        }

        int positionOffsetEdgeX = this.themeConfig.topWallEdgePattern().positionOffset()[0];
        int positionOffsetEdgeY = this.themeConfig.topWallEdgePattern().positionOffset()[1];
        int positionOffsetEdgeZ = this.themeConfig.topWallEdgePattern().positionOffset()[2];

        ChunkedBlockRegion edgePattern0 = this.themeConfig.topWallEdgePattern().
                transformedBlockRegions().getBlocks(0, false, false);
        edgePattern0.forEachEntry((x, y, z, state) -> this.chunkedBlockRegion.addBlock(
                x + maxXHouse - positionOffsetEdgeX,
                y + minY + positionOffsetEdgeY,
                z + minZHouse + positionOffsetEdgeZ, state)
        );

        ChunkedBlockRegion edgePattern1 = this.themeConfig.topWallEdgePattern().
                transformedBlockRegions().getBlocks(1, false, false);
        edgePattern1.forEachEntry((x, y, z, state) -> this.chunkedBlockRegion.addBlock(
                x + minXHouse + positionOffsetEdgeZ,
                y + minY + positionOffsetEdgeY,
                z + minZHouse + positionOffsetEdgeX, state)
        );

        ChunkedBlockRegion edgePattern2 = this.themeConfig.topWallEdgePattern().
                transformedBlockRegions().getBlocks(2, false, false);
        edgePattern2.forEachEntry((x, y, z, state) -> this.chunkedBlockRegion.addBlock(
                x + minXHouse + positionOffsetEdgeX,
                y + minY + positionOffsetEdgeY,
                z + maxZHouse - positionOffsetEdgeZ, state)
        );

        ChunkedBlockRegion edgePattern3 = this.themeConfig.topWallEdgePattern().
                transformedBlockRegions().getBlocks(3, false, false);
        edgePattern3.forEachEntry((x, y, z, state) -> this.chunkedBlockRegion.addBlock(
                x + maxXHouse - positionOffsetEdgeZ,
                y + minY + positionOffsetEdgeY,
                z + maxZHouse - positionOffsetEdgeX, state)
        );
    }

    private void calcSquareGable(int minX, int maxX, int minZ, int maxZ, int maxY, @NotNull BlockState slab, @NotNull BlockState stair, boolean isTower) {
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
            this.chunkedBlockRegion.addBlockIfNotPresent(minXIt, Y, minZ - 1, upperSlab);
            this.chunkedBlockRegion.addBlockIfNotPresent(maxXIt, Y, minZ - 1, upperSlab);
            this.chunkedBlockRegion.addBlockIfNotPresent(minXIt, Y, maxZ + 1, upperSlab);
            this.chunkedBlockRegion.addBlockIfNotPresent(maxXIt, Y, maxZ + 1, upperSlab);
        }
        for (int maxZIt = maxZ + 1, minZIt = minZ - 1; minZIt <= maxZIt; minZIt++, maxZIt--) {
            this.chunkedBlockRegion.addBlockIfNotPresent(minX - 1, Y, minZIt, upperSlab);
            this.chunkedBlockRegion.addBlockIfNotPresent(minX - 1, Y, maxZIt, upperSlab);
            this.chunkedBlockRegion.addBlockIfNotPresent(maxX + 1, Y, minZIt, upperSlab);
            this.chunkedBlockRegion.addBlockIfNotPresent(maxX + 1, Y, maxZIt, upperSlab);
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
                this.chunkedBlockRegion.addBlockIfNotPresent(minXIt, Y, minZZ, minXIt == minXX ? stairMinXMinZ : stair);
                this.chunkedBlockRegion.addBlockIfNotPresent(maxXIt, Y, minZZ, maxXIt == maxXX ? stairMaxXMinZ : stair);
                this.chunkedBlockRegion.addBlockIfNotPresent(minXIt, Y, maxZZ, minXIt == minXX ? stairMinXMaxZ : stair180);
                this.chunkedBlockRegion.addBlockIfNotPresent(maxXIt, Y, maxZZ, maxXIt == maxXX ? stairMaxXMaxZ : stair180);
            }
            for (int maxZIt = maxZ - offset, minZIt = minZ + offset; minZIt <= maxZIt; minZIt++, maxZIt--) {
                this.chunkedBlockRegion.addBlockIfNotPresent(minXX, Y, minZIt, minZIt == minZZ ? stairMinXMinZ : stair270);
                this.chunkedBlockRegion.addBlockIfNotPresent(minXX, Y, maxZIt, maxZIt == maxZZ ? stairMinXMaxZ : stair270);
                this.chunkedBlockRegion.addBlockIfNotPresent(maxXX, Y, minZIt, minZIt == minZZ ? stairMaxXMinZ : stair90);
                this.chunkedBlockRegion.addBlockIfNotPresent(maxXX, Y, maxZIt, maxZIt == maxZZ ? stairMaxXMaxZ : stair90);
            }
            minZZ++;
            maxZZ--;
            minXX++;
            maxXX--;
            Y++;
            offset++;
        }
    }

    private void calcThickWalled(int minX, int maxX, int minZ, int maxZ, int maxY, @NotNull BlockState slab, @NotNull BlockState full, @NotNull BlockState stair) {
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
            this.chunkedBlockRegion.addBlockIfNotPresent(minX, Y, minZ, full);
            this.chunkedBlockRegion.addBlockIfNotPresent(minX - 1, Y, minZ, iteration == 2 ? stair90 : full);
            this.chunkedBlockRegion.addBlockIfNotPresent(minX, Y, minZ - 1, iteration == 2 ? stair180 : full);
            this.chunkedBlockRegion.addBlockIfNotPresent(minX - 1, Y, minZ - 1, iteration == 2 ? stairMinXMinZ : full);

            this.chunkedBlockRegion.addBlockIfNotPresent(maxX, Y, maxZ, full);
            this.chunkedBlockRegion.addBlockIfNotPresent(maxX + 1, Y, maxZ, iteration == 2 ? stair270 : full);
            this.chunkedBlockRegion.addBlockIfNotPresent(maxX, Y, maxZ + 1, iteration == 2 ? stair : full);
            this.chunkedBlockRegion.addBlockIfNotPresent(maxX + 1, Y, maxZ + 1, iteration == 2 ? stairMaxXMaxZ : full);

            this.chunkedBlockRegion.addBlockIfNotPresent(minX, Y, maxZ, full);
            this.chunkedBlockRegion.addBlockIfNotPresent(minX - 1, Y, maxZ, iteration == 2 ? stair90 : full);
            this.chunkedBlockRegion.addBlockIfNotPresent(minX, Y, maxZ + 1, iteration == 2 ? stair : full);
            this.chunkedBlockRegion.addBlockIfNotPresent(minX - 1, Y, maxZ + 1, iteration == 2 ? stairMinXMaxZ : full);

            this.chunkedBlockRegion.addBlockIfNotPresent(maxX, Y, minZ, full);
            this.chunkedBlockRegion.addBlockIfNotPresent(maxX, Y, minZ - 1, iteration == 2 ? stair180 : full);
            this.chunkedBlockRegion.addBlockIfNotPresent(maxX + 1, Y, minZ, iteration == 2 ? stair270 : full);
            this.chunkedBlockRegion.addBlockIfNotPresent(maxX + 1, Y, minZ - 1, iteration == 2 ? stairMaxXMinZ : full);

            boolean placeExtrudedWall = false;
            for (int maxXIt = maxX - 1, minXIt = minX + 1; minXIt <= maxXIt; minXIt++, maxXIt--) {
                if (placeExtrudedWall) {
                    this.chunkedBlockRegion.addBlockIfNotPresent(minXIt, Y, minZ, full);
                    this.chunkedBlockRegion.addBlockIfNotPresent(minXIt, Y, minZ - 1, iteration == 2 ? stair180 : full);

                    this.chunkedBlockRegion.addBlockIfNotPresent(maxXIt, Y, minZ, full);
                    this.chunkedBlockRegion.addBlockIfNotPresent(maxXIt, Y, minZ - 1, iteration == 2 ? stair180 : full);

                    this.chunkedBlockRegion.addBlockIfNotPresent(minXIt, Y, maxZ, full);
                    this.chunkedBlockRegion.addBlockIfNotPresent(minXIt, Y, maxZ + 1, iteration == 2 ? stair : full);

                    this.chunkedBlockRegion.addBlockIfNotPresent(maxXIt, Y, maxZ, full);
                    this.chunkedBlockRegion.addBlockIfNotPresent(maxXIt, Y, maxZ + 1, iteration == 2 ? stair : full);
                } else {
                    if (iteration == 2) {
                        this.chunkedBlockRegion.addBlockIfNotPresent(minXIt, Y, minZ, slab);
                        this.chunkedBlockRegion.addBlockIfNotPresent(maxXIt, Y, minZ, slab);
                        this.chunkedBlockRegion.addBlockIfNotPresent(minXIt, Y, maxZ, slab);
                        this.chunkedBlockRegion.addBlockIfNotPresent(maxXIt, Y, maxZ, slab);
                    }
                }
                placeExtrudedWall = !placeExtrudedWall;
            }

            placeExtrudedWall = false;
            for (int maxZIt = maxZ - 1, minZIt = minZ + 1; minZIt <= maxZIt; minZIt++, maxZIt--) {
                if (placeExtrudedWall) {
                    this.chunkedBlockRegion.addBlockIfNotPresent(minX, Y, minZIt, full);
                    this.chunkedBlockRegion.addBlockIfNotPresent(minX - 1, Y, minZIt, iteration == 2 ? stair90 : full);

                    this.chunkedBlockRegion.addBlockIfNotPresent(minX, Y, maxZIt, full);
                    this.chunkedBlockRegion.addBlockIfNotPresent(minX - 1, Y, maxZIt, iteration == 2 ? stair90 : full);

                    this.chunkedBlockRegion.addBlockIfNotPresent(maxX, Y, minZIt, full);
                    this.chunkedBlockRegion.addBlockIfNotPresent(maxX + 1, Y, minZIt, iteration == 2 ? stair270 : full);

                    this.chunkedBlockRegion.addBlockIfNotPresent(maxX, Y, maxZIt, full);
                    this.chunkedBlockRegion.addBlockIfNotPresent(maxX + 1, Y, maxZIt, iteration == 2 ? stair270 : full);
                } else {
                    if (iteration == 2) {
                        this.chunkedBlockRegion.addBlockIfNotPresent(minX, Y, minZIt, slab);
                        this.chunkedBlockRegion.addBlockIfNotPresent(minX, Y, maxZIt, slab);
                        this.chunkedBlockRegion.addBlockIfNotPresent(maxX, Y, minZIt, slab);
                        this.chunkedBlockRegion.addBlockIfNotPresent(maxX, Y, maxZIt, slab);
                    }
                }
                placeExtrudedWall = !placeExtrudedWall;
            }
            Y++;
        }
    }

    private void calcWalledRoof(int minX, int maxX, int minZ, int maxZ, int maxY, @NotNull BlockState block) {
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
        this.chunkedBlockRegion.addBlockIfNotPresent(maxX, y, maxZ, maxXmaxZ);
        this.chunkedBlockRegion.addBlockIfNotPresent(minX, y, maxZ, minXmaxZ);
        this.chunkedBlockRegion.addBlockIfNotPresent(maxX, y, minZ, maxXminZ);
        this.chunkedBlockRegion.addBlockIfNotPresent(minX, y, minZ, minXminZ);
        for (int x = minX + 1; x < maxX; x++) {
            this.chunkedBlockRegion.addBlockIfNotPresent(x, y, minZ, wallEastWest);
            this.chunkedBlockRegion.addBlockIfNotPresent(x, y, maxZ, wallEastWest);

        }
        for (int z = minZ + 1; z < maxZ; z++) {
            this.chunkedBlockRegion.addBlockIfNotPresent(minX, y, z, wallNorthSouth);
            this.chunkedBlockRegion.addBlockIfNotPresent(maxX, y, z, wallNorthSouth);
        }
    }

    private void calcThickGable(int minX, int maxX, int minZ, int maxZ, int maxY, BlockState slabRoofBlock, BlockState roofStairBlock, BlockState roofStairBlockMirrored, BlockState fullRoof) {
        float width = this.roofOrientation == Direction.Axis.Z ? Math.abs(minX - maxX) + 1 : Math.abs(minZ - maxZ) + 1;
        int x, z, iterations = 0, xMirrored, zMirrored;
        boolean hasSingleRidge = width % 2 > 0;
        width = hasSingleRidge ? width + 1 : width;
        if (this.roofOrientation == Direction.Axis.Z) {
            x = minX - 1;
            xMirrored = maxX + 1;
            while (iterations <= width) {
                for (z = minZ - 1; z <= maxZ + 1; z++) {
                    int y = (int) Math.floor((double) iterations / 2) + maxY;
                    this.chunkedBlockRegion.addBlock(x, y, z, fullRoof);
                    if (hasSingleRidge && iterations == width) {
                        this.chunkedBlockRegion.addBlock(x, y + 1, z, slabRoofBlock);
                    } else {
                        this.chunkedBlockRegion.addBlock(x, y + 1, z, roofStairBlock);
                        this.chunkedBlockRegion.addBlock(xMirrored, y, z, fullRoof);
                        this.chunkedBlockRegion.addBlock(xMirrored, y + 1, z, roofStairBlockMirrored);
                    }
                    if ((z == minZ || z == maxZ) && y > maxY) {
                        for (int gableEndWall = x + 1; gableEndWall < xMirrored; gableEndWall++) {
                            this.chunkedBlockRegion.addBlock(gableEndWall, y, z, this.themeConfig.fullWall());
                            this.chunkedBlockRegion.addBlock(gableEndWall, y + 1, z, this.themeConfig.fullWall());
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
                    this.chunkedBlockRegion.addBlock(x, y, z, fullRoof);
                    if (hasSingleRidge && iterations == width) {
                        this.chunkedBlockRegion.addBlock(x, y + 1, z, slabRoofBlock);
                    } else {
                        this.chunkedBlockRegion.addBlock(x, y + 1, z, roofStairBlock);
                        this.chunkedBlockRegion.addBlock(x, y, zMirrored, fullRoof);
                        this.chunkedBlockRegion.addBlock(x, y + 1, zMirrored, roofStairBlockMirrored);
                    }
                    if ((x == minX || x == maxX) && y > maxY) {
                        for (int gableEndWall = z + 1; gableEndWall < zMirrored; gableEndWall++) {
                            this.chunkedBlockRegion.addBlock(x, y, gableEndWall, this.themeConfig.fullWall());
                            this.chunkedBlockRegion.addBlock(x, y + 1, gableEndWall, this.themeConfig.fullWall());
                        }
                    }
                }
                z += 1;
                zMirrored -= 1;
                iterations += 2;
            }
        }
    }

    private void calcHighAngle(int minX, int maxX, int minZ, int maxZ, int maxY, BlockState slabRoofBlock, BlockState roofStairBlock, BlockState roofStairBlockMirrored, BlockState fullRoof) {
        float width = this.roofOrientation == Direction.Axis.Z ? Math.abs(minX - maxX) + 1 : Math.abs(minZ - maxZ) + 1;
        int x, z, iterations = 0, xMirrored, zMirrored;
        boolean hasSingleRidge = width % 2 > 0;
        width = hasSingleRidge ? width + 1 : width;
        if (this.roofOrientation == Direction.Axis.Z) {
            x = minX - 1;
            xMirrored = maxX + 1;
            while (iterations <= width) {
                for (z = minZ - 1; z <= maxZ + 1; z++) {
                    int y = iterations + maxY;
                    this.chunkedBlockRegion.addBlock(x, y, z, fullRoof);
                    if (hasSingleRidge && iterations == width) {
                        this.chunkedBlockRegion.addBlock(x, y + 1, z, slabRoofBlock);
                    } else {
                        this.chunkedBlockRegion.addBlock(x, y + 1, z, roofStairBlock);
                        this.chunkedBlockRegion.addBlock(xMirrored, y, z, fullRoof);
                        this.chunkedBlockRegion.addBlock(xMirrored, y + 1, z, roofStairBlockMirrored);
                    }
                    y = y > maxY ? y : y + 1;
                    if ((z == minZ || z == maxZ) && y > maxY) {
                        for (int gableEndWall = x + 1; gableEndWall < xMirrored; gableEndWall++) {
                            this.chunkedBlockRegion.addBlock(gableEndWall, y, z, this.themeConfig.fullWall());
                            this.chunkedBlockRegion.addBlock(gableEndWall, y + 1, z, this.themeConfig.fullWall());
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
                    this.chunkedBlockRegion.addBlock(x, y, z, fullRoof);
                    if (hasSingleRidge && iterations == width) {
                        this.chunkedBlockRegion.addBlock(x, y + 1, z, slabRoofBlock);
                    } else {
                        this.chunkedBlockRegion.addBlock(x, y + 1, z, roofStairBlock);
                        this.chunkedBlockRegion.addBlock(x, y, zMirrored, fullRoof);
                        this.chunkedBlockRegion.addBlock(x, y + 1, zMirrored, roofStairBlockMirrored);
                    }
                    y = y > maxY ? y : y + 1;
                    if ((x == minX || x == maxX) && y > maxY) {
                        for (int gableEndWall = z + 1; gableEndWall < zMirrored; gableEndWall++) {
                            this.chunkedBlockRegion.addBlock(x, y, gableEndWall, this.themeConfig.fullWall());
                            this.chunkedBlockRegion.addBlock(x, y + 1, gableEndWall, this.themeConfig.fullWall());
                        }
                    }
                }
                z += 1;
                zMirrored -= 1;
                iterations += 2;
            }
        }
    }

    private void calcLowAngle(int minX, int maxX, int minZ, int maxZ, int maxY, @NotNull BlockState slabRoofBlock, BlockState fullRoof) {
        float width = this.roofOrientation == Direction.Axis.Z ? Math.abs(minX - maxX) + 1 : Math.abs(minZ - maxZ) + 1;
        float roofHeight;
        int x, z, iterations = 0, xMirrored, zMirrored;
        roofHeight = (float) Math.ceil(width / 2);
        BlockState upperSlab = slabRoofBlock.cycle(SlabBlock.TYPE).cycle(SlabBlock.TYPE);
        if (this.roofOrientation == Direction.Axis.Z) {
            x = minX - 1;
            xMirrored = maxX + 1;
            while (iterations <= roofHeight) {
                for (z = minZ - 1; z <= maxZ + 1; z++) {
                    int y = (int) Math.floor((double) iterations / 2) + maxY;
                    if ((z == minZ || z == maxZ) && iterations % 2 != 0 && x < maxX) {
                        this.chunkedBlockRegion.addBlock(x, y, z, fullRoof);
                        this.chunkedBlockRegion.addBlock(xMirrored, y, z, fullRoof);
                    } else {
                        this.chunkedBlockRegion.addBlock(x, y, z, (iterations % 2 != 0) ? upperSlab : slabRoofBlock);
                        this.chunkedBlockRegion.addBlock(xMirrored, y, z, (iterations % 2 != 0) ? upperSlab : slabRoofBlock);
                    }
                    if ((z == minZ || z == maxZ) && y > maxY) {
                        for (int gableEndWall = x + 1; gableEndWall < xMirrored; gableEndWall++) {
                            this.chunkedBlockRegion.addBlock(gableEndWall, y, z, this.themeConfig.fullWall());
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
                        this.chunkedBlockRegion.addBlock(x, y, z, fullRoof);
                        this.chunkedBlockRegion.addBlock(x, y, zMirrored, fullRoof);
                    } else {
                        this.chunkedBlockRegion.addBlock(x, y, z, (iterations % 2 != 0) ? upperSlab : slabRoofBlock);
                        this.chunkedBlockRegion.addBlock(x, y, zMirrored, (iterations % 2 != 0) ? upperSlab : slabRoofBlock);
                    }
                    if ((x == minX || x == maxX) && y > maxY) {
                        for (int gableEndWall = z + 1; gableEndWall < zMirrored; gableEndWall++) {
                            this.chunkedBlockRegion.addBlock(x, y, gableEndWall, this.themeConfig.fullWall());
                        }
                    }
                }
                z += 1;
                zMirrored -= 1;
                iterations += 1;
            }
        }
    }

    private void calcSteppedGable(int minX, int maxX, int minZ, int maxZ, int maxY, BlockState slabRoof, BlockState roofStair, BlockState roofStairMirrored) {
        float width = this.roofOrientation == Direction.Axis.Z ? Math.abs(minX - maxX) + 1 : Math.abs(minZ - maxZ) + 1;
        HDVoxelMap.HDVoxelBaseBlocks wallBlocks = HDVoxelMap.getAssociatedBlocks(this.themeConfig.fullWall().getBlock());
        BlockState steppedRoofBlock;
        BlockState steppedRoofBlockMirrored;
        switch (this.steppedGableType) {
            case 0 -> steppedRoofBlock = wallBlocks.slab().getDefaultState();
            case 1 ->
                    steppedRoofBlock = this.roofOrientation == Direction.Axis.Z ? wallBlocks.stair().getDefaultState().rotate(BlockRotation.CLOCKWISE_180) : wallBlocks.stair().getDefaultState().rotate(BlockRotation.CLOCKWISE_90);
            case 2 -> steppedRoofBlock = wallBlocks.full().getDefaultState();
            default -> steppedRoofBlock = wallBlocks.stair().getDefaultState();
        }
        steppedRoofBlockMirrored = steppedRoofBlock;
        float roofHeight;
        int iterations = 1, x, y, z, xMirrored, zMirrored;
        boolean hasSingleRidge = width % 2 > 0;
        roofHeight = (float) Math.ceil(width / 2);
        y = maxY + 1;
        if (this.roofOrientation == Direction.Axis.Z) {
            if (this.steppedGableType == 1)
                steppedRoofBlockMirrored = steppedRoofBlockMirrored.mirror(BlockMirror.LEFT_RIGHT);
            xMirrored = maxX;
            x = minX;
            while (iterations <= roofHeight) {
                for (z = minZ; z <= maxZ; z++) {
                    if (hasSingleRidge && iterations == roofHeight) {
                        this.chunkedBlockRegion.addBlock(x, y, z, slabRoof);
                    } else {
                        this.chunkedBlockRegion.addBlock(x, y, z, roofStair);
                        this.chunkedBlockRegion.addBlock(xMirrored, y, z, roofStairMirrored);
                    }
                }
                for (int xX = x; xX <= xMirrored; xX++) {
                    this.chunkedBlockRegion.addBlock(xX, y, minZ, this.themeConfig.fullWall());
                    this.chunkedBlockRegion.addBlock(xX, y, maxZ, this.themeConfig.fullWall());
                }
                this.chunkedBlockRegion.addBlock(x, y + 1, maxZ, steppedRoofBlock);
                this.chunkedBlockRegion.addBlock(x, y + 1, minZ, steppedRoofBlockMirrored);
                this.chunkedBlockRegion.addBlock(xMirrored, y + 1, maxZ, steppedRoofBlock);
                this.chunkedBlockRegion.addBlock(xMirrored, y + 1, minZ, steppedRoofBlockMirrored);
                x += 1;
                y += 1;
                xMirrored -= 1;
                iterations++;
            }
        } else {
            if (this.steppedGableType == 1)
                steppedRoofBlockMirrored = steppedRoofBlockMirrored.mirror(BlockMirror.FRONT_BACK);
            zMirrored = maxZ;
            z = minZ;
            while (iterations <= roofHeight) {
                for (x = minX; x <= maxX; x++) {
                    if (hasSingleRidge && iterations == roofHeight) {
                        this.chunkedBlockRegion.addBlock(x, y, z, slabRoof);
                    } else {
                        this.chunkedBlockRegion.addBlock(x, y, z, roofStair);
                        this.chunkedBlockRegion.addBlock(x, y, zMirrored, roofStairMirrored);
                    }
                }
                for (int zZ = z; zZ <= zMirrored; zZ++) {
                    this.chunkedBlockRegion.addBlock(maxX, y, zZ, this.themeConfig.fullWall());
                    this.chunkedBlockRegion.addBlock(minX, y, zZ, this.themeConfig.fullWall());
                }
                this.chunkedBlockRegion.addBlock(maxX, y + 1, z, steppedRoofBlock);
                this.chunkedBlockRegion.addBlock(minX, y + 1, z, steppedRoofBlockMirrored);
                this.chunkedBlockRegion.addBlock(maxX, y + 1, zMirrored, steppedRoofBlock);
                this.chunkedBlockRegion.addBlock(minX, y + 1, zMirrored, steppedRoofBlockMirrored);
                z += 1;
                y += 1;
                zMirrored -= 1;
                iterations++;
            }
        }
    }

    private void calcGable(int minX, int maxX, int minZ, int maxZ, int maxY, BlockState slabRoofBlock, BlockState roofStairBlock, BlockState roofStairBlockMirrored) {
        float width = this.roofOrientation == Direction.Axis.Z ? Math.abs(minX - maxX) + 1 : Math.abs(minZ - maxZ) + 1;
        float roofHeight;
        int iterations = 0, x, y, z, xMirrored, zMirrored;
        boolean hasSingleRidge = width % 2 > 0;
        roofHeight = (float) Math.ceil(width / 2);
        if (this.roofOrientation == Direction.Axis.Z) {
            xMirrored = maxX + 1;
            x = minX - 1;
            y = maxY;
            while (iterations <= roofHeight) {
                for (z = minZ - 1; z <= maxZ + 1; z++) {
                    if (hasSingleRidge && iterations == roofHeight) {
                        this.chunkedBlockRegion.addBlock(x, y, z, slabRoofBlock);
                    } else {
                        this.chunkedBlockRegion.addBlock(x, y, z, roofStairBlock);
                        this.chunkedBlockRegion.addBlock(xMirrored, y, z, roofStairBlockMirrored);
                    }
                    if ((z == minZ || z == maxZ) && y > maxY) {
                        for (int gableEndWall = x + 1; gableEndWall < xMirrored; gableEndWall++) {
                            this.chunkedBlockRegion.addBlock(gableEndWall, y, z, this.themeConfig.fullWall());
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
                        this.chunkedBlockRegion.addBlock(x, y, z, slabRoofBlock);
                    } else {
                        this.chunkedBlockRegion.addBlock(x, y, z, roofStairBlock);
                        this.chunkedBlockRegion.addBlock(x, y, zMirrored, roofStairBlockMirrored);
                    }
                    if ((x == minX || x == maxX) && y > maxY) {
                        for (int gableEndWall = z + 1; gableEndWall < zMirrored; gableEndWall++) {
                            this.chunkedBlockRegion.addBlock(x, y, gableEndWall, this.themeConfig.fullWall());
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

    private void calcEdgesToGround(int minX, int minZ, int maxZ, int maxX, BlockPos.Mutable mutableBlockPos) {
        Position2ObjectMap<BlockState> newBlocks = new Position2ObjectMap<>((k) -> new BlockState[4096]);
        World level = MinecraftClient.getInstance().world;
        if (level == null) {
            return;
        }
        this.chunkedBlockRegion.forEachEntry((x, y, z, block) -> {
            if ((x == minX && z == minZ) || (x == minX && z == maxZ) || (x == maxX && z == minZ) || (x == maxX && z == maxZ)) {
                for (int yo = 1; yo < 256 && (this.chunkedBlockRegion.getBlockStateOrAir(x, y - yo, z).isAir() || this.chunkedBlockRegion.getBlockStateOrAir(x, y - yo, z).getBlock() instanceof StairsBlock || this.chunkedBlockRegion.getBlockStateOrAir(x, y - yo, z).getBlock() instanceof SlabBlock || this.chunkedBlockRegion.getBlockStateOrAir(x, y - yo, z).getBlock() instanceof FenceBlock || this.chunkedBlockRegion.getBlockStateOrAir(x, y - yo, z).getBlock() instanceof FenceGateBlock || this.chunkedBlockRegion.getBlockStateOrAir(x, y - yo, z).getBlock() instanceof WallBlock); ++yo) {
                    BlockState below = level.getBlockState(mutableBlockPos.set(x, y - yo, z));
                    if (below.getBlock() == Blocks.VOID_AIR || !below.isReplaceable()) {
                        break;
                    }
                    newBlocks.put(x, y - yo, z, block);
                }
            }
        });
        newBlocks.forEachEntry(this.chunkedBlockRegion::addBlock);
    }

    private void calcFloorCeiling(int minX, int maxX, int minZ, int maxZ, int minY, int maxY) {
        for (int x = minX + 1; x <= maxX - 1; x++) {
            for (int z = minZ + 1; z <= maxZ - 1; z++) {
                this.chunkedBlockRegion.addBlock(x, minY, z, this.themeConfig.fullWall());
                this.chunkedBlockRegion.addBlock(x, maxY, z, this.themeConfig.fullWall());
            }
        }
    }

    private void calcWalls(int minY, int maxY, int minX, int maxX, int minZ, int maxZ) {
        if (this.themeConfig.fullWallPattern() == null) {
            return;
        }
        ChunkedBlockRegion copyFrom = this.themeConfig.fullWallPattern().transformedBlockRegions().getBlocks(0, false, false);
        ChunkedBlockRegion copyFrom1R = this.themeConfig.fullWallPattern().transformedBlockRegions().getBlocks(1, false, false);

        int minXCB = Objects.requireNonNull(copyFrom.min()).getX();
        int minYCB = Objects.requireNonNull(copyFrom.min()).getY();
        int minZCB = Objects.requireNonNull(copyFrom.min()).getZ();
        int maxXCB = Objects.requireNonNull(copyFrom.max()).getX();
        int maxYCB = Objects.requireNonNull(copyFrom.max()).getY();
        int maxZCB = Objects.requireNonNull(copyFrom.max()).getZ();
        this.patternSizeFullWall[0] = maxXCB - minXCB + 1;
        this.patternSizeFullWall[1] = maxYCB - minYCB + 1;
        this.patternSizeFullWall[2] = maxZCB - minZCB + 1;

        int minXCB1R = Objects.requireNonNull(copyFrom1R.min()).getX();
        int minYCB1R = Objects.requireNonNull(copyFrom1R.min()).getY();
        int minZCB1R = Objects.requireNonNull(copyFrom1R.min()).getZ();
        int maxXCB1R = Objects.requireNonNull(copyFrom1R.max()).getX();
        int maxYCB1R = Objects.requireNonNull(copyFrom1R.max()).getY();
        int maxZCB1R = Objects.requireNonNull(copyFrom1R.max()).getZ();
        this.patternSizeFullWall1R[0] = maxXCB1R - minXCB1R + 1;
        this.patternSizeFullWall1R[1] = maxYCB1R - minYCB1R + 1;
        this.patternSizeFullWall1R[2] = maxZCB1R - minZCB1R + 1;

        for (int y = minY; y <= maxY; y++) {

            for (int x = minX; x <= maxX; x++) {
                int modX = getPatternMod(x, minXCB, this.patternSizeFullWall[0]);
                int modY = getPatternMod(y, minYCB, this.patternSizeFullWall[1]);
                int modMinZ = getPatternMod(minZ, minZCB, this.patternSizeFullWall[2]);
                int modMaxZ = getPatternMod(maxZ, minZCB, this.patternSizeFullWall[2]);

                int copyFromX = getCopyFromCoords(minXCB, modX, this.patternOffsetFullWallXAxis[0], this.patternSizeFullWall[0]);
                int copyFromY = getCopyFromCoords(minYCB, modY, this.patternOffsetFullWallXAxis[1], this.patternSizeFullWall[1]);
                int copyFromZMin = getCopyFromCoords(minZCB, modMinZ, this.patternOffsetFullWallXAxis[2], this.patternSizeFullWall[2]);
                int copyFromZMax = getCopyFromCoords(minZCB, modMaxZ, this.patternOffsetFullWallXAxis[2], this.patternSizeFullWall[2]);

                BlockState blockStateMinZ = copyFrom.getBlockStateOrAir(copyFromX, copyFromY, copyFromZMin);
                if (!blockStateMinZ.isAir()) {
                    this.chunkedBlockRegion.addBlock(x, y, minZ, blockStateMinZ);
                }
                BlockState blockStateMaxZ = copyFrom.getBlockStateOrAir(copyFromX, copyFromY, copyFromZMax);
                if (!blockStateMaxZ.isAir()) {
                    this.chunkedBlockRegion.addBlock(x, y, maxZ, blockStateMaxZ);
                }
            }

            for (int z = minZ; z <= maxZ; z++) {
                int modMinX = getPatternMod(minX, minXCB1R, this.patternSizeFullWall1R[0]);
                int modMaxX = getPatternMod(maxX, minXCB1R, this.patternSizeFullWall1R[0]);
                int modY = getPatternMod(y, minYCB1R, this.patternSizeFullWall1R[1]);
                int modZ = getPatternMod(z, minZCB1R, this.patternSizeFullWall1R[2]);

                int copyFromXMin = getCopyFromCoords(minXCB1R, modMinX, this.patternOffsetFullWallZAxis[2], this.patternSizeFullWall1R[0]);
                int copyFromXMax = getCopyFromCoords(minXCB1R, modMaxX, this.patternOffsetFullWallZAxis[2], this.patternSizeFullWall1R[0]);
                int copyFromY = getCopyFromCoords(minYCB1R, modY, this.patternOffsetFullWallZAxis[1], this.patternSizeFullWall1R[1]);
                int copyFromZ = getCopyFromCoords(minZCB1R, modZ, this.patternOffsetFullWallZAxis[0], this.patternSizeFullWall1R[2]);

                BlockState blockStateMinZ = copyFrom1R.getBlockStateOrAir(copyFromXMin, copyFromY, copyFromZ);
                if (!blockStateMinZ.isAir()) {
                    this.chunkedBlockRegion.addBlock(minX, y, z, blockStateMinZ);
                }
                BlockState blockStateMaxZ = copyFrom1R.getBlockStateOrAir(copyFromXMax, copyFromY, copyFromZ);
                if (!blockStateMaxZ.isAir()) {
                    this.chunkedBlockRegion.addBlock(maxX, y, z, blockStateMaxZ);
                }
            }
            this.chunkedBlockRegion.addBlock(minX, y, minZ, this.themeConfig.fullEdge());
            this.chunkedBlockRegion.addBlock(minX, y, maxZ, this.themeConfig.fullEdge());
            this.chunkedBlockRegion.addBlock(maxX, y, minZ, this.themeConfig.fullEdge());
            this.chunkedBlockRegion.addBlock(maxX, y, maxZ, this.themeConfig.fullEdge());
        }
    }

    private int getCopyFromCoords(int min, int mod, int offset, int size) {
        int value = min + mod - offset;
        return value < min ? value + size : value;
    }

    private int getPatternMod(int value, int min, int size) {
        int mod = value % size;
        return (mod < min) ? mod + size : mod;
    }

    public int[] getInnerFloorPatternOffset() {
        return this.patternOffsetFloor;
    }

    public void setInnerFloorPatternOffset(int[] patternOffset) {
        if (patternOffset.length != 3) {
            throw new FaultyImplementationError("Invalid pattern offset length for inner floor: " + patternOffset.length);
        }
        this.patternOffsetFloor[0] = setOffset(patternOffset[0], 0, patternSizeFloor[0]);
        this.patternOffsetFloor[1] = setOffset(patternOffset[1], 0, patternSizeFloor[1]);
        this.patternOffsetFloor[2] = setOffset(patternOffset[2], 0, patternSizeFloor[2]);
    }

    private int setOffset(int value, int min, int max) {
        if (value < min) return min;
        return Math.min(value, max > 0 ? max - 1 : max);
    }

    public int[] getBpFullWallOffsetXAxis() {
        return this.patternOffsetFullWallXAxis;
    }

    public void setBpFullWallOffsetXAxis(int[] patternOffset) {
        if (patternOffset.length != 3) {
            throw new FaultyImplementationError("Invalid pattern offset length for full wall: " + patternOffset.length);
        }
        this.patternOffsetFullWallXAxis[0] = setOffset(patternOffset[0], 0, patternSizeFullWall[0]);
        this.patternOffsetFullWallXAxis[1] = setOffset(patternOffset[1], 0, patternSizeFullWall[1]);
        this.patternOffsetFullWallXAxis[2] = setOffset(patternOffset[2], 0, patternSizeFullWall[2]);
    }

    public int[] getBpFullWallOffsetZAxis() {
        return this.patternOffsetFullWallZAxis;
    }

    public void setBpFullWallOffsetZAxis(int[] patternOffset) {
        if (patternOffset.length != 3) {
            throw new FaultyImplementationError("Invalid pattern offset length for full wall: " + patternOffset.length);
        }
        this.patternOffsetFullWallZAxis[0] = setOffset(patternOffset[0], 0, patternSizeFullWall[0]);
        this.patternOffsetFullWallZAxis[1] = setOffset(patternOffset[1], 0, patternSizeFullWall[1]);
        this.patternOffsetFullWallZAxis[2] = setOffset(patternOffset[2], 0, patternSizeFullWall[2]);
    }

    public int[] getBpTopWallOffsetXAxis() {
        return this.patternOffsetTopWallXAxis;
    }

    public void setBpTopWallOffsetXAxis(int[] patternOffset) {
        if (patternOffset.length != 3) {
            throw new FaultyImplementationError("Invalid pattern offset length for full wall: " + patternOffset.length);
        }
        this.patternOffsetTopWallXAxis[0] = setOffset(patternOffset[0], 0, patternSizeTopWall[0]);
        this.patternOffsetTopWallXAxis[1] = setOffset(patternOffset[1], 0, patternSizeTopWall[1]);
        this.patternOffsetTopWallXAxis[2] = setOffset(patternOffset[2], 0, patternSizeTopWall[2]);
    }

    public int[] getBpTopWallOffsetZAxis() {
        return this.patternOffsetTopWallZAxis;
    }

    public void setBpTopWallOffsetZAxis(int[] patternOffset) {
        if (patternOffset.length != 3) {
            throw new FaultyImplementationError("Invalid pattern offset length for full wall: " + patternOffset.length);
        }
        this.patternOffsetTopWallZAxis[0] = setOffset(patternOffset[0], 0, patternSizeTopWall[0]);
        this.patternOffsetTopWallZAxis[1] = setOffset(patternOffset[1], 0, patternSizeTopWall[1]);
        this.patternOffsetTopWallZAxis[2] = setOffset(patternOffset[2], 0, patternSizeTopWall[2]);
    }

    public void updateGizmoState(@NotNull ArrayList<BlockPos> blockPositions) {
        BlockPos oldFirstPos = blockPositions.getFirst();
        BlockPos oldSecondPos = blockPositions.getLast();
        this.maybeSwapOffsetsForAxis(oldFirstPos, oldSecondPos, Direction.Axis.X);
        this.maybeSwapOffsetsForAxis(oldFirstPos, oldSecondPos, Direction.Axis.Y);
        this.maybeSwapOffsetsForAxis(oldFirstPos, oldSecondPos, Direction.Axis.Z);
        BlockPos firstPos = this.wallPos1Gizmo.getTargetPosition();
        BlockPos secondPos = this.wallPos2Gizmo.getTargetPosition();
        boolean firstXGreater = this.wallPos1Gizmo.getInterpPosition().getX() > this.wallPos2Gizmo.getInterpPosition().getX();
        boolean firstYGreater = this.wallPos1Gizmo.getInterpPosition().getY() > this.wallPos2Gizmo.getInterpPosition().getY();
        boolean firstZGreater = this.wallPos1Gizmo.getInterpPosition().getZ() > this.wallPos2Gizmo.getInterpPosition().getZ();
        this.wallPos1Gizmo.setAxisDirections(firstXGreater, firstYGreater, firstZGreater);
        this.wallPos2Gizmo.setAxisDirections(!firstXGreater, !firstYGreater, !firstZGreater);
        float centerX = (float) (firstPos.getX() + secondPos.getX()) / 2.0F;
        float centerY = (float) (firstPos.getY() + secondPos.getY()) / 2.0F;
        float centerZ = (float) (firstPos.getZ() + secondPos.getZ()) / 2.0F;
        this.mainMoveGizmo.moveTo(BlockPos.ofFloored(centerX, centerY, centerZ));

        for (int i = 0; i < 3; ++i) {
            Direction.Axis axis = Direction.Axis.VALUES[i];
            this.position[i] = Math.min(firstPos.getComponentAlongAxis(axis), secondPos.getComponentAlongAxis(axis));
            this.size[i] = Math.abs(firstPos.getComponentAlongAxis(axis) - secondPos.getComponentAlongAxis(axis));
        }
        this.calcBlocks();
        super.updateGizmoState();
    }

    private void maybeSwapOffsetsForAxis(BlockPos oldFirstPos, BlockPos oldSecondPos, Direction.Axis axis) {
        int size = this.wallPos1Gizmo.getTargetPosition().getComponentAlongAxis(axis) - this.wallPos2Gizmo.getTargetPosition().getComponentAlongAxis(axis);
        if (size > 0 && this.wallPos1Gizmo.getOffset().getComponentAlongAxis(axis) < 0.0 || size < 0 && this.wallPos1Gizmo.getOffset().getComponentAlongAxis(axis) > 0.0) {
            double temp = this.wallPos1Gizmo.getOffset().getComponentAlongAxis(axis);
            this.wallPos1Gizmo.setOffset(this.wallPos1Gizmo.getOffset().withAxis(axis, this.wallPos2Gizmo.getOffset().getComponentAlongAxis(axis)));
            this.wallPos2Gizmo.setOffset(this.wallPos2Gizmo.getOffset().withAxis(axis, temp));
            int firstDelta = oldFirstPos.getComponentAlongAxis(axis) - this.wallPos1Gizmo.getTargetPosition().getComponentAlongAxis(axis);
            if (firstDelta > 0) {
                this.wallPos1Gizmo.moveTo(this.wallPos1Gizmo.getTargetPosition().offset(axis, 1));
            } else if (firstDelta < 0) {
                this.wallPos1Gizmo.moveTo(this.wallPos1Gizmo.getTargetPosition().offset(axis, -1));
            }

            int secondDelta = oldSecondPos.getComponentAlongAxis(axis) - this.wallPos2Gizmo.getTargetPosition().getComponentAlongAxis(axis);
            if (secondDelta > 0) {
                this.wallPos2Gizmo.moveTo(this.wallPos2Gizmo.getTargetPosition().offset(axis, 1));
            } else if (secondDelta < 0) {
                this.wallPos2Gizmo.moveTo(this.wallPos2Gizmo.getTargetPosition().offset(axis, -1));
            }
        }

    }

    public void updateGizmosFromPositionSize() {
        for (int i = 0; i < 4; ++i) {
            if (i == 3) {
                return;
            }

            if (this.position[i] != 0 || this.size[i] != 0) {
                break;
            }
        }

        BlockPos oldFirstPos = this.wallPos1Gizmo.getTargetPosition();
        BlockPos oldSecondPos = this.wallPos2Gizmo.getTargetPosition();
        BlockPos.Mutable newFirstPos = new BlockPos.Mutable();
        BlockPos.Mutable newSecondPos = new BlockPos.Mutable();

        for (int i = 0; i < 3; ++i) {
            Direction.Axis axis = Direction.Axis.VALUES[i];
            int oldFirstPosAxis = oldFirstPos.getComponentAlongAxis(axis);
            int oldSecondPosAxis = oldSecondPos.getComponentAlongAxis(axis);
            BlockPos.Mutable lesser;
            BlockPos.Mutable greater;
            if (oldFirstPosAxis < oldSecondPosAxis) {
                lesser = newFirstPos;
                greater = newSecondPos;
            } else if (oldFirstPosAxis > oldSecondPosAxis) {
                lesser = newSecondPos;
                greater = newFirstPos;
            } else {
                double firstOffsetAxis = this.wallPos1Gizmo.getOffset().getComponentAlongAxis(axis);
                double secondOffsetAxis = this.wallPos2Gizmo.getOffset().getComponentAlongAxis(axis);
                if (firstOffsetAxis < secondOffsetAxis) {
                    lesser = newFirstPos;
                    greater = newSecondPos;
                } else {
                    if (!(firstOffsetAxis > secondOffsetAxis)) {
                        throw new FaultyImplementationError("Offsets are equal");
                    }

                    lesser = newSecondPos;
                    greater = newFirstPos;
                }
            }

            if (this.size[i] == 0) {
                this.size[i] = 1;
            } else if (this.size[i] < 0) {
                int[] var10000 = this.position;
                var10000[i] += this.size[i];
                this.size[i] = Math.abs(this.size[i]);
            }

            switch (axis) {
                case X:
                    lesser.setX(this.position[i]);
                    greater.setX(this.position[i] + this.size[i]);
                    break;
                case Y:
                    lesser.setY(this.position[i]);
                    greater.setY(this.position[i] + this.size[i]);
                    break;
                case Z:
                    lesser.setZ(this.position[i]);
                    greater.setZ(this.position[i] + this.size[i]);
            }
        }

        this.wallPos1Gizmo.moveTo(newFirstPos);
        this.wallPos2Gizmo.moveTo(newSecondPos);
        if (!oldFirstPos.equals(this.wallPos1Gizmo.getTargetPosition()) || !oldSecondPos.equals(this.wallPos2Gizmo.getTargetPosition())) {
            this.updateGizmoState(new ArrayList<>(Arrays.asList(this.wallPos1Gizmo.getTargetPosition(), this.wallPos2Gizmo.getTargetPosition())));
        }
    }

    public Direction.Axis getRoofOrientation() {
        return roofOrientation;
    }

    public Theme getHouseType() {
        return this.theme;
    }

    public HouseRoofType getRoofType() {
        return houseRoofType;
    }

    public int getSteppedGableType() {
        return steppedGableType;
    }

    public HouseRoofType getHouseRoofType() {
        return houseRoofType;
    }
}
