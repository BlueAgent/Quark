package vazkii.quark.automation.feature;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.oredict.OreDictionary;
import vazkii.arl.recipe.RecipeHandler;
import vazkii.arl.util.ProxyRegistry;
import vazkii.quark.automation.block.BlockColorSlime;
import vazkii.quark.base.module.Feature;

public class ColorSlime extends Feature {

	public static Block color_slime;
	
	boolean renameVanillaSlime;
	
	@Override
	public void setupConfig() {
		renameVanillaSlime = loadPropBool("Rename Vanilla Slime", "Set to false to not rename vanilla slime to Green Slime Block", true);
	}
	
	@Override
	public void preInit(FMLPreInitializationEvent event) {
		color_slime = new BlockColorSlime();
		
		String[] dyes = {
				"dyeRed", "dyeBlue", "dyeCyan", "dyeMagenta", "dyeYellow"
		};
		
		for(int i = 0; i < 5; i++)
			RecipeHandler.addShapelessOreDictRecipe(ProxyRegistry.newStack(color_slime, 1, i), "blockSlime", dyes[i]);
		RecipeHandler.addShapelessOreDictRecipe(new ItemStack(Blocks.SLIME_BLOCK), "blockSlime", "dyeGreen");
		
		addOreDict("blockSlime", ProxyRegistry.newStack(color_slime, 1, OreDictionary.WILDCARD_VALUE));
	}
	
	@Override
	public void postPreInit() {
		if(renameVanillaSlime)
			Blocks.SLIME_BLOCK.setTranslationKey("green_slime_block");
	}
	
	@Override
	public boolean requiresMinecraftRestartToEnable() {
		return true;
	}
	
}