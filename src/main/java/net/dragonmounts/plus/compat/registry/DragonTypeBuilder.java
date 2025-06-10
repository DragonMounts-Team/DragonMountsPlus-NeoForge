package net.dragonmounts.plus.compat.registry;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.dragonmounts.plus.common.util.ArmorMaterialBuilder;
import net.dragonmounts.plus.common.util.ItemTierBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiFunction;

import static net.dragonmounts.plus.common.DragonMountsShared.makeId;

public final class DragonTypeBuilder {
    private static final ObjectArrayList<DragonType> INSTANCES = new ObjectArrayList<>();
    public static final ResourceLocation BONUS_ID = makeId("dragon_type_bonus");
    public final int color;
    public final ImmutableMultimap.Builder<Holder<Attribute>, AttributeModifier> attributes = ImmutableMultimap.builder();
    public final ImmutableSet.Builder<ResourceKey<DamageType>> immunities = ImmutableSet.builder();
    public final ImmutableSet.Builder<Block> blocks = ImmutableSet.builder();
    public final ImmutableSet.Builder<ResourceKey<Biome>> biomes = ImmutableSet.builder();
    public final @Nullable ItemTierBuilder tier;
    public final @Nullable ArmorMaterialBuilder material;
    public boolean convertible = true;
    public @NotNull ParticleOptions sneezeParticle = ParticleTypes.LARGE_SMOKE;
    public @NotNull ParticleOptions eggParticle = ParticleTypes.MYCELIUM;
    public @NotNull MapColor scaleColor = MapColor.NONE;
    public TagKey<Item> scales;

    public DragonTypeBuilder(int color, @Nullable ArmorMaterialBuilder material, @Nullable ItemTierBuilder tier) {
        this.color = color;
        this.tier = tier;
        this.material = material;
        // ignore suffocation damage
        this.addImmunity(DamageTypes.ON_FIRE).addImmunity(DamageTypes.IN_FIRE)
                .addImmunity(DamageTypes.HOT_FLOOR)
                .addImmunity(DamageTypes.LAVA)
                .addImmunity(DamageTypes.DROWN)
                .addImmunity(DamageTypes.IN_WALL)
                .addImmunity(DamageTypes.CACTUS) // assume that cactus needles don't do much damage to animals with horned scales
                .addImmunity(DamageTypes.DRAGON_BREATH); // ignore damage from vanilla ender dragon. I kinda disabled this because it wouldn't make any sense, feel free to re enable
    }

    public DragonTypeBuilder notConvertible() {
        this.convertible = false;
        return this;
    }

    public DragonTypeBuilder putAttributeModifier(Holder<Attribute> attribute, ResourceLocation identifier, double value, AttributeModifier.Operation operation) {
        this.attributes.put(attribute, new AttributeModifier(identifier, value, operation));
        return this;
    }

    public DragonTypeBuilder addImmunity(ResourceKey<DamageType> type) {
        this.immunities.add(type);
        return this;
    }

    public DragonTypeBuilder addHabitat(Block block) {
        this.blocks.add(block);
        return this;
    }

    public DragonTypeBuilder addHabitat(ResourceKey<Biome> biome) {
        this.biomes.add(biome);
        return this;
    }

    public DragonTypeBuilder setSneezeParticle(SimpleParticleType particle) {
        this.sneezeParticle = particle;
        return this;
    }

    public DragonTypeBuilder setEggParticle(SimpleParticleType particle) {
        this.eggParticle = particle;
        return this;
    }

    public DragonTypeBuilder setMaterial(TagKey<Item> material) {
        this.scales = material;
        return this;
    }

    public DragonTypeBuilder setScaleColor(MapColor color) {
        this.scaleColor = color;
        return this;
    }

    public <T extends DragonType> T register(
            BiFunction<ResourceLocation, DragonTypeBuilder, T> factory,
            ResourceLocation identifier
    ) {
        var type = factory.apply(identifier, this);
        INSTANCES.add(type);
        return type;
    }

    public static void register(RegisterEvent.RegisterHelper<DragonType> registry) {
        for (var type : INSTANCES) {
            registry.register(type.identifier, type);
        }
    }
}
