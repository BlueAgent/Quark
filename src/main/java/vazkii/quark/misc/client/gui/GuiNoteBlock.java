package vazkii.quark.misc.client.gui;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityNote;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import scala.actors.threadpool.Arrays;
import vazkii.arl.network.NetworkHandler;
import vazkii.quark.base.network.MessageRegister;
import vazkii.quark.base.network.message.MessageTuneNoteBlock;

public class GuiNoteBlock extends GuiScreen {

	private static final SoundEvent[] INSTRUMENTS = new SoundEvent[] {
			SoundEvents.BLOCK_NOTE_HARP, SoundEvents.BLOCK_NOTE_BASEDRUM, SoundEvents.BLOCK_NOTE_SNARE, SoundEvents.BLOCK_NOTE_HAT, SoundEvents.BLOCK_NOTE_BASS
	};

	private static ResourceLocation noteblockResource = new ResourceLocation("quark", "textures/misc/noteblock.png");
	private static final int TEXTURE_WIDTH = 512;
	private static final int TEXTURE_HEIGHT = 256;

	private static final String[] KEY_NAMES = new String[] {
			"F#", "G", "G#", "A", "A#", 
			"B", "C", "C#", "D", "D#", 
			"E", "F", "F#", "G", "G#", 
			"A", "A#", "B", "C", "C#", 
			"D", "D#", "E", "F", "F#"
	};
	
	private List<Key> whiteKeys = new ArrayList();
	private List<Key> blackKeys = new ArrayList();

	Key hoveredKey = null;
	boolean hoversNoteBlock = false;

	TileEntityNote noteBlock;
	CoordinateHolder coords = new CoordinateHolder();

	public GuiNoteBlock(TileEntityNote noteBlock) {
		this.noteBlock = noteBlock;
	}

	@Override
	public void initGui() {
		whiteKeys.clear();
		blackKeys.clear();

		blackKeys.add(new BlackKey(0, 0));
		blackKeys.add(new BlackKey(1, 2));
		blackKeys.add(new BlackKey(2, 4));
		blackKeys.add(new BlackKey(4, 7));
		blackKeys.add(new BlackKey(5, 9));
		blackKeys.add(new BlackKey(7, 12));
		blackKeys.add(new BlackKey(8, 14));
		blackKeys.add(new BlackKey(9, 16));
		blackKeys.add(new BlackKey(11, 19));
		blackKeys.add(new BlackKey(12, 21));
		blackKeys.add(new BlackKey(14, 24));

		whiteKeys.add(new WhiteKey(0, 1, 1));
		whiteKeys.add(new WhiteKey(1, 3, 1));
		whiteKeys.add(new WhiteKey(2, 5, 2));
		whiteKeys.add(new WhiteKey(3, 6, 0));
		whiteKeys.add(new WhiteKey(4, 8, 1));
		whiteKeys.add(new WhiteKey(5, 10, 2));
		whiteKeys.add(new WhiteKey(6, 11, 0));
		whiteKeys.add(new WhiteKey(7, 13, 1));
		whiteKeys.add(new WhiteKey(8, 15, 1));
		whiteKeys.add(new WhiteKey(9, 17, 2));
		whiteKeys.add(new WhiteKey(10, 18, 0));
		whiteKeys.add(new WhiteKey(11, 20, 1));
		whiteKeys.add(new WhiteKey(12, 22, 2));
		whiteKeys.add(new WhiteKey(13, 23, 0));
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		if(noteBlock == null || noteBlock.getWorld().getTileEntity(noteBlock.getPos()) != noteBlock) {
			mc.displayGuiScreen(null);
			return;
		}
		
		int panelWidth = 320;
		int panelHeight = 102;
		int left = width / 2 - panelWidth / 2;
		int top = height / 2 - panelHeight / 2;

		GlStateManager.pushMatrix();
		GlStateManager.translate(left, top, 0);

		mc.renderEngine.bindTexture(noteblockResource);
		drawModalRectWithCustomSizedTexture(0, 0, 0, 0, panelWidth, panelHeight, TEXTURE_WIDTH, TEXTURE_HEIGHT);

		coords.baseX = 46;
		coords.baseY = 8;
		coords.mouseX = mouseX - left;
		coords.mouseY = mouseY - top;

		hoveredKey = null;
		hoversNoteBlock = coords.mouseX >= 9 && coords.mouseX < 40 && coords.mouseY >= 9 && coords.mouseY < 40;
		
		for(Key k : blackKeys) {
			mc.renderEngine.bindTexture(noteblockResource);
			if(k.renderKey(mc, true, coords))
				hoveredKey = k;
		}

		coords.baseX += 7;
		for(Key k : whiteKeys) {
			mc.renderEngine.bindTexture(noteblockResource);
			if(k.renderKey(mc, hoveredKey == null, coords))
				hoveredKey = k;
		}

		RenderHelper.enableGUIStandardItemLighting();
		float scale = 1.8F;
		GlStateManager.translate(9, 9, 0);
		GlStateManager.scale(scale, scale, scale);
		mc.getRenderItem().renderItemAndEffectIntoGUI(new ItemStack(Blocks.NOTEBLOCK), 0, 0);

		GlStateManager.popMatrix();

		mc.fontRendererObj.drawString(""+noteBlock.note, 0, 0, 0xFFFFFF);
		if(hoversNoteBlock)
			vazkii.arl.util.RenderHelper.renderTooltip(mouseX, mouseY, Arrays.asList(new String[] { I18n.format("quarkmisc.incrementNote") }));
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		super.mouseClicked(mouseX, mouseY, mouseButton);
		if(mouseButton == 0)
			if(hoveredKey != null)
				NetworkHandler.INSTANCE.sendToServer(new MessageTuneNoteBlock(noteBlock, false, hoveredKey.clicks));
			else if(hoversNoteBlock)
				NetworkHandler.INSTANCE.sendToServer(new MessageTuneNoteBlock(noteBlock, true, (byte) 0));
	}

