package dev.compactmods.machines.forge.room.upgrade;

import dev.compactmods.machines.forge.upgrade.MachineRoomUpgrades;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class RoomUpgradeWorkbenchEntity extends BlockEntity {
    public RoomUpgradeWorkbenchEntity(BlockPos pPos, BlockState pBlockState) {
        super(MachineRoomUpgrades.ROOM_UPDATE_ENTITY.get(), pPos, pBlockState);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
    }
}
