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

			Map<Move, Integer> movesWithScores = new HashMap<>();
			Random r = new Random();
			PassMove passmove = new PassMove(view.getCurrentPlayer());
			List<GameTreePlayer> players = createPlayers(view);


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
					DetectiveScoring scoreObject = new DetectiveScoring(view, ((TicketMove) move).destination(), players);
					movesWithScores.put(move, scoreObject.totalScore());
					//System.out.println("Move: " + move + " distanceScore = " + scoreObject.distanceScore() + " totalScore = " + scoreObject.totalScore());
				}
			}

			// Find moves with best score
			Set<Move> bestTicketMoves = findBestTicketMoves(movesWithScores, moves);

			//System.out.println("Choosing from best possible moves: " + bestTicketMoves);

			// Since all moves at this point have the same score, choose one at random
			Move chosenMove = new ArrayList<>(bestTicketMoves).get(r.nextInt(bestTicketMoves.size()));

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

		private List<GameTreePlayer> createPlayers(ScotlandYardView view) {
			List<GameTreePlayer> players = new ArrayList<>();

			for (int i = 0; i < view.getPlayers().size(); i++)
			{
				Colour currentPlayerColour = view.getPlayers().get(i);
				Map<Ticket, Integer> playerTicketMap = getPlayerTicket(view, currentPlayerColour);
				players.add(new GameTreePlayer(currentPlayerColour, view.getPlayerLocation(currentPlayerColour), playerTicketMap));
			}
			return players;
		}

		private Map<Ticket,Integer> getPlayerTicket(ScotlandYardView view, Colour currentPlayerColour) {
			Map<Ticket, Integer> ticketMap = new HashMap<>();

			for (Ticket ticket : Arrays.asList(Ticket.values()))
			{
				ticketMap.put(ticket, view.getPlayerTickets(currentPlayerColour, ticket));
			}

			return ticketMap;
		}
	}
}
