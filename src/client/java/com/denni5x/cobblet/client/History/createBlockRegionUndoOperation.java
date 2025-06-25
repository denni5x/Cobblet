package com.denni5x.cobblet.client.History;

import com.denni5x.cobblet.client.CobbletTool;
import com.denni5x.cobblet.client.Objects.CobbletObject;
import com.moulberry.axiom.tools.Tool;
import com.moulberry.axiom.tools.ToolManager;
import com.moulberry.axiom.world_modification.undo.AdditionalUndoOperation;

import java.util.List;

public record createBlockRegionUndoOperation(
        List<CobbletObject> cobbletObjects, int currentlySelected) implements AdditionalUndoOperation {

    public createBlockRegionUndoOperation(List<CobbletObject> cobbletObjects, int currentlySelected) {
        this.cobbletObjects = cobbletObjects;
        this.currentlySelected = currentlySelected;
    }

    public void perform() {
        if (ToolManager.isToolActive()) {
            Tool tool = ToolManager.getCurrentTool();
            if (ToolManager.getCurrentTool() instanceof CobbletTool) {
                CobbletTool cobbletTool = (CobbletTool) tool;
                cobbletTool.restoreBlockRegion(this.cobbletObjects, this.currentlySelected);
            }
        }

    }

    public List<CobbletObject> gladeObjects() {
        return this.cobbletObjects;
    }

    public int currentlySelected() {
        return this.currentlySelected;
    }
}
