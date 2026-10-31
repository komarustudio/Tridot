package pro.komaru.tridot.mixin.client;

import com.mojang.datafixers.util.Either;
import net.minecraft.client.renderer.block.model.BlockElement;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pro.komaru.tridot.client.gfx.postprocess.bloom.BloomItemModels;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Mixin(BlockModel.class)
public abstract class BlockModelMixin {

    @Unique
    private static final String TRIDOT_BLOOM_KEY = "tridot_bloom";

    @Shadow @Final public Boolean hasAmbientOcclusion;
    @Shadow public String name;
    @Shadow public BlockModel parent;
    @Shadow public abstract BlockModel getRootModel();
    @Shadow public abstract boolean hasTexture(String name);
    @Shadow public abstract Material getMaterial(String name);
    @Shadow public abstract BlockModel.GuiLight getGuiLight();
    @Shadow public abstract ItemTransforms getTransforms();
    @Shadow public abstract List<BlockElement> getElements();

    @Inject(method = "bake(Lnet/minecraft/client/resources/model/ModelBaker;Lnet/minecraft/client/renderer/block/model/BlockModel;Ljava/util/function/Function;Lnet/minecraft/client/resources/model/ModelState;Z)Lnet/minecraft/client/resources/model/BakedModel;", at = @At("RETURN"))
    private void tridot$bakeInlineBloom(ModelBaker baker, BlockModel owner, Function<Material, TextureAtlasSprite> spriteGetter, ModelState state, boolean guiLight3d, CallbackInfoReturnable<BakedModel> cir) {
        BlockModel self = (BlockModel) (Object) this;
        if (self != owner && owner.getRootModel() == ModelBakery.GENERATION_MARKER && owner.hasTexture(TRIDOT_BLOOM_KEY)) {
            Material bloomMaterial = owner.getMaterial(TRIDOT_BLOOM_KEY);
            Map<String, Either<Material, String>> bloomTextures = Map.of("layer0", Either.left(bloomMaterial));
            BlockModel bloomSource = new BlockModel(null, List.of(), bloomTextures, owner.hasAmbientOcclusion, owner.getGuiLight(), owner.getTransforms(), List.of());
            bloomSource.name = owner.name + "$tridot_bloom";
            bloomSource.parent = ModelBakery.GENERATION_MARKER;
            BakedModel bloomBaked = bloomSource.bake(baker, bloomSource, spriteGetter, state, false);
            BloomItemModels.putModelBloom(cir.getReturnValue(), bloomBaked);
            return;
        }

        if (self == owner && !self.customData.hasCustomGeometry() && this.getRootModel() != ModelBakery.GENERATION_MARKER && !this.getElements().isEmpty() && this.hasTexture(TRIDOT_BLOOM_KEY)) {
            Material bloomMaterial = this.getMaterial(TRIDOT_BLOOM_KEY);
            BlockModel bloomSource = tridot$getBloomSource(bloomMaterial, self);
            bloomSource.name = this.name + "$tridot_bloom";
            BakedModel bloomBaked = bloomSource.bake(baker, bloomSource, spriteGetter, state, guiLight3d);
            BloomItemModels.putModelBloom(cir.getReturnValue(), bloomBaked);
        }
    }

    @Unique
    private @NotNull BlockModel tridot$getBloomSource(Material bloomMaterial, BlockModel self) {
        Either<Material, String> bloomEither = Either.left(bloomMaterial);
        Map<String, Either<Material, String>> bloomTextures = new HashMap<>();
        for (BlockModel current = self; current != null; current = current.parent) {
            for (String key : current.textureMap.keySet()) {
                if (!TRIDOT_BLOOM_KEY.equals(key)) {
                    bloomTextures.putIfAbsent(key, bloomEither);
                }
            }
        }

        return new BlockModel(null, this.getElements(), bloomTextures, this.hasAmbientOcclusion, this.getGuiLight(), this.getTransforms(), List.of());
    }
}
