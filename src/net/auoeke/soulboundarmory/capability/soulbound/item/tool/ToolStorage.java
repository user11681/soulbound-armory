package net.auoeke.soulboundarmory.capability.soulbound.item.tool;

import com.google.common.collect.Multimap;
import net.auoeke.soulboundarmory.capability.soulbound.item.ItemStorage;
import net.auoeke.soulboundarmory.capability.soulbound.player.SoulboundCapability;
import net.auoeke.soulboundarmory.capability.statistics.StatisticType;
import net.auoeke.soulboundarmory.client.i18n.Translations;
import net.auoeke.soulboundarmory.config.Configuration;
import net.auoeke.soulboundarmory.entity.SAAttributes;
import net.auoeke.soulboundarmory.item.SoulboundItem;
import net.auoeke.soulboundarmory.item.SoulboundToolItem;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.Item;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.common.ForgeMod;

public abstract class ToolStorage<T extends ItemStorage<T>> extends ItemStorage<T> {
    public ToolStorage(SoulboundCapability component, Item item) {
        super(component, item);
    }

    @Override
    public int getLevelXP(int level) {
        return this.canLevelUp()
            ? Configuration.instance().initialToolXP + (int) Math.round(4 * Math.pow(level, 1.25))
            : -1;
    }

    public ITextComponent miningLevelName() {
        return this.miningLevelName(this.statistic(StatisticType.miningLevel).intValue());
    }

    public ITextComponent miningLevelName(int level) {
        return switch (level) {
            case 0 -> Translations.miningLevelCoal;
            case 1 -> Translations.miningLevelIron;
            case 2 -> Translations.miningLevelDiamond;
            case 3 -> Translations.miningLevelObsidian;
            default -> new StringTextComponent("unknown");
        };

    }

    @Override
    public Multimap<Attribute, AttributeModifier> attributeModifiers(Multimap<Attribute, AttributeModifier> modifiers, EquipmentSlotType slot) {
        if (slot == EquipmentSlotType.MAINHAND) {
            modifiers.put(SAAttributes.efficiency, new AttributeModifier(SAAttributes.efficiencyUUID, "Tool modifier", this.attribute(StatisticType.efficiency), AttributeModifier.Operation.ADDITION));
            modifiers.put(ForgeMod.REACH_DISTANCE.get(), new AttributeModifier(SAAttributes.reachUUID, "Tool modifier", this.attribute(StatisticType.reach), AttributeModifier.Operation.ADDITION));
        }

        return modifiers;
    }

    @Override
    public Class<? extends SoulboundItem> itemClass() {
        return SoulboundToolItem.class;
    }
}
