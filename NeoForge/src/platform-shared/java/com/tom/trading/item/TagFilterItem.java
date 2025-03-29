package com.tom.trading.item;

import java.util.function.Consumer;

import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import com.tom.trading.Content;

public class TagFilterItem extends Item {

	public TagFilterItem(Item.Properties pr) {
		super(pr);
	}

	@Override
	public void appendHoverText(ItemStack pStack, TooltipContext tooltipContext, TooltipDisplay display,
			Consumer<Component> tooltip, TooltipFlag tooltipFlag) {
		TagKey<Item> tag = pStack.get(Content.TAG_COMPONENT.get());
		if(tag != null) {
			tooltip.accept(Component.literal(tag.location().toString()));
		}
	}
}
