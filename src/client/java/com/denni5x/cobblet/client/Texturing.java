package com.denni5x.cobblet.client;

import com.moulberry.axiom.BlueNoiseArray;
import com.moulberry.axiom.block_maps.BlockColourMap;
import com.moulberry.axiom.operations.AutoshadeShading;
import com.moulberry.axiom.render.regions.ChunkedBlockRegion;
import com.moulberry.axiom.utils.BlockWithFloat;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.List;

public class Texturing {
    public static ChunkedBlockRegion autoshadeWalls(ChunkedBlockRegion chunkedBlockRegion, boolean sunShade,
                                                    boolean ambientShade, AutoshadeShading shading,
                                                    float globalIllumination, float dither, List<BlockWithFloat> customPalette,
                                                    int paletteFlags) {
        globalIllumination = Math.max(0.0F, Math.min(1.0F, globalIllumination));
        ChunkedBlockRegion blockRegion = new ChunkedBlockRegion();

        if (sunShade || ambientShade) {
            BlockPos.Mutable mutableBlockPos = new BlockPos.Mutable();
            float customPaletteTotal = 0.0F;
            if (customPalette != null) {
                for (BlockWithFloat blockWithFloat : customPalette) {
                    customPaletteTotal += blockWithFloat.percentage()[0];
                }
            }

            float finalCustomPaletteTotal = customPaletteTotal;
            float finalGlobalIllumination = globalIllumination;
            chunkedBlockRegion.forEachEntry((x, y, z, blockState) -> {
                BlockState shadedBlock = null;
                if (blockState.getBlock() == Blocks.STONE) {
                    shadedBlock = getShadedBlock(chunkedBlockRegion, x, y, z, mutableBlockPos, sunShade, ambientShade, shading,
                            finalGlobalIllumination, dither, customPalette, finalCustomPaletteTotal, paletteFlags);
                }
                blockRegion.addBlock(x, y, z, shadedBlock == null ? blockState : shadedBlock);
            });
        }
        return blockRegion;
    }

    private static BlockState getShadedBlock(ChunkedBlockRegion chunkedBlockRegion, int x, int y, int z, BlockPos.Mutable mutableBlockPos,
                                             boolean sunShade, boolean ambientShade, AutoshadeShading shading,
                                             float globalIllumination, float dither, List<BlockWithFloat> customPalette,
                                             float customPaletteTotal, int paletteFlags) {
        BlockState block = chunkedBlockRegion.getBlockState(mutableBlockPos.set(x, y, z));
        if (!block.blocksMovement()) {
            return null;
        } else {
            double[] lab = BlockColourMap.getLab(block.getBlock());
            if (lab == null) {
                return block;
            } else if (chunkedBlockRegion.getBlockState(mutableBlockPos.set(x + 1, y, z)).blocksMovement() &&
                    chunkedBlockRegion.getBlockState(mutableBlockPos.set(x - 1, y, z)).blocksMovement() &&
                    chunkedBlockRegion.getBlockState(mutableBlockPos.set(x, y + 1, z)).blocksMovement() &&
                    chunkedBlockRegion.getBlockState(mutableBlockPos.set(x, y - 1, z)).blocksMovement() &&
                    chunkedBlockRegion.getBlockState(mutableBlockPos.set(x, y, z + 1)).blocksMovement() &&
                    chunkedBlockRegion.getBlockState(mutableBlockPos.set(x, y, z - 1)).blocksMovement()) {
                return block;
            } else {
                float offsetX = 0.0F;
                float offsetY = 0.0F;
                float offsetZ = 0.0F;
                float filled = 0.0F;
                float total = 0.0F;

                for (int xo = -8; xo <= 8; ++xo) {
                    for (int yo = -8; yo <= 8; ++yo) {
                        for (int zo = -8; zo <= 8; ++zo) {
                            int distSq = xo * xo + yo * yo + zo * zo;
                            if (distSq <= 72) {
                                ++total;
                                BlockState neighbor = chunkedBlockRegion.getBlockState(mutableBlockPos.set(x + xo, y + yo, z + zo));
                                if (neighbor.blocksMovement()) {
                                    float factor = 1.0F / (float) Math.max((double) 1.0F, Math.sqrt((double) distSq));
                                    ++filled;
                                    offsetX -= (float) xo * factor;
                                    offsetY -= (float) yo * factor;
                                    offsetZ -= (float) zo * factor;
                                }
                            }
                        }
                    }
                }

                float shade = 1.0F;
                if (ambientShade) {
                    float ambientFactor = Math.min(1.0F, Math.max(0.0F, 2.0F * (1.0F - filled / total)));
                    shade *= Math.max(globalIllumination, ambientFactor * ambientFactor);
                }

                if (sunShade && (offsetX != 0.0F || offsetY != 0.0F || offsetZ != 0.0F)) {
                    float invNormalLength = 1.0F / (float) Math.sqrt((double) (offsetX * offsetX + offsetY * offsetY + offsetZ * offsetZ));
                    float normalX = offsetX * invNormalLength;
                    float normalY = offsetY * invNormalLength;
                    float normalZ = offsetZ * invNormalLength;
                    float sunDot = 0.0F;

                    for (Vec3d vector : shading.vectors) {
                        float newSunDot = (float) vector.x * normalX + (float) vector.y * normalY + (float) vector.z * normalZ;
                        sunDot = Math.max(sunDot, newSunDot * 0.5F + 0.5F);
                    }

                    for (AutoshadeShading.PositionWithIntensity position : shading.positions) {
                        float deltaX = position.x() - ((float) x + 0.5F);
                        float deltaY = position.y() - ((float) y + 0.5F);
                        float deltaZ = position.z() - ((float) z + 0.5F);
                        float deltaDist = (float) Math.sqrt((double) (deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ));
                        float newSunDot = deltaX / deltaDist * normalX + deltaY / deltaDist * normalY + deltaZ / deltaDist * normalZ;
                        sunDot = Math.max(sunDot, (newSunDot * 0.5F + 0.5F) * (float) Math.sqrt((double) position.intensity()));
                    }

                    shade *= Math.max(globalIllumination, sunDot);
                }

                if (shade < 0.0F) {
                    shade = 0.0F;
                }

                if (shade > 1.0F) {
                    shade = 1.0F;
                }

                if ((double) dither > 0.01) {
                    float noise = BlueNoiseArray.NOISE[(x & 31) + (y & 31) * 32 + (z & 31) * 32 * 32];
                    shade += noise * dither - dither / 2.0F;
                    if (shade < 0.0F) {
                        shade = 0.0F;
                    }

                    if (shade > 1.0F) {
                        shade = 1.0F;
                    }
                }

                if (customPalette == null) {
                    double lightness = lab[0] * (double) shade;
                    return BlockColourMap.getNearestLab(lightness, lab[1], lab[2], paletteFlags);
                } else {
                    float shadeTimesTotal = shade * customPaletteTotal;
                    BlockState shadedBlock = null;

                    for (BlockWithFloat blockWithFloat : customPalette) {
                        shadeTimesTotal -= blockWithFloat.percentage()[0];
                        if (shadeTimesTotal <= 0.0F) {
                            shadedBlock = blockWithFloat.blockState().getVanillaState();
                            break;
                        }
                    }

                    if (shadedBlock == null) {
                        shadedBlock = ((BlockWithFloat) customPalette.get(customPalette.size() - 1)).blockState().getVanillaState();
                    }

                    return shadedBlock;
                }
            }
        }
    }

}
