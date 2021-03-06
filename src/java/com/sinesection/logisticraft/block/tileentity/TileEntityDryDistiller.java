package com.sinesection.logisticraft.block.tileentity;

import java.util.ArrayList;
import java.util.List;

import com.sinesection.logisticraft.block.BlockDryDistiller;
import com.sinesection.logisticraft.crafting.DryDistillerCraftingRecipe;
import com.sinesection.logisticraft.crafting.LogisticraftDryDistillerCrafting;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemHoe;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemTool;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraftforge.oredict.OreDictionary;

public class TileEntityDryDistiller extends LogisticraftTileEntity implements ISidedInventory {

	private String localizedName;
	
	public static final int NUM_SLOTS = 6;

	// Slot 0 = input
	// Slot 1 = fuel
	// Slot 2-5 = output
	private ItemStack[] slots = new ItemStack[NUM_SLOTS];

	private float efficiency = 0.5f;

	// Side 0 = bottom
	// Side 1 = top
	// Side 2-5 = n,s,e,w
	private int[][] accesibleSlotsFromSide = new int[][] {
			{
					1, 2, 3, 4, 5
			}, {
					0
			}, {
					1
			}, {
					1
			}, {
					1
			}, {
					1
			}
	};

	/** Time to process the item in slot 0. (in ticks) */
	private int processSpeed = 150;

	/** Time left for fuel to be used up. (in ticks) */
	public int burnTime;
	/** Time it takes to burn the current item in slot 1. (in ticks) */
	public int currentItemBurnTime;

	/** Time left to process the item in slot 0. (in ticks) */
	public int processTime;

	@Override
	public int getSizeInventory() {
		return NUM_SLOTS;
	}

	@Override
	public String getInventoryName() {
		return this.hasCustomInventoryName() ? this.localizedName : "container.guiDryDistiller.name";
	}

	public void setGuiDisplayName(String displayName) {
		this.localizedName = displayName;
	}

	@Override
	public ItemStack getStackInSlot(int slot) {
		if(slot < 0 || slot > getSizeInventory())
			return null;
		return this.slots[slot];
	}

	@Override
	public ItemStack decrStackSize(int slot, int amt) {
		if(this.getStackInSlot(slot) != null) {
			ItemStack slotContents;
			if(this.getStackInSlot(slot).stackSize <= amt) {
				slotContents = this.getStackInSlot(slot);
				this.setInventorySlotContents(slot, null);
				return slotContents;
			} else {
				slotContents = this.getStackInSlot(slot).splitStack(amt);
				if(this.getStackInSlot(slot).stackSize == 0) {
					this.setInventorySlotContents(slot, null);
				}
				return slotContents;
			}
		}
		return null;
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int slot) {
		ItemStack slotContents = this.getStackInSlot(slot);
		if(slotContents != null) {
			this.setInventorySlotContents(slot, null);
			return slotContents;
		}
		return null;
	}

	@Override
	public void setInventorySlotContents(int slot, ItemStack itemStack) {
		if(slot < 0 || slot > getSizeInventory())
			return;
		if(itemStack != null && itemStack.stackSize > this.getInventoryStackLimit())
			itemStack.stackSize = this.getInventoryStackLimit();
		if (itemStack != null && itemStack.stackSize == 0)
			itemStack = null;
		
		this.slots[slot] = itemStack;
		this.markDirty();
	}

	@Override
	public boolean hasCustomInventoryName() {
		return this.localizedName != null && this.localizedName.length() > 0;
	}
	
