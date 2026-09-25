package pro.komaru.tridot.client.gfx.particle.screen;

import net.minecraft.client.multiplayer.*;
import net.minecraft.client.particle.*;
import net.minecraft.resources.*;
import org.jetbrains.annotations.*;
import pro.komaru.tridot.client.gfx.*;
import pro.komaru.tridot.client.gfx.particle.*;
import pro.komaru.tridot.client.gfx.particle.options.*;
import pro.komaru.tridot.client.gfx.particle.type.*;

public class TridotScreenParticleType extends ScreenParticleType<ScreenParticleOptions>{
    public static class Factory implements ParticleProvider<ScreenParticleOptions> {
        public @Nullable SpriteSet sprite;
        private final @Nullable ResourceLocation spriteSetId;

        public Factory(SpriteSet sprite) {
            this.sprite = sprite;
            this.spriteSetId = null;
        }

        public Factory(ResourceLocation spriteSetId) {
            this.sprite = null;
            this.spriteSetId = spriteSetId;
        }

        @Override
        public ScreenParticle createParticle(ClientLevel pLevel, ScreenParticleOptions options, double x, double y, double pXSpeed, double pYSpeed){
            if(sprite == null && spriteSetId != null){
                sprite = TridotScreenParticles.getSpriteSet(spriteSetId);
            }
            return new GenericScreenParticle(pLevel, options, (ParticleEngine.MutableSpriteSet) sprite, x, y, pXSpeed, pYSpeed);
        }
    }
}