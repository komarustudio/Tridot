package pro.komaru.tridot.common.registry.item.types;

import net.minecraft.network.chat.Component;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.*;
import org.jetbrains.annotations.*;
import pro.komaru.tridot.api.render.text.DotStyleEffects;
import pro.komaru.tridot.api.render.text.DotText;
import pro.komaru.tridot.client.gfx.*;
import pro.komaru.tridot.client.gfx.particle.*;
import pro.komaru.tridot.client.gfx.particle.data.*;
import pro.komaru.tridot.client.render.*;
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
            var a = Component.Serializer.toJson(DotText.create("Test Item")
                .color(Col.pink)
                    .style(b -> b.bold(true).italic(true).effects(
                            DotStyleEffects.ShakeFX.of(1f),
                            DotStyleEffects.OutlineFX.of(Col.black,true)
                    ))
                .get(), worldIn.registryAccess());
            var b = Component.Serializer.fromJson(a, worldIn.registryAccess());
            playerIn.sendSystemMessage(b);
        }


        return InteractionResultHolder.consume(itemstack);
    }

    @Override
    public int getUseDuration(@NotNull ItemStack stack, @NotNull LivingEntity entity) {
        return 72000;
    }
}
