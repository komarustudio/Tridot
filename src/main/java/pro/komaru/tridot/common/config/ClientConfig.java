package pro.komaru.tridot.common.config;

import net.minecraftforge.common.*;
import org.apache.commons.lang3.tuple.*;

public class ClientConfig{
    public static ForgeConfigSpec.ConfigValue<Boolean>
    BOSSBAR_TITLE, ABILITY_OVERLAY, BLOOM,
    ITEM_PARTICLE, ITEM_GUI_PARTICLE;
    public static ForgeConfigSpec.ConfigValue<Double>
    SCREENSHAKE_INTENSITY;
    public static ForgeConfigSpec.ConfigValue<Integer> BOSSBARS_LIMIT, PERCENT_ARMOR_X_OFFSET, PERCENT_ARMOR_Y_OFFSET;

    public ClientConfig(ForgeConfigSpec.Builder builder){
        builder.comment("Graphics").push("graphics");
            SCREENSHAKE_INTENSITY = builder.comment("Intensity of screenshake.").defineInRange("screenshakeIntensity", 1d, 0d, 10d);
            ABILITY_OVERLAY = builder.comment("When enabled shows the overlay after using a weapon ability (Default: true)").comment("Reload Resourcepacks after turning this on (F3+T)").define("AbilityOverlay", true);
            BLOOM = builder.comment("Whether the bloom will be rendered, can improve performance if it causes fps drops (Default: true)").comment("Reload Resourcepacks after turning this on (F3+T)").define("Bloom", true);
            BOSSBAR_TITLE = builder.comment("Bossbar boss titles").define("BossbarTitles", true);
            BOSSBARS_LIMIT = builder.comment("Limit of rendered bossbars (Default: 3)").define("BossbarsLimit", 3);
            PERCENT_ARMOR_X_OFFSET = builder.comment("X Offset of Percent Armor Icon").define("PercentArmorX", 0);
            PERCENT_ARMOR_Y_OFFSET = builder.comment("Y Offset of Percent Armor Icon").define("PercentArmorY", -10);

        builder.comment("Particles").push("particles");
                ITEM_PARTICLE = builder.comment("Particles of dropped item.").define("itemParticle", true);
                ITEM_GUI_PARTICLE = builder.comment("Particles of item located in GUI (ex. item in slot)").define("itemGuiParticle", true);
            builder.pop();
        builder.pop();
    }

    public static final ClientConfig INSTANCE;
    public static final ForgeConfigSpec SPEC;

    static{
        final Pair<ClientConfig, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(ClientConfig::new);
        SPEC = specPair.getRight();
        INSTANCE = specPair.getLeft();
    }
}
