# FTB Team Dimensions

## Overview

FTB Team Dimensions allows dimensions to be dynamically created for teams (FTB Teams is a required dependency).
Players join the overworld in a prebuilt lobby structure with a portal, and on entering the portal get the option
to choose from one or more "island" structures in a new dimension which will be created for them.

Currently, the only dimension chunk generation type is a void dimension with a single "void" biome, but more flexibility is planned.

## Configuration

At least basic familiarity with 1.19.2 data-driven world generation is required here.

Nearly all configuration is done via datapack, in particular the vanilla `structure` and `structure_set` to define the
prebuilt "islands" which will be pre-generated in the new dimension (always at the (0,0) chunk).  There is also a custom
`ftbdim_prebuilt_structures` datapack type, which defines which structures are available to the player (when the player
first enters the lobby portal, one entry per known `ftbdim_prebuilt_structures` will be shown in the GUI).

The default prebuilt structure (`data/ftbteamdimensions/ftbdim_prebuilt_structures/island1.json`) looks like this:

```json
{
  "id": "ftbteamdimensions:island1",
  "structure": "ftbteamdimensions:spawn/island1",
  "name": "Simple Island",
  "structure_set": "ftbteamdimensions:default",
  "height": 64,
  "dimension_type": "ftbteamdimensions:default",
  "preview_image": "ftbteamdimensions:textures/spawn/island1.png"
}
```

* "id" field is mandatory, unique, and should correspond to the JSON filename
* "structure" field is mandatory and determines the NBT structure file which will be used
  * See `data/ftbteamdimensions/structures/spawn/island1.json` for the default island, which is a tiny island of grass and dirt
* "name" field is mandatory and is the name displayed in the player's GUI when selecting a structure
  * This can be a literal string or translation key
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

## Structure NBT

Structure files (both for the overworld lobby and dimension island structures) are standard vanilla NBT structures, as saved
via [Structure Blocks](https://minecraft.fandom.com/wiki/Structure_Block).
There is one important requirement: all structures **must** contain one Structure Block in data mode with the custom data tag `spawn_point`. This is used to
determine where players will spawn in both the overworld and dimensions that are created.

Structures created will always be positioned with the spawn point at (X,Z) = (0,0). The data structure block is replaced with air when the
structure is actually placed into the world. 

The default lobby structure is at `data/ftbteamdimensions/structures/lobby.nbt`, but this can be changed in one of two ways:
* in mod config (see `lobbyStructure` in `ftbteamdimensions-common.toml`)
* or simply overwrite it via datapack!
