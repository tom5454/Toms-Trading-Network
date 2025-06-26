package com.tom.trading.screen.widget;

import java.util.Locale;

import net.minecraft.util.StringRepresentable;

import com.mojang.serialization.Codec;

public enum IOMode implements StringRepresentable {
	OFF,
	INPUT,
	OUTPUT,
	IO;

	public static final Codec<IOMode> CODEC = StringRepresentable.<IOMode>fromEnum(() -> IOMode.values());

	@Override
	public String getSerializedName() {
		return name().toLowerCase(Locale.ROOT);
	}
}
