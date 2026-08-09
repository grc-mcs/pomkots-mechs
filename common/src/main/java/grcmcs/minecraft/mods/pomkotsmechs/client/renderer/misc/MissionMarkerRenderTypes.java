package grcmcs.minecraft.mods.pomkotsmechs.client.renderer.misc;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderType;

public final class MissionMarkerRenderTypes extends RenderType {
    private MissionMarkerRenderTypes(String name, VertexFormat format, VertexFormat.Mode mode,
                                     int bufferSize, boolean affectsCrumbling, boolean sortOnUpload,
                                     Runnable setupState, Runnable clearState) {
        super(name, format, mode, bufferSize, affectsCrumbling, sortOnUpload, setupState, clearState);
    }

    public static final RenderType GLOWING_PANELS = RenderType.create(
            "pomkotsmechs_mission_marker_panels",
            DefaultVertexFormat.POSITION_COLOR,
            VertexFormat.Mode.QUADS,
            2048,
            false,
            true,
            CompositeState.builder()
                    .setShaderState(POSITION_COLOR_SHADER)
                    .setTransparencyState(ADDITIVE_TRANSPARENCY)
                    .setCullState(NO_CULL)
                    .setDepthTestState(LEQUAL_DEPTH_TEST)
                    .setWriteMaskState(COLOR_WRITE)
                    .createCompositeState(false)
    );
}
