package com.denni5x.cobblet.client;

import com.moulberry.axiom.block_maps.HDVoxelMap;
import net.minecraft.block.BlockState;
import net.minecraft.block.StairsBlock;
import net.minecraft.block.enums.StairShape;
import net.minecraft.util.math.Direction;

import java.util.HashMap;
import java.util.Map;

public class StairCombiner {
    private static final Map<StairPropertiesCombo, StairProperties> combinationMap = new HashMap<>();

    static {
        add(Direction.SOUTH, StairShape.STRAIGHT, Direction.SOUTH, StairShape.OUTER_LEFT, Direction.SOUTH, StairShape.STRAIGHT);
        add(Direction.SOUTH, StairShape.STRAIGHT, Direction.WEST, StairShape.OUTER_LEFT, Direction.SOUTH, StairShape.STRAIGHT);
        add(Direction.SOUTH, StairShape.STRAIGHT, Direction.NORTH, StairShape.OUTER_LEFT, Direction.WEST, StairShape.INNER_LEFT);
        add(Direction.SOUTH, StairShape.STRAIGHT, Direction.EAST, StairShape.OUTER_LEFT, Direction.SOUTH, StairShape.INNER_LEFT);
        add(Direction.SOUTH, StairShape.STRAIGHT, Direction.SOUTH, StairShape.STRAIGHT, Direction.SOUTH, StairShape.STRAIGHT);
        add(Direction.SOUTH, StairShape.STRAIGHT, Direction.WEST, StairShape.STRAIGHT, Direction.WEST, StairShape.INNER_LEFT);
        add(Direction.SOUTH, StairShape.STRAIGHT, Direction.NORTH, StairShape.STRAIGHT, null, null);
        add(Direction.SOUTH, StairShape.STRAIGHT, Direction.EAST, StairShape.STRAIGHT, Direction.SOUTH, StairShape.INNER_LEFT);
        add(Direction.SOUTH, StairShape.STRAIGHT, Direction.SOUTH, StairShape.INNER_LEFT, Direction.SOUTH, StairShape.INNER_LEFT);
        add(Direction.SOUTH, StairShape.STRAIGHT, Direction.WEST, StairShape.INNER_LEFT, Direction.WEST, StairShape.INNER_LEFT);
        add(Direction.SOUTH, StairShape.STRAIGHT, Direction.NORTH, StairShape.INNER_LEFT, null, null);
        add(Direction.SOUTH, StairShape.STRAIGHT, Direction.EAST, StairShape.INNER_LEFT, null, null);


        add(Direction.WEST, StairShape.STRAIGHT, Direction.WEST, StairShape.OUTER_LEFT, Direction.WEST, StairShape.STRAIGHT);
        add(Direction.WEST, StairShape.STRAIGHT, Direction.NORTH, StairShape.OUTER_LEFT, Direction.WEST, StairShape.STRAIGHT);
        add(Direction.WEST, StairShape.STRAIGHT, Direction.EAST, StairShape.OUTER_LEFT, Direction.NORTH, StairShape.INNER_LEFT);
        add(Direction.WEST, StairShape.STRAIGHT, Direction.SOUTH, StairShape.OUTER_LEFT, Direction.WEST, StairShape.INNER_LEFT);
        add(Direction.WEST, StairShape.STRAIGHT, Direction.WEST, StairShape.STRAIGHT, Direction.WEST, StairShape.STRAIGHT);
        add(Direction.WEST, StairShape.STRAIGHT, Direction.NORTH, StairShape.STRAIGHT, Direction.NORTH, StairShape.INNER_LEFT);
        add(Direction.WEST, StairShape.STRAIGHT, Direction.EAST, StairShape.STRAIGHT, null, null);
        add(Direction.WEST, StairShape.STRAIGHT, Direction.SOUTH, StairShape.STRAIGHT, Direction.WEST, StairShape.INNER_LEFT);
        add(Direction.WEST, StairShape.STRAIGHT, Direction.WEST, StairShape.INNER_LEFT, Direction.WEST, StairShape.INNER_LEFT);
        add(Direction.WEST, StairShape.STRAIGHT, Direction.NORTH, StairShape.INNER_LEFT, Direction.NORTH, StairShape.INNER_LEFT);
        add(Direction.WEST, StairShape.STRAIGHT, Direction.EAST, StairShape.INNER_LEFT, null, null);
        add(Direction.WEST, StairShape.STRAIGHT, Direction.SOUTH, StairShape.INNER_LEFT, null, null);


        add(Direction.NORTH, StairShape.STRAIGHT, Direction.NORTH, StairShape.OUTER_LEFT, Direction.NORTH, StairShape.STRAIGHT);
        add(Direction.NORTH, StairShape.STRAIGHT, Direction.EAST, StairShape.OUTER_LEFT, Direction.NORTH, StairShape.STRAIGHT);
        add(Direction.NORTH, StairShape.STRAIGHT, Direction.SOUTH, StairShape.OUTER_LEFT, Direction.EAST, StairShape.INNER_LEFT);
        add(Direction.NORTH, StairShape.STRAIGHT, Direction.WEST, StairShape.OUTER_LEFT, Direction.NORTH, StairShape.INNER_LEFT);
        add(Direction.NORTH, StairShape.STRAIGHT, Direction.NORTH, StairShape.STRAIGHT, Direction.NORTH, StairShape.STRAIGHT);
        add(Direction.NORTH, StairShape.STRAIGHT, Direction.EAST, StairShape.STRAIGHT, Direction.EAST, StairShape.INNER_LEFT);
        add(Direction.NORTH, StairShape.STRAIGHT, Direction.SOUTH, StairShape.STRAIGHT, null, null);
        add(Direction.NORTH, StairShape.STRAIGHT, Direction.WEST, StairShape.STRAIGHT, Direction.NORTH, StairShape.INNER_LEFT);
        add(Direction.NORTH, StairShape.STRAIGHT, Direction.NORTH, StairShape.INNER_LEFT, Direction.NORTH, StairShape.INNER_LEFT);
        add(Direction.NORTH, StairShape.STRAIGHT, Direction.EAST, StairShape.INNER_LEFT, Direction.EAST, StairShape.INNER_LEFT);
        add(Direction.NORTH, StairShape.STRAIGHT, Direction.SOUTH, StairShape.INNER_LEFT, null, null);
        add(Direction.NORTH, StairShape.STRAIGHT, Direction.WEST, StairShape.INNER_LEFT, null, null);


        add(Direction.EAST, StairShape.STRAIGHT, Direction.EAST, StairShape.OUTER_LEFT, Direction.EAST, StairShape.STRAIGHT);
        add(Direction.EAST, StairShape.STRAIGHT, Direction.SOUTH, StairShape.OUTER_LEFT, Direction.EAST, StairShape.STRAIGHT);
        add(Direction.EAST, StairShape.STRAIGHT, Direction.WEST, StairShape.OUTER_LEFT, Direction.SOUTH, StairShape.INNER_LEFT);
        add(Direction.EAST, StairShape.STRAIGHT, Direction.NORTH, StairShape.OUTER_LEFT, Direction.EAST, StairShape.INNER_LEFT);
        add(Direction.EAST, StairShape.STRAIGHT, Direction.EAST, StairShape.STRAIGHT, Direction.EAST, StairShape.STRAIGHT);
        add(Direction.EAST, StairShape.STRAIGHT, Direction.SOUTH, StairShape.STRAIGHT, Direction.SOUTH, StairShape.INNER_LEFT);
        add(Direction.EAST, StairShape.STRAIGHT, Direction.WEST, StairShape.STRAIGHT, null, null);
        add(Direction.EAST, StairShape.STRAIGHT, Direction.NORTH, StairShape.STRAIGHT, Direction.EAST, StairShape.INNER_LEFT);
        add(Direction.EAST, StairShape.STRAIGHT, Direction.EAST, StairShape.INNER_LEFT, Direction.EAST, StairShape.INNER_LEFT);
        add(Direction.EAST, StairShape.STRAIGHT, Direction.SOUTH, StairShape.INNER_LEFT, Direction.EAST, StairShape.INNER_LEFT);
        add(Direction.EAST, StairShape.STRAIGHT, Direction.WEST, StairShape.INNER_LEFT, null, null);
        add(Direction.EAST, StairShape.STRAIGHT, Direction.NORTH, StairShape.INNER_LEFT, null, null);
    }

