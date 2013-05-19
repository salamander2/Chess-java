import java.awt.Point;

import java.util.*;
import java.lang.*;

class ValidMoveChecker
{
	//define the total amount of possible moves a piece could (in a best case scenario) have
	public static final int PAWN_MOVES = 4;
	public static final int KING_MOVES = 8; // up, down, left, right, diagonals
	public static final int QUEEN_MOVES = 24;
	public static final int ROOK_MOVES = 18; // rooks can, theoretically, move: 6+6+6 (6 + 3 + 3?)
	public static final int BISHOP_MOVES = 18;
	public static final int KNIGHT_MOVES = 8;
	
	// flags for what to check for -- to be used as the last parameter to check_stoof(.., flag)
	public static final int DIAG_CHECK 			= 0x01;
	public static final int DIAG_CHECK_KING 	= 0x02;
	public static final int CROS_CHECK 			= 0x04;
	public static final int CROS_CHECK_KING 	= 0x08;
	public static final int L_CHECK				= 0x10;
	public static final int PAWN_CHECK			= 0x20;
	
	public static final Point L_check_additives[] = new Point[] { // add these values to xgrd, ygrd to get where to check for moves
		new Point(-1, 2), new Point(1, 2), // down 2 left 1 - down 2 right 1
		new Point(-1, -2), new Point(1, -2), //up 2 left 1 -- up 2 right 1 
		new Point(-2, -1), new Point(-2, 1), // left 2 up 1 -- left 2 down 1
		new Point(2, -1), new Point(2, 1) // right 2 up 1 -- right 2 down 1
	};
	
	public static final Point King_check_additives[] = new Point[] {
		new Point(1, 0), new Point(-1, 0), // right, left
		new Point(0, 1), new Point(0, -1), // below, above
		new Point(1, 1), new Point(1, -1), // upper right diag, lower right diag
		new Point(-1, 1), new Point(-1, -1), // upper left diag, lower left diag
	};
	
