package com.vivek.minecraft.item;

import java.util.function.Function;

import com.vivek.minecraft.LokViharTestMod;

import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;

public class ModItems {
	public static final Item RUBY = register("ruby", Item::new, new Item.Properties());

	public static Item register(String name, Function<Item.Properties, Item> itemFactory, Item.Properties properties) {
		// Items must know their own registry key before they are constructed.
		ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, LokViharTestMod.id(name));
		Item item = itemFactory.apply(properties.setId(itemKey));
		Registry.register(BuiltInRegistries.ITEM, itemKey, item);
		return item;
	}

	public static void initialize() {
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.INGREDIENTS)
				.register(output -> output.accept(RUBY));
	}
}
