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
					System.out.println("Move: " + move + " distanceScore = " + scoreObject.distanceScore() + " totalScore = " + scoreObject.totalScore());
				}
				else if (move instanceof DoubleMove) {
					Scoring scoreObject = new Scoring(view, ((DoubleMove) move).finalDestination());
					movesWithScores.put(move, scoreObject.totalScore());
					System.out.println("Move: " + move + " distanceScore = " + scoreObject.distanceScore() + " totalScore = " + scoreObject.totalScore());
				}
			}

			// Create a map that has all the best scoring move for each transport
			Map<String, Set<Move>> bestTicketMoves = findBestTicketMoves(moves, movesWithScores);

			System.out.println("Choosing from best possible moves: " + bestTicketMoves);

			// Deduce a move from the list of best moves taking into account current game factors such as round,
			// number of tickets left, etc
			Move chosenMove = chooseFromBestMoves(view, location, bestTicketMoves, movesWithScores);

			System.out.println("Chosen move: " + chosenMove);

			callback.accept(chosenMove);
		}

		private Map<String, Set<Move>> findBestTicketMoves(Set<Move> moves, Map<Move, Integer> movesWithScores)
		{
			String[] ticketTypes = {"Regular", "Secret", "Double"};
			Map<String, Set<Move>> bestTicketMoves = new HashMap<>();

			for (String ticket : ticketTypes)
			{
				Set<Move> bestMoves = extractTicketMoves(moves, movesWithScores,  ticket);
				bestTicketMoves.put(ticket, bestMoves);
			}
			return bestTicketMoves;
		}

		private Move chooseFromBestMoves(ScotlandYardView view, int location, Map<String, Set<Move>> bestTicketMoves, Map<Move, Integer> movesWithScores)
		{
			Set<Move> allMoves = new HashSet<>();

			// Collecting all moves in one set
 			for (String ticket : bestTicketMoves.keySet())
			{
				allMoves.addAll(bestTicketMoves.get(ticket));
			}

			// If only one move with highest score, then choose that
			if (allMoves.size() == 1)
				return new ArrayList<>(allMoves).get(0);

 			// Probability of choosing a secret move increases if the current round is a reveal round
			double probSecretMove = 0.2;
			if (view.getCurrentRound() != 0) {
				if (view.isRevealRound())
					probSecretMove = 0.7;
			}

			// Probability of a choosing a double move increases if distance score is performing low
			// NOTE: AI considers a detective being 4 moves away as good enough reason to use a double move
			double probDoubleMove = 0.2;
			int currentDistanceScore = new Scoring(view, location).distanceScore();
			if (currentDistanceScore < (4 * view.getPlayers().size()))
				probDoubleMove = 0.6;

			// Random double generated for probability
			Random r = new Random();
			double randomDouble = r.nextDouble();

			if ((randomDouble < probSecretMove) && (!bestTicketMoves.get("Secret").isEmpty()))
			{
				return new ArrayList<>(bestTicketMoves.get("Secret")).get(r.nextInt(bestTicketMoves.get("Secret").size()));
			}

			randomDouble = r.nextDouble();
			if ((randomDouble < probDoubleMove) && (!bestTicketMoves.get("Double").isEmpty()))
			{
				return new ArrayList<>(bestTicketMoves.get("Double")).get(r.nextInt(bestTicketMoves.get("Double").size()));
			}
			else
			{
				return new ArrayList<>(bestTicketMoves.get("Regular")).get(r.nextInt(bestTicketMoves.get("Regular").size()));
			}
		}

		private Set<Move> extractTicketMoves(Set<Move> moves, Map<Move, Integer> movesWithScores, String ticket)
		{
			int bestScore = 0;
			Set<Move> bestMoves = new HashSet<>();
			for (Move move : moves)
			{
				if ((move instanceof TicketMove) && (ticket.equals("Regular"))) {
					if ((((TicketMove) move).ticket().equals(Taxi)) || ((TicketMove) move).ticket().equals(Bus) || ((TicketMove) move).ticket().equals(Underground)){
						if ((movesWithScores.get(move) > bestScore)) {
							bestMoves.clear();
							bestScore = movesWithScores.get(move);
							bestMoves.add(move);
						} else if (movesWithScores.get(move) == bestScore) {
							bestMoves.add(move);
						}
					}
				}
				else if ((move instanceof TicketMove) && (ticket.equals("Secret"))) {
					if (((TicketMove) move).ticket().equals(Secret)){
						if ((movesWithScores.get(move) > bestScore)) {
							bestMoves.clear();
							bestScore = movesWithScores.get(move);
							bestMoves.add(move);
						} else if (movesWithScores.get(move) == bestScore) {
							bestMoves.add(move);
						}
					}
				}
				else if ((move instanceof DoubleMove) && (ticket.equals("Double"))){
						if ((movesWithScores.get(move) > bestScore)) {
							bestMoves.clear();
							bestScore = movesWithScores.get(move);
							bestMoves.add(move);
						} else if (movesWithScores.get(move) == bestScore) {
							bestMoves.add(move);
						}
				}
			}
			return bestMoves;
		}
	}
}