	/**
		@param g main game object
		@param p the piece whose valid_moves array is to be updates
		@param xgrd x grid location
		@param ygrd y grid location
		@param flag flag representing which ways to check
	*/
	public static void check_stoof(Game g, Piece p, int xgrd, int ygrd, final int flag) {
		int i, d; //loop counters
		int moves = 0;
		int testx, testy; // for less error due to redundancy
		boolean uld, urd, lld, lrd;	//upper left/right diag, lower left/lower right diag - for use in DIAG_CHECK
		boolean upc, dwc, lfc, ric;	//up, down, left, right check -- for use in CROS_CHECK
		boolean whtup, blkdwn;			//white up, black down -- for use in pawn checking
												//these values are used to check if it's valid
											  	//for us to check in that direction.
											  	//It's not valid if there's a piece in our way!
		
		List<Point> potential_prey = new ArrayList<Point>();
		
		if ((flag & DIAG_CHECK) != 0) {
			uld = urd = lld = lrd = true;	

			//we're just going to be lazy and check the board_length -- even though that many is impossible
			for (d = 1; d < (Board.BOARD_LENGTH); d++) { // note we start at 1!
				testx = xgrd - d;							 	// if we start at 0, we're checking against ourself
				testy = ygrd - d;
				
				if (isMoveValid(testx, testy) && uld) { //upper left diag && uld == true
					if (g.board.getPiece(testx, testy) == null) {
						//if (uld == true) // we still want to update possible attacks below
							p.valid_moves[moves++] = new Point(testx, testy);
					} else {//we no longer want to update valid_moves for this axis as a piece is blocking it
						attack_update(p, potential_prey, testx, testy);
						uld = false; // the 0th time will be us!
					}
				}
				testx = xgrd + d;
				testy = ygrd - d;
				if (isMoveValid(testx, testy) && urd) { //upper right diag && urd == true
					if (g.board.getPiece(testx, testy) == null) {
							p.valid_moves[moves++] = new Point(testx, testy);
					} else {
						attack_update(p, potential_prey, testx, testy);
						urd = false;
					}
				}
				testx = xgrd - d;
				testy = ygrd + d;
				if (isMoveValid(testx, testy) && lld) { //lower left diag && lld == true
					if (g.board.getPiece(testx, testy) == null) {
							p.valid_moves[moves++] = new Point(testx, testy);
					} else {
						attack_update(p, potential_prey, testx, testy);
						lld = false;
					}
				}
				testx = xgrd + d;
				testy = ygrd + d;
				if (isMoveValid(testx, testy) && lrd) { //lower right diag && lrd == true
					if (g.board.getPiece(testx, testy) == null) {
							p.valid_moves[moves++] = new Point(testx, testy);
					} else {
						attack_update(p, potential_prey, testx, testy);
						lrd = false;
					}
				}
			}
		}  
		if ((flag & CROS_CHECK) != 0) {	
			lfc = ric = upc = dwc = true;

			for (i = 1; i < Board.BOARD_LENGTH; i++) { // note that we start at 1!
				if (isMoveValid(xgrd - i, ygrd) && lfc) { //left && lfc == true
					if (g.board.getPiece(xgrd - i, ygrd) == null) {
							p.valid_moves[moves++] = new Point(xgrd - i, ygrd);
					} else {
						attack_update(p, potential_prey, xgrd - i, ygrd);
						lfc = false;
					}
				}
				if (isMoveValid(xgrd + i, ygrd) && ric) { //right && ric == true
					if (g.board.getPiece(xgrd + i, ygrd) == null) {
							p.valid_moves[moves++] = new Point(xgrd + i, ygrd);
					} else {
						attack_update(p, potential_prey, xgrd + i, ygrd);
						ric = false;
					}
				}
				if (isMoveValid(xgrd, ygrd + i) && dwc) { //down and dwc == true
					if (g.board.getPiece(xgrd, ygrd + i) == null) {
							p.valid_moves[moves++] = new Point(xgrd, ygrd + i);
					} else {
						attack_update(p, potential_prey, xgrd, ygrd + i);
						dwc = false;
					}
				}
				if (isMoveValid(xgrd, ygrd - i) && upc) { //up and upc == true
					if (g.board.getPiece(xgrd, ygrd - i) == null) {
							p.valid_moves[moves++] = new Point(xgrd, ygrd - i);
					} else {
						attack_update(p, potential_prey, xgrd, ygrd - i);
						upc = false;
					}
				}
			}
		}
		if ((flag & L_CHECK) != 0) {
			for (i = 0; i < L_check_additives.length; i++) {
				testx = xgrd + L_check_additives[i].x;
				testy = ygrd + L_check_additives[i].y;
				if (isMoveValid(testx, testy)) {
					if (g.board.getPiece(testx, testy) == null) 
						p.valid_moves[moves++] = new Point(testx, testy);
					else
						attack_update(p, potential_prey, testx, testy);
				}
			}
		}
		if ((flag & CROS_CHECK_KING) != 0) {
			for (i = 0; i < King_check_additives.length; i++) {
				testx = xgrd + King_check_additives[i].x;
				testy = ygrd + King_check_additives[i].y;
				if (isMoveValid(testx, testy)) {
					if (g.board.getPiece(testx, testy) == null)
						p.valid_moves[moves++] = new Point(testx, testy);
					else 
						attack_update(p, potential_prey, testx, testy);
				}
			}
		}
		if ((flag & PAWN_CHECK) != 0) {
			blkdwn = whtup = true;
			Piece testp = null; // for checking diagonal attacks... so messy...ugh..
			
			if (p.piece_color == Piece.COLORS.BLACK) {
				for (i = 1; i < 3; i++) { // note we start at 1
					if (isMoveValid(xgrd, ygrd - i) && blkdwn) { // i below && blkdwn
						if (g.board.getPiece(xgrd, ygrd - i) == null) {
							if (i == 1 || (i == 2 && p.total_moves == 0))
									p.valid_moves[moves++] = new Point(xgrd, ygrd - i);
						} else {
							blkdwn = false;
						}	
					}
				}
				//check for diagonal attacks
				testy = ygrd - 1;
				if (isMoveValid(xgrd - 1, testy)) { // left
					testp = g.board.getPiece(xgrd - 1, testy);
					if(testp != null) {
						attack_update(p, potential_prey, xgrd - 1, testy);
					}
				} 
				if (isMoveValid(xgrd + 1, testy)) { // right
					testp = g.board.getPiece(xgrd + 1, testy);
					if (testp != null) {
						attack_update(p, potential_prey, xgrd + 1, testy);
					}
				}
			} else { // the piece is white
				for (i = 1; i < 3; i++) { //we can possible move down twice; note the starting at 1!
					if (isMoveValid(xgrd, ygrd + i) && whtup) { //i below && whtup
						if (g.board.getPiece(xgrd, ygrd + i) == null) {// empty
							if (i == 1 || (i == 2 && p.total_moves == 0)) //only if we haven't moved before can we move two ahead
									p.valid_moves[moves++] = new Point(xgrd, ygrd + i);
						} else {
							whtup = false;
						}
					}
				}
				//check for diagonals
				testy = ygrd + 1;
				if (isMoveValid(xgrd - 1, testy)) { // left
					testp = g.board.getPiece(xgrd - 1, testy);
					if(testp != null) {
						attack_update(p, potential_prey, xgrd - 1, testy);
					}
				}
				if (isMoveValid(xgrd + 1, testy)) { // right
					testp = g.board.getPiece(xgrd + 1, testy);
					if (testp != null) {
						attack_update(p, potential_prey, xgrd + 1, testy);
					}
				}
			}
		}
		//the potential_prey list contains pieces of the same color at this moment...
		List<Point> really_real_prey_list = new ArrayList<Point>();
		if (potential_prey.size() != 0) {
			//iterate through the potential prey list, check the color against current piece color
			//only if it is different should we add it to the valid_attack_moves list
			for (i = 0; i < potential_prey.size(); i++) {
				Piece preyp = g.board.getPiece(potential_prey.get(i).x, potential_prey.get(i).y); // if we're here, preyp will never be null
				if (p.piece_color != preyp.piece_color) {
					really_real_prey_list.add(new Point(potential_prey.get(i).x, potential_prey.get(i).y));
				}
			}
		}
		 
		if (really_real_prey_list.size() != 0) {
			p.valid_attack_moves = really_real_prey_list.toArray(new Point[really_real_prey_list.size()]);
			p.valid_attack_amount = really_real_prey_list.size();
		} else {
			p.valid_attack_amount = 0;
		}
	}
	
