package net.auoeke.soulboundarmory.network.server;

import net.auoeke.soulboundarmory.capability.soulbound.item.ItemStorage;
import net.auoeke.soulboundarmory.capability.soulbound.item.weapon.StaffStorage;
import net.auoeke.soulboundarmory.network.ExtendedPacketBuffer;
import net.auoeke.soulboundarmory.network.ItemComponentPacket;
import net.minecraftforge.fml.network.NetworkEvent;

public class C2SSync implements ItemComponentPacket {
    @Override
    public void execute(ExtendedPacketBuffer buffer, NetworkEvent.Context context, ItemStorage<?> storage) {
        var tag = buffer.readNbt();

        if (tag.contains("tab")) {
            storage.tab(tag.getInt("tab"));
        }

        if (storage instanceof StaffStorage staff && tag.contains("spell")) {
            staff.setSpell((tag.getInt("spell")));
        }
    }
}
