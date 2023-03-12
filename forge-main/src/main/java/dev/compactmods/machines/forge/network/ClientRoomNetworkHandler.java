package dev.compactmods.machines.forge.network;

import dev.compactmods.machines.forge.room.RoomHelper;
import dev.compactmods.machines.forge.room.ui.MachineRoomScreen;
import dev.compactmods.machines.room.client.ClientRoomMetadata;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;

public class ClientRoomNetworkHandler {
    public static void handleBlockData(InitialRoomBlockDataPacket blockData) {
        final var mc = Minecraft.getInstance();
        if(mc.screen instanceof MachineRoomScreen mrs) {
            mrs.getMenu().setBlocks(blockData.blocks());
            mrs.updateBlockRender();
        }
    }

    public static void handleRoomSync(SyncRoomMetadataPacket sync) {
        final var mc = Minecraft.getInstance();
        mc.player.getCapability(RoomHelper.CURRENT_ROOM_META).ifPresent(meta -> {
            if(sync.owner().equals(Util.NIL_UUID))
                meta.clearCurrent();
            else
                meta.setCurrent(new ClientRoomMetadata(sync.roomCode(), sync.owner()));
        });
    }
}
