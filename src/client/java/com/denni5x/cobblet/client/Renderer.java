package com.denni5x.cobblet.client;

import com.denni5x.cobblet.client.History.HistoryAction;
import com.denni5x.cobblet.client.Objects.BlueprintAsset.BlueprintAsset;
import com.denni5x.cobblet.client.Objects.CobbletObject;
import com.denni5x.cobblet.client.Objects.House.House;
import com.denni5x.cobblet.client.Objects.Tower.Tower;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.Arrays;

public class Renderer {
    private Vec3d lookDirection;
    private boolean isLeftDown;
    private boolean isCtrlDown;
    private boolean showGizmo;
    private Camera camera;
    private long time;
    private MatrixStack matrices;
    private int currentlySelected;
    private Matrix4f projection;
    private float opacity;
    private final CobbletTool cobbletTool;

    public Renderer(CobbletTool cobbletTool) {
        this.cobbletTool = cobbletTool;
    }

    public void setToolSettings(Vec3d lookDirection, boolean isLeftDown, boolean isCtrlDown, boolean showGizmo,
                                Camera camera, long time, MatrixStack matrices, int currentlySelected, Matrix4f projection) {
        this.lookDirection = lookDirection;
        this.isLeftDown = isLeftDown;
        this.isCtrlDown = isCtrlDown;
        this.showGizmo = showGizmo;
        this.camera = camera;
        this.time = time;
        this.matrices = matrices;
        this.currentlySelected = currentlySelected;
        this.projection = projection;
    }

    public void renderObjects(ArrayList<CobbletObject> gladeObjects, float opacity) {
        this.opacity = opacity;
        for (int i = 0; i < gladeObjects.size(); i++) {
            CobbletObject gladeObject = gladeObjects.get(i);
            if (gladeObject instanceof House) {
                if (i == currentlySelected) {
                    this.renderSelectedObjectWMultiGizmo(gladeObject);
                } else {
                    this.renderDeselectedObjectWMultiGizmo(gladeObject);
                }
            }
            if (gladeObject instanceof BlueprintAsset || gladeObject instanceof Tower) {
                if (i == currentlySelected) {
                    this.renderSelectedObjectWSingleGizmo(gladeObject);
                } else {
                    this.renderDeselectedObjectWSingleGizmo(gladeObject);
                }
            }
            this.renderBlocks(gladeObject);
        }
    }

    public void renderDeselectedObjectWSingleGizmo(CobbletObject gladeObject) {
        gladeObject.mainMoveGizmo.update(time, lookDirection, isLeftDown, isCtrlDown, showGizmo);
        gladeObject.mainMoveGizmo.setAxisDirections(
                camera.getPos().x > (double) gladeObject.mainMoveGizmo.getTargetPosition().getX(),
                camera.getPos().y > (double) gladeObject.mainMoveGizmo.getTargetPosition().getY(),
                camera.getPos().z > (double) gladeObject.mainMoveGizmo.getTargetPosition().getZ());
        if (showGizmo || gladeObject.mainMoveGizmo.isGrabbed()) {
            gladeObject.mainMoveGizmo.render(matrices, camera, isCtrlDown);
        }
    }

    public void renderSelectedObjectWSingleGizmo(CobbletObject gladeObject) {
        BlockPos oldCenterPos = gladeObject.mainMoveGizmo.getTargetPosition();
        gladeObject.mainMoveGizmo.update(time, lookDirection, isLeftDown, isCtrlDown, showGizmo);
        gladeObject.mainMoveGizmo.setAxisDirections(
                camera.getPos().x > (double) gladeObject.mainMoveGizmo.getTargetPosition().getX(),
                camera.getPos().y > (double) gladeObject.mainMoveGizmo.getTargetPosition().getY(),
                camera.getPos().z > (double) gladeObject.mainMoveGizmo.getTargetPosition().getZ());
        if (!gladeObject.mainMoveGizmo.getTargetPosition().equals(oldCenterPos)) {
            if (gladeObject instanceof BlueprintAsset blueprintAsset) {
                blueprintAsset.updateGizmoState();
            }
            if (gladeObject instanceof Tower tower) {
                tower.updateGizmoState();
            }
        }
        if (showGizmo || gladeObject.mainMoveGizmo.isGrabbed()) {
            gladeObject.mainMoveGizmo.render(matrices, camera, isCtrlDown);
        }
        if (gladeObject instanceof BlueprintAsset blueprintAsset) {
            blueprintAsset.calcBlocks();
        }
        if (gladeObject instanceof Tower tower) {
            tower.calcBlocks();
        }
    }

    public void renderBlocks(CobbletObject gladeObject) {
        float blockOpacity = this.opacity;
        if (gladeObject.wallPos1Gizmo != null && gladeObject.wallPos1Gizmo.isHovered()) blockOpacity = 0.3f;
        if (gladeObject.wallPos2Gizmo != null && gladeObject.wallPos2Gizmo.isHovered()) blockOpacity = 0.3f;
        if (gladeObject.mainMoveGizmo != null && gladeObject.mainMoveGizmo.isHovered()) blockOpacity = 0.3f;
        gladeObject.chunkedBlockRegion.render(camera, Vec3d.ZERO, matrices, projection, blockOpacity, 0f);
    }

