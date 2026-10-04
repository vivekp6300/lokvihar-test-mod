/*
 * ============================================================================
 *  ExampleClientMixin.java — a template mixin that patches the CLIENT. Does nothing.
 * ============================================================================
 *
 *  This is the client-side twin of com.vivek.minecraft.mixin.ExampleMixin.
 *  Read that file first for a full explanation of what mixins are and how
 *  @Inject / @At work. This header only covers what's different here.
 *
 *  WHAT'S DIFFERENT ABOUT A CLIENT MIXIN
 *  -------------------------------------
 *  - It targets a class that ONLY exists on the client
 *    (net.minecraft.client.Minecraft). On a dedicated server that class isn't
 *    there, so this mixin must never be applied there.
 *  - That's guaranteed by registering it in a SEPARATE config,
 *    lokvihar-test-mod.client.mixins.json (in src/client/resources), where:
 *        * it's listed under the "client" array, not "mixins", and
 *        * fabric.mod.json loads that whole config with
 *          "environment": "client".
 *  - It lives in package com.vivek.minecraft.client.mixin, which is the
 *    "package" declared in the client mixin config. A mixin must sit in its
 *    config's package.
 *
 *  Same rule as the server mixin: "defaultRequire": 1 means that if the
 *  target method "run" ever stops existing (e.g. after a Minecraft update),
 *  the game crashes at startup instead of silently skipping the injection.
 */
package com.vivek.minecraft.client.mixin;

// The client's central singleton: owns the window, the render loop, the local
// player, the currently open screen, options, etc. In Yarn mappings and older
// tutorials this was called "MinecraftClient". MC 26.x uses Mojang's real name.
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Example mixin that injects an (empty) hook at the start of
 * {@code Minecraft.run()}.
 *
 * <p><b>Currently a no-op.</b> It's left over from the Fabric template as a
 * reference. To delete it, also remove {@code "ExampleClientMixin"} from the
 * {@code "client"} list in {@code lokvihar-test-mod.client.mixins.json}.
 *
 * <h2>What is {@code Minecraft.run()}?</h2>
 * The client's main game loop. It's entered once, right after the window is
 * created, and loops (tick, render, repeat) until the game closes. Injecting
 * at {@code HEAD} therefore runs exactly once, just before the first frame.
 *
 * <p>After patching, it behaves roughly like:
 * <pre>{@code
 * // inside net.minecraft.client.Minecraft
 * public void run() {
 *     ExampleClientMixin.init(new CallbackInfo(...));   // <- injected
 *     // ... vanilla game loop: while (running) { tick(); render(); } ...
 * }
 * }</pre>
 *
 * @see com.vivek.minecraft.mixin.ExampleMixin the server-side twin, with the
 *      detailed mixin explanation
 */
@Mixin(Minecraft.class)
public class ExampleClientMixin {

	/**
	 * Injected handler that runs once, at the start of {@code Minecraft.run()}.
	 *
	 * <p>{@code run} is a {@code void} method with no parameters, so the only
	 * parameter here is the {@link CallbackInfo}.
	 *
	 * @param info callback handle provided by Mixin. Unused here. (Calling
	 *             {@code info.cancel()} would need {@code cancellable = true}
	 *             and would skip the whole game loop, closing the game
	 *             immediately.)
	 */
	@Inject(at = @At("HEAD"), method = "run")
	private void init(CallbackInfo info) {
		// This code is injected into the start of Minecraft.run()V
		//
		// Intentionally empty. To see it work, try:
		//     com.vivek.minecraft.LokViharTestMod.LOGGER.info("Client game loop starting!");
	}
}