	private int getNote() {
		Material material = noteBlock.getWorld().getBlockState(noteBlock.getPos().down()).getMaterial();

		if(material == Material.ROCK)
			return 1;

		if(material == Material.SAND)
			return 2;

		if (material == Material.GLASS)
			return 3;

		if (material == Material.WOOD)
			return 4;
		
		return 0;
	}
	
	@Override
	public boolean doesGuiPauseGame() {
		return false;
	}

	private abstract static class Key {

		public final int x, y, w, h;
		public final byte clicks;
		public final String desc;

		public Key(int x, int y, int w, int h, int clicks) {
			this.x = x;
			this.y = y;
			this.w = w;
			this.h = h;
			this.clicks = (byte) clicks;
			this.desc = KEY_NAMES[clicks];
		}

		final boolean isHovered(CoordinateHolder c) {
			int x = c.baseX + this.x;
			int y = c.baseY + this.y;
			return c.mouseX >= x && c.mouseY >= y && c.mouseX < x + w && c.mouseY < y + h;
		}

		final void renderNote(Minecraft mc, CoordinateHolder c) {
			int x = c.baseX + this.x;
			int y = c.baseY + this.y;
			int sw = mc.fontRendererObj.getStringWidth(desc);

			int color = Color.HSBtoRGB((float) (24 - clicks) / 24F - 0.6F, 1F, 1F);
			mc.fontRendererObj.drawString(desc, x + w / 2 - sw / 2, y + h - 24, color);

			String s = Integer.toString(clicks);
			sw = mc.fontRendererObj.getStringWidth(s);
			mc.fontRendererObj.drawString(s, x + w / 2 - sw / 2, y + h - 13, this instanceof BlackKey ? 0xCCCCCC : 0x333333);

			GlStateManager.color(1F, 1F, 1F);
		}

		abstract boolean renderKey(Minecraft mc, boolean canHover, CoordinateHolder c);
	}

	private static class WhiteKey extends Key {

		private final int type;

		public WhiteKey(int position, int clicks, int type) {
			super(position * 18, 0, 18, 86, clicks);
			this.type = type;
		}

		@Override
		boolean renderKey(Minecraft mc, boolean canHover, CoordinateHolder c) {
			boolean hovered = canHover && isHovered(c);
			int u = 320 + w * type;
			int v = hovered ? h : 0;
			drawModalRectWithCustomSizedTexture(c.baseX + x, c.baseY + y, u, v, w, h, TEXTURE_WIDTH, TEXTURE_HEIGHT);

			if(hovered)
				renderNote(mc, c);

			return hovered;
		}

	}

	private static class BlackKey extends Key {

		public BlackKey(int position, int clicks) {
			super(position * 18, 0, 14, 45, clicks);
		}

		@Override
		boolean renderKey(Minecraft mc, boolean canHover, CoordinateHolder c) {
			boolean hovered = canHover && isHovered(c);
			int u = 374;
			int v = hovered ? h : 0;
			drawModalRectWithCustomSizedTexture(c.baseX + x, c.baseY + y, u, v, w, h, TEXTURE_WIDTH, TEXTURE_HEIGHT);

			if(hovered)
				renderNote(mc, c);

			return hovered;
		}

	}

	private static class CoordinateHolder {
		int baseX, baseY, mouseX, mouseY;
	}


}
