/*
 * ============================================================================
 *  LokViharTestMod.java — the COMMON (server + client) entrypoint of the mod
 * ============================================================================
 *
 *  WHERE THIS FILE LIVES
 *  ---------------------
 *  src/main/java/...  -> the "main" source set. Loom is configured with
 *  splitEnvironmentSourceSets() (see build.gradle), which means:
 *
 *      src/main    : code that is safe on BOTH a dedicated server and a client.
 *      src/client  : code that only exists on the client (rendering, input, ...).
 *
 *  Code in src/main must NEVER import anything from net.minecraft.client.*,
 *  because a dedicated server jar does not contain those classes. The compiler
 *  enforces this: the main source set simply cannot see client classes.
 *
 *  HOW THIS CLASS GETS CALLED
 *  --------------------------
 *  Fabric Loader does not scan for classes. Instead, fabric.mod.json lists
 *  "entrypoints":
 *
 *      "entrypoints": {
 *          "main":   [ "com.vivek.minecraft.LokViharTestMod" ],
 *          "client": [ "com.vivek.minecraft.client.LokViharTestModClient" ]
 *      }
 *
 *  At startup, Fabric Loader:
 *      1. discovers every mod jar and reads its fabric.mod.json,
 *      2. applies all mixins (bytecode patches) to Minecraft's classes,
 *      3. instantiates each "main" entrypoint with its no-arg constructor
 *         and calls onInitialize() (on both physical client and server),
 *      4. on a physical client only, then does the same for "client"
 *         entrypoints and calls onInitializeClient().
 *
 *  So if you rename or move this class, you MUST update fabric.mod.json too,
 *  otherwise the game crashes at launch with a "class not found" error.
 *
 *  WHAT THIS CLASS PROVIDES TO THE REST OF THE MOD
 *  -----------------------------------------------
 *      MOD_ID  : the mod's namespace string, used everywhere.
 *      LOGGER  : a shared SLF4J logger.
 *      id(...) : a helper that builds namespaced Identifiers.
 */
package com.vivek.minecraft;

// Our own item registry class. Its static fields (like RUBY) are created
// the first time the class is touched, which happens inside onInitialize().
import com.vivek.minecraft.item.ModItems;

// The Fabric interface for a "main" entrypoint. Just one method: onInitialize().
import net.fabricmc.api.ModInitializer;

// Minecraft's namespaced ID type ("namespace:path"), e.g. "minecraft:diamond"
// or "lokvihar-test-mod:ruby". Older versions and Yarn mappings called this
// "ResourceLocation" or "Identifier" in a different package. Since MC 26.x is
// unobfuscated, we use Mojang's real name: net.minecraft.resources.Identifier.
import net.minecraft.resources.Identifier;

