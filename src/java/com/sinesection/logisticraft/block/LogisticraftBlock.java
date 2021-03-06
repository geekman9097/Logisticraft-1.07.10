package com.sinesection.logisticraft.block;

import com.sinesection.logisticraft.Main;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;

public abstract class LogisticraftBlock extends Block {
	
	private final String registryName;
	
	public LogisticraftBlock(String universalName, Material mat) {
		this(universalName, universalName, universalName, mat);
	}

	protected LogisticraftBlock(String name, String textureName, String registryName, Material mat) {
		super(mat);
		this.setBlockName(name);
		this.setBlockTextureName(Main.MODID + ":" + textureName);
		this.registryName = registryName;
		this.setCreativeTab(Main.tabLogisticraftBlocks);
	}
	
	public Block setCreativeTab(CreativeTabs tab) {
		return super.setCreativeTab(tab);
	}
	
	
	public String getRegistryName() {
		return registryName;
	}

}
