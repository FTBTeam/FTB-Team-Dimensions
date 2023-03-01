# FTB Team Dimensions

## Overview

FTB Team Dimensions allows dimensions to be dynamically created for teams (FTB Teams is a required dependency).
Players join the overworld in a prebuilt lobby structure with a portal, and on entering the portal get the option
to choose from one or more "island" structures in a new dimension which will be created for their team. The party team
will also be auto-created if it does not exist yet. New players can join the team in the usual way, and will be
immediately ported to the team's existing dimension upon joining.

Currently, there are two chunk generation types, both for simple void dimensions:

* Multi-biome generation with an overworld-like biome distribution; this is the default
* Single biome generation; set `singleBiomeDimension` in mod config to true to enforce a single biome for the entire dimension.
  * You can override the biome that is used via the `singleBiomeName` config setting (default is `minecraft:the_void`)

More flexibility is planned in terms of types of world-gen in the future.

## Configuration

At least basic familiarity with 1.19.2 data-driven world generation is required here.

Nearly all configuration is done via datapack, in particular the vanilla `structure` and `structure_set` to define the
prebuilt "islands" which will be pre-generated in the new dimension (always at the (0,0) chunk).  There is also a custom
`ftbdim_prebuilt_structures` datapack type, which defines which structures are available to the player (when the player
first enters the lobby portal, one entry per known `ftbdim_prebuilt_structures` will be shown in the GUI).

The default prebuilt structure (`data/ftbteamdimensions/ftbdim_prebuilt_structures/island1.json`) looks something like (with optional
fields not necessarily included in the actual file):

```json5
{
  "id": "ftbteamdimensions:island1",
  "structure": "ftbteamdimensions:spawn/island1",
  "name": "Simple Island",
  // optional fields below here
  "author": "FTB Team",
  "structure_set": "ftbteamdimensions:default",
  "height": 64,
  "dimension_type": "ftbteamdimensions:default",
  "preview_image": "ftbteamdimensions:textures/spawn/island1.png",
  "spawn_override": [ 0, 64, 0 ]
}
```

* "id" field is mandatory, unique, and should correspond to the JSON filename
* "structure" field is mandatory and determines the NBT structure file which will be used
  * See `data/ftbteamdimensions/structures/spawn/island1.nbt` for the default island, which is a tiny island of grass and dirt
* "name" field is mandatory and is the name displayed in the player's GUI when selecting a structure
  * This can be a literal string or a translation key
* "author" field is optional and defaults to "FTB Team" - displayed as "by <author>" in the player's GUI when selecting a structure
  * this is a literal string
* "structure_set" field is optional and is the structure set tag to use
  * Defaults to `ftbteamdimensions:default`, which includes only the structure set `ftbteamdimensions:start`
  * This controls which structure set(s) will be used for the island during generation of the dimension
  * It's unlikely you'll need to change this.
* "height" is optional, and controls the absolute Y-level at which spawn islands are generated.
  * Defaults to 64. **TODO support for surface-relative Y values**.
* "dimension_type" is optional, and determines the dimension type used for created dimensions
  * Defaults to `ftbteamdimensions:default`, an overworld-like dimension type
* "preview_image" is optional, and points to a texture (which should be 128x64) to be shown in the structure selection GUI; typically a screenshot of the structure, but could be any image
  * Default texture for `<modname>:<id>` is `<modname>:textures/spawn/<id>.png`
* "spawn_override" is optional, and can be used to spawn the player at a non-default position
  * Default position is (0, HEIGHT, 0), where HEIGHT is the island Y-level (see "height" above)

## Structure NBT

Structure files (both for the overworld lobby and dimension island structures) are standard vanilla NBT structures, as saved
via [Structure Blocks](https://minecraft.fandom.com/wiki/Structure_Block).
There is one important requirement: all structures **must** contain one Structure Block in data mode with the custom data 
tag `spawn_point`. This is used to determine where players will spawn in both the overworld lobby and in team dimensions
that are created.

Structures created will always be positioned with the `spawn_point` block at (X,Y,Z) = (0,H,0), where H = the "height" 
field from the prebuilt structure JSON above. The data structure block is replaced with air when the structure is
actually placed into the world, and the player will spawn with their feet in this block by default (but see the
"spawn_override" field above).

The default overworld lobby structure is at `data/ftbteamdimensions/structures/lobby.nbt`, but this can be changed 
in one of two ways:
* in mod config (see `lobbyStructure` in `ftbteamdimensions-common.toml`)
* or simply overwrite it via datapack!