    private static void add(Direction existingDirection, StairShape existingShape, Direction pasteDirection,
                            StairShape pasteShape, Direction resultDirection, StairShape resultShape) {
        StairProperties stair1 = new StairProperties(existingDirection, existingShape);
        StairProperties stair2 = new StairProperties(pasteDirection, pasteShape);
        StairProperties result = resultShape == null ? null : new StairProperties(resultDirection, resultShape);
        combinationMap.put(new StairPropertiesCombo(stair1, stair2), result);
    }

    public static BlockState combine(BlockState existingState, BlockState pasteState) {
        StairProperties stair1 = new StairProperties(existingState.get(StairsBlock.FACING), existingState.get(StairsBlock.SHAPE));
        StairProperties stair2 = new StairProperties(pasteState.get(StairsBlock.FACING), pasteState.get(StairsBlock.SHAPE));
        StairProperties resultStairProperties = combinationMap.getOrDefault(new StairPropertiesCombo(stair1, stair2), null);
        if (resultStairProperties == null) {
            resultStairProperties = combinationMap.getOrDefault(new StairPropertiesCombo(stair2, stair1), null);
        }
        return resultStairProperties != null ? existingState.with(StairsBlock.FACING,
                resultStairProperties.direction()).with(StairsBlock.SHAPE, resultStairProperties.shape()) :
                HDVoxelMap.getAssociatedBlocks(existingState.getBlock()).full().getDefaultState();
    }
}
