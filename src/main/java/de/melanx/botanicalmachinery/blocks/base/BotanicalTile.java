package de.melanx.botanicalmachinery.blocks.base;

import com.google.common.base.Predicates;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import de.melanx.botanicalmachinery.core.TileTags;
import io.github.noeppi_noeppi.libx.inventory.BaseItemStackHandler;
import io.github.noeppi_noeppi.libx.inventory.ItemStackHandlerWrapper;
import io.github.noeppi_noeppi.libx.mod.registration.TileEntityBase;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.lwjgl.opengl.GL11;
import vazkii.botania.api.mana.IKeyLocked;
import vazkii.botania.api.mana.IManaPool;
import vazkii.botania.api.mana.IThrottledPacket;
import vazkii.botania.api.mana.spark.ISparkAttachable;
import vazkii.botania.api.mana.spark.ISparkEntity;
import vazkii.botania.client.core.handler.HUDHandler;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Supplier;

public abstract class BotanicalTile extends TileEntityBase implements IManaPool, IManaMachineTile, IKeyLocked, ISparkAttachable, IThrottledPacket, ITickableTileEntity {

    private int mana;
    private final int manaCap;
    private String inputKey = "";
    private String outputKey = "";

    private final LazyOptional<IItemHandlerModifiable> capability = this.createCap(this::getInventory);

    public BotanicalTile(TileEntityType<?> tileEntityTypeIn, int manaCap) {
        super(tileEntityTypeIn);
        this.manaCap = manaCap;
    }

    /**
     * This can be used to add canExtract or canInsert to the wrapper used as capability. You may not call the supplier
     * now. Always use IItemHandlerModifiable.createLazy. You may call the supplier inside the canExtract and canInsert
     * lambda.
     */
    protected LazyOptional<IItemHandlerModifiable> createCap(Supplier<IItemHandlerModifiable> inventory) {
        return ItemStackHandlerWrapper.createLazy(inventory);
    }

    @Nonnull
    public abstract BaseItemStackHandler getInventory();

    public abstract boolean isValidStack(int slot, ItemStack stack);

    public abstract int getComparatorOutput();

    @Override
    public void markDirty() {
        super.markDirty();
        if (this.world != null) {
            this.world.updateComparatorOutputLevel(this.pos, this.getBlockState().getBlock());
        }
    }

    @Nonnull
    @Override
    public <X> LazyOptional<X> getCapability(@Nonnull Capability<X> cap, Direction direction) {
        if (!this.removed && cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return this.capability.cast();
        }
        return super.getCapability(cap, direction);
    }

    @Override
    public void read(@Nonnull BlockState state, @Nonnull CompoundNBT cmp) {
        super.read(state, cmp);
        this.getInventory().deserializeNBT(cmp.getCompound(TileTags.INVENTORY));
        this.mana = cmp.getInt(TileTags.MANA);
        if (cmp.contains(TileTags.INPUT_KEY)) this.inputKey = cmp.getString(TileTags.INPUT_KEY);
        if (cmp.contains(TileTags.OUTPUT_KEY)) this.outputKey = cmp.getString(TileTags.OUTPUT_KEY);
    }

    @Nonnull
    @Override
    public CompoundNBT write(@Nonnull CompoundNBT cmp) {
        cmp.put(TileTags.INVENTORY, this.getInventory().serializeNBT());
        cmp.putInt(TileTags.MANA, this.getCurrentMana());
        cmp.putString(TileTags.INPUT_KEY, this.inputKey);
        cmp.putString(TileTags.OUTPUT_KEY, this.outputKey);
        return super.write(cmp);
    }

    @Override
    public void handleUpdateTag(BlockState state, CompoundNBT cmp) {
        if (this.world != null && !this.world.isRemote) return;
        this.getInventory().deserializeNBT(cmp.getCompound(TileTags.INVENTORY));
        this.mana = cmp.getInt(TileTags.MANA);
        if (cmp.contains(TileTags.INPUT_KEY)) this.inputKey = cmp.getString(TileTags.INPUT_KEY);
        if (cmp.contains(TileTags.OUTPUT_KEY)) this.outputKey = cmp.getString(TileTags.OUTPUT_KEY);
    }

    @Nonnull
    @Override
    public CompoundNBT getUpdateTag() {
        if (this.world != null && this.world.isRemote) return super.getUpdateTag();
        CompoundNBT cmp = super.getUpdateTag();
        cmp.put(TileTags.INVENTORY, this.getInventory().serializeNBT());
        cmp.putInt(TileTags.MANA, this.getCurrentMana());
        cmp.putString(TileTags.INPUT_KEY, this.inputKey);
        cmp.putString(TileTags.OUTPUT_KEY, this.outputKey);
        return cmp;
    }

    @OnlyIn(Dist.CLIENT)
    public void renderHUD(MatrixStack ms, Minecraft mc) {
        ItemStack block = new ItemStack(this.getBlockState().getBlock());
        String name = block.getDisplayName().getString();
        int color = 0x4444FF;
        HUDHandler.drawSimpleManaHUD(ms, color, this.getCurrentMana(), this.getManaCap(), name);

        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        mc.textureManager.bindTexture(HUDHandler.manaBar);

        //noinspection deprecation
        RenderSystem.disableLighting();
        RenderSystem.disableBlend();
    }

    @Override
    public String getInputKey() {
        return this.inputKey;
    }

    @Override
    public String getOutputKey() {
        return this.outputKey;
    }

    @Override
    public boolean canAttachSpark(ItemStack itemStack) {
        return true;
    }

    @Override
    public void attachSpark(ISparkEntity iSparkEntity) {

    }

    @Override
    public int getAvailableSpaceForMana() {
        return Math.max(Math.max(0, this.getManaCap() - this.getCurrentMana()), 0);
    }

    @Override
    public ISparkEntity getAttachedSpark() {
        @SuppressWarnings("ConstantConditions")
        List<Entity> sparks = this.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(this.pos.up(), this.pos.up().add(1, 1, 1)), Predicates.instanceOf(ISparkEntity.class));
        if (sparks.size() == 1) {
            Entity entity = sparks.get(0);
            return (ISparkEntity) entity;
        }
        return null;
    }

    @Override
    public boolean areIncomingTranfersDone() {
        return false;
    }

    @Override
    public boolean isFull() {
        return this.getCurrentMana() >= this.getManaCap();
    }

    @Override
    public void receiveMana(int i) {
        int old = this.getCurrentMana();
        this.mana = Math.max(0, Math.min(this.getCurrentMana() + i, this.getManaCap()));
        if (old != this.getCurrentMana()) {
            this.markDirty();
            this.markDispatchable();
        }
    }

    @Override
    public boolean canReceiveManaFromBursts() {
        return true;
    }

    @Override
    public int getCurrentMana() {
        return this.mana;
    }

    @Override
    public int getManaCap() {
        return this.manaCap;
    }

    @Override
    public boolean isOutputtingPower() {
        return false;
    }

    @Override
    public DyeColor getColor() {
        return null;
    }

    @Override
    public void setColor(DyeColor dyeColor) {
        // unused
    }
}
