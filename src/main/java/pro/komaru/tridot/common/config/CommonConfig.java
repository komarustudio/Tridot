package pro.komaru.tridot.common.config;

import net.neoforged.neoforge.common.*;
import org.apache.commons.lang3.tuple.*;

public class CommonConfig{
    public static ModConfigSpec.ConfigValue<Boolean>
    PERCENT_ARMOR;

    static {
        final Pair<CommonConfig, ModConfigSpec> specPair = new ModConfigSpec.Builder().configure(CommonConfig::new);
        SPEC = specPair.getRight();
        INSTANCE = specPair.getLeft();
    }

    public static final CommonConfig INSTANCE;
    public static final ModConfigSpec SPEC;

    public CommonConfig(ModConfigSpec.Builder builder) {
        PERCENT_ARMOR = builder.comment("When enabled armor is defined as percent (Default: true)").comment("keep in mind that Minecraft attributes are limited, so you'll need to download some mod that removes the limits, otherwise high tier armor will be a nonsense thanks to Mojang... that's the reason why percentage armor is implemented").define("PercentArmor", true);
    }
}