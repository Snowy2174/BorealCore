package plugin.borealcore.functions.herbalism.objects;

import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;

public enum InvertedEffect {
    SPEED(PotionEffectType.SPEED, PotionEffectType.SLOW),
    SLOW(PotionEffectType.SLOW, PotionEffectType.SPEED),
    FAST_DIGGING(PotionEffectType.FAST_DIGGING, PotionEffectType.SLOW_DIGGING),
    SLOW_DIGGING(PotionEffectType.SLOW_DIGGING, PotionEffectType.FAST_DIGGING),
    INCREASE_DAMAGE(PotionEffectType.INCREASE_DAMAGE, PotionEffectType.WEAKNESS),
    WEAKNESS(PotionEffectType.WEAKNESS, PotionEffectType.INCREASE_DAMAGE),
    HEAL(PotionEffectType.HEAL, PotionEffectType.HARM),
    HARM(PotionEffectType.HARM, PotionEffectType.HEAL),
    REGENERATION(PotionEffectType.REGENERATION, PotionEffectType.POISON),
    POISON(PotionEffectType.POISON, PotionEffectType.REGENERATION),
    FIRE_RESISTANCE(PotionEffectType.FIRE_RESISTANCE, PotionEffectType.CONFUSION),
    WATER_BREATHING(PotionEffectType.WATER_BREATHING, PotionEffectType.FIRE_RESISTANCE),
    INVISIBILITY(PotionEffectType.INVISIBILITY, PotionEffectType.GLOWING),
    NIGHT_VISION(PotionEffectType.NIGHT_VISION, PotionEffectType.BLINDNESS),
    JUMP(PotionEffectType.JUMP, PotionEffectType.SLOW_FALLING),
    LEVITATION(PotionEffectType.LEVITATION, PotionEffectType.SLOW_FALLING),
    DAMAGE_RESISTANCE(PotionEffectType.DAMAGE_RESISTANCE, PotionEffectType.INCREASE_DAMAGE),
    HEALTH_BOOST(PotionEffectType.HEALTH_BOOST, PotionEffectType.WEAKNESS),
    ABSORPTION(PotionEffectType.ABSORPTION, PotionEffectType.HUNGER),
    SATURATION(PotionEffectType.SATURATION, PotionEffectType.HUNGER),
    LUCK(PotionEffectType.LUCK, PotionEffectType.UNLUCK),
    SLOW_FALLING(PotionEffectType.SLOW_FALLING, PotionEffectType.JUMP),
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
