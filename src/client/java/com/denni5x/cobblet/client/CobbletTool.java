package com.denni5x.cobblet.client;

import com.denni5x.cobblet.client.History.HistoryAction;
import com.denni5x.cobblet.client.History.createBlockRegionUndoOperation;
import com.denni5x.cobblet.client.Objects.BlueprintAsset.BlueprintAsset;
import com.denni5x.cobblet.client.Objects.CobbletObject;
import com.denni5x.cobblet.client.Objects.House.House;
import com.denni5x.cobblet.client.Objects.House.HouseRoofType;
import com.denni5x.cobblet.client.Objects.Tower.Tower;
import com.moulberry.axiom.RayCaster;
import com.moulberry.axiom.Restrictions;
import com.moulberry.axiom.UserAction;
import com.moulberry.axiom.block_maps.HDVoxelMap;
import com.moulberry.axiom.blueprint.Blueprint;
import com.moulberry.axiom.brush_shapes.SphereBrushShape;
import com.moulberry.axiom.custom_blocks.CustomBlockState;
import com.moulberry.axiom.editor.EditorUI;
import com.moulberry.axiom.editor.ImGuiHelper;
import com.moulberry.axiom.editor.widgets.SelectBlockWidget;
import com.moulberry.axiom.editor.windows.clipboard.BlueprintBrowserWindow;
import com.moulberry.axiom.gizmo.Gizmo;
import com.moulberry.axiom.i18n.AxiomI18n;
import com.moulberry.axiom.operations.AutoshadeShading;
import com.moulberry.axiom.render.Effects;
import com.moulberry.axiom.render.SharedBrushPreviewRenderer;
import com.moulberry.axiom.render.regions.ChunkedBlockRegion;
import com.moulberry.axiom.services.RegionProvider;
import com.moulberry.axiom.tools.Tool;
import com.moulberry.axiom.tools.ToolManager;
import com.moulberry.axiom.utils.BlockWithFloat;
import com.moulberry.axiom.utils.PositionUtils;
import com.moulberry.axiom.utils.RegionHelper;
import com.moulberry.axiom.world_modification.HistoryEntry;
import imgui.ImGui;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.StairsBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.EmptyBlockView;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

public class CobbletTool implements Tool {
    private static final Logger LOGGER = LoggerFactory.getLogger(CobbletTool.class);
    private static final SelectBlockWidget selectBlockWidget;
    private static final List<BlockWithFloat> blockPercentages;

    static {
        selectBlockWidget = new SelectBlockWidget(false);
        blockPercentages = new ArrayList<>();
        blockPercentages.add(new BlockWithFloat((CustomBlockState) Blocks.DEEPSLATE.getDefaultState(), new float[]{20.0F}));
        blockPercentages.add(new BlockWithFloat((CustomBlockState) Blocks.TUFF.getDefaultState(), new float[]{35.0F}));
        blockPercentages.add(new BlockWithFloat((CustomBlockState) Blocks.ANDESITE.getDefaultState(), new float[]{45.0F}));
        blockPercentages.add(new BlockWithFloat((CustomBlockState) Blocks.STONE.getDefaultState(), new float[]{60.0F}));
    }

    public final House recentHouseSettings;
    public final Tower recentTowerSettings;
    public final BlueprintAsset blueprintAsset;
    private final int[] mode;
    private final Renderer renderer;
    private final float[] opacity;
    public int currentlySelected;
    public ArrayList<CobbletObject> cobbletObjects = null;
    private CobbletObject copiedCobbletObject;
    private boolean keepExisting;
    private boolean changeAutoshadePalette = false;
    private boolean doAutoTexture = false;

    public CobbletTool() {
        this.keepExisting = false;
        RegionProvider regionProvider = new RegionProvider();
        this.currentlySelected = -1;
        this.cobbletObjects = new ArrayList<>();
        this.mode = new int[]{0};
        this.renderer = new Renderer(this);
        this.opacity = new float[]{0.7f};
        this.recentHouseSettings = new House(new int[]{8, 4, 8}, this, true);
        this.recentTowerSettings = new Tower(new int[]{12, 20, 12}, this, true);
        this.blueprintAsset = new BlueprintAsset(this, true);
    }

