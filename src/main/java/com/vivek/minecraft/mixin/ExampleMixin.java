/*
 * ============================================================================
 *  ExampleMixin.java — a template mixin that patches the SERVER. Does nothing.
 * ============================================================================
 *
 *  WHAT IS A MIXIN?
 *  ----------------
 *  Fabric API offers events for common needs ("a block was broken", "server
 *  ticked"). When no event exists for what you want, you use a MIXIN: a class
 *  whose code gets merged into one of Minecraft's own classes at load time,
 *  by rewriting that class's bytecode before the JVM ever sees it.
 *
 *  The flow:
 *      1. Fabric Loader reads the mixin configs listed in fabric.mod.json.
 *      2. This file's config is lokvihar-test-mod.mixins.json (in
 *         src/main/resources). It lists "ExampleMixin" under "mixins".
 *      3. When the JVM is about to load net.minecraft.server.MinecraftServer,
 *         the Mixin library intercepts it, finds every mixin that targets it,
 *         and splices their injected code in.
 *      4. The patched class is what actually runs.
 *
 *  Your mixin class itself is never instantiated. Think of it as a "patch
 *  file written in Java".
 *
 *  WHY IS THIS FILE IN src/main AND NOT src/client?
 *  -------------------------------------------------
 *  MinecraftServer exists on both sides: a dedicated server runs it, and so
 *  does the singleplayer "integrated server" inside the client. So this mixin
 *  is "common" and goes in the main source set and the common mixin config.
 *  Compare ExampleClientMixin, which targets a client-only class.
 *
 *  THINGS THAT WILL BITE YOU
 *  -------------------------
 *  - A new mixin class does NOTHING until its simple name is added to the
 *    "mixins" array in lokvihar-test-mod.mixins.json.
 *  - The config sets "injectors": { "defaultRequire": 1 }. That means every
 *    @Inject must match at least one target, or the game CRASHES at startup.
 *    That's deliberate: a silent no-op after a Minecraft update would be
 *    much harder to debug than a loud crash.
 *  - Mixin classes must live in the package named in the config
 *    ("com.vivek.minecraft.mixin"). Never reference a mixin class from
 *    normal code; the package is reserved for mixins.
 *  - Method names here are Mojang's official names, because MC 26.x ships
 *    unobfuscated. Run ./gradlew genSources to browse the real Minecraft
 *    source and find methods to target.
 */
package com.vivek.minecraft.mixin;

// The class we are patching. It runs the game logic: worlds, ticking, players.
import net.minecraft.server.MinecraftServer;
// @Mixin: marks this class as a mixin and names its target class.
import org.spongepowered.asm.mixin.Mixin;
// @At: WHERE inside the target method to inject ("HEAD", "RETURN", "INVOKE", ...).
import org.spongepowered.asm.mixin.injection.At;
// @Inject: "insert a call to my method into the target method".
import org.spongepowered.asm.mixin.injection.Inject;
// The handle every @Inject handler receives. For void target methods it's
// CallbackInfo. For methods that return a value it's CallbackInfoReturnable<T>.
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Example mixin that injects an (empty) hook at the start of
 * {@code MinecraftServer.loadLevel()}.
 *
 * <p><b>Currently a no-op.</b> It's left over from the Fabric template as a
 * working reference for writing mixins. You can delete it, but remember to
 * also remove {@code "ExampleMixin"} from {@code lokvihar-test-mod.mixins.json},
 * or the game will crash looking for a class that no longer exists.
 *
 * <h2>What the injection does, conceptually</h2>
 * After patching, Minecraft's method behaves as if it had been written like:
 * <pre>{@code
 * // inside net.minecraft.server.MinecraftServer
 * protected void loadLevel() {
 *     ExampleMixin.init(new CallbackInfo(...));   // <- injected at HEAD
 *     // ... original vanilla code continues unchanged ...
 * }
 * }</pre>
 *
 * <h2>When does {@code loadLevel} run?</h2>
 * Once per server start, when the server loads (or creates) the worlds, i.e.
 * before any player can join. On a client, that means each time you open a
 * singleplayer world. Useful as a "the world is about to load" hook.
 *
 * @see com.vivek.minecraft.client.mixin.ExampleClientMixin the client-side twin
 */
// The argument is the TARGET class. A mixin class can target one or more classes.
@Mixin(MinecraftServer.class)
public class ExampleMixin {

	/**
	 * Injected handler that runs at the very start of
	 * {@code MinecraftServer.loadLevel()}.
	 *
	 * <h2>Annotation breakdown</h2>
	 * <ul>
	 *   <li>{@code method = "loadLevel"}: the target method's name. If there
	 *       were several overloads, you'd add its descriptor to choose one, e.g.
	 *       {@code "loadLevel()V"} (no arguments, returns void).</li>
	 *   <li>{@code at = @At("HEAD")}: inject before the first instruction.
	 *       Other common choices: {@code "RETURN"} (before every return),
	 *       {@code "TAIL"} (before the final return), or
	 *       {@code @At(value = "INVOKE", target = "...")} (around a specific
	 *       method call inside the target).</li>
	 * </ul>
	 *
	 * <h2>Handler method rules</h2>
	 * <ul>
	 *   <li>Return type must be {@code void}.</li>
	 *   <li>The last parameter must be {@link CallbackInfo}. If the target
	 *       method has parameters, you may declare them first, in order, to
	 *       receive their values.</li>
	 *   <li>The method name ({@code init}) is arbitrary. Mixin renames it
	 *       when merging to avoid clashes. Make it {@code private}.</li>
	 * </ul>
	 *
	 * <h2>Things you could do with {@code info}</h2>
	 * {@code info.cancel()} returns from the target method immediately, skipping
	 * the rest of the vanilla code. That only works if the annotation also sets
	 * {@code cancellable = true}. Cancelling {@code loadLevel} would stop worlds
	 * from loading, so don't.
	 *
	 * @param info callback handle provided by Mixin. Unused here.
	 */
	@Inject(at = @At("HEAD"), method = "loadLevel")
	private void init(CallbackInfo info) {
		// This code is injected into the start of MinecraftServer.loadLevel()V
		//
		// Intentionally empty. To see it work, try:
		//     com.vivek.minecraft.LokViharTestMod.LOGGER.info("Server is loading the level!");
		//
		// Inside a mixin, "this" really is the MinecraftServer instance at
		// runtime. To call its methods, cast through Object first:
		//     MinecraftServer self = (MinecraftServer) (Object) this;
	}
}
