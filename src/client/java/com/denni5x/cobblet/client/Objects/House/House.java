package com.denni5x.cobblet.client.Objects.House;

import com.denni5x.cobblet.client.CobbletTool;
import com.denni5x.cobblet.client.Objects.CobbletObject;
import com.denni5x.cobblet.client.Objects.ObjectCalculation.InnerFloorCalculation;
import com.denni5x.cobblet.client.Objects.ObjectCalculation.ObjectCalculationUtils;
import com.denni5x.cobblet.client.Objects.ObjectCalculation.OuterWallObjectCalculation;
import com.denni5x.cobblet.client.Objects.Theme.Theme;
import com.denni5x.cobblet.client.Objects.Theme.ThemeRecord;
import com.moulberry.axiom.editor.ImGuiHelper;
import com.moulberry.axiom.exceptions.FaultyImplementationError;
import com.moulberry.axiom.gizmo.Gizmo;
import com.moulberry.axiom.render.regions.ChunkedBlockRegion;
import imgui.ImGui;
import imgui.type.ImInt;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;

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
    private HouseRoofType houseRoofType;
    private int steppedGableType;

    public House(int[] position, int[] size, CobbletTool cobbletTool, boolean isRecord, Theme gladeTheme, HouseRoofType houseRoofType, int steppedGableType, Direction.Axis roofOrientation) {
        super.cobbletTool = cobbletTool;
        super.isRecord = isRecord;
        super.chunkedBlockRegion = new ChunkedBlockRegion();
        this.position = position;
        this.size = size;
        this.roofOrientation = roofOrientation;
        this.houseRoofType = houseRoofType;
        this.steppedGableType = steppedGableType;
        super.theme = gladeTheme;
        if (isRecord) return;
        super.themeConfig = ThemeRecord.getHouseConfig(this.theme);
        setupGizmos(new ArrayList<>(Arrays.asList(new BlockPos(position[0], position[1], position[2]), new BlockPos(size[0] + position[0], size[1] + position[1], size[2] + position[2]))));
    }

    public House(int[] position, int[] size, CobbletTool cobbletTool, boolean isRecord) {
        this(position, size, cobbletTool, isRecord, Theme.BRICKS, HouseRoofType.STEPPED_GABLE, 0, Direction.Axis.X);
    }

    public House(int[] size, CobbletTool cobbletTool, boolean isRecord) {
        this(new int[]{0, 0, 0}, size, cobbletTool, isRecord);
    }

    public House copy() {
        return new House(this.position.clone(), this.size.clone(), this.cobbletTool, this.isRecord, this.theme, this.houseRoofType, this.steppedGableType, this.roofOrientation);
    }

    public void updateSettings(Theme gladeTheme, HouseRoofType houseRoofType, int steppedGableType, Direction.Axis roofOrientation) {
        this.roofOrientation = roofOrientation;
        this.theme = gladeTheme;
        this.themeConfig = ThemeRecord.getHouseConfig(gladeTheme);
        this.houseRoofType = houseRoofType;
        this.steppedGableType = steppedGableType;
        if (this.isRecord) return;
        this.updateGizmoState(new ArrayList<>(Arrays.asList(this.wallPos1Gizmo.getTargetPosition(), this.wallPos2Gizmo.getTargetPosition())));
    }

    public void updateSettings(Direction.Axis roofOrientation) {
        this.roofOrientation = roofOrientation;
        if (!this.isRecord) this.calcBlocks();
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

    @Override
    public boolean renderSettings() {
        boolean objectSettingsChanged = false;
        boolean roofOrientation = this.getRoofOrientation() == Direction.Axis.X;
        int[] roofTypeInt = new int[]{HouseRoofType.valueOf(this.getRoofType().name()).ordinal()};
        int[] houseType = new int[]{Theme.valueOf(this.getType().name()).ordinal()};
        int[] bpFullWallOffsetXAxis = this.getBpFullWallOffsetXAxis().clone();
        int[] bpFullWallOffsetZAxis = this.getBpFullWallOffsetZAxis().clone();
        int[] bpTopWallOffsetXAxis = this.getBpTopWallOffsetXAxis().clone();
        int[] bpTopWallOffsetZAxis = this.getBpTopWallOffsetZAxis().clone();
        int[] bpInnerFloorOffset = this.getInnerFloorPatternOffset().clone();
        ImInt steppedGableType = new ImInt(this.getSteppedGableType());


        objectSettingsChanged |= ImGuiHelper.combo("House Type", houseType,
                new String[]{"Bricks", "Plain", "Desert", "Noordigrad"});
        objectSettingsChanged |= ImGuiHelper.combo("Roof Type", roofTypeInt,
                new String[]{"Stepped Gable", "Gable", "Square Gable", "Tower", "Wall Pattern", "Walled",
                        "Thick Walled", "Slabbed", "Thick Gable", "Low Angle", "High Angle", "Flat",});
        HouseRoofType houseRoofType = HouseRoofType.values()[roofTypeInt[0]];

        if (houseRoofType == HouseRoofType.STEPPED_GABLE) {
            objectSettingsChanged |= ImGui.radioButton("Slabs", steppedGableType, 0);
            ImGui.sameLine();
            objectSettingsChanged |= ImGui.radioButton("Stairs", steppedGableType, 1);
            ImGui.sameLine();
            objectSettingsChanged |= ImGui.radioButton("Full", steppedGableType, 2);
            ImGui.sameLine();
            ImGui.text("Stepped Roof Types");
        }

        ImGuiHelper.setupBorder();
        ImGuiHelper.separatorWithText("Pattern Offsets");
        objectSettingsChanged |= ImGuiHelper.inputInt("Walls X", bpFullWallOffsetXAxis);
        objectSettingsChanged |= ImGuiHelper.inputInt("Walls Z", bpFullWallOffsetZAxis);
        objectSettingsChanged |= ImGuiHelper.inputInt("Inner Floors", bpInnerFloorOffset);
        if (houseRoofType == HouseRoofType.WALL_PATTERN) {
            objectSettingsChanged |= ImGuiHelper.inputInt("Top Walls X", bpTopWallOffsetXAxis);
            objectSettingsChanged |= ImGuiHelper.inputInt("Top Walls Z", bpTopWallOffsetZAxis);
        }
        ImGuiHelper.finishBorder();

        if (objectSettingsChanged) {
            this.setBpFullWallOffsetXAxis(bpFullWallOffsetXAxis);
            this.setBpFullWallOffsetZAxis(bpFullWallOffsetZAxis);
            this.setBpTopWallOffsetXAxis(bpTopWallOffsetXAxis);
            this.setBpTopWallOffsetZAxis(bpTopWallOffsetZAxis);
            this.setInnerFloorPatternOffset(bpInnerFloorOffset);
            this.updateSettings(Theme.values()[houseType[0]], houseRoofType, steppedGableType.get(), roofOrientation ? Direction.Axis.X : Direction.Axis.Z);
            return true;
        }
        return false;
    }

    public void calcBlocks() {
        super.chunkedBlockRegion.clear();
        BlockPos pos1 = this.wallPos1Gizmo.getTargetPosition();
        BlockPos pos2 = this.wallPos2Gizmo.getTargetPosition();
        int minX = Math.min(pos1.getX(), pos2.getX());
        int minY = Math.min(pos1.getY(), pos2.getY());
        int minZ = Math.min(pos1.getZ(), pos2.getZ());
        int maxX = Math.max(pos1.getX(), pos2.getX());
        int maxY = Math.max(pos1.getY(), pos2.getY());
        int maxZ = Math.max(pos1.getZ(), pos2.getZ());

        OuterWallObjectCalculation.outerWalls(this.themeConfig, super.chunkedBlockRegion, minY, maxY, minX, maxX, minZ, maxZ,
                this.patternOffsetFullWallXAxis, this.patternOffsetFullWallZAxis, this.patternSizeFullWall, this.patternSizeFullWall1R);
        ObjectCalculationUtils.calcFloorCeiling(super.chunkedBlockRegion, this.themeConfig, minX, maxX, minZ, maxZ, minY, maxY);
        ObjectCalculationUtils.roof(super.chunkedBlockRegion, this.themeConfig, this.steppedGableType, this.houseRoofType, this.roofOrientation, minX, maxX, minZ, maxZ, minY, maxY);
        InnerFloorCalculation.innerFloor(this.themeConfig, super.chunkedBlockRegion,
                minX + 1, maxX - 1, minY + 1, maxY - 1, minZ + 1, maxZ - 1,
                this.patternOffsetFloor, this.patternSizeFloor);
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

    @Override
    public void updateGizmoState() {

    }

    @Override
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

    public HouseRoofType getRoofType() {
        return houseRoofType;
    }

    public int getSteppedGableType() {
        return steppedGableType;
    }


}
