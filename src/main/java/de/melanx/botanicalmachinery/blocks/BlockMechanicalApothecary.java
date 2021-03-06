package de.melanx.botanicalmachinery.blocks;

import de.melanx.botanicalmachinery.blocks.base.BotanicalBlock;
import de.melanx.botanicalmachinery.blocks.containers.ContainerMechanicalApothecary;
import de.melanx.botanicalmachinery.blocks.tiles.TileMechanicalApothecary;
import io.github.noeppi_noeppi.libx.block.DirectionShape;
import io.github.noeppi_noeppi.libx.mod.ModX;
import io.github.noeppi_noeppi.libx.mod.registration.BlockGUI;
import io.github.noeppi_noeppi.libx.mod.registration.TileEntityBase;
import io.github.noeppi_noeppi.libx.render.ItemStackRenderer;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidActionResult;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockMechanicalApothecary extends BlockGUI<TileMechanicalApothecary, ContainerMechanicalApothecary> {

    public static final DirectionShape SHAPE = new DirectionShape(VoxelShapes.or(
            BotanicalBlock.FRAME_SHAPE,
            makeCuboidShape(3, 1, 3, 13, 2, 13),
            makeCuboidShape(4, 2, 4, 12, 3, 12),
            makeCuboidShape(6, 3, 6, 10, 8, 10),
            makeCuboidShape(4, 8, 4, 12, 10, 12),
            makeCuboidShape(3, 10, 12, 13, 14, 13),
            makeCuboidShape(3, 10, 3, 13, 14, 4),
            makeCuboidShape(3, 10, 4, 4, 14, 12),
            makeCuboidShape(12, 10, 4, 13, 14, 12)
    ));

    public BlockMechanicalApothecary(ModX mod, Class<TileMechanicalApothecary> teClass, ContainerType<ContainerMechanicalApothecary> container) {
        super(mod, teClass, container, Properties.create(Material.ROCK).hardnessAndResistance(2, 10).variableOpacity(),
                new Item.Properties().setISTER(() -> ItemStackRenderer::get));
    }

    @Override
    public void registerClient(ResourceLocation id) {
        ItemStackRenderer.addRenderTile(this.getTileType(), true);
    }

    @Nonnull
    @Override
    public ActionResultType onBlockActivated(@Nonnull BlockState state, World world, @Nonnull BlockPos pos, @Nonnull PlayerEntity player, @Nonnull Hand hand, @Nonnull BlockRayTraceResult hit) {
        if (!world.isRemote) {
            TileEntity tile = world.getTileEntity(pos);

            ItemStack held = player.getHeldItemMainhand();
            @SuppressWarnings("ConstantConditions")
            FluidActionResult fluidActionResult = FluidUtil.tryEmptyContainer(held, tile.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null).orElse(null), 1000, player, true);
            if (fluidActionResult.isSuccess()) {
                if (tile instanceof TileEntityBase) {
                    ((TileEntityBase) tile).markDispatchable();
                }
                if (!player.isCreative()) {
                    player.setHeldItem(hand, fluidActionResult.getResult());
                }
                return ActionResultType.SUCCESS;
            }

            super.onBlockActivated(state, world, pos, player, hand, hit);
        }
        return ActionResultType.SUCCESS;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        return this.getDefaultState().with(BlockStateProperties.HORIZONTAL_FACING, context.getPlacementHorizontalFacing().getOpposite());
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.HORIZONTAL_FACING);
    }

    @SuppressWarnings("deprecation")
    @Override
    public int getOpacity(@Nonnull BlockState state, @Nonnull IBlockReader world, @Nonnull BlockPos pos) {
        return 0;
    }

    @Override
    public boolean propagatesSkylightDown(@Nonnull BlockState state, @Nonnull IBlockReader reader, @Nonnull BlockPos pos) {
        return true;
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean isTransparent(@Nonnull BlockState state) {
        return true;
    }

    @SuppressWarnings("deprecation")
    @Nonnull
    @Override
    public VoxelShape getShape(@Nonnull BlockState state, @Nonnull IBlockReader world, @Nonnull BlockPos pos, @Nonnull ISelectionContext context) {
        return SHAPE.getShape(state.get(BlockStateProperties.HORIZONTAL_FACING));
    }

    @Override
    protected boolean shouldDropInventory(World world, BlockPos pos, BlockState state) {
        return false;
    }
}
