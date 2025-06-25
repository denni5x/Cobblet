package com.denni5x.cobblet.client.History;

import com.denni5x.cobblet.client.CobbletTool;
import com.denni5x.cobblet.client.Objects.CobbletObject;
import com.moulberry.axiom.tools.Tool;
import com.moulberry.axiom.tools.ToolManager;
import com.moulberry.axiom.world_modification.undo.AdditionalUndoOperation;

import java.util.ArrayList;

public record createUndoOperation(HistoryAction historyAction, ArrayList<CobbletObject> gladeObjects,
                                  int currentlySelected)
        implements AdditionalUndoOperation {

    public createUndoOperation(HistoryAction historyAction, ArrayList<CobbletObject> gladeObjects, int currentlySelected) {
        this.historyAction = historyAction;
        this.gladeObjects = gladeObjects;
        this.currentlySelected = currentlySelected;
    }

    public void perform() {
        if (ToolManager.isToolActive()) {
            Tool tool = ToolManager.getCurrentTool();
            if (ToolManager.getCurrentTool() instanceof CobbletTool) {
                CobbletTool gladeTool = (CobbletTool) tool;
                gladeTool.restore(this.gladeObjects, this.currentlySelected);
            }
        }
    }

    public HistoryAction historyAction() {
        return this.historyAction;
    }
}
