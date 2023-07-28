package org.dave.compactmachines.handler;

import org.dave.compactmachines.CompactMachines;
import org.dave.compactmachines.machines.tools.TeleportTools;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;
import cpw.mods.fml.common.gameevent.TickEvent.Type;
import cpw.mods.fml.relauncher.Side;

public class CMTickHandler {

    @SubscribeEvent
    public void tick(TickEvent event) {
        if (event.side == Side.SERVER && event.phase == Phase.START && event.type == Type.SERVER) {
            if (CompactMachines.instance.machineHandler != null) {
                TeleportTools.checkPlayerPositions();
            }
        }
    }
}
