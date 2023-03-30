package dev.ftb.mods.ftbteamdimensions.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.ftb.mods.ftbteamdimensions.net.OpenVisitGui;
import dev.ftb.mods.ftbteamdimensions.net.VisitDimension;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class VisitScreen extends Screen {
    private final Map<ResourceLocation, OpenVisitGui.DimData> dim2name;
    private VisitList visitList;
    private EditBox searchBox;
    private Button createButton;
    private boolean showArchived = true;

    public VisitScreen(Map<ResourceLocation, OpenVisitGui.DimData> dim2name) {
        super(Component.empty());
        this.dim2name = dim2name;
    }

    @Override
    protected void init() {
        super.init();

        visitList = new VisitList(getMinecraft(), width, height, 80, height - 40);

        searchBox = new EditBox(font, width / 2 - 160 / 2, 40, 160, 20, Component.empty());
        searchBox.setResponder(visitList::onFilterChanged);

        Component label = Component.translatable("screens.ftbteamdimensions.show_archived");
        int lw = font.width(label) + 35;
        Checkbox checkbox = new Checkbox(width - lw, 40, lw, 20, label, true) {
            @Override
            public void onPress() {
                super.onPress();
                showArchived = selected();
                visitList.onFilterChanged(searchBox.getValue());
            }
        };

        addRenderableWidget(new Button(width / 2 - 130, height - 30, 100, 20, Component.translatable("screens.ftbteamdimensions.back"), btn -> onClose()));

        addRenderableWidget(createButton = new Button(width / 2 - 20, height - 30, 150, 20, Component.translatable("screens.ftbteamdimensions.visit"), btn -> onActivate()));

        createButton.active = false;

        addRenderableWidget(checkbox);
        addWidget(searchBox);
        addWidget(visitList);
    }

    @Override
    public void render(PoseStack matrices, int mouseX, int mouseY, float partialTick) {
        visitList.render(matrices, mouseX, mouseY, partialTick);
        searchBox.render(matrices, mouseX, mouseY, partialTick);

        super.render(matrices, mouseX, mouseY, partialTick);

        String value = Component.translatable("screens.ftbteamdimensions.select_dimension").getString();
        font.drawShadow(matrices, value, width / 2f - font.width(value) / 2f, 20, 0xFFFFFF);
    }

    private void onActivate() {
        if (visitList.getSelected() == null) {
            return;
        }

        new VisitDimension(visitList.getSelected().dim).sendToServer();

        if (getMinecraft().level != null) {
            onClose();
        }
    }

    private class VisitList extends AbstractSelectionList<VisitList.Entry> {
        public VisitList(Minecraft minecraft, int width, int height, int top, int bottom) {
            super(minecraft, width, height, top, bottom, 50);

            addChildren("");
        }

        private void addChildren(String filter) {
            List<Entry> entries = new ArrayList<>();

            dim2name.forEach((dim, data) -> {
                if (showArchived || !data.archived()) {
                    String name = data.teamName();
                    if (filter.isEmpty() || dim.toString().toLowerCase().contains(filter) || name.toLowerCase().contains(filter)) {
                        entries.add(new Entry(dim, data));
                    }
                }
            });

            children().addAll(entries.stream().sorted(Comparator.comparing(o -> o.data.teamName())).toList());
        }

        @Override
        public int getRowWidth() {
            return 400;
        }

        @Override
        protected int getScrollbarPosition() {
            return width / 2 + 200;
        }

        @Override
        public void setSelected(@Nullable VisitScreen.VisitList.Entry entry) {
            VisitScreen.this.createButton.active = entry != null;
            super.setSelected(entry);
        }

        @Override
        public void updateNarration(NarrationElementOutput narrationElementOutput) {
        }

        private void onFilterChanged(String filter) {
            children().clear();
            addChildren(filter);
        }

        private class Entry extends AbstractSelectionList.Entry<Entry> {
            private final ResourceLocation dim;
            private final OpenVisitGui.DimData data;
            private long lastClickTime;

            public Entry(ResourceLocation dim, OpenVisitGui.DimData data) {
                this.dim = dim;
                this.data = data;
            }

            @Override
            public boolean mouseClicked(double x, double y, int partialTick) {
                VisitScreen.VisitList.this.setSelected(this);

                if (Util.getMillis() - this.lastClickTime < 250L) {
                    VisitScreen.this.onActivate();
                    return true;
                } else {
                    this.lastClickTime = Util.getMillis();
                    return false;
                }
            }

            @Override
            public void render(PoseStack poseStack, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean isMouseOver, float partialTick) {
                Font font = Minecraft.getInstance().font;
                int lh = font.lineHeight + 1;

                int tpsCol;
                if (data.tickTime() < 50.0) {
                    tpsCol = 0x80FF80;
                } else if (data.tickTime() < 100.0) {
                    tpsCol = 0xFFFF80;
                } else {
                    tpsCol = 0xFF8080;
                }
                int startX = left + 5;
                font.drawShadow(poseStack, Component.literal(dim.toString()), startX, top + 5, data.archived() ? 0x808080 : 0xFFFFFF);
                font.drawShadow(poseStack, Component.literal(data.teamName()), startX, top + 5 + lh, data.archived() ? 0x606060 : 0xD3D3D3);
                double tps = Math.min(1000.0 / data.tickTime(), 20.0);
                font.drawShadow(poseStack, Component.literal(String.format("%.3f ms/tick (%.3f TPS)", data.tickTime(), tps)), startX + 5, top + 5 + lh * 2, tpsCol);
                if (data.archived()) {
                    font.drawShadow(poseStack, Component.literal("Archived"), startX + 5, top + 5 + lh * 3, 0xD0A000);
                } else {
                    font.drawShadow(poseStack, Component.literal("Active"), startX + 5, top + 5 + lh * 3, 0xD0D000);
                }
                if (isMouseOver) {
                    List<Component> tooltip = List.of(
                            Component.translatable("screens.ftbteamdimensions.block_entities", data.blockEntities()),
                            Component.translatable("screens.ftbteamdimensions.entities", data.entities()),
                            Component.translatable("screens.ftbteamdimensions.loaded_chunks", data.loadedChunks())
                    );
                    renderComponentTooltip(poseStack, tooltip, mouseX, mouseY, ItemStack.EMPTY);
                }
            }
        }
    }
}
