package com.denni5x.cobblet.client.Objects;

import com.denni5x.cobblet.client.CobbletTool;
import com.denni5x.cobblet.client.Objects.Theme.Theme;
import com.denni5x.cobblet.client.Objects.Theme.ThemeConfig;
import com.moulberry.axiom.gizmo.Gizmo;
import com.moulberry.axiom.render.regions.ChunkedBlockRegion;

import java.util.ArrayList;


public abstract class CobbletObject {


    public ChunkedBlockRegion chunkedBlockRegion;
    public ArrayList<Gizmo> gizmos;
    public Gizmo mainMoveGizmo;
    public Gizmo wallPos1Gizmo;
    public Gizmo wallPos2Gizmo;
    public int[] position;
    public int[] size;
    protected CobbletTool cobbletTool;
    protected boolean isRecord;
    protected Theme theme;
    protected ThemeConfig themeConfig;


    public Theme getType() {
        return this.theme;
    }

    public abstract void updateGizmoState();

    public abstract void updateGizmosFromPositionSize();

    public abstract boolean renderSettings();
}
