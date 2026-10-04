/*
 * ============================================================================
 *  LokViharTestModClient.java — the CLIENT-ONLY entrypoint of the mod
 * ============================================================================
 *
 *  WHERE THIS FILE LIVES
 *  ---------------------
 *  src/client/java/...  -> the "client" source set (Loom's
 *  splitEnvironmentSourceSets(), see build.gradle).
 *
 *  - Code here CAN use net.minecraft.client.* (rendering, screens, keybinds,
 *    sounds, the Minecraft singleton...).
 *  - Code here CAN use anything from src/main (e.g. LokViharTestMod.LOGGER,
 *    ModItems.RUBY).
 *  - Code in src/main CANNOT use anything from here. The dependency only goes
 *    one way: client -> main.
 *
 *  WHY THE SPLIT EXISTS
 *  --------------------
 *  Minecraft ships as two programs: the client (with graphics) and the
 *  dedicated server (headless). The server jar doesn't contain any
 *  net.minecraft.client classes. If common code touched one, a dedicated
 *  server would crash with NoClassDefFoundError. Separate source sets turn
 *  that runtime crash into a compile error.
 *
 *  HOW IT GETS CALLED
 *  ------------------
 *  fabric.mod.json lists this class under "entrypoints" -> "client". Fabric
 *  Loader calls onInitializeClient() only on a physical client, and only
 *  AFTER every mod's "main" entrypoint (LokViharTestMod.onInitialize) has run.
 *  So by the time this runs, ModItems.RUBY is already registered.
 *
 *  "physical client" = the game launched with a window, even when you're
 *  playing singleplayer, which also runs an internal server.
 */
package com.vivek.minecraft.client;

// The Fabric interface for a "client" entrypoint. One method: onInitializeClient().
import net.fabricmc.api.ClientModInitializer;

/**
 * Client-side entrypoint. Runs once at startup on the physical client only.
 *
 * <p><b>Currently empty.</b> The mod has no client-only behavior yet. The
 * ruby's appearance is fully data-driven by the JSON files and texture under
 * {@code assets/lokvihar-test-mod/}, so it needs no Java code here.
 *
 * <h2>Typical things that go here later</h2>
 * <ul>
 *   <li>Key bindings ({@code KeyBindingHelper.registerKeyBinding(...)})</li>
 *   <li>Block/entity renderers and render layers (e.g. making a glass block
 *       translucent)</li>
 *   <li>HUD overlays drawn every frame</li>
 *   <li>Client-side network receivers (handling packets sent by the server)</li>
 *   <li>Custom screens / GUIs</li>
 *   <li>Client tick events ({@code ClientTickEvents.END_CLIENT_TICK})</li>
 * </ul>
 *
 * @see com.vivek.minecraft.LokViharTestMod the common entrypoint, which runs first
 */
public class LokViharTestModClient implements ClientModInitializer {

	/**
	 * Called once by Fabric Loader on the physical client, after all common
	 * ({@code main}) entrypoints have finished.
	 *
	 * <p>Like the common initializer, this runs before any world is loaded.
	 * {@code Minecraft.getInstance().level} and {@code .player} are still
	 * {@code null}. Register callbacks here; don't do world work.
	 */
	@Override
	public void onInitializeClient() {
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.
		//
		// Intentionally empty. Nothing client-specific has been needed yet.
	}
}
