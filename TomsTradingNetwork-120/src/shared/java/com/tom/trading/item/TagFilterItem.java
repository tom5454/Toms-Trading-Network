package com.tom.trading.item;

import java.util.List;

import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public class TagFilterItem extends Item {

	public TagFilterItem() {
		super(new Item.Properties().stacksTo(1));
	}

	@Override
	public void appendHoverText(ItemStack pStack, Level pLevel, List<Component> pTooltipComponents,
			TooltipFlag pIsAdvanced) {
		if(pStack.hasTag() && pStack.getTag().contains("tag", Tag.TAG_STRING)) {
			pTooltipComponents.add(Component.literal(pStack.getTag().getString("tag")));
		}
	}
}
