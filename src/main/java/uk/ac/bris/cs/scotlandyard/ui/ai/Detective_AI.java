package uk.ac.bris.cs.scotlandyard.ui.ai;

import java.util.*;
import java.util.function.Consumer;

import org.apache.commons.lang3.ObjectUtils;
import uk.ac.bris.cs.scotlandyard.ai.ManagedAI;
import uk.ac.bris.cs.scotlandyard.ai.PlayerFactory;
import uk.ac.bris.cs.scotlandyard.model.*;

// Name the AI
@ManagedAI("Detective AI")
public class Detective_AI implements PlayerFactory {

	// Create a new player
	@Override
	public Player createPlayer(Colour colour) {
		return new MyPlayer();
	}

	// Sample player that selects a random move
	private static class MyPlayer implements Player {

		@Override
		public void makeMove(ScotlandYardView view, int location, Set<Move> moves, Consumer<Move> callback) {
			System.out.println("New round");

			Map<Move, Integer> movesWithScores = new HashMap<>();
			Random r = new Random();
			PassMove passmove = new PassMove(view.getCurrentPlayer());

			// If the only move available is a pass move then select that move
			if (moves.contains(passmove))
			{
				callback.accept(new ArrayList<>(moves).get(0));
				return;
			}

			if (view.getPlayerLocation(Colour.Black) == 0) {
				callback.accept(new ArrayList<>(moves).get(r.nextInt(moves.size())));
				return;
			}

			// Add a score to an array for each move possible
			for (Move move : moves) {
				if (move instanceof TicketMove) {
					DetectiveScoring scoreObject = new DetectiveScoring(view, ((TicketMove) move).destination());
					movesWithScores.put(move, scoreObject.totalScore());
					//System.out.println("Move: " + move + " distanceScore = " + scoreObject.distanceScore() + " totalScore = " + scoreObject.totalScore());
				}
			}

			// Find moves with best score
			Set<Move> bestTicketMoves = findBestTicketMoves(movesWithScores, moves);

			System.out.println("Choosing from best possible moves: " + bestTicketMoves);

			// Since all moves at this point have the same score, choose one at random
			Move chosenMove = new ArrayList<>(bestTicketMoves).get(r.nextInt(bestTicketMoves.size()));
			System.out.println("Chosen move: " + chosenMove + "at Round " + view.getCurrentRound());
			callback.accept(chosenMove);
		}

		private Set<Move> findBestTicketMoves(Map<Move, Integer> movesWithScores, Set<Move> moves)
		{
			int bestScore = Integer.MAX_VALUE;
			Set<Move> bestMoves = new HashSet<>();
			for (Move move : moves) {
				if ((movesWithScores.get(move) < bestScore)) {
					bestMoves.clear();
					bestScore = movesWithScores.get(move);
					bestMoves.add(move);
				} else if (movesWithScores.get(move) == bestScore) {
					bestMoves.add(move);
				}
			}
			return bestMoves;
		}
	}
}