// SLF4J is the logging facade bundled with Minecraft. Output goes to both
// the console and logs/latest.log in the run directory.
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The common entrypoint of the mod, instantiated by Fabric Loader on both
 * the physical client and the dedicated server.
 *
 * <p>The only thing that <em>must</em> exist here is {@link #onInitialize()}.
 * Everything else is shared utility that other classes import.
 *
 * <h2>Lifecycle summary</h2>
 * <pre>
 *   JVM starts
 *     └─ Fabric Loader boots, reads every fabric.mod.json
 *         └─ mixins are applied to Minecraft classes
 *             └─ new LokViharTestMod().onInitialize()        ← this class
 *                 └─ (client only) new LokViharTestModClient().onInitializeClient()
 *                     └─ title screen / server starts accepting players
 * </pre>
 *
 * @see com.vivek.minecraft.client.LokViharTestModClient the client-only counterpart
 * @see ModItems where the mod's items are registered
 */
public class LokViharTestMod implements ModInitializer {

	/**
	 * The mod's unique ID, also used as its <b>namespace</b>.
	 *
	 * <p>This string has to match in several places, or things silently break:
	 * <ul>
	 *   <li>{@code "id"} in {@code fabric.mod.json}</li>
	 *   <li>{@code rootProject.name} in {@code settings.gradle} (convention only)</li>
	 *   <li>the folder names {@code assets/lokvihar-test-mod/} and
	 *       {@code data/lokvihar-test-mod/} under {@code src/main/resources}</li>
	 *   <li>the prefix of every translation key, e.g.
	 *       {@code item.lokvihar-test-mod.ruby} in {@code lang/en_us.json}</li>
	 *   <li>the mixin config file names</li>
	 * </ul>
	 *
	 * <p>Rules for a valid namespace: lowercase {@code a-z}, digits, {@code _},
	 * {@code -} and {@code .} only. No uppercase, no spaces.
	 */
	public static final String MOD_ID = "lokvihar-test-mod";

	/**
	 * The mod's shared logger. Any class can call
	 * {@code LokViharTestMod.LOGGER.info("...")}.
	 *
	 * <p>Naming the logger after {@link #MOD_ID} makes every log line start with
	 * {@code [lokvihar-test-mod]}, so it's obvious which mod printed what when
	 * dozens of mods share one log file.
	 *
	 * <p>Use the levels on purpose:
	 * <ul>
	 *   <li>{@code LOGGER.debug(...)}: noisy diagnostics, hidden by default</li>
	 *   <li>{@code LOGGER.info(...)}: normal milestones ("loaded 3 items")</li>
	 *   <li>{@code LOGGER.warn(...)}: something odd that we recovered from</li>
	 *   <li>{@code LOGGER.error(...)}: something actually failed</li>
	 * </ul>
	 * SLF4J supports {@code {}} placeholders, e.g.
	 * {@code LOGGER.info("Registered {} items", count)}. Prefer those over string
	 * concatenation; the message is then only built if the level is enabled.
	 */
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	/**
	 * Called once by Fabric Loader, very early during game startup, on both
	 * client and server.
	 *
	 * <h2>What belongs here</h2>
	 * <ul>
	 *   <li><b>Registration</b> of items, blocks, entities, sounds, etc.
	 *       Minecraft's registries are "frozen" shortly after mod
	 *       initialization, so registering anything later throws an exception.</li>
	 *   <li><b>Subscribing to Fabric API events</b> (server tick, player join,
	 *       block break, ...).</li>
	 *   <li>Networking payload type registration that both sides need.</li>
	 * </ul>
	 *
	 * <h2>What does NOT belong here</h2>
	 * <ul>
	 *   <li>Anything touching a world, player or server instance. None of
	 *       those exist yet. Use an event instead, e.g.
	 *       {@code ServerLifecycleEvents.SERVER_STARTED}.</li>
	 *   <li>Reading resource/data packs (recipes, tags, loot tables). They are
	 *       loaded later, when a world is opened.</li>
	 *   <li>Rendering or anything from {@code net.minecraft.client}. That goes
	 *       in {@code LokViharTestModClient}.</li>
	 * </ul>
	 */
	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		// A simple "the mod loaded" marker. Search the log for this line to
		// confirm the entrypoint actually ran.
		LOGGER.info("Hello Fabric world!");

		// Register all of the mod's items.
		//
		// Subtle Java detail: the items are created in ModItems' *static field
		// initializers*, and those only run when the JVM first loads the class.
		// Calling ModItems.initialize() forces the class to load right here, at
		// a moment when registration is still allowed. If nothing ever
		// referenced ModItems, the RUBY item would never be registered at all.
		ModItems.initialize();
	}

	/**
	 * Builds an {@link Identifier} in this mod's namespace.
	 *
	 * <p>Example: {@code id("ruby")} returns {@code lokvihar-test-mod:ruby}.
	 *
	 * <p>Identifiers name almost everything in Minecraft: registry entries
	 * (items, blocks), resources (textures, models, sounds), data (recipes,
	 * loot tables, tags), network channels, and more. Always use your own
	 * namespace for your own content so you never collide with vanilla
	 * ({@code minecraft:}) or other mods.
	 *
	 * <p>{@code Identifier.fromNamespaceAndPath} is the Mojang-mapped factory.
	 * If you're following an older tutorial that says {@code new Identifier(...)}
	 * or {@code Identifier.of(...)}, that's a different version/mapping. Use
	 * this helper instead.
	 *
	 * @param path the path part of the ID. Lowercase {@code a-z0-9/._-} only,
	 *             e.g. {@code "ruby"} or {@code "tools/ruby_pickaxe"}
	 * @return a new identifier {@code lokvihar-test-mod:<path>}
	 * @throws RuntimeException (Minecraft's identifier exception) if {@code path}
	 *         contains illegal characters, such as uppercase letters or spaces
	 */
	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