    public void renderDeselectedObjectWMultiGizmo(CobbletObject gladeObject) {
        gladeObject.mainMoveGizmo.update(time, lookDirection, isLeftDown, isCtrlDown, showGizmo);
        gladeObject.mainMoveGizmo.setAxisDirections(
                camera.getPos().x > (double) gladeObject.mainMoveGizmo.getTargetPosition().getX(),
                camera.getPos().y > (double) gladeObject.mainMoveGizmo.getTargetPosition().getY(),
                camera.getPos().z > (double) gladeObject.mainMoveGizmo.getTargetPosition().getZ());

        if (showGizmo || gladeObject.wallPos1Gizmo.isGrabbed() || gladeObject.wallPos2Gizmo.isGrabbed() || gladeObject.mainMoveGizmo.isGrabbed()) {
            gladeObject.mainMoveGizmo.render(matrices, camera, isCtrlDown);
        }
    }

    public void renderSelectedObjectWMultiGizmo(CobbletObject gladeObject) {
        BlockPos oldCenterPos = gladeObject.mainMoveGizmo.getTargetPosition();
        BlockPos oldFirstPos = gladeObject.wallPos1Gizmo.getTargetPosition();
        BlockPos oldSecondPos = gladeObject.wallPos2Gizmo.getTargetPosition();

        gladeObject.mainMoveGizmo.update(time, lookDirection, isLeftDown, isCtrlDown, showGizmo);
        gladeObject.mainMoveGizmo.setAxisDirections(
                camera.getPos().x > (double) gladeObject.mainMoveGizmo.getTargetPosition().getX(),
                camera.getPos().y > (double) gladeObject.mainMoveGizmo.getTargetPosition().getY(),
                camera.getPos().z > (double) gladeObject.mainMoveGizmo.getTargetPosition().getZ());

        if (!gladeObject.mainMoveGizmo.getTargetPosition().equals(oldCenterPos)) {
            BlockPos delta = gladeObject.mainMoveGizmo.getTargetPosition().subtract(oldCenterPos);
            gladeObject.wallPos1Gizmo.moveTo(gladeObject.wallPos1Gizmo.getTargetPosition().add(delta));
            gladeObject.wallPos2Gizmo.moveTo(gladeObject.wallPos2Gizmo.getTargetPosition().add(delta));

            if (delta.getX() > 0) this.cobbletTool.addHistory(HistoryAction.SCROLL_MG_XP);
            if (delta.getX() < 0) this.cobbletTool.addHistory(HistoryAction.SCROLL_MG_XN);
            if (delta.getY() > 0) this.cobbletTool.addHistory(HistoryAction.SCROLL_MG_YP);
            if (delta.getY() < 0) this.cobbletTool.addHistory(HistoryAction.SCROLL_MG_YN);
            if (delta.getZ() > 0) this.cobbletTool.addHistory(HistoryAction.SCROLL_MG_ZP);
            if (delta.getZ() < 0) this.cobbletTool.addHistory(HistoryAction.SCROLL_MG_ZN);
        }

        gladeObject.wallPos1Gizmo.update(time, lookDirection, isLeftDown, isCtrlDown, showGizmo);
        gladeObject.wallPos2Gizmo.update(time, lookDirection, isLeftDown, isCtrlDown, showGizmo);

        if (!oldFirstPos.equals(gladeObject.wallPos1Gizmo.getTargetPosition()) ||
                !oldSecondPos.equals(gladeObject.wallPos2Gizmo.getTargetPosition())) {
            if (gladeObject instanceof House house) {
                BlockPos deltaFirstPos = gladeObject.wallPos1Gizmo.getTargetPosition().subtract(oldFirstPos);
                BlockPos deltaSecondPos = gladeObject.wallPos2Gizmo.getTargetPosition().subtract(oldSecondPos);

                if (deltaFirstPos.getX() > 0) this.cobbletTool.addHistory(HistoryAction.SCROLL_FG_XP);
                if (deltaFirstPos.getX() < 0) this.cobbletTool.addHistory(HistoryAction.SCROLL_FG_XN);
                if (deltaFirstPos.getY() > 0) this.cobbletTool.addHistory(HistoryAction.SCROLL_FG_YP);
                if (deltaFirstPos.getY() < 0) this.cobbletTool.addHistory(HistoryAction.SCROLL_FG_YN);
                if (deltaFirstPos.getZ() > 0) this.cobbletTool.addHistory(HistoryAction.SCROLL_FG_ZP);
                if (deltaFirstPos.getZ() < 0) this.cobbletTool.addHistory(HistoryAction.SCROLL_FG_ZN);

                if (deltaSecondPos.getX() > 0) this.cobbletTool.addHistory(HistoryAction.SCROLL_SG_XP);
                if (deltaSecondPos.getX() < 0) this.cobbletTool.addHistory(HistoryAction.SCROLL_SG_XN);
                if (deltaSecondPos.getY() > 0) this.cobbletTool.addHistory(HistoryAction.SCROLL_SG_YP);
                if (deltaSecondPos.getY() < 0) this.cobbletTool.addHistory(HistoryAction.SCROLL_SG_YN);
                if (deltaSecondPos.getZ() > 0) this.cobbletTool.addHistory(HistoryAction.SCROLL_SG_ZP);
                if (deltaSecondPos.getZ() < 0) this.cobbletTool.addHistory(HistoryAction.SCROLL_SG_ZN);
                house.updateGizmoState(new ArrayList<>(Arrays.asList(oldFirstPos, oldSecondPos)));
            }
        }

        if (gladeObject.wallPos1Gizmo.isGrabbed() ||
                gladeObject.wallPos2Gizmo.isGrabbed() ||
                gladeObject.mainMoveGizmo.isGrabbed() || showGizmo) {
            gladeObject.wallPos1Gizmo.render(matrices, camera, isCtrlDown);
            gladeObject.wallPos2Gizmo.render(matrices, camera, isCtrlDown);
            gladeObject.mainMoveGizmo.render(matrices, camera, isCtrlDown);
        }
    }
}
