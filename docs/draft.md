# Color Challenge

## Theme

> Collect and deliver all 16 dyes as fast as you can. Compete for the fastest time in Color Challenge.

## Settings & Basic Rules
- Players collect and deliver all dyes
- The world has a "Delivery Area" where players can deliver dyes
- The first player to deliver all dyes ends the game
- Mod Name: Color Challenge
- Mod ID: colorchallenge

## Gameplay Image
- In multiplayer, it can be played as a competitive game to be the first to finish
- In singleplayer, it can be played as an RTA (Real-Time Attack) to minimize completion time

## Delivery Area Structure
- One per world, generated once at the initial spawn point
	- ID: colorchallenge:deliveryarea
- Generated only once in a new world; not generated in existing worlds
	- However, it can be placed via the command `/place structure colorchallenge:deliveryarea`
- Players initially spawn at a designated location near the Delivery Area
- This means the initial spawn point is fixed at a common location for all players
- Upon death, players respawn at this initial spawn point
- However, players can set a different respawn point by using a bed
- When generating the structure, the placement location is checked for sufficient space; if the location has minimal contact points or would be submerged in water, a suitable nearby location is searched
- However, this search is not unlimited and is restricted to within a few chunks to avoid performance impact

## Delivery Area Staff NPC
- A dedicated mob "Staff" with a Wandering Trader model is placed within the Delivery Area structure
	- ID: colorchallenge:staff
- Players can deliver dyes by using dyes on the Staff mob

## Easing Dye Acquisition Requirements

Some items used as ingredients for vanilla dyes are found in relatively rare biomes, making them difficult to discover in a short time.
To address this, the following systems make discovery easier and keep the game from becoming tedious.

- The following items are generated in village chests:
	- 1 Jungle Sapling (for Cocoa Beans; ingredient for Brown Dye)
	- 1 Cactus (ingredient for Green Dye)
	- 1 Sea Pickle (ingredient for Lime Dye)
- After a set time (40 minutes), the same items above are added to the Wandering Trader's trade offerings

## Delivery Progress Check

- An "Instructions" item (a dedicated map item) is distributed to players at the start, and using it allows checking delivery status
- When checking, dye items are displayed in a grid layout, with delivered dyes having a green background
- This item is not dropped or lost upon death

## Game Start & End
- Start
	- A block to declare the game start is provided
		- ID: colorchallenge:game_start_block
		- Name: EN: Game Start Block / JA: Game Start Block
	- The Game Start Block is placed within the Delivery Area structure
	- Right-clicking the Game Start Block prompts whether to start the game
		- If this mechanism is difficult to implement, this confirmation step is excluded from the MVP
	- Upon confirming the intention to start, a countdown begins, and the game enters the "In Progress" state when it reaches zero
	- The Game Start Block can only be used when the game is in the "Not Started" state
	- Play time is counted until the game ends and is also displayed on screen as a HUD element
- End
	- When the first player delivers all dyes, the game enters the "Ended" state
		- Even if multiple players deliver all dyes in the same tick, the first one detected is considered the winner
	- A message is displayed on all players' screens along with the winner's player name
	- The names and times of players who have finished are displayed as a HUD list
	- The play time counter continues running
- Reset
	- A reset operation returns the game to the "Not Started" state
	- A block to reset the game is provided
		- ID: colorchallenge:game_reset_block
		- Name: EN: Game Reset Block / JA: Game Reset Block
	- The Game Reset Block is placed within the Delivery Area structure
	- Right-clicking the Game Reset Block prompts whether to reset
		- If this mechanism is difficult to implement, the reset operation itself is excluded from the MVP
	- Play time counter and delivery status are cleared

## Game States
- Not Started
	- Internal State
		- Delivered items: None
		- Play time counter: 0:00 (0 min 0 sec)
	- Operations
		- Delivery: Not allowed
		- Game Start Block: Usable
		- Game Reset Block: Not usable
- In Progress
	- Internal State
		- Delivered items: Changes according to delivery status
		- Play time counter: Time since entering "In Progress" state (displayed as min:sec)
	- Operations
		- Delivery: Allowed
		- Game Start Block: Not usable
		- Game Reset Block: Usable
- Ended
	- Internal State
		- Delivered items: Changes according to delivery status
		- Play time counter: Time since entering "In Progress" state (displayed as min:sec)
	- Operations
		- Delivery: Allowed
		- Game Start Block: Not usable
		- Game Reset Block: Usable

## Provided Items & Effects
- The following items are provided to players upon initial spawn:
	- Instructions
		- ID: colorchallenge:instructions
		- Appearance is a vanilla map item
	- Food
		- Bread: 1 stack
- Players are automatically given the Night Vision effect with no time limit

## HUD Display
- Number of delivered items
	- Position: Top-right of screen, right-aligned
	- Format: 2 / 16
- Play time (elapsed time since start)
	- Position: Top-right of screen, right-aligned, below the delivered items count
	- Format: 00:00 (mm:ss)

## Architecture
- Minecraft mod using Architectury
- Compatible with both Fabric and NeoForge
- Initially implemented for Minecraft version 1.21.1
- The project will be configured with Gradle as a multi-project setup

## Directory structure

- common/shared
    - Common code without loader dependencies or version dependencies. Not a Gradle subproject, but incorporated as one of the srcDirs from each version-specific subproject
- common/<version>
    - Common code for each Minecraft version without loader dependencies. Gradle subproject `:common`.
- fabric/base
    - Code for Fabric without Minecraft version dependencies. Not a Gradle subproject.
- fabric/<version>
    - Code for Fabric and each Minecraft version. Gradle subproject `:fabric`. Depends on fabric/base.
- neoforge/base
    - Code for NeoForge without Minecraft version dependencies. Not a Gradle subproject.
- neoforge/<version>
    - Code for NeoForge and each Minecraft version. Gradle subproject `:neoforge`. Depends on neoforge/base.

## License

- LGPL-3.0-only