    private static boolean shouldReplaceExisting(boolean keepExisting, BlockState existingState, BlockState pasteState) {
        if (existingState.isAir()) {
            return !pasteState.isAir();
        } else if (existingState.isReplaceable() && !pasteState.isReplaceable()) {
            return true;
        } else {
            VoxelShape existingShape;
            VoxelShape pasteShape;
            if (keepExisting) {
                existingShape = existingState.getOutlineShape(EmptyBlockView.INSTANCE, BlockPos.ORIGIN);
                pasteShape = pasteState.getOutlineShape(EmptyBlockView.INSTANCE, BlockPos.ORIGIN);
                return existingShape != pasteShape && VoxelShapes.matchesAnywhere(existingShape, pasteShape, BooleanBiFunction.ONLY_SECOND);
            } else {
                existingShape = existingState.getOutlineShape(EmptyBlockView.INSTANCE, BlockPos.ORIGIN);
                pasteShape = pasteState.getOutlineShape(EmptyBlockView.INSTANCE, BlockPos.ORIGIN);
                if (existingShape == pasteShape) {
                    return true;
                } else {
                    return !VoxelShapes.matchesAnywhere(pasteShape, existingShape, BooleanBiFunction.ONLY_SECOND);
                }
            }
        }
    }

    @Override
    public void reset() {
    }

    @Override
    public void render(Camera camera, float tickDelta, long time, MatrixStack matrices, Matrix4f projection) {
        BlockPos hitResult = getHitResult();
        if (hitResult != null) {
            SharedBrushPreviewRenderer.render(SphereBrushShape.create(0), camera, Vec3d.of(hitResult), matrices, projection, time, Effects.OUTLINE);
        }

        if (!this.cobbletObjects.isEmpty()) {
            Vec3d lookDirection = Tool.getLookDirection();
            boolean isLeftDown = Tool.isMouseDown(0);
            boolean isCtrlDown = EditorUI.isCtrlOrCmdDown();
            boolean showGizmo = !EditorUI.isActive() || !isCtrlDown;
            this.renderer.setToolSettings(lookDirection, isLeftDown, isCtrlDown, showGizmo, camera, time, matrices, this.currentlySelected, projection);
            this.renderer.renderObjects(cobbletObjects, this.opacity[0]);
        }
    }

    public void displayImguiOptions() {
        ImGuiHelper.separatorWithText("DBTools: Cobblet");

        if (ImGui.checkbox(AxiomI18n.get("axiom.editorui.window.clipboard.placement_options.keep_existing"), this.keepExisting)) {
            this.keepExisting = !this.keepExisting;
        }

        ImGui.sliderFloat("Render Opacity", this.opacity, 0.0f, 1.0f);
        if (this.opacity[0] < 0.0f) this.opacity[0] = 0.0f;
        if (this.opacity[0] > 1.0f) this.opacity[0] = 1.0f;

        ImGuiHelper.combo("Mode", this.mode, new String[]{"House", "Tower", "Assets"});

        if (this.mode[0] == 2) {
            ImGuiHelper.setupBorder();
            this.displayBlueprintSettings();
            ImGuiHelper.finishBorder();
        }

        if (!this.cobbletObjects.isEmpty() && this.currentlySelected >= 0) {
            ImGuiHelper.setupBorder();
            ImGuiHelper.separatorWithText("Object Settings");
            if (this.cobbletObjects.get(this.currentlySelected) instanceof House house) {
                if (house.renderSettings()) {
                    this.recentHouseSettings.updateSettings(house.getType(), house.getRoofType(), house.getSteppedGableType(), house.getRoofOrientation());
                    this.addHistory(HistoryAction.OBJECT_SETTING);
                }
            }
            if (this.cobbletObjects.get(this.currentlySelected) instanceof Tower tower) {
                if (tower.renderSettings()) {
                    this.recentTowerSettings.updateSettings(tower.getType(), tower.getRoofType(), tower.getConeRounding(), tower.getRoofSizeY(), tower.getRoofOffset(), tower.getSize());
                    this.addHistory(HistoryAction.OBJECT_SETTING);
                }
            }
            if (this.cobbletObjects.get(this.currentlySelected) instanceof BlueprintAsset blueprint) {
                blueprint.renderSettings();
            }
            ImGuiHelper.finishBorder();
        }

        if (ImGui.checkbox("Do auto texture", this.doAutoTexture)) {
            this.doAutoTexture = !this.doAutoTexture;
        }

        if (ImGui.checkbox("Change Autoshade Palette", this.changeAutoshadePalette)) {
            this.changeAutoshadePalette = !this.changeAutoshadePalette;
        }
        if (this.changeAutoshadePalette) {
            ImGuiHelper.setupBorder();
            ImGuiHelper.separatorWithText("Autoshade Palette");
            BlockWithFloat.renderList(blockPercentages, selectBlockWidget, BlockWithFloat.ExtraRenderType.PERCENTAGE, 1);
            ImGuiHelper.finishBorder();
        }

        if (this.copiedCobbletObject != null) {
            ImGuiHelper.separatorWithText("Clipboard");
            if (ImGui.button("Clear Cobblet Clipboard")) {
                this.copiedCobbletObject = null;
            }
        }
    }

