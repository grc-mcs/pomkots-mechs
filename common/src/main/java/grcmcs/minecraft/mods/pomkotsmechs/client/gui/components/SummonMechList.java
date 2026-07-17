package grcmcs.minecraft.mods.pomkotsmechs.client.gui.components;

import com.mojang.datafixers.util.Pair;
import grcmcs.minecraft.mods.pomkotsmechs.client.gui.MechSalvagerScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

import java.util.List;
import java.util.UUID;

public class SummonMechList
        extends AbstractPomkotsSelectionList<SummonMechList.Entry> {

    public static final int LIST_X = 48 + 10;
    public static final int LIST_Y = 40 + 20;

    public static final int LIST_WIDTH = 145;
    public static final int LIST_HEIGHT = 150;

    public static final int ROW_HEIGHT = 22;

    private final MechSalvagerScreen parent;

    public SummonMechList(
            MechSalvagerScreen parent,
            Minecraft minecraft
    ) {
        super(
                parent,
                minecraft,
                LIST_X,
                LIST_Y,
                LIST_WIDTH,
                LIST_HEIGHT,
                ROW_HEIGHT
        );

        this.parent = parent;
    }

    public void addTargets(
            List<Pair<UUID, String>> targets
    ) {
        clearEntries();

        for (int i = 0; i < targets.size(); i++) {
            var target = targets.get(i);
            addEntry(
                    new Entry(
                            parent,
                            target.getFirst(),
                            target.getSecond(),
                            i
                    )
            );
        }
    }

    public int getSize() {
        return this.getItemCount();
    }

    public static class Entry extends AbstractPomkotsSelectionList.PomkotsEntry<Entry> {
        private static final int TEXT_X = 8;
        private static final int TEXT_Y = 7;

        private final UUID mechId;
        private final String mechName;
        private final int index;

        private final MechSalvagerScreen parent;

        public Entry(
                MechSalvagerScreen parent,
                UUID id,
                String name,
                int index
        ) {
            this.parent = parent;
            this.mechId = id;
            this.mechName = name;
            this.index = index;
        }

        @Override
        public void render(
                GuiGraphics graphics,
                int index,
                int top,
                int left,
                int width,
                int height,
                int mouseX,
                int mouseY,
                boolean hovered,
                float partialTick
        ) {
            super.render(graphics, index, top, left, width, height, mouseX, mouseY, hovered, partialTick);

            graphics.drawString(
                    Minecraft.getInstance().font,
                    "Name : " + mechName + " / ID : " + mechId,
                    left + TEXT_X,
                    top + TEXT_Y,
                    0xFFFFFF,
                    false
            );
        }

        @Override
        public boolean mouseClicked(
                double mouseX,
                double mouseY,
                int button
        ) {
            System.out.println(this.mechName);
            parent.setSelected(this.mechId);

            return true;
        }
    }
}