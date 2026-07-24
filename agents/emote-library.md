# The cloud emote library

A hosted library of emotes players can like on the website and then use in game, reached through a small SDK
(`game-sdk`). The Minecraft client's implementation under the mod's library package is the reference to follow.

## Shape of the API

One client object wraps one **player's** session: the liked list and search both return that account's own likes. A
server therefore needs a client per player, not one per server.

Authorization is per platform, and each takes whatever proves ownership of an account on that platform: Minecraft
answers a challenge through the session service, Bedrock presents its multiplayer token, Hytale its session-service
identity token. Sessions expire on an idle window, so re-authorizing is a normal branch rather than an error path.

Every call blocks except the live stream, which spins its own thread. None of them may run on a game thread.

Errors are typed: ownership not proven, account not linked, session expired, no session, rate limited, download quota
exceeded, network failure. Distinguish them — several are recoverable and one is fatal to the live stream.

## Cost, and why laziness matters

Three tiers, cheapest first:

1. **Metadata** — one call per page of results. Carries name, description, author, tags and an icon URL, but no body.
2. **Icon** — one HTTP fetch per emote actually shown.
3. **Body** — the emote itself. This is the call under a **download quota**.

Browsing must never touch tier 3. Fetch a body only when a player actually picks an emote, and only if it has not
already been published — deduplicate on the publication, where the result is already recorded, rather than caching
parsed animations that are useless once converted. Walking a player's whole liked list to fetch it up front is exactly
what the quota is there to stop, whatever it would buy in convenience.

Anywhere the converted form can be kept, keep it: a quota is spent per download, so an emote that survives a restart on
disk is one that is never paid for twice. See the Hytale context file for how that plays out there.

## Getting the token on Hytale

The player's identity token is only ever visible on the packet that opens the connection; the login-phase handler
keeps it private and is gone by the time a mod could ask. So it is read off that packet through the inbound packet
adapter and kept, keyed weakly by connection, until the player's session needs it. Weakly, because there is no
disconnect hook to clean up after and a player's credential should not outlive their session. It is never logged.

## Text is Minecraft components

Names, descriptions and authors arrive as **Minecraft component JSON**, not plain text — the Minecraft client feeds
them straight through its component deserializer. Handing those strings to another game's label shows raw JSON.

The two models line up closely: text and translation nodes, children, colour, bold and italic all have counterparts.
Translation arguments do not: Minecraft substitutes positionally, most other systems by name.

### Multiple fallbacks

This is the one real divergence from vanilla Minecraft. Vanilla allows a translation node a single fallback; the
library instead ships a **map of locale to text**. On Minecraft that needs a client mod, because components resolve
client-side where the chosen language lives.

Anywhere pages are built per player on the server, no such mod is needed: the viewer's own language selects the entry
directly. Note the priority inverts, too. On a platform that has no Minecraft translation keys at all, the localized
literal is the *only* thing that can be displayed, so the fallback is preferred outright and the key is a last resort
rather than the first choice. Compare locale tags case-insensitively and normalise the separator, since platforms
disagree on whether it is a dash or an underscore.