    public void displayBlueprintSettings() {
        ImGuiHelper.separatorWithText("Blueprint Settings");
        String buttonDescription = this.blueprintAsset.getBlueprint() == null ? AxiomI18n.get("axiom.tool.stamp.add_blueprint") : "Change Blueprint";
        if (ImGui.button(buttonDescription)) {
            if (!Restrictions.canImportBlocks) {
                ClientPlayerEntity player = MinecraftClient.getInstance().player;
                if (player != null) {
                    player.sendMessage(Text.literal("The server has disallowed the use of blueprints").formatted(Formatting.RED), false);
                }
            } else {
                Predicate<Blueprint> callback = (blueprintx) -> {
                    if (ToolManager.isToolActive() && ToolManager.getCurrentTool() instanceof CobbletTool) {
                        int[] offset = new int[3];
                        int minY = Objects.requireNonNull(blueprintx.blockRegion().min()).getY();
                        int maxY = Objects.requireNonNull(blueprintx.blockRegion().max()).getY();
                        offset[1] = (minY + maxY) / 2 - minY;
                        this.blueprintAsset.setOffset(offset);
                        this.blueprintAsset.setBlueprint(blueprintx);
                        return true;
                    } else {
                        return false;
                    }
                };
                BlueprintBrowserWindow.open(callback, true);
            }
        }
        if (this.blueprintAsset.getBlueprint() != null) {
            Blueprint blueprint = this.blueprintAsset.getBlueprint();
            int[] offsets = this.blueprintAsset.getOffsets();
            ImGui.imageButton(blueprint.thumbnail().getGlId(), 64.0F, 64.0F, 0.0F, 1.0F, 1.0F, 0.0F, 2);
            if (ImGui.isItemClicked(1)) {
                ImGui.openPopup("##EditRecentBlueprint");
            }

            if (ImGuiHelper.beginPopup("##EditRecentBlueprint")) {
                if (ImGui.menuItem(AxiomI18n.get("axiom.tool.path.remove"))) {
                    this.blueprintAsset.setBlueprint(null);
                }
                ImGui.endPopup();
            }

            ImGui.sameLine();
            ImGui.beginGroup();
            String name = blueprint.header().name();
            boolean blankName = name.isBlank();
            String displayName = !blankName ? name : AxiomI18n.get("axiom.editorui.window.blueprint_browser.unnamed_blueprint");
            String formattedCount = NumberFormat.getNumberInstance().format(blueprint.blockRegion().count());
            String blockCount = AxiomI18n.get("axiom.editorui.window.clipboard.n_blocks", formattedCount);
            ImGui.text(displayName + " (" + blockCount + ")");
            if (ImGuiHelper.inputInt(AxiomI18n.get("axiom.tool.stamp.offset") + "##", offsets, true)) {
                if (!blankName) {
                    this.blueprintAsset.setOffset(offsets);
                }
            }
            ImGui.endGroup();
        }
    }

    public void restoreBlockRegion(List<CobbletObject> cobbletObjects, int currentlySelected) {
        this.currentlySelected = currentlySelected;
        this.cobbletObjects.clear();
        this.cobbletObjects.addAll(cobbletObjects);
    }

