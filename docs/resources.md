# Resource files explained

JSON can't hold comments, and Minecraft and Fabric Loader parse these files strictly, so their documentation lives here instead. Each section matches one file under `src/`.

Two kinds of resources ship in the jar:

| Folder | Read by | Loaded when | Reloadable in game |
|---|---|---|---|
| `assets/<modid>/` | the **client** only (resource pack) | at startup, and again on F3+T | yes, F3+T |
| `data/<modid>/` | the **server** (data pack); singleplayer counts too | each time a world is opened | yes, `/reload` |

`<modid>` must be exactly `lokvihar-test-mod`, which is `LokViharTestMod.MOD_ID`. A typo in a folder name fails silently: the file just never loads.

---

## How the Ruby's files connect

```
ModItems.RUBY  (Java, ID "lokvihar-test-mod:ruby")
   │
   ├─ assets/lokvihar-test-mod/items/ruby.json          "which model to draw?"
   │     └─ model: lokvihar-test-mod:item/ruby
   │           └─ assets/lokvihar-test-mod/models/item/ruby.json     "how to draw it"
   │                 └─ layer0: lokvihar-test-mod:item/ruby
   │                       └─ assets/lokvihar-test-mod/textures/item/ruby.png
   │
   ├─ assets/lokvihar-test-mod/lang/en_us.json           "item.lokvihar-test-mod.ruby" → "Ruby"
   │
   └─ data/lokvihar-test-mod/recipe/ruby.json            how to craft it
```

**Reading resource IDs.** `lokvihar-test-mod:item/ruby` means namespace `lokvihar-test-mod`, path `item/ruby`. Which folder and file extension get added depends on what the ID refers to:

- model ID → `assets/<ns>/models/<path>.json`
- texture ID → `assets/<ns>/textures/<path>.png`

---

## `src/main/resources/fabric.mod.json`

The mod's manifest. Fabric Loader reads it before any code runs. Without it the jar isn't a mod at all.

| Field | Value | Meaning |
|---|---|---|
| `schemaVersion` | `1` | Version of this file format. Always 1. |
| `id` | `lokvihar-test-mod` | Unique mod ID. Must equal `MOD_ID` in Java and the asset/data folder names. |
| `version` | `${version}` | A placeholder. Gradle's `processResources` replaces it with `version` from `gradle.properties` (`1.0.0`). |
| `name` / `description` | | Shown in mod menus (e.g. Mod Menu). |
| `authors` | `["Vivek Pandey"]` | Credits. |
| `contact` | homepage / sources | **Still the Fabric template's URLs.** Point them at your own repo. |
| `license` | `CC0-1.0` | SPDX license ID. Should match the `LICENSE` file. |
| `icon` | `assets/lokvihar-test-mod/icon.png` | Path *inside the jar*. Square PNG. |
| `environment` | `*` | Loads on client and server. `client` or `server` would restrict it. |
| `entrypoints.main` | `com.vivek.minecraft.LokViharTestMod` | Class whose `onInitialize()` runs on both sides. |
| `entrypoints.client` | `com.vivek.minecraft.client.LokViharTestModClient` | Class whose `onInitializeClient()` runs on the client only. |
| `mixins` | two configs | The common config always loads. The client config is wrapped in `{ "config": ..., "environment": "client" }`, so dedicated servers skip it. |
| `depends` | | Hard requirements. If any is unmet, Fabric refuses to launch and shows an error screen. |

`depends` details:

- `fabricloader: >=0.19.5`: this loader version or newer.
- `minecraft: ~26.1.2`: any `26.1.x` that is at least `26.1.2`, but not `26.2`.
- `java: >=25`: the JVM must be Java 25+.
- `fabric-api: *`: any version of Fabric API, as long as it's installed.

Keep these in sync with `gradle.properties` whenever you upgrade.

---

## `src/main/resources/lokvihar-test-mod.mixins.json`

The **common** mixin config, for patches that apply on both client and server.

| Field | Value | Meaning |
|---|---|---|
| `required` | `true` | If this config fails to apply, crash instead of continuing. |
| `package` | `com.vivek.minecraft.mixin` | Every mixin class listed below lives in this package. The whole package is reserved for mixins, so don't put normal classes there. |
| `compatibilityLevel` | `JAVA_25` | Highest Java bytecode level the mixins use. |
| `mixins` | `["ExampleMixin"]` | Simple class names, relative to `package`, applied on **both** sides. A mixin missing from this list never runs. |
| `injectors.defaultRequire` | `1` | Each `@Inject` must match at least 1 target, or the game crashes at load. This catches broken mixins after a Minecraft update. |
| `overwrites.requireAnnotations` | `true` | `@Overwrite` methods must be explicitly annotated. A safety check. |

