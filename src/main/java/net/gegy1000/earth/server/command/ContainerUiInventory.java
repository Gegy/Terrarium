package net.gegy1000.earth.server.command;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;

public class ContainerUiInventory implements IInventory {
    private final EntityPlayerMP player;
    private final ITextComponent title;

    private final Int2ObjectMap<ContainerUi.Element> elements;
    private final int rows;

    private final NonNullList<ItemStack> stacks;

    public ContainerUiInventory(EntityPlayerMP player, ITextComponent title, ContainerUi.Element[] elements, int rows) {
        this.player = player;
        this.title = title;
        this.rows = rows;

        this.stacks = NonNullList.withSize(this.getSizeInventory(), ItemStack.EMPTY);

        this.elements = new Int2ObjectOpenHashMap<>();
        for (ContainerUi.Element element : elements) {
            int slot = element.getColumn() + element.getRow() * ContainerUi.WIDTH;
            this.elements.put(slot, element);
            this.stacks.set(slot, element.createStack());
        }
    }

    @Override
    public int getSizeInventory() {
        return this.rows * ContainerUi.WIDTH;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public ItemStack getStackInSlot(int index) {
        return this.stacks.get(index);
    }

    @Override
    public ItemStack decrStackSize(int index, int count) {
        this.handleElementClick(index);
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeStackFromSlot(int index) {
        this.handleElementClick(index);
        return ItemStack.EMPTY;
    }

    private void handleElementClick(int index) {
        ContainerUi.Element element = this.elements.get(index);
        if (element != null) {
            this.player.closeScreen();
            element.handleClick();
        }
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
    }

    @Override
    public int getInventoryStackLimit() {
        return 1;
    }

    @Override
    public void markDirty() {
    }

    @Override
    public boolean isUsableByPlayer(EntityPlayer player) {
        return true;
    }

    @Override
    public void openInventory(EntityPlayer player) {
    }

    @Override
    public void closeInventory(EntityPlayer player) {
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        return false;
    }

    @Override
    public int getField(int id) {
        return 0;
    }

    @Override
    public void setField(int id, int value) {
    }

    @Override
    public int getFieldCount() {
        return 0;
    }

    @Override
    public void clear() {
    }

    @Override
    public String getName() {
        return "Container UI";
    }

    @Override
    public boolean hasCustomName() {
        return false;
    }

    @Override
    public ITextComponent getDisplayName() {
        return this.title;
    }
}