    public UserAction.ActionResult callAction(UserAction action, Object object) {
        return switch (action) {
            case CUT, DELETE -> {
                if (!this.cobbletObjects.isEmpty() && this.currentlySelected >= 0) {
                    CobbletObject cobbletObject = this.cobbletObjects.get(this.currentlySelected);
                    this.cobbletObjects.remove(this.currentlySelected);
                    this.addHistory(HistoryAction.DELETE);
                    this.currentlySelected = -1;
                }
                yield UserAction.ActionResult.USED_CONT;
            }
            case ROTATE_PLACEMENT -> {
                if (!this.cobbletObjects.isEmpty() && this.currentlySelected >= 0) {
                    if (this.cobbletObjects.get(this.currentlySelected) instanceof House house) {
                        if (house.getRoofType() == HouseRoofType.STEPPED_GABLE ||
                                house.getRoofType() == HouseRoofType.GABLE ||
                                house.getRoofType() == HouseRoofType.THICK_GABLE ||
                                house.getRoofType() == HouseRoofType.LOW_ANGLE ||
                                house.getRoofType() == HouseRoofType.HIGH_ANGLE) {
                            Direction.Axis roofOrientation = house.getRoofOrientation() == Direction.Axis.X ? Direction.Axis.Z : Direction.Axis.X;
                            house.updateSettings(roofOrientation);
                            this.recentHouseSettings.updateSettings(roofOrientation);
                            this.addHistory(HistoryAction.ROTATE);
                        }
                    }
                    if (this.cobbletObjects.get(this.currentlySelected) instanceof BlueprintAsset blueprintAsset) {
                        blueprintAsset.rotate();
                        this.blueprintAsset.rotate();
                    }
                    this.addHistory(HistoryAction.ROTATE);
                }
                yield UserAction.ActionResult.USED_STOP;
            }
            case FLIP_PLACEMENT -> {
                if (this.currentlySelected == -1 || this.cobbletObjects.isEmpty()) {
                    yield UserAction.ActionResult.NOT_HANDLED;
                }
                Vec3d lookDirection = Tool.getLookDirection();
                if (lookDirection == null) {
                    lookDirection = MinecraftClient.getInstance().getCameraEntity().getRotationVector();
                }
                Direction direction = PositionUtils.orderedByNearest(lookDirection)[0];
                this.flip(this.cobbletObjects.get(this.currentlySelected), direction.getAxis());
                yield UserAction.ActionResult.USED_STOP;
            }
            case ENTER -> {
                this.enter();
                yield UserAction.ActionResult.USED_STOP;
            }
            case LEFT_MOUSE -> {
                if (this.leftClick()) {
                    yield UserAction.ActionResult.USED_STOP;
                }
                yield UserAction.ActionResult.NOT_HANDLED;
            }
            case RIGHT_MOUSE -> {
                this.rightClick();
                yield UserAction.ActionResult.USED_STOP;
            }
            case UNDO -> {
//                LOGGER.info("UNDO: i{} h{}", this.currentHistoryIndex, this.history.size());
//                if (this.prevHistWasBlockRegion) {
//                    this.prevHistWasBlockRegion = false;
//                    yield UserAction.ActionResult.USED_CONT;
//                }
//                if (!this.history.isEmpty() && this.currentHistoryIndex >= 0) {
//                    this.currentHistoryIndex -= 1;
//                    if (this.currentHistoryIndex == -1) {
//                        this.currentlySelected = -1;
//                        this.cobbletObjects.clear();
//                        yield UserAction.ActionResult.USED_CONT;
//                    }
//                    this.history.get(this.currentHistoryIndex).perform();
//                }
                yield UserAction.ActionResult.USED_CONT;
            }
            case REDO -> {
//                LOGGER.info("REDO: i{} h{}", this.currentHistoryIndex, this.history.size());
//                if (this.prevHistWasBlockRegion) {
//                    this.prevHistWasBlockRegion = false;
//                    yield UserAction.ActionResult.USED_CONT;
//                }
//                if (!this.history.isEmpty() && this.currentHistoryIndex >= -1) {
//                    if (this.currentHistoryIndex + 1 >= this.history.size()) yield UserAction.ActionResult.USED_CONT;
//                    this.currentHistoryIndex += 1;
//                    this.history.get(this.currentHistoryIndex).perform();
//                }
                yield UserAction.ActionResult.USED_CONT;
            }
            case COPY -> {
                if (copy()) {
                    yield UserAction.ActionResult.USED_STOP;
                }
                yield UserAction.ActionResult.NOT_HANDLED;
            }
            case PASTE -> {
                if (paste()) {
                    yield UserAction.ActionResult.USED_STOP;
                }
                yield UserAction.ActionResult.NOT_HANDLED;
            }
            case ESCAPE -> {
                if (this.deseletGizmos()) {
                    yield UserAction.ActionResult.USED_STOP;
                }
                yield UserAction.ActionResult.NOT_HANDLED;
            }
            default -> UserAction.ActionResult.NOT_HANDLED;
        };
    }

    public boolean deseletGizmos() {
        if (this.currentlySelected >= 0 && !this.cobbletObjects.isEmpty()) {
            for (CobbletObject cobbletObject : this.cobbletObjects) {
                if (cobbletObject instanceof House) {
                    cobbletObject.wallPos1Gizmo.enableAxes = false;
                    cobbletObject.mainMoveGizmo.enableAxes = false;
                    cobbletObject.wallPos2Gizmo.enableAxes = false;
                }
                if (cobbletObject instanceof BlueprintAsset || cobbletObject instanceof Tower) {
                    cobbletObject.mainMoveGizmo.enableAxes = false;
                    if (cobbletObject instanceof BlueprintAsset) {
                        cobbletObject.mainMoveGizmo.enableRotation = false;
                    }
                }
            }
            this.currentlySelected = -1;
            return true;
        } else {
            return false;
        }
    }

