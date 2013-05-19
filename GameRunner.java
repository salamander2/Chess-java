/*
	Chess grille credit of Moreu Guillaume (Guyome41):
	http://opengameart.org/content/chess-grille
*/

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.event.*;

import java.util.*;
import java.lang.*;

import java.io.*;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.JOptionPane;


class ButtonListener implements ActionListener
{
	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() == GameRunner.saveButton) {
			Game.board.saveBoard();
			JOptionPane.showMessageDialog(null, "Successfully saved the game!", "Save Successful", JOptionPane.INFORMATION_MESSAGE);
		} else if (e.getSource() == GameRunner.loadButton) {
			Game.board.loadBoard();
		}
	}
}

/**
	* Image helper class to load in regular images (such as board) and packed spritesheets containing the images of the individual pieces.		
*/
class ImgLoader
{

	public Image board;
	public String board_loc = "data/board.png";
	
	public Image white_pieces[];
	public String white_pc_loc = "data/white-62-transp.PNG";
	
	public Image black_pieces[];
	public String black_pc_loc = "data/black-62-transp.PNG";
	
	public static final int TILE_SIZE = 62;
	
	/**
		Default constructor attempts to load in the board image and the pieces images.
	*/
	public ImgLoader()
	{
		//load in the board
		board = (new ImageIcon(board_loc)).getImage();
		
		// we need to load in the individual pieces now
		// each individual piece is 62x62, each full sprite sheet is 372x128
		// the top row of each file is the piece as it looks from the front
		// the bottom row of each file is the piece as it looks from the back
		try {
			BufferedImage big_white_sprite = ImageIO.read(new File(white_pc_loc));
			BufferedImage big_black_sprite = ImageIO.read(new File(black_pc_loc));
			
			white_pieces = new Image[Game.NUM_PIECES];
			black_pieces = new Image[Game.NUM_PIECES];
			
			int i;
			for (i = 0; (i * TILE_SIZE) < big_white_sprite.getWidth(); i++) {
				white_pieces[i] = big_white_sprite.getSubimage(i * TILE_SIZE, 0, TILE_SIZE, TILE_SIZE);
				black_pieces[i] = big_black_sprite.getSubimage(i * TILE_SIZE, 0, TILE_SIZE, TILE_SIZE);
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

/**
	* This class handles the drawing of the individual pieces.
*/
class DrawGame extends JPanel 
{
	Game game = new Game(this);
	ImgLoader imgs = new ImgLoader();
	
	public DrawGame() { }
	
	/**
		* Overriding paintComponent to paint individual pieces
		* The Graphics object is typecast to Graphics2D and all work is done with that.
		* Also calls the Game classes draw() method.
	*/
	public void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g; 
		
		g2.drawImage(imgs.board, 0, 0, this); //draw the board, then everything else over it
		
		//draw the pieces
		int o, i;
		for (o = 0; o < Board.BOARD_LENGTH; o++) {
			for (i = 0; i < Board.BOARD_LENGTH; i++) { //iterate through the board and display images that need it
				Piece p = game.board.board[o][i];
				if (p != null) {
					if (p.piece_color == Piece.COLORS.WHITE) {
						g2.drawImage(imgs.white_pieces[p.piecenum], p.x, p.y, this);
					} else { //black
						g2.drawImage(imgs.black_pieces[p.piecenum], p.x, p.y, this);
					}
				}
			}
		}
		game.draw(g);
		repaint();
	}
}

/**
	* Holds useful information about game logic.
*/
class Game
{
	public static final int NUM_PIECES = 6;
	public enum PIECES {
		KING, QUEEN, ROOK, BISHOP, KNIGHT, PAWN
	};
	public static Board board = new Board();
	
	public Piece white_player[]; // array of white players' pieces
	public Piece black_player[]; // array of black players' pieces
	
	public Piece.COLORS turn_color = Piece.COLORS.BLACK; // keep track of whose turn it is
	
	static Piece current_piece = null; // currently selected piece (has been clicked)
	static Piece old_current_piece = null; //old piece to check for consecutive clicks
	
	int previous_gridx = -1; //dummy values so that they don't match anything at start
	int previous_gridy = -1; //used to check for consecutive mouse clicks
	
	public static String UpdateString = new String(); // draw text updates(captures,moves,etc.)
	
	public Game() { this(new DrawGame()); }
	
	/**
		* Constructor initializes pieces to a new game starting position.
	*/
	public Game(final DrawGame dg)
	{
		white_player = new Piece[16]; // 16 is the default amount of starting pieces
		black_player = new Piece[16];
		
		//init pieces
		int i;
		for (i = 0; i < 2; i++) {
			//rooks
			white_player[i] = new Piece(i * 434, 0, Game.PIECES.ROOK.ordinal(), Piece.COLORS.WHITE, this);
			black_player[i] = new Piece(i * 434, 434, Game.PIECES.ROOK.ordinal(), Piece.COLORS.BLACK, this);
			
			//knights
			white_player[i + 2] = new Piece(ImgLoader.TILE_SIZE + i * 310, 0, Game.PIECES.KNIGHT.ordinal(), Piece.COLORS.WHITE, this);
			black_player[i + 2] = new Piece(ImgLoader.TILE_SIZE + i * 310, 434, Game.PIECES.KNIGHT.ordinal(), Piece.COLORS.BLACK, this);
			
			//bishops
			white_player[i + 4] = new Piece((ImgLoader.TILE_SIZE * 2) + i * 186, 0, Game.PIECES.BISHOP.ordinal(), Piece.COLORS.WHITE, this);
			black_player[i + 4] = new Piece((ImgLoader.TILE_SIZE * 2) + i * 186, 434, Game.PIECES.BISHOP.ordinal(), Piece.COLORS.BLACK, this);
		}
		//queens
		white_player[6] = new Piece((ImgLoader.TILE_SIZE * 3), 0, Game.PIECES.QUEEN.ordinal(), Piece.COLORS.WHITE, this);
		black_player[6] = new Piece((ImgLoader.TILE_SIZE * 3), 434, Game.PIECES.QUEEN.ordinal(), Piece.COLORS.BLACK, this);
		
		//kings
		white_player[7] = new Piece((ImgLoader.TILE_SIZE * 4), 0, Game.PIECES.KING.ordinal(), Piece.COLORS.WHITE, this);
		black_player[7] = new Piece((ImgLoader.TILE_SIZE * 4), 434, Game.PIECES.KING.ordinal(), Piece.COLORS.BLACK, this);
		
		//pawns
		for (i = 0; i < 8; i++) {
			white_player[8 + i] = new Piece(i * ImgLoader.TILE_SIZE, ImgLoader.TILE_SIZE, Game.PIECES.PAWN.ordinal(), Piece.COLORS.WHITE, this);
			black_player[8 + i] = new Piece(i * ImgLoader.TILE_SIZE, 372, Game.PIECES.PAWN.ordinal(), Piece.COLORS.BLACK, this);
		}
		
		// handle mouse clicks
		dg.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e)
			{
				int x = e.getX();
				int y = e.getY();
				
				//we only want to know if it's on the board, the board ends at x = 497
				if (x >= 497) {
					// past the board
				} else { // within the board
					
					//normalize coordinates to TILE_SIZExTILE_SIZE (62x62) grid
					int xgrid = x - (x % ImgLoader.TILE_SIZE);
					int ygrid = y - (y % ImgLoader.TILE_SIZE);
					
					current_piece = board.getPiece(xgrid / ImgLoader.TILE_SIZE, ygrid / ImgLoader.TILE_SIZE);
					
					if (previous_gridx != xgrid || previous_gridy != ygrid) { // using && here instead of || is incorrect and will lead to a column being ignored just because a row wasn't valid
						if (previous_gridx != -1 && previous_gridy != -1) { // we're also not on a first pass
							old_current_piece = board.getPiece(previous_gridx / ImgLoader.TILE_SIZE, previous_gridy / ImgLoader.TILE_SIZE);
							if (current_piece == null) {// current selected square is empty
								if (old_current_piece != null) {
									if (old_current_piece.CanIMoveTo(xgrid / ImgLoader.TILE_SIZE, ygrid / ImgLoader.TILE_SIZE)) { //only move the piece if the move is in the valid_moves array
										
										if (turn_color == old_current_piece.piece_color) {
											UpdateString += ("<" + old_current_piece.piece_color.toString() + "> " + PIECES.values()[old_current_piece.piecenum].toString() + "\n" +
																	 " moves to " + ((xgrid / ImgLoader.TILE_SIZE) + 1) + "," + ((ygrid / ImgLoader.TILE_SIZE) + 1) + "\n");
											
											old_current_piece.move(xgrid, ygrid, dg.game);
											current_piece = old_current_piece;
											nextTurn();
										}
									}
								}
							} else { // currently selected square is NOT empty, check if we're trying to attack
								if (old_current_piece != null) {
									if (old_current_piece.piece_color != current_piece.piece_color) { // opposing colors
										if (old_current_piece.CanIAttack(current_piece.x / ImgLoader.TILE_SIZE, current_piece.y / ImgLoader.TILE_SIZE)) {
											if (turn_color == old_current_piece.piece_color) {
												UpdateString += "<" + old_current_piece.piece_color.toString() + "> " + PIECES.values()[old_current_piece.piecenum].toString() + "\n" +
																	 " takes over " + ((xgrid / ImgLoader.TILE_SIZE) + 1) + "," + ((ygrid / ImgLoader.TILE_SIZE) + 1) + "\n";
												
												old_current_piece.move(xgrid, ygrid, dg.game);
												current_piece = old_current_piece;
												nextTurn();
											}
											
										}
									}
									old_current_piece = null;
								}
							}
						}
					} 
					if (current_piece != null) {
						previous_gridx = xgrid;
						previous_gridy = ygrid;
					} else {
						current_piece = null;
					}
				}
			}
		});
		// set the default string
		UpdateString += "Black player starts\n";
	}
	
