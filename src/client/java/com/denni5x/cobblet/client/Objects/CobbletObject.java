package com.denni5x.cobblet.client.Objects;

import com.denni5x.cobblet.client.CobbletTool;
import com.moulberry.axiom.gizmo.Gizmo;
import com.moulberry.axiom.render.regions.ChunkedBlockRegion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;


public class CobbletObject {
    public static Logger LOGGER = LoggerFactory.getLogger(CobbletObject.class);
    public final CobbletTool cobbletTool;
    public ChunkedBlockRegion chunkedBlockRegion;
    public Gizmo mainMoveGizmo;
    public ArrayList<Gizmo> gizmos;
    public Gizmo wallPos1Gizmo;
    public Gizmo wallPos2Gizmo;
    public int[] position;
    public int[] size;
    protected final boolean isRecord;

    public CobbletObject(CobbletTool cobbletTool, boolean isRecord) {
        this.cobbletTool = cobbletTool;
        this.isRecord = isRecord;
        this.gizmos = new ArrayList<>();
        this.chunkedBlockRegion = new ChunkedBlockRegion();
    }

    public void updateGizmoState() {
        if (this.isRecord) return;
        if (this.cobbletTool != null) {
            this.cobbletTool.setRecentSize(size, this);
        }
    }

    public void updateGizmosFromPositionSize() {

    }
}
