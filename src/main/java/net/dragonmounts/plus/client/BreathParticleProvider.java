package net.dragonmounts.plus.client;

import net.dragonmounts.plus.common.entity.breath.BreathParticleOption;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum BreathParticleProvider implements ParticleProvider<BreathParticleOption> {
    INSTANCE;
    private TextureAtlas atlas;

    @SuppressWarnings("deprecation")
    public TextureAtlas getAtlas() {
        var atlas = this.atlas;
        if (atlas == null) {
            var texture = Minecraft.getInstance().getTextureManager().getTexture(TextureAtlas.LOCATION_PARTICLES);
            if (!(texture instanceof TextureAtlas)) throw new IllegalStateException();
            this.atlas = atlas = (TextureAtlas) texture;
        }
        return atlas;
    }

    @Override
    public @Nullable Particle createParticle(BreathParticleOption option, @NotNull ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
        return option.variant().appearance.createParticle(option, this.getAtlas(), level, x, y, z, xSpeed, ySpeed, zSpeed);
    }
}