	public void nextTurn()
	{
		if (turn_color == Piece.COLORS.WHITE)
			turn_color = Piece.COLORS.BLACK;
		else
			turn_color = Piece.COLORS.WHITE;
	}
	
	/**
		* called from DrawGame()'s paintComponent method with that Graphics object.
		* So we'll highlight the currently selected grid square and some other eye candy. 
	*/
	public void draw (Graphics g)
	{
		int i; //for loop counting
		if (current_piece != null) {
			g.setColor(Color.GREEN);
			g.drawRoundRect(current_piece.x, current_piece.y, ImgLoader.TILE_SIZE, ImgLoader.TILE_SIZE, 10, 10);
			
			// draw a white border around possible move locations
			g.setColor(Color.WHITE);
			
			current_piece.UpdateMoves(this);
			for (i = 0; i < current_piece.valid_moves.length; i++) {
				Point p = current_piece.valid_moves[i];
				if (p != null) {
					g.drawRoundRect(p.x * ImgLoader.TILE_SIZE, p.y * ImgLoader.TILE_SIZE, ImgLoader.TILE_SIZE, ImgLoader.TILE_SIZE, 10, 10);
				}	
			}
			// draw a border around possible prey
			if (current_piece.valid_attack_amount > 0) {
				g.setColor(Color.MAGENTA);
				for (i = 0; i < current_piece.valid_attack_amount; i++) {
					Point attackp = current_piece.valid_attack_moves[i];
					if (attackp != null)
						g.drawOval(attackp.x * ImgLoader.TILE_SIZE, attackp.y * ImgLoader.TILE_SIZE, ImgLoader.TILE_SIZE, ImgLoader.TILE_SIZE);
				}
			}
		}
		g.setColor(Color.BLACK);
		//split up UpdateString based on newlines and draw them individually
		i = 0;
		for (String part : UpdateString.split("\n")) {
			g.drawString(part, 500, 200 + (g.getFontMetrics().getHeight() * i));
			i++;
		}
		if (i > 18) UpdateString = ""; // reset the UpdateString if we get too far down
	}
}