	@Override
	public int getInventoryStackLimit() {
		return 64;
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		
		NBTTagList list = nbt.getTagList("items", 10);
		this.slots = new ItemStack[this.getSizeInventory()];
		
		for(int i = 0; i < list.tagCount(); i++) {
			NBTTagCompound compound = list.getCompoundTagAt(i);
			byte slot = compound.getByte("slot");
			if(slot >= 0 && slot < this.getSizeInventory()) {
				this.setInventorySlotContents(slot, ItemStack.loadItemStackFromNBT(compound));
			}
		}
		
		this.processTime = nbt.getShort("processTime");
		this.burnTime = nbt.getShort("burnTime");
		this.currentItemBurnTime = this.getItemBurnTime(this.getStackInSlot(1));
		
		String customName = nbt.getString("customName");
		if(!customName.isEmpty()) {
			 this.localizedName = customName;
		}
	}
	
	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		
		nbt.setShort("processTime", (short)this.processTime);
		nbt.setShort("burnTime", (short)this.burnTime);
		
		NBTTagList list = new NBTTagList();
		for(int i = 0; i < this.slots.length; i++) {
			if(this.slots[i] != null) {
				NBTTagCompound compound = new NBTTagCompound();
				compound.setByte("slot", (byte) i);
				this.slots[i].writeToNBT(compound);
				list.appendTag(compound);
			}
		}
		nbt.setTag("items", list);
		if(this.hasCustomInventoryName()) {
			nbt.setString("customName", this.localizedName);
		}
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer player) {
		return this.worldObj.getTileEntity(this.xCoord, this.yCoord, this.zCoord) != this ? false : player.getDistanceSq((double) this.xCoord + 0.5D, (double) this.yCoord + 0.5D, (double) this.zCoord + 0.5D) <= 64.0D;
	}

	@Override
	public void openInventory() {}

	@Override
	public void closeInventory() {}
	
	@Override
	public Packet getDescriptionPacket() {
		NBTTagCompound compound = new NBTTagCompound();
		writeToNBT(compound);
		return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 0, compound);
	}

	@Override
	public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt) {
		NBTTagCompound compound = pkt.func_148857_g();
		readFromNBT(compound);
	}

	@Override
	public void updateEntity() {
		boolean blockUpdate = isBurning();
		boolean invChanged = false;

		if (!this.worldObj.isRemote) {
			if (isBurning()) {
				this.burnTime--;
			}
			if (this.burnTime == 0 && this.canProcess()) {
				this.currentItemBurnTime = this.burnTime = this.getItemBurnTime(getStackInSlot(1));
				if (this.burnTime > 0) {
					if (this.getStackInSlot(1) != null) {
						this.getStackInSlot(1).stackSize--;
						if (this.getStackInSlot(1).stackSize == 0) {
							this.setInventorySlotContents(1, this.getStackInSlot(1).getItem().getContainerItem(this.getStackInSlot(1)));
						}
						invChanged = true;
					}
				}
			}

			if (isBurning() && canProcess()) {
				this.processTime++;
				if (this.processTime == this.processSpeed) {
					this.processTime = 0;
					this.process();
					invChanged = true;
				}
			} else {
				this.processTime = 0;
			}

			if (blockUpdate != isBurning()) {
				invChanged = true;
				BlockDryDistiller.updateState(isBurning(), this.worldObj, this.xCoord, this.yCoord, this.zCoord);
			}

			if (invChanged) {
				this.markDirty();
			}
		}
	}

	private void process() {
		if (this.canProcess()) {
			DryDistillerCraftingRecipe recipe = LogisticraftDryDistillerCrafting.getRecipeFromInput(getStackInSlot(0));
			for(int i = 0; i < recipe.outputs.length; i++) {
				ItemStack result = recipe.outputs[i];
				if(this.getStackInSlot(i + 2) == null) {
					this.setInventorySlotContents(i + 2, result.copy());
				} else if(this.getStackInSlot(i + 2).isItemEqual(recipe.outputs[i])) {
					this.getStackInSlot(i + 2).stackSize += recipe.outputs[i].stackSize;
				}
			}
			this.getStackInSlot(0).stackSize -= recipe.input.stackSize;
			if(this.getStackInSlot(0).stackSize <= 0) {
				this.setInventorySlotContents(0, null);
			}
		}
	}

	private boolean canProcess() {
		if (getStackInSlot(0) == null)
			return false;
		DryDistillerCraftingRecipe recipe = LogisticraftDryDistillerCrafting.getRecipeFromInput(getStackInSlot(0));
		return recipe != null && !recipe.fractionatorRequired && canOutput(recipe) && (getStackInSlot(0).stackSize - recipe.input.stackSize) >= 0;
	}

	private boolean canOutput(DryDistillerCraftingRecipe recipe) {
		boolean canOutput = false;
		for(int i = 0; i < recipe.outputs.length; i++) {
			if(getStackInSlot(i + 2) == null) {
				canOutput = true;
			} else {
				int resultAmt = getStackInSlot(i + 2).stackSize + recipe.outputs[i].stackSize;
				if(getStackInSlot(i + 2).isItemEqual(recipe.outputs[i])) {
					if(resultAmt <= getStackInSlot(i + 2).getMaxStackSize() && resultAmt <= getInventoryStackLimit()) {
						canOutput = true;
					}
				}
			}
		}
		return canOutput;
	}

	public boolean isBurning() {
		return this.burnTime > 0;
	}

	public boolean isRunning() {
		return this.processTime > 0;
	}

	public int getItemBurnTime(ItemStack itemStack) {
		int burnTime = 0;
		if (itemStack == null) {
			burnTime = 0;
		} else {
			List<String> oreDictNames = new ArrayList<String>();
			for (int i : OreDictionary.getOreIDs(itemStack)) {
				oreDictNames.add(OreDictionary.getOreName(i));
			}
			boolean isWood = false;
			for (String oreDictName : oreDictNames) {
				if (oreDictName.toLowerCase().contains("wood")) {
					isWood = true;
					break;
				}
			}

			if (itemStack.getItem() instanceof ItemTool)
				isWood = ((ItemTool) itemStack.getItem()).getToolMaterialName().equalsIgnoreCase("WOOD");
			if (itemStack.getItem() instanceof ItemSword)
				isWood = ((ItemSword) itemStack.getItem()).getToolMaterialName().equalsIgnoreCase("WOOD");
			if (itemStack.getItem() instanceof ItemHoe)
				isWood = ((ItemHoe) itemStack.getItem()).getToolMaterialName().equalsIgnoreCase("WOOD");
			if (itemStack.getItem() == Items.stick)
				isWood = true;
			if (itemStack.getItem() instanceof ItemBlock)
				isWood = ((ItemBlock) itemStack.getItem()).field_150939_a.getMaterial() == Material.wood;
			
			if (isWood) {
				burnTime = 0;
			} else {
				burnTime = GameRegistry.getFuelValue(itemStack);
			}
			
			if (itemStack.getItem() == Items.coal)
				burnTime = 200 * 8;
		}
		return (int) Math.round((float) burnTime * efficiency);
	}

	public boolean isItemFuel(ItemStack itemStack) {
		return getItemBurnTime(itemStack) > 0;
	}

	@Override
	public boolean isItemValidForSlot(int slot, ItemStack itemStack) {
		return slot > 1 ? false : (slot == 1 ? isItemFuel(itemStack) : true);
	}

	@Override
	public int[] getAccessibleSlotsFromSide(int side) {
		return accesibleSlotsFromSide[side];
	}

	@Override
	public boolean canInsertItem(int slot, ItemStack itemStack, int side) {
		return this.isItemValidForSlot(slot, itemStack);
	}

	@Override
	public boolean canExtractItem(int slot, ItemStack itemStack, int side) {
		return side != 0 || slot != 1 || itemStack.isItemEqual(new ItemStack(Items.bucket));
	}

	public int getBurnTimeScaled(int i) {
		if(this.currentItemBurnTime == 0) {
			this.currentItemBurnTime = this.processSpeed;
		}
		
		return (this.burnTime * i) / this.currentItemBurnTime;
	}
	
	public int getProgressScaled(int i) {
		return (this.processTime * i) / this.processSpeed;
	}

}
