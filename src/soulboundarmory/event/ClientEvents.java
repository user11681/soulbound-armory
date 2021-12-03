package soulboundarmory.event;

import cell.client.gui.CellElement;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.SinglePreparationResourceReloader;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.profiler.Profiler;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import soulboundarmory.SoulboundArmory;
import soulboundarmory.SoulboundArmoryClient;
import soulboundarmory.client.gui.bar.ExperienceBarOverlay;
import soulboundarmory.component.Components;
import soulboundarmory.component.soulbound.item.ItemStorage;
import soulboundarmory.component.soulbound.item.weapon.StaffStorage;
import soulboundarmory.config.Configuration;
import soulboundarmory.item.SoulboundItem;
import soulboundarmory.text.Translation;

@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(modid = SoulboundArmory.ID)
public class ClientEvents {
    public static ExperienceBarOverlay overlayBar;
    public static ExperienceBarOverlay tooltipBar;

    @SubscribeEvent
    public static void scroll(InputEvent.MouseScrollEvent event) {
        if (Screen.hasAltDown()) {
            PlayerEntity player = SoulboundArmoryClient.client.player;

            if (player != null && player.world != null) {
                var storage = Components.weapon.of(player).heldItemStorage();

                if (storage instanceof StaffStorage) {
                    var dY = (int) event.getMouseY();

                    if (dY != 0) {
                        var staffStorage = (StaffStorage) storage;

                        staffStorage.cycleSpells(-dY);
                        SoulboundArmoryClient.client.inGameHud.setOverlayMessage(new Translation("§4§l%s", staffStorage.spell()), false);

                        event.setCanceled(true);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onRenderGameOverlay(RenderGameOverlayEvent.Pre event) {
        if (event.getType() == RenderGameOverlayEvent.ElementType.EXPERIENCE && Configuration.instance().client.overlayExperienceBar && overlayBar.render(event.getMatrixStack(), event.getWindow())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        var player = event.getPlayer();

        if (player != null) {
            var itemStack = event.getItemStack();
            var item = itemStack.getItem();

            ItemStorage.get(player, item).ifPresent(storage -> {
                var tooltip = event.getToolTip();
                var startIndex = 1;

                for (int index = 0, size = tooltip.size(); index < size; index++) {
                    var entry = tooltip.get(index);

                    if (entry instanceof TranslatableText translation && translation.getKey().equals("item.modifiers.mainhand")) {
                        startIndex += index;
                    }
                }

                var toIndex = tooltip.size();
                // var fromIndex = Math.min(toIndex - 1, startIndex + ((SoulboundItem) item).getMainhandAttributeEntries(itemStack, player));

                var prior = new ArrayList<>(tooltip).subList(0, startIndex);
                var insertion = storage.tooltip();
                // var posterior = new ArrayList<>(tooltip).subList(fromIndex, toIndex);

                tooltip.clear();
                tooltip.addAll(prior);
                tooltip.addAll(insertion);
                // tooltip.addAll(posterior);

                var row = insertion.lastIndexOf(LiteralText.EMPTY) + prior.size();
                tooltipBar.data(row, CellElement.textDrawer.getWidth(tooltip.get(row - 2)) - 4);
            });
        }
    }

    @SubscribeEvent
    public static void onRenderTooltip(RenderTooltipEvent.PostBackground event) {
        if (event.getStack().getItem() instanceof SoulboundItem) {
            tooltipBar.drawTooltip(event.getMatrixStack(), event.getX(), event.getY(), event.getStack());
        }
    }

    @SubscribeEvent
    public static void onLoadResources(AddReloadListenerEvent event) {
        event.addListener(new ExperienceBarReloader());
    }

    private static class ExperienceBarReloader extends SinglePreparationResourceReloader<Void> {
        @Override
        protected Void prepare(ResourceManager manager, Profiler profiler) {
            return null;
        }

        @Override
        protected void apply(Void nothing, ResourceManager manager, Profiler profiler) {
            RenderSystem.recordRenderCall(() -> {
                overlayBar = new ExperienceBarOverlay();
                tooltipBar = new ExperienceBarOverlay();
                overlayBar.width(182).height(5).center(true);
            });
        }
    }
}