/**
	* Holds useful information concerning individual pieces.
*/
class Piece
{
	public int x = 186; // these are set to the middle of the board for startup purposes
	public int y = 186; // otherwise we'll set 0,0 on the board to null the first time
	public int piecenum;
	public int total_moves = -1; //total moves for this piece; start at -1 because we call move() at startup
	
	public enum COLORS {
		BLACK, WHITE
	};
	public COLORS piece_color;
	
	public Point valid_moves[];
	public Point valid_attack_moves[];
	public int valid_attack_amount = 0; //we need to know this for drawing things around them
	
	public Piece() { }
	
	/**
		* @param xx x position
		* @param yy y position
		* @param pieceord integer value representing a place in the PIECES enum
		* @param pc the color of this piece
		* @param info a link to the main Game object
	*/
	public Piece(int xx, int yy, int pieceord, COLORS pc, Game info)
	{
		piecenum = pieceord;
		piece_color = pc;
		move(xx, yy, info);
		//because move() updates the main game UpdateString, we need to reset it here...
		info.UpdateString = "";
	}
	
	/**
		* checks the newx and newy parameter against the valid_moves array
		* returns true if the move can be done (the coordinates are in the valid_moves array)
		* returns false if the move cannot be done (no matching coordinates in the valid_moves array)
	*/
	public boolean CanIMoveTo(int newx, int newy)
	{
		int i;
		Point move = new Point(newx, newy);
		for (i = 0; i < valid_moves.length; i++) {
			if (move.equals(valid_moves[i]))
				return true;
		}
		//not in the valid moves array...
		return false;
	}
	