    public void enter() {
        ChunkedBlockRegion mergedBlockRegion = new ChunkedBlockRegion();
        ClientWorld level = MinecraftClient.getInstance().world;
        BlockPos.Mutable mutableBlockPos = new BlockPos.Mutable();

        for (CobbletObject cobbletObject : this.cobbletObjects) {
            cobbletObject.chunkedBlockRegion.forEachEntry((x, y, z, block) -> {
                HDVoxelMap.HDVoxelBaseBlocks blockMap = HDVoxelMap.getAssociatedBlocks(block.getBlock());

                BlockState existingStateFinalRegion = mergedBlockRegion.getBlockState(mutableBlockPos.set(x, y, z));
                if (existingStateFinalRegion.getBlock() instanceof SlabBlock && block.getBlock() instanceof SlabBlock && existingStateFinalRegion.get(SlabBlock.TYPE) != block.get(SlabBlock.TYPE)) {
                    mergedBlockRegion.addBlock(x, y, z, blockMap.full().getDefaultState());
                } else if (existingStateFinalRegion.getBlock() instanceof StairsBlock && block.getBlock() instanceof StairsBlock) {
                    BlockState blockState = StairCombiner.combine(existingStateFinalRegion, block);
                    mergedBlockRegion.addBlock(x, y, z, blockState);
                }

                assert level != null;
                BlockState existingState = level.getBlockState(mutableBlockPos.set(x, y, z));
                if (existingState.getBlock() instanceof SlabBlock && block.getBlock() instanceof SlabBlock && existingState.get(SlabBlock.TYPE) != block.get(SlabBlock.TYPE)) {
                    mergedBlockRegion.addBlock(x, y, z, blockMap.full().getDefaultState());
                } else if (existingState.getBlock() instanceof StairsBlock && block.getBlock() instanceof StairsBlock) {
                    BlockState blockState = StairCombiner.combine(existingState, block);
                    mergedBlockRegion.addBlock(x, y, z, blockState);
                } else {
                    if (!block.isAir() && shouldReplaceExisting(this.keepExisting, level.getBlockState(mutableBlockPos.set(x, y, z)), block) && shouldReplaceExisting(this.keepExisting, mergedBlockRegion.getBlockState(mutableBlockPos.set(x, y, z)), block)) {
                        mergedBlockRegion.addBlock(x, y, z, block);
                    }
                }
            });
        }
        ChunkedBlockRegion finalBlockRegion = this.doAutoTexture(mergedBlockRegion);

        int i = this.keepExisting ? HistoryEntry.MODIFIER_KEEP_EXISTING : 0;
        String countString = NumberFormat.getInstance().format(finalBlockRegion.count());
        String historyDescription = AxiomI18n.get("axiom.history_description.placed", countString);
        createBlockRegionUndoOperation operation = new createBlockRegionUndoOperation(new ArrayList<>(this.cobbletObjects), this.currentlySelected);
        RegionHelper.pushBlockRegionChange(finalBlockRegion, null, historyDescription, Tool.getSourceInfo(this), i, operation);

        this.cobbletObjects.clear();
        this.currentlySelected = -1;
    }

    public ChunkedBlockRegion doAutoTexture(ChunkedBlockRegion chunkedBlockRegion) {
        int distance = 60;
        if (!this.doAutoTexture) return chunkedBlockRegion;
        List<Vec3d> vectors = new ArrayList<>();
        List<AutoshadeShading.PositionWithIntensity> positions = new ArrayList<>();
        BlockPos minPos = chunkedBlockRegion.min();
        BlockPos maxPos = chunkedBlockRegion.max();
        float minX;
        float minZ;
        float maxX;
        float maxY;
        float maxZ;
        if (minPos != null && maxPos != null) {
            minX = minPos.getX() + distance;
            minZ = minPos.getZ() + distance;
            maxX = maxPos.getX() + distance;
            maxY = maxPos.getY() + distance;
            maxZ = maxPos.getZ() + distance;
            positions.add(new AutoshadeShading.PositionWithIntensity(minX, maxY, maxZ, 1.0F));
            positions.add(new AutoshadeShading.PositionWithIntensity(minX, maxY, minZ, 1.0F));
            positions.add(new AutoshadeShading.PositionWithIntensity(maxX, maxY, maxZ, 1.0F));
            positions.add(new AutoshadeShading.PositionWithIntensity(maxX, maxY, minZ, 1.0F));
        }
        if (positions.isEmpty()) {
            Entity camera = MinecraftClient.getInstance().getCameraEntity();
            if (camera != null) {
                positions.add(new AutoshadeShading.PositionWithIntensity((float) camera.getX(), (float) camera.getY(), (float) camera.getZ(), 1.0F));
            }
        }
        float globalIlluminationFloat = 0.0F;
        AutoshadeShading shading = new AutoshadeShading(vectors, positions);
        return Texturing.autoshadeWalls(chunkedBlockRegion, true, true, shading, globalIlluminationFloat, 0.1f, blockPercentages, 71);
    }

