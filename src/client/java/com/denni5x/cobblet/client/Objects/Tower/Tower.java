package com.denni5x.cobblet.client.Objects.Tower;

import com.denni5x.cobblet.client.CobbletTool;
import com.denni5x.cobblet.client.Objects.CobbletObject;
import com.denni5x.cobblet.client.Objects.Theme.Theme;
import com.denni5x.cobblet.client.Objects.Theme.ThemeConfig;
import com.denni5x.cobblet.client.Objects.Theme.ThemeRecord;
import com.denni5x.cobblet.client.Raterization.HalfSphereRasterization;
import com.denni5x.cobblet.client.Raterization.TubeRasterization;
import com.moulberry.axiom.block_maps.HDVoxelMap;
import com.moulberry.axiom.gizmo.Gizmo;
import com.moulberry.axiom.rasterization.ConeRasterization;
import com.moulberry.axiom.render.regions.ChunkedBlockRegion;
import com.moulberry.axiom.tools.stamp.StampPlacement;
import com.moulberry.axiom.tools.stamp.TransformedBlockRegions;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class Tower extends CobbletObject {
    public boolean unlockXZWidth;
    public int rotation;
    public boolean flipX = false;
    public boolean flipZ = false;
    private Theme theme;
    private ThemeConfig themeConfig;
    private TowerRoofType towerRoofType;
    private float coneRounding;
    private int roofSizeY;
    private int towerRoofOffset;

    public Tower(int[] position, int[] size, CobbletTool cobbletTool, boolean isRecord,
                 Theme theme, TowerRoofType towerRoofType, float coneRounding, int roofSizeY,
                 int towerRoofOffset) {
        super(cobbletTool, isRecord);
        this.theme = theme;
        this.towerRoofType = towerRoofType;
        this.position = position;
        this.size = size;
        this.coneRounding = coneRounding;
        this.roofSizeY = roofSizeY;
        this.towerRoofOffset = towerRoofOffset;
        if (isRecord) return;
        this.themeConfig = ThemeRecord.getHouseConfig(this.theme);
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
        this.chunkedBlockRegion.forEachEntry(copiedRegion::addBlock);
        TransformedBlockRegions transformedBlockRegions = new TransformedBlockRegions(copiedRegion, null);

        StampPlacement stampPlacement = new StampPlacement(
                transformedBlockRegions.getBlocks(this.rotation, this.flipX, this.flipZ),
                new Long2ObjectOpenHashMap<>(),
                transformedBlockRegions,
                this.rotation, this.flipX, this.flipZ,
                this.mainMoveGizmo.getTargetPosition().getX(),
                this.mainMoveGizmo.getTargetPosition().getY(),
                this.mainMoveGizmo.getTargetPosition().getZ(),
                this.mainMoveGizmo.getTargetPosition().getX(),
                this.mainMoveGizmo.getTargetPosition().getY(),
                this.mainMoveGizmo.getTargetPosition().getZ());

        this.chunkedBlockRegion.clear();
        stampPlacement.pasteInto(this.chunkedBlockRegion, new Long2ObjectOpenHashMap<>());
    }

    public void updateGizmoState() {
        this.position = new int[]{this.mainMoveGizmo.getTargetPosition().getX(), this.mainMoveGizmo.getTargetPosition().getY(), this.mainMoveGizmo.getTargetPosition().getZ()};
        this.calcBlocks();
        super.updateGizmoState();
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

    public Theme getTowerType() {
        return theme;
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

    public int getTowerRoofOffset() {
        return towerRoofOffset;
    }

    public int[] getSize() {
        return this.size;
    }
}