	/**
		* returns whether or not this piece can attack the piece on the board represented by newx, newy
	*/
	public boolean CanIAttack(int newx, int newy)
	{
		int i;
		Point move = new Point(newx, newy);
		for (i = 0; i < valid_attack_amount; i++) {
			if (move.equals(valid_attack_moves[i])) {
				return true;
			}
		}
		return false;
	}
	
	/**
		* Move a piece.
		* @param newx destination x position
		* @param newy destination y position
		* @param g link to the main Game object
	*/
	public void move(int newx, int newy, Game g)
	{
		g.board.Update(x / ImgLoader.TILE_SIZE, y / ImgLoader.TILE_SIZE, null); // set where the piece was to null
		g.board.Update(newx / ImgLoader.TILE_SIZE, newy / ImgLoader.TILE_SIZE, this); // update to the new location
		x = newx;
		y = newy;
		total_moves++;
		
		UpdateMoves(g);
	}
	
	/**
		* Update the valid_moves array
	*/
	public void UpdateMoves(Game g)
	{
		ValidMoveChecker.update(g, this);
	}
}

/**
	* Helper class to define important board characteristics.
	* The main Game object has one instance of this class representing the main game board.
*/
class Board
{
	public static final int BOARD_LENGTH = 8;
	public static Piece board[][] = new Piece[BOARD_LENGTH][BOARD_LENGTH]; //x,y board element contains a Piece or null
	
	public Board() 
	{ 
		int i, o;
		for (i = 0; i < BOARD_LENGTH; i++) { //initialize board to null on startup
			for (o = 0; o < BOARD_LENGTH; o++) {
				board[i][o] = null;
			}
		}
	}
	
	/**
		* Returns the Piece object at the grid square represented by x,y
		* @param x the x value of the grid to get
		* @param y the y value of the grid to get
	*/
	public Piece getPiece(int x, int y) 
	{
		return board[x][y];
	}
	