    public void flip(CobbletObject cobbletObject, Direction.Axis axis) {
        if (cobbletObject instanceof BlueprintAsset blueprintAsset) {
            switch (axis) {
                case X -> {
                    if (blueprintAsset.rotation == 1 || blueprintAsset.rotation == 3) {
                        blueprintAsset.flipZ = !blueprintAsset.flipZ;
                    } else {
                        blueprintAsset.flipX = !blueprintAsset.flipX;
                    }
                }
                case Z -> {
                    if (blueprintAsset.rotation == 1 || blueprintAsset.rotation == 3) {
                        blueprintAsset.flipX = !blueprintAsset.flipX;
                    } else {
                        blueprintAsset.flipZ = !blueprintAsset.flipZ;
                    }
                }
            }
        }
    }

    public void addHistory(HistoryAction historyAction) {
//        if (!this.history.isEmpty() && this.history.size() > 1000) this.history.removeFirst();
//        ArrayList<CobbletObject> copyCobbletObject = new ArrayList<>();
//
//        this.cobbletObjects.forEach(cobbletObject -> {
//            CobbletObject copiedObject = null;
//            if (cobbletObject instanceof House house) {
//                copiedObject = house.copy();
//            }
//            if (cobbletObject instanceof Tower tower) {
//                copiedObject = tower.copy();
//            }
//            if (cobbletObject instanceof BlueprintAsset blueprintAsset) {
//                copiedObject = blueprintAsset.copy();
//            }
//            copyCobbletObject.add(copiedObject);
//        });
//
//        if (!this.history.isEmpty() && this.currentHistoryIndex >= 0 && this.history.get(this.currentHistoryIndex).historyAction() == historyAction
//                && historyAction != HistoryAction.OBJECT_SETTING && historyAction != HistoryAction.ROTATE && historyAction != HistoryAction.ADD
//                && historyAction != HistoryAction.DELETE) {
//            this.history.set(this.currentHistoryIndex, new createUndoOperation(historyAction, copyCobbletObject, this.currentlySelected));
//            LOGGER.info("OVERRIDE: i{} h{} {}", this.currentHistoryIndex, this.history.size(), copyCobbletObject);
//        } else {
//            this.currentHistoryIndex += 1;
//            if (this.history.size() > this.currentHistoryIndex + 1)
//                this.history.subList(this.currentHistoryIndex, this.history.size()).clear();
//            this.history.add(new createUndoOperation(historyAction, copyCobbletObject, this.currentlySelected));
//            LOGGER.info("ADD: i{} h{} {}", this.currentHistoryIndex, this.history.size(), copyCobbletObject);
//        }
    }

    private boolean copy() {
        if (this.currentlySelected >= 0) {
            if (this.cobbletObjects.get(this.currentlySelected) instanceof House house) {
                this.copiedCobbletObject = house.copy();
            }
            if (this.cobbletObjects.get(this.currentlySelected) instanceof Tower tower) {
                this.copiedCobbletObject = tower.copy();
            }
            if (this.cobbletObjects.get(this.currentlySelected) instanceof BlueprintAsset blueprintAsset) {
                this.copiedCobbletObject = blueprintAsset.copy();
            }
        }
        return false;
    }

    private boolean paste() {
        if (!this.cobbletObjects.isEmpty() && this.currentlySelected >= 0) {
            this.cobbletObjects.get(this.currentlySelected).mainMoveGizmo.enableAxes = false;
        }
        BlockPos hitResult = getHitResult();
        if (this.copiedCobbletObject != null) {
            CobbletObject cobbletObject = this.copiedCobbletObject;
            if (this.copiedCobbletObject instanceof House house) {
                cobbletObject = house.copy();
                cobbletObject.position = new int[]{hitResult.getX(), hitResult.getY(), hitResult.getZ()};
            }
            if (this.copiedCobbletObject instanceof Tower tower) {
                cobbletObject = tower.copy();
                cobbletObject.position = new int[]{hitResult.getX(), hitResult.getY(), hitResult.getZ()};
            }
            if (this.copiedCobbletObject instanceof BlueprintAsset blueprintAsset) {
                cobbletObject = blueprintAsset.copy();
                cobbletObject.position = new int[]{hitResult.getX(), hitResult.getY(), hitResult.getZ()};
            }
            cobbletObject.updateGizmosFromPositionSize();
            this.cobbletObjects.add(cobbletObject);
            this.currentlySelected = this.cobbletObjects.size() - 1;
            return true;
        }
        return false;
    }

