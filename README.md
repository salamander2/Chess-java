Chess-java
==========
Credit for the Chess grille go to Moreu Guillaume (Guyome41):
http://opengameart.org/content/chess-grille

Credit for the piece sprites go to U+2654 - U+265F.

A project I wrote to help me better understand java.

* The white_player[] and black_player[] Piece arrays in the Game class are only used for instantiating the Pieces on startup. All subsequent drawing is done through getting the individual Piece's from the game board -- see DrawGame.pantComponent(). This makes it easy to save/load the game.
