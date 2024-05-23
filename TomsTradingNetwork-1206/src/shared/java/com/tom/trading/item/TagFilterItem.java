package com.tom.trading.item;

import java.util.List;

import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import com.tom.trading.Content;

public class TagFilterItem extends Item {

	public TagFilterItem() {
		super(new Item.Properties());
	}

	@Override
	public void appendHoverText(ItemStack pStack, TooltipContext tooltipContext, List<Component> pTooltipComponents,
			TooltipFlag tooltipFlag) {
		TagKey<Item> tag = pStack.get(Content.TAG_COMPONENT.get());
		if(tag != null) {
			pTooltipComponents.add(Component.literal(tag.location().toString()));
		}
	}
}
