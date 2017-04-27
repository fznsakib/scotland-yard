package uk.ac.bris.cs.scotlandyard.ui.ai;

import java.util.ArrayList;
import java.util.Random;
import java.util.Set;
import java.util.function.Consumer;
import java.util.*;

import uk.ac.bris.cs.scotlandyard.ai.ManagedAI;
import uk.ac.bris.cs.scotlandyard.ai.PlayerFactory;
import uk.ac.bris.cs.scotlandyard.model.Colour;
import uk.ac.bris.cs.scotlandyard.model.Move;
import uk.ac.bris.cs.scotlandyard.model.Player;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYardView;

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
			List<GameTreePlayer> players = createPlayers(view, location, null, 0, null);
			GameTreeNode<GameConfig> gameTree = new GameTreeNode<>(new GameConfig(view, players, null));

			// Add a score to an array for each move possible
			for (Move move : moves)
			{
				if (move instanceof TicketMove) {
					MrXScoring scoreObject = new MrXScoring(view, ((TicketMove) move).destination(), players);
					movesWithScores.put(move, scoreObject.totalScore());
				}
				else if (move instanceof DoubleMove) {
					MrXScoring scoreObject = new MrXScoring(view, ((DoubleMove) move).finalDestination(), players);
					movesWithScores.put(move, scoreObject.totalScore());
				}
			}

			//List<ScotlandYardPlayer> players = createPlayers(view);
			int bestScore = minimax(view, players, 1, 0, gameTree);
			System.out.println("RESULT! Best Score = " + bestScore);

			// Create a map that has all the best scoring moves for each transport
			//Map<String, Set<Move>> bestTicketMoves = findBestTicketMoves(moves, movesWithScores, bestScore);
			Set<Move> bestMoves = new HashSet<>();
			for (Move move : moves)
			{
				if (movesWithScores.get(move) == bestScore)
					bestMoves.add(move);
			}

			// Deduce a move from the list of best moves taking into account current game factors such as round,
			// number of tickets left, etc
			// Move chosenMove = chooseFromBestMoves(view, location, bestTicketMoves, players);

			Random r = new Random();
			Move chosenMove = new ArrayList<>(bestMoves).get(r.nextInt(bestMoves.size()));

			callback.accept(chosenMove);
		}

		private int minimax(ScotlandYardView view, List<GameTreePlayer> players, int level, int currentPlayer, GameTreeNode<GameConfig> currentNode)
		{

			Set<Move> nextMoves;
			List<GameTreePlayer> playersAfterMove = new ArrayList<>();


			// Find valid moves and their scores depending on which player it is
			if (players.get(currentPlayer).isMrX()) {
				nextMoves = new MrXValidMovesFinder(view, players.get(0)).findValidMoves();
			}
			else {
				nextMoves = new DetectiveValidMovesFinder(view, players.get(currentPlayer)).findValidMoves();
			}

			int bestScore = (currentPlayer == 0) ? Integer.MIN_VALUE : Integer.MAX_VALUE;
			int currentScore;
			int roundsToNextRevealRound = 0;

			for (int i = view.getCurrentRound(); i < view.getRounds().size(); i ++)
			{
				if (view.getRounds().get(i))
				{
					roundsToNextRevealRound =  (i + 1) - view.getCurrentRound();
					break;
				}
			}

			//roundsToNextRevealRound = 3;
			System.out.println(roundsToNextRevealRound);

			if (nextMoves.isEmpty() || level == roundsToNextRevealRound) { // Terminating condition (if round being evaluated is a reveal round)
				//System.out.println("minimax is returning: " + currentNode.getData().getMrXScore() + " when current player is : " + currentPlayer);
				return currentNode.getData().getMrXScore();
			}

			for (Move move : nextMoves) {

				if (move instanceof TicketMove) {
					playersAfterMove = createPlayers(view, players.get(0).location(), players.get(currentPlayer).colour(), ((TicketMove) move).destination(), ((TicketMove) move).ticket());
					GameConfig newConfig = new GameConfig(view, playersAfterMove, move);
					currentNode.addChild(newConfig);
				}

				//else if (move instanceof DoubleMove)
				//	playersAfterMove = createPlayers(view, players.get(0).location(), players.get(currentPlayer).colour(), ((DoubleMove) move).finalDestination(), Double);


				/*GameConfig newConfig = new GameConfig(view, playersAfterMove, move);
				currentNode.addChild(newConfig);*/
			}

			if (currentPlayer == 0)
			{
				for (GameTreeNode<GameConfig> childNode : currentNode.children())
				{
					List<GameTreePlayer> currentPlayerConfig = childNode.getData().getPlayers();
					Move currentMove = childNode.getData().getMove();

					if (currentMove instanceof TicketMove)
						playersAfterMove = createPlayers(view, currentPlayerConfig.get(0).location(), currentPlayerConfig.get(currentPlayer).colour(), ((TicketMove) currentMove).destination(), ((TicketMove) currentMove).ticket());
					else
						playersAfterMove = createPlayers(view, currentPlayerConfig.get(0).location(), currentPlayerConfig.get(currentPlayer).colour(), ((DoubleMove) currentMove).finalDestination(), Double);

					currentScore = minimax(view, playersAfterMove, level, currentPlayer + 1, childNode);
					if (currentScore > bestScore) bestScore = currentScore;
					//System.out.println("Level : " + level + " Score : " + bestScore + " Player: " + currentPlayer);
				}
				return bestScore;
			}
			else
			{
				for (GameTreeNode<GameConfig> childNode : currentNode.children())
				{
					List<GameTreePlayer> currentPlayerConfig = childNode.getData().getPlayers();
					Move currentMove = childNode.getData().getMove();

					playersAfterMove = createPlayers(view, currentPlayerConfig.get(0).location(), currentPlayerConfig.get(currentPlayer).colour(), ((TicketMove) currentMove).destination(), ((TicketMove) currentMove).ticket());

					if (currentPlayer == (players.size() - 1))
						currentScore = minimax(view, playersAfterMove, level + 1, 0, childNode);
					else
						currentScore = minimax(view, playersAfterMove, level, currentPlayer + 1, childNode);

					if (currentScore < bestScore) bestScore = currentScore;
					//System.out.println("Level : " + level + " Score : " + bestScore + " Player: " + currentPlayer);
				}
				return bestScore;
			}
		}


		private List<GameTreePlayer> createPlayers(ScotlandYardView view, int MrXLocation, Colour colourToMove, int colourDestination, Ticket colourTicket) {
			List<GameTreePlayer> players = new ArrayList<>();

			for (Colour colour : view.getPlayers())
			{
				Map<Ticket, Integer> playerTicketMap = getPlayerTicket(view, colour);

				if ((colour == colourToMove) && (colourDestination != 0))
				{
					int currentTicket = playerTicketMap.get(colourTicket);
					playerTicketMap.replace(colourTicket, currentTicket - 1);
					players.add(new GameTreePlayer(colour, colourDestination, playerTicketMap));
				}

				else if ((colour == Colour.Black) && (colourToMove != Colour.Black))
					players.add(new GameTreePlayer(colour, MrXLocation, playerTicketMap));

				else if ((colour != Colour.Black) && (colour != colourToMove))
					players.add(new GameTreePlayer(colour, view.getPlayerLocation(colour), playerTicketMap));
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

		private Map<String, Set<Move>> findBestTicketMoves(Set<Move> moves, Map<Move, Integer> movesWithScores, int bestScore)
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

		private Move chooseFromBestMoves(ScotlandYardView view, int location, Map<String, Set<Move>> bestTicketMoves, List<GameTreePlayer> players)
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
			int currentDistanceScore = new MrXScoring(view, location, players).distanceScore();
			if (currentDistanceScore < (4 * view.getPlayers().size()))
				probDoubleMove = 0.6;

			// Random double generated for probability
			Random r = new Random();
			double randomDouble = r.nextDouble();

			if ((randomDouble < probSecretMove) && (!bestTicketMoves.get("Secret").isEmpty())) {
				return new ArrayList<>(bestTicketMoves.get("Secret")).get(r.nextInt(bestTicketMoves.get("Secret").size()));
			}

			randomDouble = r.nextDouble();
			if ((randomDouble < probDoubleMove) && (!bestTicketMoves.get("Double").isEmpty())) {
				return new ArrayList<>(bestTicketMoves.get("Double")).get(r.nextInt(bestTicketMoves.get("Double").size()));
			}

			else if (!bestTicketMoves.get("Regular").isEmpty()) {
				return new ArrayList<>(bestTicketMoves.get("Regular")).get(r.nextInt(bestTicketMoves.get("Regular").size()));
			}

			else {
				randomDouble = r.nextDouble();
				if (randomDouble < 0.5 && (!bestTicketMoves.get("Secret").isEmpty()))
					return new ArrayList<>(bestTicketMoves.get("Secret")).get(r.nextInt(bestTicketMoves.get("Secret").size()));
				else
					return new ArrayList<>(bestTicketMoves.get("Double")).get(r.nextInt(bestTicketMoves.get("Double").size()));
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
