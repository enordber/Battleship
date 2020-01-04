A Battleship game based on Hasbro’s Battleship (https://www.hasbro.com/common/instruct/Battleship.PDF).

Use `ant` to build. This will create a runnable jar file in the ‘dist’ directory.

The player can choose between three opponent types:

- Random Opponent. The simplest. Chooses a random unrevealed cell for each shot.
- Probability Opponent. Selects an occupied cell with a probability settable with the Dfficulty slider. This opponent cheats by looking at the player's ship locations.
- Seek and Destroy Opponent. This opponent attempts to mimic a human player, using only information that would be available to a
 player during the game. The Difficulty slider sets the probability that the opponent will attempt a good shot each turn. Otherwise, it will select a random shot.
