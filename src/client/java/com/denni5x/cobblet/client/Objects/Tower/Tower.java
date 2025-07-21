package com.denni5x.cobblet.client.Objects.Tower;

import com.denni5x.cobblet.client.CobbletTool;
import com.denni5x.cobblet.client.Objects.CobbletObject;
import com.denni5x.cobblet.client.Objects.Theme.Theme;
import com.denni5x.cobblet.client.Objects.Theme.ThemeRecord;
import com.denni5x.cobblet.client.Raterization.HalfSphereRasterization;
import com.denni5x.cobblet.client.Raterization.TubeRasterization;
import com.moulberry.axiom.block_maps.HDVoxelMap;
import com.moulberry.axiom.editor.ImGuiHelper;
import com.moulberry.axiom.gizmo.Gizmo;
import com.moulberry.axiom.i18n.AxiomI18n;
import com.moulberry.axiom.rasterization.ConeRasterization;
import com.moulberry.axiom.render.regions.ChunkedBlockRegion;
import com.moulberry.axiom.tools.stamp.StampPlacement;
import com.moulberry.axiom.tools.stamp.TransformedBlockRegions;
import com.moulberry.axiom.world_modification.CompressedBlockEntity;
import imgui.ImGui;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class Tower extends CobbletObject {
    public boolean unlockXZWidth;
    public int rotation;
    public boolean flipX = false;
    public boolean flipZ = false;
    private TowerRoofType towerRoofType;
    private float coneRounding;
    private int roofSizeY;
    private int towerRoofOffset;

    public Tower(int[] position, int[] size, CobbletTool cobbletTool, boolean isRecord,
                 Theme theme, TowerRoofType towerRoofType, float coneRounding, int roofSizeY,
                 int towerRoofOffset) {
        super.cobbletTool = cobbletTool;
        super.isRecord = isRecord;
        this.towerRoofType = towerRoofType;
        this.position = position;
        this.size = size;
        this.coneRounding = coneRounding;
        this.roofSizeY = roofSizeY;
        this.towerRoofOffset = towerRoofOffset;
        super.theme = theme;
        super.themeConfig = ThemeRecord.getHouseConfig(this.theme);
        if (isRecord) return;
        this.setupGizmos();
        this.calcBlocks();
    }

    public Tower(int[] position, int[] size, CobbletTool cobbletTool, boolean isRecord) {
        this(position, size, cobbletTool, isRecord, Theme.BRICKS, TowerRoofType.CONE, 0.0f, 10, 0);
    }

    public Tower(int[] size, CobbletTool cobbletTool, boolean isRecord) {
        this(new int[]{0, 0, 0}, size, cobbletTool, isRecord);
    }

    public void setupGizmos() {
        this.mainMoveGizmo = new Gizmo(new Vec3d(this.position[0], this.position[1], this.position[2]));
        this.mainMoveGizmo.enableAxes = true;
        this.mainMoveGizmo.enableScale = false;
    }

    public Tower copy() {
        return new Tower(this.position.clone(), this.size.clone(), super.cobbletTool, super.isRecord, this.theme,
                this.towerRoofType, this.coneRounding, this.roofSizeY, this.towerRoofOffset);
    }

    public boolean displaySettings() {
        boolean objectSettingsChanged = false;
        int[] roofTypeInt = new int[]{TowerRoofType.valueOf(this.getRoofType().name()).ordinal()};
        int[] towerType = new int[]{Theme.valueOf(this.getType().name()).ordinal()};
        float[] coneRounding = new float[]{this.getConeRounding()};
        int[] roofSizeY = new int[]{this.getRoofSizeY()};
        int[] towerRoofOffset = new int[]{this.getRoofOffset()};
        int[] size = this.getSize();
        int[] height = new int[]{size[1]};
        int[] width = new int[]{size[0]};

        objectSettingsChanged |= ImGuiHelper.combo("Tower Type", towerType, new String[]{"Bricks", "Plain", "Desert"});
        objectSettingsChanged |= ImGuiHelper.combo("Roof Type", roofTypeInt, new String[]{"Cone", "Half Sphere", "Offset Sphere", "Slabbed", "Flat"});
        TowerRoofType towerRoofType = TowerRoofType.values()[roofTypeInt[0]];

        ImGuiHelper.separatorWithText("General Settings");
        if (!this.unlockXZWidth) {
            objectSettingsChanged |= ImGui.sliderInt(AxiomI18n.get("axiom.tool.shape.height") + "##TowerHeight", height, 1, 64);
            objectSettingsChanged |= ImGui.sliderInt(AxiomI18n.get("axiom.tool.shape.width") + "##TowerWidth", width, 1, 64);
            if (height[0] < 1) height[0] = 1;
            if (width[0] < 1) height[0] = 1;
            size[0] = width[0];
            size[1] = height[0];
            size[2] = width[0];
        } else {
            if (ImGuiHelper.inputInt("Tower Size", size)) {
                for (int i = 0; i < 2; i++) {
                    if (size[i] < 1) size[i] = 1;
                }
                objectSettingsChanged = true;
            }
        }
        if (ImGui.checkbox("Unlock Width", this.unlockXZWidth)) {
            this.unlockXZWidth = !this.unlockXZWidth;
        }

        ImGuiHelper.separatorWithText("Roof Settings");
        if (towerRoofType == TowerRoofType.OFFSET_SPHERE || towerRoofType == TowerRoofType.HALF_SPHERE || towerRoofType == TowerRoofType.CONE) {
            objectSettingsChanged |= ImGui.sliderInt(AxiomI18n.get("axiom.tool.shape.height") + "##SizeY", roofSizeY, 1, 64);
        }
        if (towerRoofType == TowerRoofType.OFFSET_SPHERE) {
            objectSettingsChanged |= ImGui.sliderInt("Roof Offset" + "##RoofOffset", towerRoofOffset, -64, 64);
        }
        if (towerRoofType == TowerRoofType.CONE) {
            objectSettingsChanged |= ImGui.sliderFloat(AxiomI18n.get("axiom.tool.shape.cone.rounding"), coneRounding, 0.0F, 1.0F);
        }

        if (objectSettingsChanged) {
            this.updateSettings(Theme.values()[towerType[0]], towerRoofType, coneRounding[0], roofSizeY[0], towerRoofOffset[0], size);
            return true;
        }
        return false;
    }

    public void updateSettings(Theme theme, TowerRoofType towerRoofType, float coneRounding,
                               int roofSizeY, int towerRoofOffset, int[] size) {
        this.theme = theme;
        this.themeConfig = ThemeRecord.getHouseConfig(theme);
        this.towerRoofType = towerRoofType;
        this.coneRounding = coneRounding;
        this.roofSizeY = roofSizeY;
        this.towerRoofOffset = towerRoofOffset;
        this.size = size;
        if (super.isRecord) return;
        this.calcBlocks();
        this.updateGizmoState();
    }

    public void calcBlocks() {
        if (this.size == null || this.mainMoveGizmo == null) return;
        this.chunkedBlockRegion.clear();
        int minY = this.mainMoveGizmo.getTargetPosition().getY();
        int maxY = minY + this.size[1];
        calcWalls(Math.abs(minY - maxY) / 2);
        calcFloorCeiling(minY, maxY);
        calcRoof(maxY);

        ChunkedBlockRegion copiedRegion = new ChunkedBlockRegion();
        Long2ObjectOpenHashMap<CompressedBlockEntity> blockEntities = new Long2ObjectOpenHashMap<>();
        this.chunkedBlockRegion.forEachEntry(copiedRegion::addBlock);

        TransformedBlockRegions transformedBlockRegions = new TransformedBlockRegions(copiedRegion, blockEntities);

        StampPlacement stampPlacement = new StampPlacement(
                transformedBlockRegions.getBlocks(this.rotation, this.flipX, this.flipZ),
                blockEntities,
                transformedBlockRegions,
                this.rotation, this.flipX, this.flipZ,
                this.mainMoveGizmo.getTargetPosition().getX(),
                this.mainMoveGizmo.getTargetPosition().getY(),
                this.mainMoveGizmo.getTargetPosition().getZ(),
                this.mainMoveGizmo.getTargetPosition().getX(),
                this.mainMoveGizmo.getTargetPosition().getY(),
                this.mainMoveGizmo.getTargetPosition().getZ());

        this.chunkedBlockRegion.clear();
        stampPlacement.pasteInto(this.chunkedBlockRegion, blockEntities);
    }

    @Override
    public void updateGizmoState() {
        this.position = new int[]{this.mainMoveGizmo.getTargetPosition().getX(), this.mainMoveGizmo.getTargetPosition().getY(), this.mainMoveGizmo.getTargetPosition().getZ()};
        this.calcBlocks();
    }

    public void updateGizmosFromPositionSize() {
        for (int i = 0; i < 4; ++i) {
            if (i == 3) {
                return;
            }

            if (this.position[i] != 0) {
                break;
            }
        }
        this.mainMoveGizmo.moveTo(new BlockPos(this.position[0], this.position[1], this.position[2]));
    }

    @Override
    public boolean renderSettings() {
        return false;
    }

    private void calcRoof(int maxY) {
        HDVoxelMap.HDVoxelBaseBlocks roofBlocks = HDVoxelMap.getAssociatedBlocks(this.themeConfig.fullRoof().getBlock());
        BlockState full = roofBlocks.full().getDefaultState();
        BlockState slab = roofBlocks.slab().getDefaultState();
        BlockPos blockPos = this.mainMoveGizmo.getTargetPosition();
        switch (this.towerRoofType) {
            case CONE -> {
                int offsetY = this.roofSizeY % 2 == 0 ? 0 : 1;
                ConeRasterization.cone(this.chunkedBlockRegion, full, new BlockPos(blockPos.getX(),
                                maxY + roofSizeY / 2 + offsetY, blockPos.getZ()), this.size[0], roofSizeY,
                        this.size[2], true, this.coneRounding, null);
            }
            case HALF_SPHERE -> {
                HalfSphereRasterization.sphere(this.chunkedBlockRegion, full, new BlockPos(blockPos.getX(),
                                maxY + 1, blockPos.getZ()), this.size[0], this.roofSizeY, this.size[2],
                        true, null, 0);
            }
            case OFFSET_SPHERE -> {
                HalfSphereRasterization.sphere(this.chunkedBlockRegion, full, new BlockPos(blockPos.getX(),
                                maxY + 1 + (-this.towerRoofOffset), blockPos.getZ()), this.size[0],
                        this.roofSizeY, this.size[2], true, null, this.towerRoofOffset);
            }
            case SLABBED -> {
                TubeRasterization.tube(this.chunkedBlockRegion, slab,
                        new BlockPos(blockPos.getX(), maxY + 1, blockPos.getZ()), this.size[0], 1,
                        this.size[2], false, 1, null);
            }
        }
    }

    private void calcWalls(int middleHeight) {
        BlockPos blockPos = this.mainMoveGizmo.getTargetPosition();
        TubeRasterization.tube(this.chunkedBlockRegion, this.themeConfig.fullWall(),
                new BlockPos(blockPos.getX(), blockPos.getY() + middleHeight, blockPos.getZ()),
                this.size[0], this.size[1], this.size[2], true, 1, null);
    }

    private void calcFloorCeiling(int minY, int maxY) {
        BlockPos blockPos = this.mainMoveGizmo.getTargetPosition();
        TubeRasterization.tube(this.chunkedBlockRegion, this.themeConfig.fullEdge(),
                new BlockPos(blockPos.getX(), minY, blockPos.getZ()), this.size[0], 1, this.size[2],
                false, 100, null);
        TubeRasterization.tube(this.chunkedBlockRegion, this.themeConfig.fullEdge(),
                new BlockPos(blockPos.getX(), maxY, blockPos.getZ()), this.size[0], 1, this.size[2],
                false, 100, null);
    }

    public void rotate() {
        if (this.rotation == 0) {
            this.rotation = 3;
        } else {
            this.rotation--;
        }
    }

    public TowerRoofType getRoofType() {
        return towerRoofType;
    }

    public float getConeRounding() {
        return coneRounding;
    }

    public int getRoofSizeY() {
        return roofSizeY;
    }

    public int getRoofOffset() {
        return towerRoofOffset;
    }

    public int[] getSize() {
        return this.size;
    }
}
