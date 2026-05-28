package plugin.borealcore.functions.herbalism.objects;

import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;

public enum InvertedEffect {
    SPEED(PotionEffectType.SPEED, PotionEffectType.SLOWNESS),
    SLOW(PotionEffectType.SLOWNESS, PotionEffectType.SPEED),
    FAST_DIGGING(PotionEffectType.HASTE, PotionEffectType.MINING_FATIGUE),
    SLOW_DIGGING(PotionEffectType.MINING_FATIGUE, PotionEffectType.HASTE),
    INCREASE_DAMAGE(PotionEffectType.STRENGTH, PotionEffectType.WEAKNESS),
    WEAKNESS(PotionEffectType.WEAKNESS, PotionEffectType.STRENGTH),
    HEAL(PotionEffectType.INSTANT_HEALTH, PotionEffectType.INSTANT_DAMAGE),
    HARM(PotionEffectType.INSTANT_DAMAGE, PotionEffectType.INSTANT_HEALTH),
    REGENERATION(PotionEffectType.REGENERATION, PotionEffectType.POISON),
    POISON(PotionEffectType.POISON, PotionEffectType.REGENERATION),
    FIRE_RESISTANCE(PotionEffectType.FIRE_RESISTANCE, PotionEffectType.NAUSEA),
    WATER_BREATHING(PotionEffectType.WATER_BREATHING, PotionEffectType.FIRE_RESISTANCE),
    INVISIBILITY(PotionEffectType.INVISIBILITY, PotionEffectType.GLOWING),
    NIGHT_VISION(PotionEffectType.NIGHT_VISION, PotionEffectType.BLINDNESS),
    JUMP(PotionEffectType.JUMP_BOOST, PotionEffectType.SLOW_FALLING),
    LEVITATION(PotionEffectType.LEVITATION, PotionEffectType.SLOW_FALLING),
    DAMAGE_RESISTANCE(PotionEffectType.RESISTANCE, PotionEffectType.STRENGTH),
    HEALTH_BOOST(PotionEffectType.HEALTH_BOOST, PotionEffectType.WEAKNESS),
    ABSORPTION(PotionEffectType.ABSORPTION, PotionEffectType.HUNGER),
    SATURATION(PotionEffectType.SATURATION, PotionEffectType.HUNGER),
    LUCK(PotionEffectType.LUCK, PotionEffectType.UNLUCK),
    SLOW_FALLING(PotionEffectType.SLOW_FALLING, PotionEffectType.JUMP_BOOST),
    WITHER(PotionEffectType.WITHER, PotionEffectType.REGENERATION),
    GLIMMERING(PotionEffectType.GLOWING, PotionEffectType.INVISIBILITY);

    private final PotionEffectType original;
    private final PotionEffectType inverted;

    private static final Map<PotionEffectType, PotionEffectType> MAP = new HashMap<>();

    static {
        for (InvertedEffect ie : values()) {
            MAP.put(ie.original, ie.inverted);
        }
    }

    InvertedEffect(PotionEffectType original, PotionEffectType inverted) {
        this.original = original;
        this.inverted = inverted;
    }

    public static PotionEffectType getInverted(PotionEffectType type) {
        return MAP.getOrDefault(type, type);
    }
}
