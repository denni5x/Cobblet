package com.denni5x.cobblet.client.Objects.BlueprintAsset;

import com.denni5x.cobblet.client.CobbletTool;
import com.denni5x.cobblet.client.Objects.CobbletObject;
import com.moulberry.axiom.blueprint.Blueprint;
import com.moulberry.axiom.gizmo.Gizmo;
import com.moulberry.axiom.tools.stamp.StampPlacement;
import com.moulberry.axiom.tools.stamp.TransformedBlockRegions;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class BlueprintAsset extends CobbletObject {

    private int[] offset;
    public int rotation;
    public boolean flipX = false;
    public boolean flipZ = false;
    private Blueprint blueprint;

    public BlueprintAsset(CobbletTool cobbletTool, int[] position, boolean isRecord, Blueprint blueprint, int[] offset) {
        super(cobbletTool, isRecord);
        super.position = position;
        this.offset = offset;
        this.blueprint = blueprint;
        if (isRecord) return;
        this.setupGizmos();
        this.calcBlocks();
    }

    public BlueprintAsset(CobbletTool cobbletTool, boolean isRecord) {
        this(cobbletTool, new int[]{0, 0, 0}, isRecord, null, new int[]{0, 0, 0});
    }

    public void setupGizmos() {
        this.mainMoveGizmo = new Gizmo(new Vec3d(this.position[0], this.position[1], this.position[2]));
        this.mainMoveGizmo.enableAxes = true;
        this.mainMoveGizmo.enableScale = false;
    }

    public void updateGizmoState() {
        this.position = new int[]{this.mainMoveGizmo.getTargetPosition().getX(), this.mainMoveGizmo.getTargetPosition().getY(), this.mainMoveGizmo.getTargetPosition().getZ()};
        super.updateGizmoState();
    }

    public void calcBlocks() {
        if (this.blueprint == null || this.mainMoveGizmo == null) return;

        TransformedBlockRegions transformedBlockRegions = new TransformedBlockRegions(this.blueprint.blockRegion(), this.blueprint.blockEntities());

        StampPlacement stampPlacement = new StampPlacement(
                transformedBlockRegions.getBlocks(this.rotation, this.flipX, this.flipZ),
                transformedBlockRegions.getBlockEntities(this.rotation, this.flipX, this.flipZ),
                transformedBlockRegions,
                this.rotation, this.flipX, this.flipZ,
                this.mainMoveGizmo.getTargetPosition().getX() + this.offset[0],
                this.mainMoveGizmo.getTargetPosition().getY() + this.offset[1],
                this.mainMoveGizmo.getTargetPosition().getZ() + this.offset[2],
                this.mainMoveGizmo.getTargetPosition().getX(),
                this.mainMoveGizmo.getTargetPosition().getY(),
                this.mainMoveGizmo.getTargetPosition().getZ());

        this.chunkedBlockRegion.clear();
        stampPlacement.pasteInto(this.chunkedBlockRegion, new Long2ObjectOpenHashMap<>());
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

    public void setRotation(int rotation) {
        this.rotation = Math.abs(this.rotation + rotation);
    }

    public void rotate() {
        if (this.rotation == 0) {
            this.rotation = 3;
        } else {
            this.rotation--;
        }
    }

    public void setBlueprint(Blueprint blueprint) {
        this.blueprint = blueprint;
        this.calcBlocks();
    }

    public Blueprint getBlueprint() {
        return this.blueprint;
    }

    public void setOffset(int[] offset) {
        this.offset = offset;
    }

    public int[] getOffsets() {
        return this.offset;
    }

    public BlueprintAsset copy() {
        return new BlueprintAsset(super.cobbletTool, this.position.clone(), super.isRecord, this.blueprint, this.offset.clone());
    }
}