	/**
		* helper function to update a specific pieces potential attack list
	*/
	private static void attack_update(Piece p, List<Point> al, int alx, int aly)
	{
		al.add(new Point(alx, aly));
	}
	
	/**
		* This function is called from a Pieces UpdateMoves() method.
		* It allocates the valid_moves array and calls check_stoof() with the proper flags.
		@param g a link to the main Game object
		@param p a link to the Piece object who called us
	*/ 
	public static void update(Game g, Piece p)
	{
		p.valid_attack_amount = 0;//reset attack amount
		if(p.valid_moves != null) Arrays.fill(p.valid_moves, null);
		
		int i; //loop counter

		int xgrd = p.x / ImgLoader.TILE_SIZE;
		int ygrd = p.y / ImgLoader.TILE_SIZE;
		
		
		if(p.piecenum == Game.PIECES.PAWN.ordinal()) { 
			p.valid_moves = new Point[PAWN_MOVES];
			for (i = 0; i < PAWN_MOVES; i++) p.valid_moves[i] = null;
			
			check_stoof(g, p, xgrd, ygrd, PAWN_CHECK);

		} else if (p.piecenum == Game.PIECES.KING.ordinal()) { // King piece
			p.valid_moves = new Point[KING_MOVES];
			for (i = 0; i < KING_MOVES; i++) p.valid_moves[i] = null;
			
			check_stoof(g, p, xgrd, ygrd, CROS_CHECK_KING);
			
		} else if (p.piecenum == Game.PIECES.QUEEN.ordinal()) {
			p.valid_moves = new Point[QUEEN_MOVES];
			for (i = 0; i < QUEEN_MOVES; i++) p.valid_moves[i] = null;
			
			//the queen is really just a rook and a bishop in one
			check_stoof(g, p, xgrd, ygrd, DIAG_CHECK | CROS_CHECK);
			
		} else if (p.piecenum == Game.PIECES.ROOK.ordinal()) {
			p.valid_moves = new Point[ROOK_MOVES];
			for (i = 0; i < ROOK_MOVES; i++) p.valid_moves[i] = null;
			
			check_stoof(g, p, xgrd, ygrd, CROS_CHECK);
			
		} else if (p.piecenum == Game.PIECES.BISHOP.ordinal()) {
			p.valid_moves = new Point[BISHOP_MOVES];
			for (i = 0; i < BISHOP_MOVES; i++) p.valid_moves[i] = null;
			
			check_stoof(g, p, xgrd, ygrd, DIAG_CHECK);
			
		} else if (p.piecenum == Game.PIECES.KNIGHT.ordinal()) {
			p.valid_moves = new Point[KNIGHT_MOVES];
			for (i = 0; i < KNIGHT_MOVES; i++) p.valid_moves[i] = null;
			
			check_stoof(g, p, xgrd, ygrd, L_CHECK);
		}
	}
	
	public static boolean isMoveValid(int xx, int yy)
	{
		if (xx < 0 || xx >= Board.BOARD_LENGTH) return false;
		if (yy < 0 || yy >= Board.BOARD_LENGTH) return false;
		
		return true;
	}
}