    private boolean leftClick() {
        int activePoint;
        for (CobbletObject cobbletObject : this.cobbletObjects) {
            if (cobbletObject instanceof House) {
                if ((cobbletObject.mainMoveGizmo.enableAxes && cobbletObject.mainMoveGizmo.leftClick()) ||
                        (cobbletObject.wallPos1Gizmo.enableAxes && cobbletObject.wallPos1Gizmo.leftClick()) ||
                        (cobbletObject.wallPos2Gizmo.enableAxes && cobbletObject.wallPos2Gizmo.leftClick())) {
                    return true;
                }
            }
            if (cobbletObject instanceof BlueprintAsset || cobbletObject instanceof Tower) {
                if ((cobbletObject.mainMoveGizmo.enableAxes && cobbletObject.mainMoveGizmo.leftClick())) {
                    return true;
                }
            }
        }

        Gizmo disableExcept = null;

        for (activePoint = 0; activePoint < this.cobbletObjects.size(); ++activePoint) {
            CobbletObject cobbletObject = this.cobbletObjects.get(activePoint);
            if (cobbletObject instanceof House) {
                if ((!cobbletObject.mainMoveGizmo.enableAxes && cobbletObject.mainMoveGizmo.leftClick())) {
                    this.currentlySelected = activePoint;
                    disableExcept = cobbletObject.mainMoveGizmo;
                    break;
                }
                if ((!cobbletObject.wallPos1Gizmo.enableAxes && cobbletObject.wallPos1Gizmo.leftClick())) {
                    disableExcept = cobbletObject.wallPos1Gizmo;
                    break;
                }
                if ((!cobbletObject.wallPos2Gizmo.enableAxes && cobbletObject.wallPos2Gizmo.leftClick())) {
                    disableExcept = cobbletObject.wallPos2Gizmo;
                    break;
                }
            }
            if (cobbletObject instanceof BlueprintAsset || cobbletObject instanceof Tower) {
                if ((!cobbletObject.mainMoveGizmo.enableAxes && cobbletObject.mainMoveGizmo.leftClick())) {
                    this.currentlySelected = activePoint;
                    disableExcept = cobbletObject.mainMoveGizmo;
                    break;
                }
            }
        }

        if (disableExcept != null) {
            for (CobbletObject cobbletObject : this.cobbletObjects) {
                if (cobbletObject instanceof House) {
                    if (cobbletObject.mainMoveGizmo != disableExcept) {
                        cobbletObject.mainMoveGizmo.enableAxes = false;
                    }
                    if (cobbletObject.wallPos1Gizmo != disableExcept) {
                        cobbletObject.wallPos1Gizmo.enableAxes = false;
                    }
                    if (cobbletObject.wallPos2Gizmo != disableExcept) {
                        cobbletObject.wallPos2Gizmo.enableAxes = false;
                    }
                }
                if (cobbletObject instanceof BlueprintAsset || cobbletObject instanceof Tower) {
                    if (cobbletObject.mainMoveGizmo != disableExcept) {
                        cobbletObject.mainMoveGizmo.enableAxes = false;
                        if (cobbletObject instanceof BlueprintAsset) {
                            cobbletObject.mainMoveGizmo.enableRotation = false;
                        }
                    }
                }
            }
            return true;
        }
        return false;
    }

    private void rightClick() {
        int oldSize = this.cobbletObjects.size();
        switch (this.mode[0]) {
            case 0 -> {
                this.cobbletObjects.add(getHouse(getHitResult()));
            }
            case 1 -> {
                this.cobbletObjects.add(getTower(getHitResult()));
            }
            case 2 -> {
                if (this.blueprintAsset.getBlueprint() != null)
                    this.cobbletObjects.add(getBlueprintAsset(getHitResult()));
            }
        }
        if (oldSize != this.cobbletObjects.size()) {
            if (!this.cobbletObjects.isEmpty() && this.currentlySelected >= 0) {
                this.cobbletObjects.get(this.currentlySelected).mainMoveGizmo.enableAxes = false;
            }
            this.currentlySelected = this.cobbletObjects.size() - 1;
            this.addHistory(HistoryAction.ADD);
        }
    }

