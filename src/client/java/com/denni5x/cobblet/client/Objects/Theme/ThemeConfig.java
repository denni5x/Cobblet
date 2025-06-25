package com.denni5x.cobblet.client.Objects.Theme;

import com.moulberry.axiom.utils.BlockWithFloat;
import net.minecraft.block.BlockState;

import java.util.List;

public record ThemeConfig(
        Theme theme,
        String description,

        BlockState stairEdge,
        BlockState slabEdge,
        BlockState fullEdge,
        BlockState stairRoof,
        BlockState slabRoof,
        BlockState fullRoof,
        BlockState fullWall,
        BlockState fullWallRoof,

        PatternRegion fullWallPattern,
        PatternRegion innerFloorPattern,
        PatternRegion topWallPattern,
        PatternRegion topWallEdgePattern,

        List<BlockWithFloat>blockPercentages
) {
}