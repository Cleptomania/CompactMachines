package dev.compactmods.machines.forge.machine.block;

import dev.compactmods.machines.forge.CompactMachines;
import dev.compactmods.machines.forge.config.ServerConfig;
import dev.compactmods.machines.forge.wall.Walls;
import dev.compactmods.machines.api.core.CMTags;
import dev.compactmods.machines.api.dimension.CompactDimension;
import dev.compactmods.machines.api.dimension.MissingDimensionException;
import dev.compactmods.machines.api.room.RoomTemplate;
import dev.compactmods.machines.machine.EnumMachinePlayersBreakHandling;
import dev.compactmods.machines.forge.machine.item.BoundCompactMachineItem;
import dev.compactmods.machines.forge.machine.item.LegacyCompactMachineItem;
import dev.compactmods.machines.forge.machine.item.UnboundCompactMachineItem;
import dev.compactmods.machines.forge.room.RoomHelper;
import dev.compactmods.machines.machine.graph.DimensionMachineGraph;
import dev.compactmods.machines.room.BasicRoomInfo;
import dev.compactmods.machines.room.exceptions.NonexistentRoomException;
import dev.compactmods.machines.room.graph.CompactRoomProvider;
import dev.compactmods.machines.tunnel.graph.TunnelConnectionGraph;
import dev.compactmods.machines.tunnel.graph.traversal.TunnelMachineFilters;
import dev.compactmods.machines.util.CompactStructureGenerator;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;
import java.util.Optional;
import java.util.UUID;

@SuppressWarnings("removal")
public class MachineBlockUtil {

    static void cleanupTunnelsPostMachineRemove(Level level, BlockPos pos) {
        if (level instanceof ServerLevel sl) {
            final var serv = sl.getServer();
            final var compactDim = serv.getLevel(CompactDimension.LEVEL_KEY);

            if (level.getBlockEntity(pos) instanceof CompactMachineBlockEntity entity) {
                entity.connectedRoom().ifPresent(roomCode -> {
                    final var dimGraph = DimensionMachineGraph.forDimension(sl);
                    dimGraph.unregisterMachine(pos);

                    if (compactDim == null)
                        return;

                    final var tunnels = TunnelConnectionGraph.forRoom(compactDim, roomCode);
                    tunnels.positions(TunnelMachineFilters.all(entity.getLevelPosition()))
                            .forEach(pos1 -> {
                                tunnels.unregister(pos1);
                                compactDim.setBlock(pos1, Walls.BLOCK_SOLID_WALL.get().defaultBlockState(), Block.UPDATE_ALL);
                            });
                });
            }
        }
    }

    @Nonnull
    static InteractionResult tryRoomTeleport(Level level, BlockPos pos, ServerPlayer player, MinecraftServer server) {
        // Try teleport to compact machine dimension
        if (level.getBlockEntity(pos) instanceof CompactMachineBlockEntity tile) {
            tile.connectedRoom().ifPresentOrElse(roomCode -> {
                try {
                    RoomHelper.teleportPlayerIntoMachine(level, player, tile.getLevelPosition(), roomCode);
                } catch (MissingDimensionException e) {
                    e.printStackTrace();
                }
            }, () -> {
                final var state = level.getBlockState(pos);
                RoomTemplate template = RoomTemplate.INVALID_TEMPLATE;
                if(state.is(LegacySizedCompactMachineBlock.LEGACY_MACHINES_TAG)) {
                    if(state.getBlock() instanceof LegacySizedCompactMachineBlock b)
                        template = LegacySizedCompactMachineBlock.getLegacyTemplate(b.getSize());
                } else {
                    template = tile.getRoomTemplate().orElse(RoomTemplate.INVALID_TEMPLATE);
                }

                createAndEnterRoom(player, server, template, tile);
                // AdvancementTriggers.getTriggerForMachineClaim(size).trigger(sp);
            });

            return InteractionResult.SUCCESS;
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    static void createAndEnterRoom(ServerPlayer owner, MinecraftServer server, RoomTemplate template, CompactMachineBlockEntity machine) {
        try {
            final var compactDim = CompactDimension.forServer(server);
            if(template.equals(RoomTemplate.INVALID_TEMPLATE)) {
                CompactMachines.LOGGER.fatal("Tried to create and enter an invalidly-registered room. Something went very wrong!");
                return;
            }

            final var roomInfo = CompactRoomProvider.instance(compactDim);
            final var newRoom = roomInfo.registerNew(builder -> builder
                    .setColor(template.color())
                    .setDimensions(template.dimensions())
                    .setOwner(owner.getUUID()));

            // Generate a new machine room
            final var unbreakableWall = Walls.BLOCK_SOLID_WALL.get().defaultBlockState();
            CompactStructureGenerator.generateRoom(compactDim, template.dimensions(), newRoom.center(), unbreakableWall);

            // If template specified, prefill new room
            if (!template.prefillTemplate().equals(RoomTemplate.NO_TEMPLATE)) {
                CompactStructureGenerator.fillWithTemplate(compactDim, template.prefillTemplate(), template.dimensions(), newRoom.center());
            }

            machine.setConnectedRoom(newRoom.code());

            RoomHelper.teleportPlayerIntoRoom(server, owner, newRoom, machine.getLevelPosition());
        } catch (MissingDimensionException | NonexistentRoomException e) {
            CompactMachines.LOGGER.error("Error occurred while generating new room and machine info for first player entry.", e);
        }
    }

    public static float destroyProgress(BlockState state, Player player, BlockGetter worldIn, BlockPos pos) {
        CompactMachineBlockEntity tile = (CompactMachineBlockEntity) worldIn.getBlockEntity(pos);
        float normalHardness = state.getDestroyProgress(player, worldIn, pos);

        if (tile == null)
            return normalHardness;

        boolean hasPlayers = tile.hasPlayersInside();


        // If there are players inside, check config for break handling
        if (hasPlayers) {
            EnumMachinePlayersBreakHandling hand = ServerConfig.MACHINE_PLAYER_BREAK_HANDLING.get();
            switch (hand) {
                case UNBREAKABLE:
                    return 0;

                case OWNER:
                    Optional<UUID> ownerUUID = tile.getOwnerUUID();
                    return ownerUUID
                            .map(uuid -> player.getUUID() == uuid ? normalHardness : 0)
                            .orElse(normalHardness);

                case ANYONE:
                    return normalHardness;
            }
        }

        // No players inside - let anyone break it
        return normalHardness;
    }

    public static ItemStack getCloneItemStack(BlockGetter world, BlockState state, BlockPos pos) {
        if(state.is(LegacySizedCompactMachineBlock.LEGACY_MACHINES_TAG) && state.getBlock() instanceof LegacySizedCompactMachineBlock l)
        {
            final var item = LegacyCompactMachineItem.getItemBySize(l.getSize());
            return new ItemStack(item);
        }

        // If not a machine or the block data is invalid...
        if(!state.is(CMTags.MACHINE_BLOCK) || !(world.getBlockEntity(pos) instanceof CompactMachineBlockEntity tile))
            return UnboundCompactMachineItem.unbound();

        return tile.connectedRoom().map(roomCode -> {
            final var roomInfo = new BasicRoomInfo(roomCode, tile.getColor());
            return BoundCompactMachineItem.createForRoom(roomInfo);
        }).orElse(UnboundCompactMachineItem.unbound());
    }

}
