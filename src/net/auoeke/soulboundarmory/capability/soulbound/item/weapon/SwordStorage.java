package net.auoeke.soulboundarmory.capability.soulbound.item.weapon;

import com.google.common.collect.Multimap;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;
import net.auoeke.soulboundarmory.SoulboundArmory;
import net.auoeke.soulboundarmory.capability.Capabilities;
import net.auoeke.soulboundarmory.capability.soulbound.item.StorageType;
import net.auoeke.soulboundarmory.capability.soulbound.player.SoulboundCapability;
import net.auoeke.soulboundarmory.capability.statistics.Category;
import net.auoeke.soulboundarmory.capability.statistics.EnchantmentStorage;
import net.auoeke.soulboundarmory.capability.statistics.SkillStorage;
import net.auoeke.soulboundarmory.capability.statistics.StatisticType;
import net.auoeke.soulboundarmory.capability.statistics.Statistics;
import net.auoeke.soulboundarmory.client.gui.screen.StatisticEntry;
import net.auoeke.soulboundarmory.client.i18n.Translations;
import net.auoeke.soulboundarmory.entity.SAAttributes;
import net.auoeke.soulboundarmory.registry.Skills;
import net.auoeke.soulboundarmory.text.Translation;
import net.auoeke.soulboundarmory.util.AttributeModifierIdentifiers;
import net.auoeke.soulboundarmory.util.AttributeModifierOperations;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

import static net.minecraft.enchantment.Enchantments.UNBREAKING;
import static net.minecraft.enchantment.Enchantments.VANISHING_CURSE;

public class SwordStorage extends WeaponStorage<SwordStorage> {
    protected int lightningCooldown;

    public SwordStorage(SoulboundCapability component, Item item) {
        super(component, item);

        this.statistics = Statistics.create()
            .category(Category.datum, StatisticType.experience, StatisticType.level, StatisticType.skillPoints, StatisticType.attributePoints, StatisticType.enchantmentPoints, StatisticType.spentAttributePoints, StatisticType.spentEnchantmentPoints)
            .category(Category.attribute, StatisticType.attackSpeed, StatisticType.attackDamage, StatisticType.criticalStrikeRate, StatisticType.efficiency, StatisticType.reach)
            .min(1.6, StatisticType.attackSpeed).min(4, StatisticType.attackDamage).min(3, StatisticType.reach)
            .max(1, StatisticType.criticalStrikeRate)
            .build();
        this.enchantments = new EnchantmentStorage(enchantment -> {
            var name = enchantment.getFullname(1).getContents().toLowerCase();

            return enchantment.canEnchant(this.itemStack) && !Arrays.asList(UNBREAKING, VANISHING_CURSE).contains(enchantment)
                && (enchantment == SoulboundArmory.impact || !name.contains("soulbound")) && !name.contains("holding")
                && !name.contains("mending");
        });
        this.skills = new SkillStorage(Skills.nourishment, Skills.summonLightning);
    }

    public static SwordStorage get(Entity entity) {
        return Capabilities.weapon.get(entity).get().storage(StorageType.sword);
    }

    @Override
    public ITextComponent getName() {
        return Translations.soulboundSword;
    }

    @Override
    public StorageType<SwordStorage> type() {
        return StorageType.sword;
    }

    public int getLightningCooldown() {
        return this.lightningCooldown;
    }

    public void resetLightningCooldown() {
        if (!this.player.isCreative()) {
            this.lightningCooldown = (int) Math.round(96 / this.attribute(StatisticType.attackSpeed));
        }
    }

