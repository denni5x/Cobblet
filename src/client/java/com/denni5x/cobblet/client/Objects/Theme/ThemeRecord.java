package com.denni5x.cobblet.client.Objects.Theme;

import com.moulberry.axiom.blueprint.Blueprint;
import com.moulberry.axiom.blueprint.BlueprintIo;
import com.moulberry.axiom.custom_blocks.CustomBlockState;
import com.moulberry.axiom.render.regions.ChunkedBlockRegion;
import com.moulberry.axiom.tools.stamp.TransformedBlockRegions;
import com.moulberry.axiom.utils.BlockWithFloat;
import net.minecraft.block.Blocks;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ThemeRecord {
    private static final HashMap<Theme, ThemeConfig> houseConfigMap;

    static {
        houseConfigMap = new HashMap<>();
        houseConfigMap.put(Theme.BRICKS,
                new ThemeConfig(
                        Theme.BRICKS,
                        "Bricks",
                        Blocks.STONE_BRICK_STAIRS.getDefaultState(),
                        Blocks.STONE_BRICK_SLAB.getDefaultState(),
                        Blocks.STONE_BRICKS.getDefaultState(),
                        Blocks.SPRUCE_STAIRS.getDefaultState(),
                        Blocks.SPRUCE_SLAB.getDefaultState(),
                        Blocks.SPRUCE_PLANKS.getDefaultState(),
                        Blocks.STONE.getDefaultState(),
                        Blocks.STONE_BRICK_WALL.getDefaultState(),
                        loadBlockRegion("Blueprints/Bricks/fullwall.bp", new int[]{0, 0, 0}),
                        loadBlockRegion("Blueprints/Bricks/innerfloor.bp", new int[]{0, 0, 0}),
                        loadBlockRegion("Blueprints/Bricks/topwallpattern.bp", new int[]{-1, -1, -1}),
                        loadBlockRegion("Blueprints/Bricks/topwalledgepattern.bp", new int[]{0, +1, -1}),
                        loadAutoShadeBlockPalette(Theme.BRICKS)
                ));
        houseConfigMap.put(Theme.PLAIN,
                new ThemeConfig(
                        Theme.PLAIN,
                        "Plain",
                        Blocks.SPRUCE_STAIRS.getDefaultState(),
                        Blocks.SPRUCE_SLAB.getDefaultState(),
                        Blocks.MANGROVE_WOOD.getDefaultState(),
                        Blocks.OAK_STAIRS.getDefaultState(),
                        Blocks.OAK_SLAB.getDefaultState(),
                        Blocks.OAK_PLANKS.getDefaultState(),
                        Blocks.SPRUCE_PLANKS.getDefaultState(),
                        Blocks.SPRUCE_FENCE.getDefaultState(),
                        loadBlockRegion("Blueprints/Plain/fullwall.bp", new int[]{0, 0, 0}),
                        loadBlockRegion("Blueprints/Plain/innerfloor.bp", new int[]{0, 0, 0}),
                        loadBlockRegion("Blueprints/Plain/topwallpattern.bp", new int[]{-1, -1, -1}),
                        loadBlockRegion("Blueprints/Plain/topwalledgepattern.bp", new int[]{0, +1, 0}),
                        loadAutoShadeBlockPalette(Theme.PLAIN)
                ));
        houseConfigMap.put(Theme.DESERT,
                new ThemeConfig(
                        Theme.DESERT,
                        "Desert",
                        Blocks.SANDSTONE_STAIRS.getDefaultState(),
                        Blocks.SANDSTONE_SLAB.getDefaultState(),
                        Blocks.CUT_SANDSTONE.getDefaultState(),
                        Blocks.OAK_STAIRS.getDefaultState(),
                        Blocks.OAK_SLAB.getDefaultState(),
                        Blocks.OAK_PLANKS.getDefaultState(),
                        Blocks.SMOOTH_SANDSTONE.getDefaultState(),
                        Blocks.SANDSTONE_WALL.getDefaultState(),
                        loadBlockRegion("Blueprints/Desert/fullwall.bp", new int[]{0, 0, 0}),
                        loadBlockRegion("Blueprints/Desert/innerfloor.bp", new int[]{0, 0, 0}),
                        loadBlockRegion("Blueprints/Desert/topwallpattern.bp", new int[]{-1, -1, -1}),
                        loadBlockRegion("Blueprints/Desert/topwalledgepattern.bp", new int[]{0, +1, -1}),
                        loadAutoShadeBlockPalette(Theme.DESERT)
                ));
        houseConfigMap.put(Theme.NOORDIGRAD,
                new ThemeConfig(
                        Theme.NOORDIGRAD,
                        "Noordigrad",
                        Blocks.STONE_BRICK_STAIRS.getDefaultState(),
                        Blocks.STONE_BRICK_SLAB.getDefaultState(),
                        Blocks.STONE_BRICKS.getDefaultState(),
                        Blocks.BRICK_STAIRS.getDefaultState(),
                        Blocks.BRICK_SLAB.getDefaultState(),
                        Blocks.BRICKS.getDefaultState(),
                        Blocks.STONE.getDefaultState(),
                        Blocks.STONE_BRICK_WALL.getDefaultState(),
                        loadBlockRegion("Blueprints/Noordigrad/fullwall.bp", new int[]{0, 0, 0}),
                        loadBlockRegion("Blueprints/Noordigrad/innerfloor.bp", new int[]{0, 0, 0}),
                        loadBlockRegion("Blueprints/Noordigrad/topwallpattern.bp", new int[]{-1, -1, -1}),
                        loadBlockRegion("Blueprints/Noordigrad/topwalledgepattern.bp", new int[]{0, +1, -1}),
                        loadAutoShadeBlockPalette(Theme.NOORDIGRAD)
                ));
    }

    public static ThemeConfig getHouseConfig(Theme theme) {
        return houseConfigMap.get(theme);
    }

    private static List<BlockWithFloat> loadAutoShadeBlockPalette(Theme theme) {
        List<BlockWithFloat> blockPercentages = new ArrayList<>();
        switch (theme) {
            case BRICKS, NOORDIGRAD -> {
                blockPercentages.add(new BlockWithFloat((CustomBlockState) Blocks.DEEPSLATE.getDefaultState(), new float[]{20.0F}));
                blockPercentages.add(new BlockWithFloat((CustomBlockState) Blocks.TUFF.getDefaultState(), new float[]{35.0F}));
                blockPercentages.add(new BlockWithFloat((CustomBlockState) Blocks.ANDESITE.getDefaultState(), new float[]{45.0F}));
                blockPercentages.add(new BlockWithFloat((CustomBlockState) Blocks.STONE.getDefaultState(), new float[]{60.0F}));
            }
            case PLAIN -> {
                blockPercentages.add(new BlockWithFloat((CustomBlockState) Blocks.ROOTED_DIRT.getDefaultState(), new float[]{20.0F}));
                blockPercentages.add(new BlockWithFloat((CustomBlockState) Blocks.COARSE_DIRT.getDefaultState(), new float[]{35.0F}));
                blockPercentages.add(new BlockWithFloat((CustomBlockState) Blocks.DIRT.getDefaultState(), new float[]{45.0F}));
                blockPercentages.add(new BlockWithFloat((CustomBlockState) Blocks.SPRUCE_PLANKS.getDefaultState(), new float[]{60.0F}));
            }
            case DESERT -> {
                blockPercentages.add(new BlockWithFloat((CustomBlockState) Blocks.CALCITE.getDefaultState(), new float[]{20.0F}));
                blockPercentages.add(new BlockWithFloat((CustomBlockState) Blocks.SAND.getDefaultState(), new float[]{35.0F}));
                blockPercentages.add(new BlockWithFloat((CustomBlockState) Blocks.SANDSTONE.getDefaultState(), new float[]{45.0F}));
                blockPercentages.add(new BlockWithFloat((CustomBlockState) Blocks.SMOOTH_SANDSTONE.getDefaultState(), new float[]{60.0F}));
            }
            default -> {
                blockPercentages.add(new BlockWithFloat((CustomBlockState) Blocks.DEEPSLATE.getDefaultState(), new float[]{25.0F}));
                blockPercentages.add(new BlockWithFloat((CustomBlockState) Blocks.TUFF.getDefaultState(), new float[]{25.0F}));
                blockPercentages.add(new BlockWithFloat((CustomBlockState) Blocks.ANDESITE.getDefaultState(), new float[]{25.0F}));
                blockPercentages.add(new BlockWithFloat((CustomBlockState) Blocks.STONE.getDefaultState(), new float[]{25.0F}));
            }
        }
        return blockPercentages;
    }

    private static PatternRegion loadBlockRegion(String name, int[] positionOffset) {
        TransformedBlockRegions transformedBlockRegions = new TransformedBlockRegions(new ChunkedBlockRegion(), null);
        InputStream is = ThemeRecord.class.getClassLoader().getResourceAsStream(name);
        if (is != null) {
            try {
                Blueprint fullBlueprint = BlueprintIo.readBlueprint(is);
                transformedBlockRegions = new TransformedBlockRegions(fullBlueprint.blockRegion(), fullBlueprint.blockEntities());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return new PatternRegion(transformedBlockRegions, positionOffset);

    }
}