	/**
		* This function updates the board with a newly moved piece, and replaces the pieces old position with null
		* It is important that this function is called after every single move.
		* @param x the x value of the grid to assign
		* @param y the y value of the grid to assign
		* @param piece the Piece object to assign to the grid at x, y
	*/
	public void Update(int x, int y, Piece piece) // this should be called on every move to update where the pieces are on the board
	{
		board[x][y] = piece;
	}
	
	public void TakeOver(int srcx, int srcy, int destx, int desty)
	{
		board[destx][desty] = board[srcx][srcy];
		Arrays.fill(board[srcx][srcy].valid_moves, null);
		Update(srcx, srcy, null); // where we were is no longer!
	}
	
	/**
		* Saves the game to data/savegame.txt
	*/
	public void saveBoard() 
	{
		try {
				BufferedWriter f = new BufferedWriter(new FileWriter("data/savegame.txt"));
				int o, i;
				Board b = Game.board; // a board is always 8x8 = 64 tiles
				
				for (o = 0; o < BOARD_LENGTH; o++) {
					for (i = 0; i < BOARD_LENGTH; i++) {
						if (b.board[o][i] == null) {
							f.write("null\r\n");
						} else { // valid piece
							Piece p = b.getPiece(o, i);
							if (p.piece_color == Piece.COLORS.WHITE) {
								f.write("w");
							} else { // must be black
								f.write("b");
							}
							f.write("[" + p.x + "," + p.y + "]:" + p.piecenum + "\r\n");
						}
					}
				}
				f.close();
			} catch (IOException io_exception) {
				io_exception.printStackTrace();
			}
	}
	
	/**
		* Loads the game from data/savegame.txt
		* The file must exist and be valid.
	*/
	public void loadBoard()
	{
		try {
				BufferedReader f = new BufferedReader(new FileReader("data/savegame.txt"));
				String line;
				Board mainboard = Game.board;
				
				int o, i;
				for (o = 0; o < BOARD_LENGTH; o++) {
					for (i = 0; i < BOARD_LENGTH; i++) {
						line = f.readLine();
						if (line != null) {
							if (line.equals("null")) {
								board[o][i] = null;
							} else { // must be a valid piece...grab info
								Piece p = new Piece();
								
								if (line.charAt(0) == 'w') // white piece
									p.piece_color = Piece.COLORS.WHITE;
								else if (line.charAt(0) == 'b')
									p.piece_color = Piece.COLORS.BLACK;
									
								p.x = Integer.parseInt(line.substring(line.indexOf('[', 0) + 1, line.indexOf(',', 0)));
								p.y = Integer.parseInt(line.substring(line.indexOf(',', 0) + 1, line.indexOf(']', 0))); 
								
								p.piecenum = Integer.parseInt(line.substring(line.indexOf(":", 0) + 1));
								
								if (line.charAt(0) == 'w') {// white piece
									p.piece_color = Piece.COLORS.WHITE;
								} else if (line.charAt(0) == 'b') {
									p.piece_color = Piece.COLORS.BLACK;
								}
								
								//assign the newly loaded piece to the board
								board[o][i] = p;
							}
						}
					}
				}
				f.close();
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
	}
}


/**
	* Brings the windows into existence, assign buttons, and get the game started.
*/
class GameRunner
{
	static int screen_x = 650;
	static int screen_y = 535;
	
	public static JButton saveButton;
	public static JButton loadButton;
		
	public static void main(String[] args)
	{
		DrawGame dg = new DrawGame();
		JFrame win = new JFrame();
		win.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		win.add(dg);
		win.setTitle("Chess by ebx");
		win.setSize(screen_x, screen_y);
		
		//create save/load buttons
		ButtonListener bListen = new ButtonListener();
		
		saveButton = new JButton("Save game");
		saveButton.addActionListener(bListen);
		saveButton.setBounds(515, 100, 100, 20); // board ends at 497
		dg.add(saveButton);
		
		loadButton = new JButton("Load game");
		loadButton.addActionListener(bListen);
		loadButton.setBounds(515, 150, 100, 20);
		dg.add(loadButton);
		
		dg.setLayout(null);
		win.setVisible(true);
	}
}