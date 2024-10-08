package io.wispforest.affinity.client.render;

import io.wispforest.affinity.Affinity;
import io.wispforest.owo.shader.GlProgram;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.render.VertexFormats;

public class SkyBlitProgram extends GlProgram {

    public SkyBlitProgram() {
        super(Affinity.id("blit_sky"), VertexFormats.BLIT_SCREEN);
    }

    public ShaderProgram setupAndGet() {
        this.backingProgram.addSampler("MainDepthSampler", MinecraftClient.getInstance().getFramebuffer().getDepthAttachment());
        this.backingProgram.addSampler("SkyDepthSampler", SkyCaptureBuffer.skyStencil.getDepthAttachment());
        return this.backingProgram;
    }

}