    private @NotNull BlueprintAsset getBlueprintAsset(BlockPos from) {
        return new BlueprintAsset(this, new int[]{from.getX(), from.getY(), from.getZ()},
                false, this.blueprintAsset.getBlueprint(), this.blueprintAsset.getOffsets());
    }

    private @NotNull Tower getTower(BlockPos from) {
        Tower recent = this.recentTowerSettings;
        return new Tower(new int[]{from.getX(), from.getY(), from.getZ()},
                new int[]{recent.size[0], recent.size[1], recent.size[2]},
                this, false, recent.getType(), recent.getRoofType(),
                recent.getConeRounding(), recent.getRoofSizeY(), recent.getRoofOffset());
    }

    private @NotNull House getHouse(BlockPos from) {
        House recent = this.recentHouseSettings;
        return new House(new int[]{from.getX(), from.getY(), from.getZ()},
                new int[]{recent.size[0], recent.size[1], recent.size[2]},
                this, false, recent.getType(), recent.getRoofType(),
                recent.getSteppedGableType(), recent.getRoofOrientation());
    }

    public BlockPos getHitResult() {
        RayCaster.RaycastResult raycastResult = Tool.raycastBlock();
        if (raycastResult != null) {
            return raycastResult.getBlockPos();
        }
        return null;
    }

    public void restore(ArrayList<CobbletObject> cobbletObjects, int currentlySelected) {
        LOGGER.info("Restore: {} {}", currentlySelected, cobbletObjects);
        this.cobbletObjects.clear();
        this.cobbletObjects.addAll(cobbletObjects);
        this.currentlySelected = currentlySelected;
        Gizmo disableExcept = null;
        if (!this.cobbletObjects.isEmpty() && this.currentlySelected >= 0) {
            this.cobbletObjects.get(this.currentlySelected).mainMoveGizmo.enableAxes = true;
            disableExcept = this.cobbletObjects.get(this.currentlySelected).mainMoveGizmo;
        }
        if (disableExcept != null) {
            for (CobbletObject cobbletObject : this.cobbletObjects) {
                if (cobbletObject instanceof House) {
                    if (cobbletObject.mainMoveGizmo != disableExcept) {
                        cobbletObject.mainMoveGizmo.enableAxes = false;
                    }
                    if (cobbletObject.wallPos1Gizmo != disableExcept) {
                        cobbletObject.wallPos1Gizmo.enableAxes = false;
                    }
                    if (cobbletObject.wallPos2Gizmo != disableExcept) {
                        cobbletObject.wallPos2Gizmo.enableAxes = false;
                    }
                }
                if (cobbletObject instanceof BlueprintAsset || cobbletObject instanceof Tower) {
                    if (cobbletObject.mainMoveGizmo != disableExcept) {
                        cobbletObject.mainMoveGizmo.enableAxes = false;
                        if (cobbletObject instanceof BlueprintAsset) {
                            cobbletObject.mainMoveGizmo.enableRotation = false;
                        }
                    }
                }
            }
        }
        for (CobbletObject cobbletObject : this.cobbletObjects) {
            if (cobbletObject instanceof House) {
                if (cobbletObject.mainMoveGizmo != disableExcept) {
                    cobbletObject.mainMoveGizmo.enableAxes = false;
                }
                if (cobbletObject.wallPos1Gizmo != disableExcept) {
                    cobbletObject.wallPos1Gizmo.enableAxes = false;
                }
                if (cobbletObject.wallPos2Gizmo != disableExcept) {
                    cobbletObject.wallPos2Gizmo.enableAxes = false;
                }
            }
            if (cobbletObject instanceof BlueprintAsset || cobbletObject instanceof Tower) {
                if (cobbletObject.mainMoveGizmo != disableExcept) {
                    cobbletObject.mainMoveGizmo.enableAxes = false;
                    if (cobbletObject instanceof BlueprintAsset) {
                        cobbletObject.mainMoveGizmo.enableRotation = false;
                    }
                }
            }
        }
    }

    @Override
    public String name() {
        return "DBTools: Cobblet";
    }

    @Override
    public char iconChar() {
        return 0;
    }

    @Override
    public String keybindId() {
        return "9";
    }

    @Override
    public void writeSourceInfo(NbtCompound nbtCompound, boolean b) {

    }

    @Override
    public void writeSettings(NbtCompound nbtCompound) {

    }

    @Override
    public void loadSettings(NbtCompound nbtCompound) {

    }
}