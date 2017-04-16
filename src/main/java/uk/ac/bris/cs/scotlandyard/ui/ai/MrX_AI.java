package uk.ac.bris.cs.scotlandyard.ui.ai;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;
import java.util.Set;
import java.util.function.Consumer;
import java.util.*;
import uk.ac.bris.cs.gamekit.graph.Edge;
import uk.ac.bris.cs.scotlandyard.ai.AIPool;
import uk.ac.bris.cs.scotlandyard.ai.ManagedAI;
import uk.ac.bris.cs.scotlandyard.ai.PlayerFactory;
import uk.ac.bris.cs.scotlandyard.ai.Visualiser;
import uk.ac.bris.cs.scotlandyard.model.Colour;
import uk.ac.bris.cs.scotlandyard.model.Move;
import uk.ac.bris.cs.scotlandyard.model.Player;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYardView;
import uk.ac.bris.cs.scotlandyard.model.Transport;
import uk.ac.bris.cs.scotlandyard.model.Ticket;



import uk.ac.bris.cs.scotlandyard.model.*;

import static uk.ac.bris.cs.scotlandyard.model.Ticket.*;

// Name the AI
@ManagedAI("Mr X AI")
public class MrX_AI implements PlayerFactory {

	// Create a new player
	@Override
	public Player createPlayer(Colour colour) {
		return new MyPlayer();
	}

	// Sample player that selects a move
	private static class MyPlayer implements Player {

		@Override
		public void makeMove(ScotlandYardView view, int location, Set<Move> moves, Consumer<Move> callback) {

			Map<Move, Integer> movesWithScores = new HashMap<>();

			// Add a score to an array for each move possible
			for (Move move : moves)
			{
				if (move instanceof TicketMove) {
					Scoring scoreObject = new Scoring(view, ((TicketMove) move).destination());
					movesWithScores.put(move, scoreObject.totalScore());
				}
				else if (move instanceof DoubleMove) {
					Scoring scoreObject = new Scoring(view, ((DoubleMove) move).finalDestination());
					movesWithScores.put(move, scoreObject.totalScore());
				}
			}

			// Create a map that has all the best scoring move for each transport
			Map<Ticket, Set<Move>> bestTicketMoves = findBestTicketMoves(view, moves, movesWithScores);

			System.out.println("Choosing from best possible moves: " + bestTicketMoves);

			// Deduce a move from the list of best moves taking into account current game factors such as round,
			// number of tickets left, etc
			Move chosenMove = chooseFromBestMoves(bestTicketMoves);

			System.out.println("Chosen move: " + chosenMove);


			callback.accept(chosenMove);
		}

		private Map<Ticket, Set<Move>> findBestTicketMoves(ScotlandYardView view, Set<Move> moves, Map<Move, Integer> movesWithScores)
		{
			Ticket[] ticketTypes = {Taxi, Bus, Underground, Secret, Double};
			Map<Ticket, Set<Move>> bestTicketMoves = new HashMap<>();

			for (Ticket ticket : ticketTypes)
			{
				Set<Move> bestMoves = extractTicketMoves(moves, movesWithScores,  ticket);
				bestTicketMoves.put(ticket, bestMoves);
			}
			return bestTicketMoves;
		}

		private Move chooseFromBestMoves(Map<Ticket, Set<Move>> bestTicketMoves)
		{
			Set<Move> allMoves = new HashSet<>();

			// Collecting all moves in one set
 			for (Ticket ticket : bestTicketMoves.keySet())
			{
				allMoves.addAll(bestTicketMoves.get(ticket));
			}

			// If only one move with highest score, then choose that
			if (allMoves.size() == 1)
				return new ArrayList<>(allMoves).get(0);
			

		}

		private Set<Move> extractTicketMoves(Set<Move> moves, Map<Move, Integer> movesWithScores, Ticket ticket)
		{
			int bestScore = 0;
			Set<Move> bestMoves = new HashSet<>();
			for (Move move : moves)
			{
				if (move instanceof TicketMove) {
					if (((TicketMove) move).ticket().equals(ticket)) {
						if ((movesWithScores.get(move) > bestScore)) {
							bestMoves.clear();
							bestScore = movesWithScores.get(move);
							bestMoves.add(move);
						} else if (movesWithScores.get(move) == bestScore) {
							bestMoves.add(move);
						}
					}
				}
				else if (move instanceof DoubleMove){
					if (Double.equals(ticket)) {
						if ((movesWithScores.get(move) > bestScore)) {
							bestMoves.clear();
							bestScore = movesWithScores.get(move);
							bestMoves.add(move);
						} else if (movesWithScores.get(move) == bestScore) {
							bestMoves.add(move);
						}
					}
				}
			}
			return bestMoves;
		}
	}
}
