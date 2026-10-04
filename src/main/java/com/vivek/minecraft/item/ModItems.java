/*
 * ============================================================================
 *  ModItems.java — registers every custom item this mod adds
 * ============================================================================
 *
 *  THE BIG PICTURE: WHAT IT TAKES TO ADD ONE ITEM
 *  ----------------------------------------------
 *  Adding an item to Minecraft takes Java code (this file) PLUS several
 *  resource files. For the "ruby" item they are:
 *
 *    Java (server + client):
 *      ModItems.RUBY                          -> creates and registers the Item
 *
 *    Client assets (src/main/resources/assets/lokvihar-test-mod/):
 *      items/ruby.json                        -> "item model definition": which
 *                                                model to draw for this item
 *      models/item/ruby.json                  -> the model: a flat 2D sprite
 *                                                built from one texture
 *      textures/item/ruby.png                 -> the 16x16 pixel art
 *      lang/en_us.json                        -> display name "Ruby"
 *
 *    Server data (src/main/resources/data/lokvihar-test-mod/):
 *      recipe/ruby.json                       -> crafting recipe
 *
 *  If any asset is missing, the game still runs, but you'll see a
 *  purple/black checkerboard (missing texture) or the raw translation key
 *  "item.lokvihar-test-mod.ruby" instead of "Ruby".
 *
 *  All of those files are tied together by one string: the item's ID,
 *  "lokvihar-test-mod:ruby". The "ruby" part comes from the name passed to
 *  register(...) below.
 *
 *  See docs/resources.md for a walkthrough of each JSON file.
 */
package com.vivek.minecraft.item;

// Java's standard "takes one argument, returns a value" functional interface.
// Here: Function<Item.Properties, Item>, i.e. "given properties, build an item".
// Lets callers pass a constructor reference such as Item::new or SwordItem::new.
import java.util.function.Function;

// For LokViharTestMod.id(...), which builds "lokvihar-test-mod:<path>" IDs.
import com.vivek.minecraft.LokViharTestMod;

// Fabric API event for adding entries to vanilla creative inventory tabs.
// Vanilla has no hook for this, which is why Fabric API provides one.
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;

// Registry: the generic "ID -> object" table type, plus Registry.register(...).
import net.minecraft.core.Registry;
// BuiltInRegistries: the actual registry *instances* (ITEM, BLOCK, ...).
import net.minecraft.core.registries.BuiltInRegistries;
// Registries: the registry *keys* (identifies WHICH registry), used to build
// ResourceKeys. Easy to mix up with BuiltInRegistries:
//     Registries.ITEM          -> a key naming the item registry
//     BuiltInRegistries.ITEM   -> the item registry itself
import net.minecraft.core.registries.Registries;
// ResourceKey<T>: a typed pair of (which registry, which ID). Stronger than a
// bare Identifier because it also says what kind of thing is being named.
import net.minecraft.resources.ResourceKey;
// The vanilla creative tab keys: BUILDING_BLOCKS, INGREDIENTS, COMBAT, ...
import net.minecraft.world.item.CreativeModeTabs;
// The base class of every item. Plain "Item" has no special behavior: it just
// sits in your inventory, stacks to 64 and can be used in recipes. That's all
// a ruby needs.
import net.minecraft.world.item.Item;

/**
 * Central place where all of this mod's {@link Item}s are created and
 * registered.
 *
 * <p>The pattern: every item is a {@code public static final} field
 * initialized by {@link #register}. Other code refers to the item through
 * the field, e.g. {@code ModItems.RUBY}, never by looking it up by string.
 *
 * <h2>Why static fields?</h2>
 * Minecraft expects exactly <b>one</b> {@code Item} instance per item type for
 * the whole game. An {@code ItemStack} ("5 rubies in slot 3") points at that
 * one shared instance. Static final fields are a simple way to guarantee
 * a single instance and give the rest of the code a typed handle to it.
 *
 * <h2>When does registration happen?</h2>
 * Static fields are initialized when the JVM first loads this class. That
 * happens when {@link com.vivek.minecraft.LokViharTestMod#onInitialize()}
 * calls {@link #initialize()}. This timing matters: registries freeze shortly
 * after mod initialization, and registering later throws an exception.
 *
 * <h2>How to add a new item</h2>
 * <pre>{@code
 * // 1. Add a field here:
 * public static final Item SAPPHIRE = register("sapphire", Item::new, new Item.Properties());
 *
 * // 2. Optionally add it to a creative tab in initialize():
 * output.accept(SAPPHIRE);
 * }</pre>
 * Then create {@code items/sapphire.json}, {@code models/item/sapphire.json},
 * {@code textures/item/sapphire.png} and a {@code lang} entry, mirroring the
 * ruby files.
 */
public class ModItems {

