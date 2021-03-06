package de.melanx.botanicalmachinery.blocks.containers;

import de.melanx.botanicalmachinery.blocks.base.BotanicalTile;
import de.melanx.botanicalmachinery.blocks.tiles.TileMechanicalBrewery;
import io.github.noeppi_noeppi.libx.inventory.container.ContainerBase;
import io.github.noeppi_noeppi.libx.inventory.slot.SlotOutputOnly;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerMechanicalBrewery extends ContainerBase<TileMechanicalBrewery> {

    public ContainerMechanicalBrewery(ContainerType<?> type, int windowId, World world, BlockPos pos, PlayerInventory playerInventory, PlayerEntity player) {
        super(type, windowId, world, pos, playerInventory, player, 7, 8);

        IItemHandlerModifiable inventory = ((BotanicalTile) this.tile).getInventory().getUnrestricted();
        this.addSlot(new SlotItemHandler(inventory, 0, 44, 48));
        this.addSlot(new SlotItemHandler(inventory, 1, 29, 18));
        this.addSlot(new SlotItemHandler(inventory, 2, 59, 18));
        this.addSlot(new SlotItemHandler(inventory, 3, 14, 48));
        this.addSlot(new SlotItemHandler(inventory, 4, 74, 48));
        this.addSlot(new SlotItemHandler(inventory, 5, 29, 78));
        this.addSlot(new SlotItemHandler(inventory, 6, 59, 78));
        this.addSlot(new SlotOutputOnly(inventory, 7, 128, 49));
        this.layoutPlayerInventorySlots(8, 110);
    }
}
