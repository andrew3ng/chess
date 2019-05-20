# chess
Chess fully implemented in Java to be played on the console/terminal
Includes a debugging feature with basic load and save capabilities (so testing isn't as annoying).
To turn debugging on, go to main and set the 'debug' flag to true. 
The board will be have as if it's both players' turn at the same time, so feel free to play god.
However, the debugger is still a little janky. lmk if there are any bugs with it lol

**The debugger has the following commands:**
- help o:\<cmd>
  - Gets help for the given instruction including arguments and uses
  - If no instruction is specified, help will print out all instructions
- load o:\<file>
  - loads a file previously used
  - if no file is specified, it will attempt to load in the previous file
  - NOTE: to load in a file, you must end it with '.txt'
- save o:\<file>
  - saves the current state to the given file name
  - if no file is specified, it will save to the last file
  - NOTE: all files are saved as '.txt' files and must be specified as such
- set \<role> \<color> \<coord>
  - Sets a piece on the board at the given location
- move (\<name>/\<coord>) \<coord>
  - Moves a piece to the given coord
- remove (\<name\>/\<coord\>)
  - Removes a piece
- getMoves (\<name>/\<coord>)
  - Given the current state, what are the moves the piece can do?
  - This command is a bit jank atm. If used before isMate, it may say some impossible moves are possible 
- isCrit (\<name>/\<coord>)
  - Will tell us if a piece is "critical" to the king's survival
  - ie, if the piece wasn't there, the king would be in check
- inCheck \<color>
  - Tells us if a specific team is in check in the given state
- isMate \<color>
  - Tells us if a given team can make a move
  - If they can't, they're in mate and the game should end
- nextTurn
  - Advances the turn count by 1
  - Mostly to get the perspective from the other side
- exit
  - Quits the program
  - If there exists any unsaved changes, the program will prompt the user if he wants to save them

**TODO for the future:**
- Implement basic GUI
- Create basic AI
- Update debugger to be less jank
