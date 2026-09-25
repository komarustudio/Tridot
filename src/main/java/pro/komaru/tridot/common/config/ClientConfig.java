package pro.komaru.tridot.common.config;

import net.neoforged.neoforge.common.*;
import org.apache.commons.lang3.tuple.*;

public class ClientConfig{
    public static ModConfigSpec.ConfigValue<Boolean>
    BOSSBAR_TITLE, ABILITY_OVERLAY,
    ITEM_PARTICLE, ITEM_GUI_PARTICLE;
    public static ModConfigSpec.ConfigValue<Double>
    SCREENSHAKE_INTENSITY;
    public static ModConfigSpec.ConfigValue<Integer> BOSSBARS_LIMIT, PERCENT_ARMOR_X_OFFSET, PERCENT_ARMOR_Y_OFFSET;

    public ClientConfig(ModConfigSpec.Builder builder){
        builder.comment("Graphics").push("graphics");
            SCREENSHAKE_INTENSITY = builder.comment("Intensity of screenshake.").defineInRange("screenshakeIntensity", 1d, 0d, 10d);
            ABILITY_OVERLAY = builder.comment("When enabled shows the overlay after using a weapon ability (Default: true)").comment("Reload Resourcepacks after turning this on (F3+T)").define("AbilityOverlay", true);
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
    public static final ModConfigSpec SPEC;

    static{
        final Pair<ClientConfig, ModConfigSpec> specPair = new ModConfigSpec.Builder().configure(ClientConfig::new);
        SPEC = specPair.getRight();
        INSTANCE = specPair.getLeft();
    }
}
