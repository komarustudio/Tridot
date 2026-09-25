package pro.komaru.tridot.api.render.bossbars;

import net.minecraft.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.*;
import net.minecraft.network.chat.*;
import net.minecraft.util.*;
import net.neoforged.api.distmarker.*;
import pro.komaru.tridot.client.*;

import java.util.*;

@OnlyIn(Dist.CLIENT)
public abstract class ClientBossbar extends TridotBossBar{
    protected float targetPercent;
    protected long setTime;
    public int increment = 0;

    public ClientBossbar(UUID id, Component name) {
        super(id, name);
        this.setTime = Util.getMillis();
        this.targetPercent = 0.0F;
    }

    public void increase(int increment){
        this.increment = increment;
    }

    public abstract void render(BossBarsOverlay overlay, GuiGraphics pGuiGraphics, int baseOffset, Minecraft mc);

    public ClientBossbar setPercentage(float percentage){
        this.percentage = this.getPercentage();
        this.targetPercent = percentage;
        this.setTime = Util.getMillis();
        return this;
    }

    public float getPercentage() {
        float time = Util.getMillis() - this.setTime;
        float delta = Mth.clamp(time / 100.0F, 0.0F, 1.0F);
        return Mth.lerp(delta, this.percentage, this.targetPercent);
    }
}
