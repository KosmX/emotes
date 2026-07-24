# The `hytale` subproject

A Hytale **server** mod. Hytale exposes no client modding API, so everything here runs server-side and drives stock
clients.

## Research sources

Hytale's full server source and every game asset ship in `hytale-shared-source-release.zip` (~1.4 GB, ~80k files),
normally in the user's `Downloads`. Server sources are under `HytaleServer/`, assets under `HytaleAssets/`, the packet
schema (C# definitions) under `Protocol/`. **Never unpack the whole archive** — list with `unzip -l | grep` and extract
single files with `unzip -o -q -j <zip> "<path>" -d <dir>`.

Prefer that archive over decompiling the published `Server` artifact. Only the server is published to Hytale's Maven —
the client and the assets are not. Client sources are in neither, so anything the client alone interprets has to be
inferred from the assets it reads.

The MCP documentation server is a decent index but lags the archive: it was built against an older server and is
missing, among other things, the runtime emote registration API. **When the docs and the archive disagree, the archive
wins.** The docs' packet list is also truncated and omits the client-to-server emote packet.

## Platform constraints

- The server is compiled for the **same Java version as this project**, despite documentation claiming an older one.
  No bytecode downgrade step is needed, unlike the Geyser frontend.
- **The server bundles no SLF4J.** The core logs through SLF4J, so this module shades the API plus the JDK binding,
  which lands on the JDK's built-in logging — the same sink Hytale's own logger writes to.
- Asset and animation formats are all **JSON**, not binary.
- Commands, UI callbacks and anything touching entity components run on a **world thread**. Blocking it freezes the
  world's tick. Push blocking work onto another thread and hop back with the world's executor.

## Emote delivery

There is **no per-bone channel** to stream a live pose over, and Hytale's animation format has no expression language
to parameterise — unlike Bedrock's Molang, which is exactly what lets the Geyser frontend drive bones through entity
properties. The only way to show an arbitrary pose is to hand the client a finished keyframe clip.

So the pipeline is: run the Emotecraft engine over an emote, sample the resulting pose, serialise it as a Hytale
animation clip, register that clip plus an icon as *common assets*, then register an *emote asset* pointing at both.
Registering the emote asset broadcasts an update — metadata only, not the blobs — and the emote becomes playable,
including from the client's own emote wheel, which is built from the built-in list plus everything in the emote asset
store. There is no "add to wheel" call and none is needed; slot assignment is client-side and the server can neither
read nor set it.

Two constraints the validators enforce: the animation must sit under the characters directory with the animation
extension, and the icon under the emote icon directory as a PNG. Both must already be registered as common assets
before the emote asset is registered, or validation fails.

### Registration is in memory, delivery is ours

Registration only mutates maps in the running server, so everything published is lost on restart — which is what the
disk cache below exists to undo.

The convenience registration call is unusable at this scale: it **broadcasts every asset to every connected player**
with no distance or visibility check, announces each one with a toast to the whole server, and grows the list of assets
every future login must download. The low-level registry call sends nothing at all, so registration and delivery are
separated — register through the registry, then send the blobs to exactly the clients that need them.

Who needs what follows from what each blob is for:

- The **icon** is drawn by the emote wheel, which lists every registered emote for every player, so every player needs
  every icon. They are small, and they go out once the client reports itself ready — not on connect, which would land
  in the middle of the client's own asset phase.
- The **clip** is only needed by someone about to watch the emote. That set is exactly the set the animation packet
  itself is written to, and the server already has a helper for it, so an emote and the clip it needs reach the same
  clients by construction. Going through the entity tracker's visibility index directly would be the same answer with
  more code; the helper follows it if the core ever switches which side of the relation it reads.

Nothing is sent to a connection twice, tracked weakly so a disconnect forgets it — the next connection is a new client
that may have kept nothing.

Because delivery is ours, the login-time required-asset list never mentions these blobs, and the client is never told
to fetch them. That is the point: a player downloads a clip when somebody performs it in front of them, not a library's
worth at every login.

### Someone who arrives part-way through

The animation packet is a one-shot write to whoever can see the performer at that instant; the core never replays it,
and a TODO in the stop path admits as much. What it does have is a component meant for exactly this — "active animations
on an entity so that looping animations are played when the entity comes into range" — whose system pushes an update to
the newly-visible set every tick. It is documented as used only by NPCs so far, but nothing about it is NPC-specific,
so an emote is recorded there as well as played.

Recording it *and* playing it is not belt-and-braces, it is the core's own pattern: the NPC entity does both, and says
why — "we also need to play the animation to all existing players since we only use the animation component for sending
to new players."

That update carries the animation id and nothing else, which leaves two gaps to fill locally:

- The arrival needs the **clip** before the id reaches them. Our own system runs in the same group against the same
  newly-visible set and writes the blobs straight to the connection, which puts them ahead of the core's update — that
  one is merely queued in this group and flushed a group later.
- Nothing would ever **end** the emote, so every future arrival would be shown a performance that finished long ago.
  The clip's own length ends it, which is why the baker reports its frame count and the cache index keeps it.

The performer's state — what is playing, until when, and the resolved blobs — lives in a component of ours on their
entity. The blobs ride along because a viewer who has already appeared cannot be made to wait on a disk read, and they
are let go the moment the emote ends. Cancelling an emote clears the same state; that packet still reaches the core,
which stops the animation for everyone watching.

**What cannot be done:** starting the clip at an offset. Neither the animation packet nor the active-animation update
carries a time, and the emote asset has no offset or speed field, so someone who walks up mid-emote sees it **from the
beginning** rather than from where it has got to. Emotecraft's other backends send the elapsed tick and resume in
place; here the protocol has no field to put it in. Leaving the view and returning restarts it the same way.

### Taking over the wheel

An emote can be started from our page or from the client's own wheel, and the wheel path is a client-to-server packet
handled by the core, which would play the emote against clients that may not hold the clip. So that packet is
intercepted and cancelled **for our emotes only** — the blobs go out, then the animation plays. The stock handler is
short and does nothing else of substance, which is what makes taking it over safe; everything that is not ours falls
straight through to it.

### The disk cache

Baking is deterministic and downloading an emote body is the one thing the library charges for, so every emote that
came from the library is written out next to an index of `emote UUID -> asset id` plus each blob's hash. Emotes that
already live on disk in Emotecraft's own format — the built-in ones — are not cached: baking them is cheap, and an
emote file that changes under an unchanged UUID would otherwise be served stale forever.

At startup the whole cache is republished. That is the preload: a restart costs no downloads. Blobs stay on disk until
a client actually needs one, since the file-backed asset keeps its bytes only weakly.

The index carries the baker's version stamp. The retargeting constants below are unverified, and a cached clip has no
record of the axes it was baked with, so **any change to the baker or the rig must bump that stamp** — a mismatch
empties the directory instead of replaying stale poses. The same happens if the index is unreadable, since the blobs
beside it cannot be addressed without it.

## Retargeting the rig

Both rigs express a pose as a delta from the rest pose, so no rest transform has to be folded in — only naming, scale
and axis convention differ. `HytaleRig` owns all of it; `BlockyAnimBaker` only samples and serialises.

The engine does **not** animate in Minecraft's model space. Its own bone table puts the origin at the feet, Y up, with
the right arm on +X and the cape on +Z. Hytale mirrors both of those, so the conversion is a **half turn about Y**,
not about X. Hytale is also the taller rig, so translations are scaled by the two rigs' proportions rather than by a
plain unit conversion.

Rotations are retargeted onto the **target's own joints**. The Emotecraft pivots land a fair distance from the
matching Hytale sockets — its torso pivots at the neck rather than the chest — so honouring them would swing each part
out of its socket and tear the skeleton. The one exception is the root bone, which has no socket to tear: there the
pivot difference is compensated for, since it visibly changes a full-body lean.

The engine already returns *local* per-bone transforms. The Geyser frontend composes parents itself because Bedrock's
rig is flat; Hytale nests its skeleton and composes client-side, so nothing is pre-composed here.

Limb bend is a single scalar in Emotecraft and a real joint in Hytale, so it is replayed as a rotation on the segment
below. Held items map onto the hand attachment nodes, so an item follows the arm through the skeleton and stays
visible. The cape maps onto the back attachment node with its pitch inverted, matching a correction the Geyser
frontend applies to the same authoring quirk.

**Unverified against a running client:** the euler order, the sign of bend, the proportional scale factor and the
cape's pitch. Expect to calibrate these the first time an emote is watched in game.

## Sampling, not transcription

The clip is sampled frame by frame rather than transcribed keyframe for keyframe. Emotecraft keeps a separate keyframe
list *per axis*, each with its own easing and expressions, while a Hytale keyframe carries one whole vector and knows
only two interpolation modes. Merging those axis timelines means evaluating them at every distinct time anyway.

Runs where a node does not move collapse to the two keyframes that bound them. The trailing one is not a size saving:
interpolation is linear, so a node holding still would otherwise drift across the whole span instead of staying put.

## UI

Pages are built server-side and pushed whole; there is no data binding, and the declarative data-context commands
visible in the packet schema are unimplemented — nothing in the server references them. Lists are a scrolling group
filled by appending rows and addressing them by index, and there is **no pagination widget**: the server's own
convention is a search box plus a hard result cap.

Read the player's chosen text out of a field with a selector-valued event key, which the client resolves at the moment
the event fires. Doing that on a button press avoids the update-acknowledgement gate, which silently drops incoming
events while an update is still in flight — the reason a search that reacts to every keystroke loses characters.

Rebuilding a list means re-registering its event bindings, since they are bound to index selectors that now point at
new elements.

Text coming from the emote library is **Minecraft component JSON**, not plain text. `McComponents` converts it; see
[emote-library.md](emote-library.md) for the fallback handling that matters there.

## Keybindings

A server mod **cannot** register one. The client feature registry takes a closed enum of gameplay toggles, and the
hotkey UI element only draws an existing client binding. Key events reach the server only through bindings inside an
open custom page, where the key itself cannot be chosen, and the HUD layer carries no event bindings at all.

In practice this does not matter: the emote wheel's own hotkey already works with emotes this mod registers.

## Still to do

- An emote published while players are online reaches their wheels as metadata immediately, but its icon only when they
  browse it or watch it performed. Until then that one row draws a placeholder.
- Whether the client honours an active-animation update on a *player* entity is unproven. The channel itself is
  entity-generic — a network id and a list of component updates — and demonstrably works for players, since skins are
  synced over it. What no server-side source can settle is whether the client's handler for this particular update type
  applies it to a player. If it does not, arriving part-way through shows nothing, and the protocol offers no
  alternative.
- Whether a client that receives a clip and the animation packet back to back processes them in that order is unproven.
  If not, the first performance of an emote may be dropped by onlookers and only work from the second onwards.
- The page markup and its path have never been loaded by a real client.
- Calibrate the retargeting constants listed above.
