package pro.komaru.tridot.common.registry.item.types;

import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import java.util.List;
import net.minecraft.resources.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.*;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.*;
import pro.komaru.tridot.api.render.text.DotStyleEffects;
import pro.komaru.tridot.api.render.text.DotText;
import pro.komaru.tridot.client.gfx.*;
import pro.komaru.tridot.client.gfx.particle.*;
import pro.komaru.tridot.client.gfx.particle.data.*;
import pro.komaru.tridot.client.render.*;
import pro.komaru.tridot.client.render.gui.overlay.*;
import pro.komaru.tridot.client.render.screenshake.*;
import pro.komaru.tridot.util.*;
import pro.komaru.tridot.util.math.*;

public class TestItem extends Item{
    public TestItem(Properties pProperties){
        super(pProperties);
    }

    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level worldIn, Player playerIn, @NotNull InteractionHand handIn) {
        ItemStack itemstack = playerIn.getItemInHand(handIn);
        var rand = Tmp.rnd;
        net.minecraft.world.phys.Vec3 pos = new net.minecraft.world.phys.Vec3(playerIn.getX() + (rand.nextDouble() - 0.5f) / 6, playerIn.getY() + 0.4F, playerIn.getZ());
        Col particleColor = Col.pink;
        Col particleColorTo = Col.blue;

        ScreenshakeHandler.add(new ScreenshakeInstance(10).intensity(1).fov(true).vec(true).interp(Interp.bounceIn));
        ParticleBuilder.create(TridotParticles.HEART.get())
            .setRenderType(TridotRenderTypes.ADDITIVE_PARTICLE)

            .setScaleData(GenericParticleData.create(1 + Tmp.rnd.randomValueUpTo(0.15f), Tmp.rnd.randomValueUpTo(0.2f)).build())
            .setLifetime(100)
            .setColorData(ColorParticleData.create(particleColor, particleColorTo).build())
            .setVelocity((Tmp.rnd.nextDouble() / 5), 0.05f, (Tmp.rnd.nextDouble() / 5))
            .randomOffset(5)
            .repeat(worldIn, pos.x, pos.y, pos.z, 5);

        if(!worldIn.isClientSide) {
            playerIn.sendSystemMessage(
                DotText.create("[Tridot Benchmark] Rainbow + Wave + Outline FX")
                    .color(Col.pink)
                    .style(b -> b.bold(true).effects(
                            DotStyleEffects.RainbowFX.of(1.5f, true),
                            DotStyleEffects.WaveFX.of(1.5f),
                            DotStyleEffects.OutlineFX.of(Col.black, false)
                    ))
                    .get()
            );

            playerIn.sendSystemMessage(
                DotText.create("[Tridot Benchmark] Glint + Shake + Scale FX")
                    .color(Col.cyan)
                    .style(b -> b.italic(true).effects(
                            DotStyleEffects.GlintFX.of(2f, 1f, Col.white),
                            DotStyleEffects.ShakeFX.of(0.6f),
                            DotStyleEffects.ScaleFX.of(1.05f)
                    ))
                    .get()
            );

            playerIn.sendSystemMessage(
                DotText.create("Long stress-test line: The quick brown fox jumps over the lazy dog 0123456789!")
                    .color(Col.yellow)
                    .style(b -> b.effects(
                            DotStyleEffects.OutlineFX.of(Col.black, false),
                            DotStyleEffects.WaveFX.of(1f)
                    ))
                    .get()
            );
        }


        return InteractionResultHolder.consume(itemstack);
    }

    public int getUseDuration(@NotNull ItemStack stack) {
        return 72000;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level world, @NotNull List<Component> tooltip, @NotNull TooltipFlag flags) {
        super.appendHoverText(stack, world, tooltip, flags);

        tooltip.add(
            DotText.create("--- Tridot Text Profiling ---")
                .color(Col.gray)
                .style(b -> b.bold(true))
                .get()
        );

        tooltip.add(
            DotText.create("Wave + Rainbow FX")
                .color(Col.pink)
                .style(b -> b.effects(
                    DotStyleEffects.WaveFX.of(1.5f),
                    DotStyleEffects.RainbowFX.of(1.5f, true)
                ))
                .get()
        );

        tooltip.add(
            DotText.create("Outline (d4) + Shake FX")
                .color(Col.white)
                .style(b -> b.effects(
                    DotStyleEffects.OutlineFX.of(Col.black, false),
                    DotStyleEffects.ShakeFX.of(0.5f)
                ))
                .get()
        );

        tooltip.add(
            DotText.create("Glint + Scale FX")
                .color(Col.yellow)
                .style(b -> b.effects(
                    DotStyleEffects.GlintFX.of(2f, 1f, Col.cyan),
                    DotStyleEffects.ScaleFX.of(1.05f)
                ))
                .get()
        );

        tooltip.add(
            DotText.create("Spin + PulseAlpha FX")
                .color(Col.orange)
                .style(b -> b.effects(
                    DotStyleEffects.SpinFX.of(1f),
                    DotStyleEffects.PulseAlphaFX.of(2f)
                ))
                .get()
        );

        tooltip.add(
            DotText.create("PulseColor + AdvanceWave FX")
                .style(b -> b.effects(
                    DotStyleEffects.PulseColorFX.of(1f),
                    DotStyleEffects.AdvanceWaveFX.of(1f, 1.5f)
                ))
                .get()
        );

        tooltip.add(
            DotText.create("Stress-test: 0123456789 ABCDEFGHIJKLMNOPQRSTUVWXYZ")
                .color(Col.green)
                .style(b -> b.effects(
                    DotStyleEffects.OutlineFX.of(Col.black, false),
                    DotStyleEffects.WaveFX.of(1f)
                ))
                .get()
        );
    }
}
