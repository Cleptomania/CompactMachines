package dev.compactmods.machines.forge.machine.block;

import dev.compactmods.machines.api.shrinking.PSDTags;
import dev.compactmods.machines.forge.machine.item.BoundCompactMachineItem;
import dev.compactmods.machines.forge.machine.item.UnboundCompactMachineItem;
import dev.compactmods.machines.forge.room.RoomHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.stream.Collectors;

/**
 * Primary block.
 *
 * @since 5.2.0
 */
public class CompactMachineBlock extends Block implements EntityBlock {

    public static final BooleanProperty IS_CONNECTED = BooleanProperty.create("connected");

    public CompactMachineBlock(Properties props) {
        super(props);
        registerDefaultState(getStateDefinition().any()
                .setValue(IS_CONNECTED, false)
        );
    }

    // client-side
    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        if (level.getBlockEntity(pos) instanceof CompactMachineBlockEntity be) {
            return be.basicRoomInfo()
                    .map(BoundCompactMachineItem::createForRoom)
                    .orElse(UnboundCompactMachineItem.unbound());
        }

        return UnboundCompactMachineItem.unbound();
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(IS_CONNECTED);
        super.createBlockStateDefinition(builder);
    }

    @Override
    public void fillItemCategory(@NotNull CreativeModeTab tab, @NotNull NonNullList<ItemStack> tabItems) {
        var reg = RoomHelper.getTemplates();
        // todo - fix ordering
        tabItems.addAll(reg.entrySet()
                .stream()
                .map((template) -> UnboundCompactMachineItem.forTemplate(template.getKey().location(), template.getValue()))
                .collect(Collectors.toSet()));
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        // force client redraw
        super.setPlacedBy(level, pos, state, placer, stack);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new CompactMachineBlockEntity(pos, state);
    }

    @NotNull
    @Override
    @SuppressWarnings("deprecation")
    public InteractionResult use(@NotNull BlockState state, Level level, @NotNull BlockPos pos, Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hitResult) {
        MinecraftServer server = level.getServer();
        ItemStack mainItem = player.getMainHandItem();
        if (mainItem.is(PSDTags.ITEM) && player instanceof ServerPlayer sp) {
            return MachineBlockUtil.tryRoomTeleport(level, pos, sp, server);
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
