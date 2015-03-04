/**
 * ClueReasoner.java - project skeleton for a propositional reasoner
 * for the game of Clue.  Unimplemented portions have the comment "TO
 * BE IMPLEMENTED AS AN EXERCISE".  The reasoner does not include
 * knowledge of how many cards each player holds.  See
 * http://cs.gettysburg.edu/~tneller/nsf/clue/ for details.
 *
 * @author Todd Neller
 * @version 1.0
 *

Copyright (C) 2005 Todd Neller

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

Information about the GNU General Public License is available online at:
  http://www.gnu.org/licenses/
To receive a copy of the GNU General Public License, write to the Free
Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
02111-1307, USA.

 */

import java.io.*;
import java.util.*;

public class ClueReasoner 
{
    private int numPlayers;
    private int playerNum;
    private int numCards;
    private SATSolver solver;    
    private String caseFile = "cf";
    private String[] players = {"sc", "mu", "wh", "gr", "pe", "pl"};
    private String[] suspects = {"mu", "pl", "gr", "pe", "sc", "wh"};
    private String[] weapons = {"kn", "ca", "re", "ro", "pi", "wr"};
    private String[] rooms = {"ha", "lo", "di", "ki", "ba", "co", "bi", "li", "st"};
    private String[] cards;

    public ClueReasoner()
    {
        numPlayers = players.length;

        // Initialize card info
        cards = new String[suspects.length + weapons.length + rooms.length];
        int i = 0;
        for (String card : suspects)
            cards[i++] = card;
        for (String card : weapons)
            cards[i++] = card;
        for (String card : rooms)
            cards[i++] = card;
        numCards = i;

        // Initialize solver
        solver = new SATSolver();
        addInitialClauses();
    }

    private int getPlayerNum(String player) 
    {
        if (player.equals(caseFile))
            return numPlayers;
        for (int i = 0; i < numPlayers; i++)
            if (player.equals(players[i]))
                return i;
        System.out.println("Illegal player: " + player);
        return -1;
    }

    private int getCardNum(String card)
    {
        for (int i = 0; i < numCards; i++)
            if (card.equals(cards[i]))
                return i;
        System.out.println("Illegal card: " + card);
        return -1;
    }

    private int getPairNum(String player, String card) 
    {
        return getPairNum(getPlayerNum(player), getCardNum(card));
    }

    private int getPairNum(int playerNum, int cardNum)
    {
        return playerNum * numCards + cardNum + 1;
    }    

    public void addInitialClauses() 
    {
        // TO BE IMPLEMENTED AS AN EXERCISE
        
        // Each card is in at least one place (including case file).
        for (int c = 0; c < numCards; c++) {
            int[] clause = new int[numPlayers + 1];
            for (int p = 0; p <= numPlayers; p++)
                clause[p] = getPairNum(p, c);
            solver.addClause(clause);
        }    
        
        // If a card is one place, it cannot be in another place.
        for (int c = 0; c < numCards; c++){
		for (int p = 0; p <= numPlayers; p++){
			for (int pp = 0; pp <= numPlayers; pp++){
				if (p != pp){
					int[] clause = {-1 * getPairNum(p,c), -1 * getPairNum(pp,c)};
					solver.addClause(clause);
				}
			}
		}
	}

        // At least one card of each category is in the case file.
	int[] sclause = new int[suspects.length];	
	for (int s = 0; s < suspects.length; s++)
		sclause[s] = getPairNum(caseFile, suspects[s]);
	solver.addClause(sclause);

	int[] wclause = new int[weapons.length];	
	for (int w = 0; w < weapons.length; w++)
		wclause[w] = getPairNum(caseFile, weapons[w]);
	solver.addClause(wclause);

	int[] rclause = new int[rooms.length];	
	for (int r = 0; r < rooms.length; r++)
		rclause[r] = getPairNum(caseFile, rooms[r]);
	solver.addClause(rclause);
            
        // No two cards in each category can both be in the case file.
	for (String s: suspects){
		for (String ss: suspects){
			if (!s.equals(ss)){
				int[] clause = {-1 * getPairNum(caseFile,s), -1 * getPairNum(caseFile,ss)};
				solver.addClause(clause);
			}
		}
	}
	for (String w: weapons){
		for (String ww: weapons){
			if (!w.equals(ww)){
				int[] clause = {-1 * getPairNum(caseFile,w), -1 * getPairNum(caseFile,ww)};
				solver.addClause(clause);
			}
		}
	}
	for (String r: rooms){
		for (String rr: rooms){
			if (!r.equals(rr)){
				int[] clause = {-1 * getPairNum(caseFile,r), -1 * getPairNum(caseFile,rr)};
				solver.addClause(clause);
			}
		}
	}

    }
        
    public void hand(String player, String[] cards) 
    {
	for (String c: cards){
		int[] clause = {getPairNum(player, c)};
		solver.addClause(clause);
	}

        // TO BE IMPLEMENTED AS AN EXERCISE
    }

