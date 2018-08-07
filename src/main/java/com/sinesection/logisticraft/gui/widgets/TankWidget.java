package com.sinesection.logisticraft.gui.widgets;

import javax.annotation.Nullable;

import org.lwjgl.opengl.GL11;

import com.sinesection.logisticraft.container.IContainerLiquidTanks;
import com.sinesection.logisticraft.fluid.StandardTank;
import com.sinesection.logisticraft.gui.tooltips.ToolTip;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;

/**
 * Slot for liquid tanks
 */
@SideOnly(Side.CLIENT)
public class TankWidget extends Widget {

	private int overlayTexX = 176;
	private int overlayTexY = 0;
	private int slot = 0;
	protected boolean drawOverlay = true;

	public TankWidget(WidgetManager manager, int xPos, int yPos, int slot) {
		super(manager, xPos, yPos);
		this.slot = slot;
		this.height = 58;
	}

	public TankWidget setOverlayOrigin(int x, int y) {
		overlayTexX = x;
		overlayTexY = y;
		return this;
	}

	@Nullable
	public IFluidTank getTank() {
		Container container = manager.gui.inventorySlots;
		if (container instanceof IContainerLiquidTanks) {
			return ((IContainerLiquidTanks) container).getTank(slot);
		}
		return null;
	}

	@Override
	public void draw(int startX, int startY) {
		GL11.glDisable(GL11.GL_BLEND);
		IFluidTank tank = getTank();
		if (tank == null || tank.getCapacity() <= 0) {
			return;
		}

		FluidStack contents = tank.getFluid();
		Minecraft minecraft = Minecraft.getMinecraft();
		TextureManager textureManager = minecraft.getTextureManager();
		if (contents != null && contents.amount > 0 && contents.getFluid() != null) {
			Fluid fluid = contents.getFluid();
			if (fluid != null) {
				TextureMap textureMapBlocks = minecraft.getTextureMapBlocks();
				IIcon fluidStill = fluid.getStillIcon();
				TextureAtlasSprite fluidStillSprite = null;
				if (fluidStill != null) {
					fluidStillSprite = textureMapBlocks.getTextureExtry(fluidStill.toString());
				}
				if (fluidStillSprite == null) {
					fluidStillSprite = textureMapBlocks.getAtlasSprite("missingno");
				}

				int fluidColor = fluid.getColor(contents);

				int scaledAmount = contents.amount * height / tank.getCapacity();
				if (contents.amount > 0 && scaledAmount < 1) {
					scaledAmount = 1;
				}
				if (scaledAmount > height) {
					scaledAmount = height;
				}

				textureManager.bindTexture(TextureMap.locationBlocksTexture);
				setGLColorFromInt(fluidColor);

				final int xTileCount = width / 16;
				final int xRemainder = width - xTileCount * 16;
				final int yTileCount = scaledAmount / 16;
				final int yRemainder = scaledAmount - yTileCount * 16;

				final int yStart = startY + height;

				for (int xTile = 0; xTile <= xTileCount; xTile++) {
					for (int yTile = 0; yTile <= yTileCount; yTile++) {
						int width = xTile == xTileCount ? xRemainder : 16;
						int height = yTile == yTileCount ? yRemainder : 16;
						int x = startX + xTile * 16;
						int y = yStart - (yTile + 1) * 16;
						if (width > 0 && height > 0) {
							int maskTop = 16 - height;
							int maskRight = 16 - width;

							drawFluidTexture(x + xPos, y + yPos, fluidStillSprite, maskTop, maskRight, 100);
						}
					}
				}
			}
		}

		if (drawOverlay) {
			GL11.glDisable(GL11.GL_DEPTH);
			textureManager.bindTexture(manager.gui.textureFile);
			manager.gui.drawTexturedModalRect(startX + xPos, startY + yPos, overlayTexX, overlayTexY, 16, 60);
			GL11.glEnable(GL11.GL_DEPTH);
		}

		GL11.glColor4f(1, 1, 1, 1);
	}

	@Override
	public ToolTip getToolTip(int mouseX, int mouseY) {
		IFluidTank tank = getTank();
		if (!(tank instanceof StandardTank)) {
			return null;
		}
		StandardTank standardTank = (StandardTank) tank;
		return standardTank.getToolTip();
	}

	private static void setGLColorFromInt(int color) {
		float red = (color >> 16 & 0xFF) / 255.0F;
		float green = (color >> 8 & 0xFF) / 255.0F;
		float blue = (color & 0xFF) / 255.0F;

		GL11.glColor4f(red, green, blue, 1.0F);
	}

	private static void drawFluidTexture(double xCoord, double yCoord, TextureAtlasSprite textureSprite, int maskTop, int maskRight, double zLevel) {
		double uMin = textureSprite.getMinU();
		double uMax = textureSprite.getMaxU();
		double vMin = textureSprite.getMinV();
		double vMax = textureSprite.getMaxV();
		uMax = uMax - maskRight / 16.0 * (uMax - uMin);
		vMax = vMax - maskTop / 16.0 * (vMax - vMin);

		Tessellator tessellator = Tessellator.instance;
		tessellator.addVertexWithUV(xCoord, yCoord + 16, zLevel, uMin, vMax);
		tessellator.addVertexWithUV(xCoord + 16 - maskRight, yCoord + 16, zLevel, uMax, vMax);
		tessellator.addVertexWithUV(xCoord + 16 - maskRight, yCoord + maskTop, zLevel, uMax, vMin);
		tessellator.addVertexWithUV(xCoord, yCoord + maskTop, zLevel, uMin, vMin);
		tessellator.draw();
	}

	@Override
	public void handleMouseClick(int mouseX, int mouseY, int mouseButton) {
		EntityPlayer player = manager.minecraft.thePlayer;
		ItemStack itemstack = player.inventory.getItemStack();
		if (itemstack == null) {
			return;
		}
	}
}