    @Override
    public Multimap<Attribute, AttributeModifier> attributeModifiers(Multimap<Attribute, AttributeModifier> modifiers, EquipmentSlotType slot) {
        if (slot == EquipmentSlotType.MAINHAND) {
            modifiers.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(AttributeModifierIdentifiers.attackDamageModifier, "Weapon modifier", this.attributeRelative(StatisticType.attackDamage), AttributeModifier.Operation.ADDITION));
            modifiers.put(Attributes.ATTACK_SPEED, new AttributeModifier(AttributeModifierIdentifiers.attackSpeedModifier, "Weapon modifier", this.attributeRelative(StatisticType.attackSpeed), AttributeModifier.Operation.ADDITION));
            modifiers.put(SAAttributes.efficiency, new AttributeModifier(SAAttributes.efficiencyUUID, "Weapon modifier", this.attribute(StatisticType.efficiency), AttributeModifier.Operation.MULTIPLY_TOTAL));
            modifiers.put(SAAttributes.criticalStrikeRate, new AttributeModifier(SAAttributes.criticalStrikeRateUUID, "Weapon modifier", this.attribute(StatisticType.criticalStrikeRate), AttributeModifierOperations.percentageAddition));
        }

        return modifiers;
    }

    @Override
    public List<StatisticEntry> screenAttributes() {
        return Arrays.asList(
            new StatisticEntry(this.statistic(StatisticType.attackSpeed), new Translation("%s%s: %s", Translations.attackSpeedFormat, Translations.attackSpeedName, this.formatStatistic(StatisticType.attackSpeed)).withStyle(TextFormatting.GOLD)),
            new StatisticEntry(this.statistic(StatisticType.attackDamage), new Translation("%s%s: %s", Translations.attackDamageFormat, Translations.attackDamageName, this.formatStatistic(StatisticType.attackDamage))),
            new StatisticEntry(this.statistic(StatisticType.criticalStrikeRate), new Translation("%s%s: %s%%", Translations.criticalStrikeRateFormat, Translations.criticalStrikeRateName, this.formatStatistic(StatisticType.criticalStrikeRate))),
            new StatisticEntry(this.statistic(StatisticType.efficiency), new Translation("%s%s: %s", Translations.weaponEfficiencyFormat, Translations.weaponEfficiencyName, this.formatStatistic(StatisticType.efficiency)))
        );
    }

    @Override
    public List<ITextComponent> tooltip() {
        var format = DecimalFormat.getInstance();

        return new ReferenceArrayList<>(List.of(
            new StringTextComponent(String.format(" %s%s %s", Translations.attackSpeedFormat, format.format(this.attribute(StatisticType.attackSpeed)), Translations.attackSpeedName)),
            new StringTextComponent(String.format(" %s%s %s", Translations.attackDamageFormat, format.format(this.attributeTotal(StatisticType.attackDamage)), Translations.attackDamageName)),
            StringTextComponent.EMPTY,
            StringTextComponent.EMPTY,
            new StringTextComponent(String.format(" %s%s%% %s", Translations.criticalStrikeRateFormat, format.format(this.attribute(StatisticType.criticalStrikeRate) * 100), Translations.criticalStrikeRateName)),
            new StringTextComponent(String.format(" %s%s %s", Translations.toolEfficiencyFormat, format.format(this.attribute(StatisticType.efficiency)), Translations.toolEfficiencyName))
        ));
    }

    @Override
    public Item getConsumableItem() {
        return Items.WOODEN_SWORD;
    }

    @Override
    public double increase(StatisticType statistic, int points) {
        if (statistic == StatisticType.attackSpeed) return 0.03;
        if (statistic == StatisticType.attackDamage) return 0.07;
        if (statistic == StatisticType.criticalStrikeRate) return 0.015;
        if (statistic == StatisticType.efficiency) return 0.04;

        return 0;
    }

    @Override
    public void tick() {
        if (!this.isClientSide) {
            if (this.lightningCooldown > 0) {
                this.lightningCooldown--;
            }
        }
    }

    @Override
    public void serializeNBT(CompoundNBT tag) {
        super.serializeNBT(tag);

        tag.putInt("lightningCooldown", this.getLightningCooldown());
    }

    @Override
    public void deserializeNBT(CompoundNBT tag) {
        super.deserializeNBT(tag);

        this.lightningCooldown = tag.getInt("lightningCooldown");
    }
}