    public void suggest(String suggester, String card1, String card2, 
                        String card3, String refuter, String cardShown) 
    {
	int sugg = getPlayerNum(suggester);
	
	if (refuter == null){
		for (String p: players){
			if (!p.equals(suggester)){
				int[] clause = {-1 * getPairNum(p, card1)};
				solver.addClause(clause);
				clause[0] = -1 * getPairNum(p, card2);
				solver.addClause(clause);
				clause[0] = -1 * getPairNum(p, card3);
				solver.addClause(clause);
			}
		}
	}
	else{
		int x = (sugg + 1) % numPlayers;
		while (!players[x].equals(refuter)){
			int[] clause = {-1 * getPairNum(players[x], card1)};
			solver.addClause(clause);
			clause[0] = -1 * getPairNum(players[x], card2);
			solver.addClause(clause);
			clause[0] = -1 * getPairNum(players[x], card3);
			solver.addClause(clause);

			x = (x + 1) % numPlayers;
		}
		if (cardShown == null){
			int[] clause = {getPairNum(refuter, card1),
					getPairNum(refuter, card2),
					getPairNum(refuter, card3)};
			solver.addClause(clause);
		}
		else{
			int[] clause = {getPairNum(refuter, cardShown)};
			solver.addClause(clause);
		}
	}
	
        // TO BE IMPLEMENTED AS AN EXERCISE
    }

    public void accuse(String accuser, String card1, String card2, 
                       String card3, boolean isCorrect)
    {
	if (isCorrect){
		int[] clause = {getPairNum(caseFile, card1)};
		solver.addClause(clause);
		clause[0] = getPairNum(caseFile, card2);
		solver.addClause(clause);
		clause[0] = getPairNum(caseFile, card3);
		solver.addClause(clause);
	}
	else {
		int[] clause = {-1 * getPairNum(caseFile, card1),
				-1 * getPairNum(caseFile, card2),
				-1 * getPairNum(caseFile, card3)};
		solver.addClause(clause);
	}

	int[] clause = {-1 * getPairNum(accuser, card1)};
	solver.addClause(clause);
	clause[0] = -1 * getPairNum(accuser, card2);
	solver.addClause(clause);
	clause[0] = -1 * getPairNum(accuser, card3);
	solver.addClause(clause);

        // TO BE IMPLEMENTED AS AN EXERCISE
    }

    public int query(String player, String card) 
    {
        return solver.testLiteral(getPairNum(player, card));
    }

    public String queryString(int returnCode) 
    {
        if (returnCode == SATSolver.TRUE)
            return "Y";
        else if (returnCode == SATSolver.FALSE)
            return "n";
        else
            return "-";
    }
        
    public void printNotepad() 
    {
        PrintStream out = System.out;
        for (String player : players)
            out.print("\t" + player);
        out.println("\t" + caseFile);
        for (String card : cards) {
            out.print(card + "\t");
            for (String player : players) 
                out.print(queryString(query(player, card)) + "\t");
            out.println(queryString(query(caseFile, card)));
        }
    }
        
    public static void main(String[] args) 
    {
        ClueReasoner cr = new ClueReasoner();
        String[] myCards = {"wh", "li", "st"};
        cr.hand("sc", myCards);
        cr.suggest("sc", "sc", "ro", "lo", "mu", "sc");
        cr.suggest("mu", "pe", "pi", "di", "pe", null);
        cr.suggest("wh", "mu", "re", "ba", "pe", null);
        cr.suggest("gr", "wh", "kn", "ba", "pl", null);
        cr.suggest("pe", "gr", "ca", "di", "wh", null);
        cr.suggest("pl", "wh", "wr", "st", "sc", "wh");
        cr.suggest("sc", "pl", "ro", "co", "mu", "pl");
        cr.suggest("mu", "pe", "ro", "ba", "wh", null);
        cr.suggest("wh", "mu", "ca", "st", "gr", null);
        cr.suggest("gr", "pe", "kn", "di", "pe", null);
        cr.suggest("pe", "mu", "pi", "di", "pl", null);
        cr.suggest("pl", "gr", "kn", "co", "wh", null);
        cr.suggest("sc", "pe", "kn", "lo", "mu", "lo");
        cr.suggest("mu", "pe", "kn", "di", "wh", null);
        cr.suggest("wh", "pe", "wr", "ha", "gr", null);
        cr.suggest("gr", "wh", "pi", "co", "pl", null);
        cr.suggest("pe", "sc", "pi", "ha", "mu", null);
        cr.suggest("pl", "pe", "pi", "ba", null, null);
        cr.suggest("sc", "wh", "pi", "ha", "pe", "ha");
        cr.suggest("wh", "pe", "pi", "ha", "pe", null);
        cr.suggest("pe", "pe", "pi", "ha", null, null);
        cr.suggest("sc", "gr", "pi", "st", "wh", "gr");
        cr.suggest("mu", "pe", "pi", "ba", "pl", null);
        cr.suggest("wh", "pe", "pi", "st", "sc", "st");
        cr.suggest("gr", "wh", "pi", "st", "sc", "wh");
        cr.suggest("pe", "wh", "pi", "st", "sc", "wh");
        cr.suggest("pl", "pe", "pi", "ki", "gr", null);
        cr.printNotepad();
        cr.accuse("sc", "pe", "pi", "bi", true);
    }           
}
