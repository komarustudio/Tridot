package pro.komaru.tridot.api.events;

import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

/**
 * Fired before the final Percent Armor damage reduction is applied.
 * Allows other mods to modify the damage multiplier or cancel the reduction entirely.
 */
@Cancelable
public class CalculatePercentArmorEvent extends Event {
    private final LivingEntity entity;
    private final float incomingDamage;
    private float multiplier;

    public CalculatePercentArmorEvent(LivingEntity entity, float incomingDamage, float multiplier) {
        this.entity = entity;
        this.incomingDamage = incomingDamage;
        this.multiplier = multiplier;
    }

    public LivingEntity getEntity() {
        return entity;
    }

    public float getIncomingDamage() {
        return incomingDamage;
    }

    public float getMultiplier() {
        return multiplier;
    }

    /**
     * Sets the new damage multiplier.
     * @param multiplier 1.0 means no reduction, 0.0 means 100% reduction.
     */
    public void setMultiplier(float multiplier) {
        this.multiplier = multiplier;
    }
}