## `src/client/resources/lokvihar-test-mod.client.mixins.json`

The **client-only** mixin config. Same fields as above, except:

- `package` is `com.vivek.minecraft.client.mixin`.
- Mixins are listed under `"client"` instead of `"mixins"`. Mixin only applies the `client` list on a physical client.
- `fabric.mod.json` also gives this whole config `"environment": "client"`, so a dedicated server never even reads it. That's two layers of protection, because its mixins target classes (like `net.minecraft.client.Minecraft`) that don't exist on a server.

---

## `assets/lokvihar-test-mod/items/ruby.json`: item model definition

```json
{ "model": { "type": "minecraft:model", "model": "lokvihar-test-mod:item/ruby" } }
```

Since Minecraft 1.21.4, every item needs a file in `assets/<ns>/items/` named after its ID. It tells the client *which model to render* for the item, and it can switch models based on conditions (damage, held in hand vs. in GUI, custom data, ...).

- `type: minecraft:model`: the simplest case, "always use this one model". Other types such as `minecraft:condition`, `minecraft:select` and `minecraft:range_dispatch` choose between several models.
- `model`: the model ID. It resolves to `models/item/ruby.json` below.

If this file is missing, the item renders as the purple/black missing-model cube.

## `assets/lokvihar-test-mod/models/item/ruby.json`: the model

```json
{ "parent": "minecraft:item/generated", "textures": { "layer0": "lokvihar-test-mod:item/ruby" } }
```

- `parent: minecraft:item/generated`: inherit vanilla's standard "flat item" behavior, which turns a 2D texture into a thin 3D sprite with correct hand and GUI positioning. Almost all simple items use it. (Use `minecraft:item/handheld` for tools, which are held diagonally.)
- `textures.layer0`: the texture to extrude. It resolves to `textures/item/ruby.png`. Higher layers (`layer1`, ...) can be stacked on top, for example for tinted overlays.

## `assets/lokvihar-test-mod/textures/item/ruby.png`

The 16×16 pixel art for the ruby. Transparent pixels stay transparent in game. Bigger power-of-two sizes (32×32, 64×64) also work, but break the vanilla style.

## `assets/lokvihar-test-mod/icon.png`

The mod's icon in mod lists, referenced by `"icon"` in `fabric.mod.json`. It isn't used by the game world itself.

## `assets/lokvihar-test-mod/lang/en_us.json`: translations

```json
{ "item.lokvihar-test-mod.ruby": "Ruby" }
```

Maps translation keys to display text for US English. An item's key is generated from its ID: `item.<namespace>.<path>`. The `:` becomes `.`, and `/` in a path would also become `.`.

To support another language, add a file with the same keys, such as `lang/de_de.json`. Languages without a file fall back to `en_us`. If a key is missing entirely, the raw key text shows up in game, which is an easy way to spot gaps.

---

## `data/lokvihar-test-mod/recipe/ruby.json`: crafting recipe

```json
{
  "type": "minecraft:crafting_shaped",
  "category": "misc",
  "pattern": ["NNN", "NDN", "NNN"],
  "key": { "N": "minecraft:iron_nugget", "D": "minecraft:red_dye" },
  "result": { "id": "lokvihar-test-mod:ruby", "count": 1 }
}
```

In a crafting table:

```
┌─────────┬─────────┬─────────┐
│ nugget  │ nugget  │ nugget  │
├─────────┼─────────┼─────────┤
│ nugget  │ red dye │ nugget  │   →  1 × Ruby
├─────────┼─────────┼─────────┤
│ nugget  │ nugget  │ nugget  │
└─────────┴─────────┴─────────┘
```

- **Location:** `data/<ns>/recipe/<name>.json`. The folder is `recipe`, singular. Older versions used `recipes`, and files in that folder are now ignored without any error. The recipe's own ID is `lokvihar-test-mod:ruby`, taken from the file path. It's unrelated to the item ID; the two just happen to match.
- `type: minecraft:crafting_shaped`: the arrangement matters. (`crafting_shapeless` would accept the ingredients in any slots.)
- `category: misc`: which tab of the recipe book it appears under (`building`, `redstone`, `equipment`, `misc`).
- `pattern`: up to 3 rows of up to 3 characters. Each character stands for an ingredient, and a space means an empty slot. A pattern smaller than 3×3 can be placed anywhere in the grid.
- `key`: maps each pattern character to an item ID. A tag, such as `#minecraft:planks`, also works.
- `result`: the output item's `id` and stack `count`.

Recipes are server data, so `/reload` picks up changes without restarting. Check the log if a recipe doesn't appear: an invalid recipe is skipped with an error, and the game keeps running.
