# Emotecraft

Emotes for Minecraft and other block games. One shared animation engine, several platform frontends.

## Conventions

- **Agent files are written in English**, including this one. Conversations with the user may be in another
  language, but anything committed to the repository is English.
- Keep this file to general, slow-changing information. Anything long, detailed, or specific to one subproject goes in
  the per-context files under `agents/`.
- **Never write a fully qualified class name.** Not in code — import the type, even for a single use, and even where an
  import would collide (import one and qualify nothing: rename or restructure instead). Not in prose either: these
  files name types plainly, and a package path in a sentence dates the moment the class moves.
- Match the surrounding code: same comment density, naming and idiom as the file being edited. Comments explain *why*,
  never restate *what* the line already says.
- Nullability annotations come from JetBrains, never from `javax.annotation` — including in code that overrides a
  platform API annotated the other way.

## Context files

| File | Covers |
|------|--------|
| [agents/hytale.md](agents/hytale.md) | The Hytale server mod: asset formats, rig retargeting, emote delivery, UI |
| [agents/emote-library.md](agents/emote-library.md) | The cloud emote library SDK and how each platform authorizes |

## Modules

Platform-independent core:

- `emotesAPI` — the animation model and emote protocol. Depends on PlayerAnimationLib Core and NoteBlockLib.
- `emotesServer` — emote (de)serialization, config and the service lookups every platform plugs into.
- `emotesAssets` — shared resources, including the built-in emotes.
- `emotesMc` — the Minecraft-specific half of the core.

Platform frontends:

- `minecraft` — the Fabric/NeoForge mod, built with Unimined. The reference implementation.
- `paper` — the Paper/Folia plugin.
- `geyser` — the Geyser extension, letting Bedrock clients see emotes.
- `hytale` — the Hytale server mod. See [agents/hytale.md](agents/hytale.md).

## Building

Gradle, Kotlin DSL. Versions live in `gradle.properties`; helper functions in `buildSrc`. The whole project compiles
against one Java version, set by `java_version`.

Each frontend shades the core modules it needs, so a released artifact is self-contained. When a target runtime already
ships a library, exclude it rather than shading a second copy.

## Services

The core resolves platform behaviour through a small service lookup: an interface in `emotesServer`, a default
implementation with the lowest priority, and a platform implementation registered through `META-INF/services` at a
higher priority. Anything a platform must answer differently — where the game directory is, how permissions work —
goes through that mechanism rather than through conditionals in shared code.