	/**
	 * The Ruby: a plain crafting-material item with no special behavior.
	 *
	 * <ul>
	 *   <li><b>ID:</b> {@code lokvihar-test-mod:ruby}</li>
	 *   <li><b>Class:</b> plain {@link Item}, built via the constructor
	 *       reference {@code Item::new}</li>
	 *   <li><b>Properties:</b> defaults: stacks to 64, not fire resistant,
	 *       common rarity, no durability, not food</li>
	 *   <li><b>Obtained by:</b> crafting 8 iron nuggets around 1 red dye (see
	 *       {@code data/lokvihar-test-mod/recipe/ruby.json}), or from the
	 *       creative Ingredients tab</li>
	 *   <li><b>Give command:</b> {@code /give @s lokvihar-test-mod:ruby}</li>
	 * </ul>
	 *
	 * <p>To customize it, chain calls on the properties, e.g.
	 * {@code new Item.Properties().stacksTo(16).fireResistant()}.
	 */
	public static final Item RUBY = register("ruby", Item::new, new Item.Properties());

	/**
	 * Creates an item and registers it in Minecraft's item registry under
	 * {@code lokvihar-test-mod:<name>}.
	 *
	 * <h2>Step by step</h2>
	 * <ol>
	 *   <li><b>Build the key.</b> A {@link ResourceKey} says "an entry in the
	 *       ITEM registry, with ID {@code lokvihar-test-mod:<name>}".</li>
	 *   <li><b>Attach the key to the properties, then construct.</b> Modern
	 *       Minecraft requires an item to know its own ID <em>while it is being
	 *       constructed</em>: the constructor derives things like the default
	 *       translation key and model from it. {@code properties.setId(key)}
	 *       does that. Forgetting it crashes with an "Item id not set"
	 *       error.</li>
	 *   <li><b>Register.</b> {@link Registry#register} puts the item into the
	 *       global table so the game (commands, recipes, networking, saves)
	 *       can find it by ID.</li>
	 * </ol>
	 *
	 * <h2>Why take a factory instead of an Item?</h2>
	 * Because of step 2, the caller can't construct the item themselves. It has
	 * to be built <em>after</em> the ID is attached. So the caller passes a
	 * recipe for building it ({@code itemFactory}) and this method calls it at
	 * the right moment. Any item subclass works as long as it has a constructor
	 * taking {@code Item.Properties}, e.g. {@code SwordItem::new} or your own
	 * {@code RubyWandItem::new}.
	 *
	 * @param name        the path part of the ID, e.g. {@code "ruby"}. Must be
	 *                    lowercase {@code a-z0-9_-./}. Must also match the
	 *                    asset file names ({@code items/<name>.json}, etc.).
	 * @param itemFactory builds the item from finished properties, usually a
	 *                    constructor reference like {@code Item::new}
	 * @param properties  the item's settings (stack size, durability, rarity, ...).
	 *                    The ID is added to it here, so pass a fresh instance.
	 * @return the registered item. Store it in a static final field.
	 */
	public static Item register(String name, Function<Item.Properties, Item> itemFactory, Item.Properties properties) {
		// Items must know their own registry key before they are constructed.
		ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, LokViharTestMod.id(name));

		// setId(...) mutates and returns the same Properties object, so it can
		// be passed straight into the factory.
		Item item = itemFactory.apply(properties.setId(itemKey));

		// Put it into the global item registry. After this line the item
		// "exists" in the game: /give works, recipes can reference it, and it
		// is synced to clients by ID.
		Registry.register(BuiltInRegistries.ITEM, itemKey, item);

		return item;
	}

	/**
	 * Called once from {@link com.vivek.minecraft.LokViharTestMod#onInitialize()}.
	 *
	 * <p>This method has two jobs:
	 * <ol>
	 *   <li><b>Force this class to load</b> so the static item fields above
	 *       are created and registered now, while registries are still open.
	 *       Even if the method body were empty, calling it would do this.</li>
	 *   <li><b>Put the items in creative tabs.</b> Registering an item does
	 *       <em>not</em> make it show up in the creative inventory. That's a
	 *       separate step.</li>
	 * </ol>
	 *
	 * <h2>How the creative-tab hook works</h2>
	 * {@code CreativeModeTabEvents.modifyOutputEvent(tab)} returns an event for
	 * one specific vanilla tab. The lambda we register is called whenever the
	 * game (re)builds that tab's contents, and {@code output.accept(item)}
	 * appends the item to the end of the tab. Here the ruby goes in the
	 * <b>Ingredients</b> tab, next to diamonds, emeralds and other materials.
	 *
	 * <p>The lambda runs later, on the client, every time the tab is rebuilt,
	 * not right now. It's just a callback being stored.
	 */
	public static void initialize() {
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.INGREDIENTS)
				// "output" collects the items shown in the tab. accept(...) adds
				// one entry, in order.
				.register(output -> output.accept(RUBY));
	}
}
