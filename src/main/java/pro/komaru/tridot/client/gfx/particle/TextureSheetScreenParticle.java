package pro.komaru.tridot.client.gfx.particle;

import net.minecraft.client.multiplayer.*;
import net.minecraft.client.particle.*;
import net.minecraft.client.renderer.texture.*;
import net.neoforged.api.distmarker.*;

@OnlyIn(Dist.CLIENT)
public abstract class TextureSheetScreenParticle extends QuadScreenParticle {
    public TextureAtlasSprite sprite;

    protected TextureSheetScreenParticle(ClientLevel pLevel, double pX, double pY) {
        super(pLevel, pX, pY);
    }

    protected TextureSheetScreenParticle(ClientLevel pLevel, double pX, double pY, double pXSpeed, double pYSpeed) {
        super(pLevel, pX, pY, pXSpeed, pYSpeed);
    }

    protected void setSprite(TextureAtlasSprite pSprite) {
        this.sprite = pSprite;
    }

    protected float getU0() {
        return this.sprite.getU0();
    }

    protected float getU1() {
        return this.sprite.getU1();
    }

    protected float getV0() {
        return this.sprite.getV0();
    }

    protected float getV1() {
        return this.sprite.getV1();
    }

    public void pickSprite(SpriteSet pSprite) {
        this.setSprite(pSprite.get(random));
    }
}