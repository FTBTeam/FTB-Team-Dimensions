# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.1.3]

### Added
* Added some pregen functionality for both the overworld and generated dimensions
  * Can greatly improve dimension creation and loading times, but requires either void dimensions, or non-void dimensions with a fixed seed.
* Added config to control inventory clearing when joining a team

### Fixed
* Waterlogging fix processor now also works when called via `/jigsaw place` or other commands which work on an already-generated level
  * Previously assumed a `WorldGenRegion`, which is only during world generation
* Fixed crash with waterlogging fix processor and unloaded chunks

## [1.1.0]

### Added
* Added a new CUSTOM worldgen type
  * Allows for generation of non-void dimensions with customisable biome and noise settings
* Added some extra lobby config settings (customisable game mode and entity damage while in lobby)

## Changed
* Reworked the config system a bit - review your configs

## [1.0.2]

### Fixed

* Actually register the structure processors... Whoops 

## [1.0.1]

### Added
* Y position of the overworld lobby is now configurable via config (`lobbyYposition` setting)
* The "World Type" cycler button in the create world screen is now disabled and locked
  * Changing world types is not intended since it can break custom worldgen used with this mod

## [1.0.0]

### Added
* Initial mod release
