# RuneWinRate
Calculates rune win rates given a Json[] of (long)gameIDs.


Brawlers.java requires user input:
your API key
the path to your Json file (if on windows swap (\\ or \) for /)
the region your gameIDs are from.

once input is collected Brawlers.java runs through the matches collecting data about teamcomps, runes and matchwinners calculating the winrates of individual runes.
Then Brawlers.java writes a HashMap<String, HashMap<String, TreeMap<Double, String>>> object to disk for easy access using webapps and android.


ReadObject.java requires the path to the resulting object from Brawlers.java
ReadObject.java also requires a champion name(first letter capitalized) and a teamcomp(eg. "AP", "AD", "Neutral" capitalization matters).
ReadObject.java prints out a specific champions winrates on different runes, includes only greater runes.
