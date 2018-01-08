package com.sinesection.logisticraft.gui;

import com.sinesection.logisticraft.Main;
import com.sinesection.logisticraft.container.ContainerDryDistiller;
import com.sinesection.logisticraft.tileentity.TileEntityDryDistiller;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;

public class GuiDryDistiller extends GuiContainer {

	public static final ResourceLocation guiBgTexture = new ResourceLocation(Main.MODID, "textures/gui/guiDistiller.png");

	public TileEntityDryDistiller tEntity;

	private final int textColor = 4210752;
	
	public GuiDryDistiller(InventoryPlayer inventory, TileEntityDryDistiller tEntity) {
		super(new ContainerDryDistiller(inventory, tEntity));
		this.tEntity = tEntity;
		this.xSize = 176;
		this.ySize = 166;
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int i, int j) {
		String displayName = this.tEntity.isInventoryNameLocalized() ? this.tEntity.getInventoryName() : I18n.format(this.tEntity.getInventoryName());

		this.fontRendererObj.drawString(displayName, this.xSize / 2 - this.fontRendererObj.getStringWidth(displayName) / 2, 6, textColor);
		this.fontRendererObj.drawString(I18n.format("container.inventory"), 8, this.ySize - 96 + 2, textColor);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int i, int j) {

	}

}