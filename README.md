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

The default prebuilt structure (`data/ftbdimensions/ftbdim_prebuilt_structures/island1.json`) looks like this:

```json
{
  "id": "ftbdimensions:island1",
  "structure": "ftbdimensions:spawn/island1",
  "name": "Simple Island",
  "structure_set": "ftbdimensions:default",
  "height": 64,
  "dimension_type": "ftbdimensions:default"
}
```

* "id" field is mandatory, unique, and should correspond to the JSON filename.
* "structure" field is mandatory and determines the NBT structure file which will be generated (see `data/ftbdimensions/structures/spawn/island1.json`).
* "name" field is mandatory and is the name displayed in the player's GUI when selecting a structure (this can be a translation key).
* "author" field is optional and defaults to "FTB Team". It is displayed as "by ..." in the player's GUI when selecting a structure.
* "structure_set" field is optional and is the structure set tag to use; it defaults to `ftbdimensions:default`, which includes only the structure set `ftbdimensions:start`.
  * This controls which structure set(s) will be used during generation of the dimension. 
* "height" is optional, and controls the absolute Y-level at which spawn islands are generated. It defaults to 64. **TODO support for surface-relative Y values**.
* "dimension_type" is optional, and determines the dimension type used for created dimensions. It defaults to `ftbdimensions:default`, an overworld-like dimension type.

