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

		GameTreeNode<GameConfig> bestNode;

		@Override
		public void makeMove(ScotlandYardView view, int location, Set<Move> moves, Consumer<Move> callback) {

			// Create a list of players for the current configuration to be passed into MiniMax method
			List<GameTreePlayer> players = createPlayers(view, location, null, 0, null);
			// Current configuration node
			GameTreeNode<GameConfig> gameTree = new GameTreeNode<>(new GameConfig(view, players, null));

			// Use MiniMax to figure out the best move to take
			minimax(view, players, 0, 0, gameTree, Integer.MIN_VALUE, Integer.MAX_VALUE);

			while (bestNode.getParent().getParent() != null)
					bestNode = bestNode.getParent();

			Move chosenMove = (bestNode.getData().getMove());
			System.out.println("TicketMove: " + chosenMove);

			Move newChosenMove = null;
			if ((secretMoveChooser(view, view.getCurrentRound())) && (view.getPlayerTickets(Colour.Black, Secret) > 0)) {
				if (chosenMove instanceof TicketMove)
					newChosenMove = new TicketMove(Colour.Black, Secret, ((TicketMove) chosenMove).destination());
				else if ((chosenMove instanceof DoubleMove) && view.getPlayerTickets(Colour.Black, Double) > 0)
				{
					boolean firstMoveSecret = secretMoveChooser(view, view.getCurrentRound());
					boolean secondMoveSecret = secretMoveChooser(view, view.getCurrentRound() + 1);
					if (firstMoveSecret && secondMoveSecret)
					{
						newChosenMove = new DoubleMove(Colour.Black, Secret, ((DoubleMove) chosenMove).firstMove().destination(), Secret, ((DoubleMove) chosenMove).finalDestination());
					}
					else if (!firstMoveSecret)
					{
						if (!secondMoveSecret)
						{
							newChosenMove = new DoubleMove(Colour.Black, ((DoubleMove) chosenMove).firstMove(), ((DoubleMove) chosenMove).secondMove());
						}
						else
						{
							newChosenMove = new DoubleMove(Colour.Black, ((DoubleMove) chosenMove).firstMove().ticket(), ((DoubleMove) chosenMove).firstMove().destination(), Secret, ((DoubleMove) chosenMove).finalDestination());
						}
					}
				}
				System.out.println("Secret/Double :" + newChosenMove);
				callback.accept(newChosenMove);
				return;
			}

			callback.accept(chosenMove);
		}


		private int minimax(ScotlandYardView view, List<GameTreePlayer> players, int level, int currentPlayer, GameTreeNode<GameConfig> currentNode, int alpha, int beta)
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

			//int bestScore = (currentPlayer == 0) ? Integer.MIN_VALUE : Integer.MAX_VALUE;
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
			//System.out.println(roundsToNextRevealRound);

			if (nextMoves.isEmpty() || level == roundsToNextRevealRound) { // Terminating condition (if round being evaluated is a reveal round)
				//System.out.println("minimax is returning: " + currentNode.getData().getMrXScore() + " when current player is : " + currentPlayer);
				bestNode = currentNode;
				return currentNode.getData().getMrXScore();
			}

			//nodeTotal = 0;
			//System.out.println(nextMoves.size());

			for (Move move : nextMoves) {

				if (move instanceof TicketMove) {
					if (!((TicketMove) move).ticket().equals(Secret)) {
						playersAfterMove = createPlayers(view, players.get(0).location(), players.get(currentPlayer).colour(), ((TicketMove) move).destination(), ((TicketMove) move).ticket());
						GameConfig newConfig = new GameConfig(view, playersAfterMove, move);
						currentNode.addChild(newConfig);
						//nodeTotal = nodeTotal + 1;
					}
				}
				else if (move instanceof DoubleMove){
					playersAfterMove = createPlayers(view, players.get(0).location(), players.get(currentPlayer).colour(), ((DoubleMove) move).finalDestination(), Double);
					GameConfig newConfig = new GameConfig(view, playersAfterMove, move);
					currentNode.addChild(newConfig);
				}
			}

			//System.out.println("Player : " + currentPlayer + " node total: " + nodeTotal);

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

					currentScore = minimax(view, playersAfterMove, level, currentPlayer + 1, childNode, alpha, beta);
					if (currentScore > alpha) alpha = currentScore;
					if (alpha <= beta) break;
					//System.out.println("Level : " + level + " Score : " + bestScore + " Player: " + currentPlayer);
				}
				return alpha;
			}
			else
			{
				for (GameTreeNode<GameConfig> childNode : currentNode.children())
				{
					List<GameTreePlayer> currentPlayerConfig = childNode.getData().getPlayers();
					Move currentMove = childNode.getData().getMove();

					playersAfterMove = createPlayers(view, currentPlayerConfig.get(0).location(), currentPlayerConfig.get(currentPlayer).colour(), ((TicketMove) currentMove).destination(), ((TicketMove) currentMove).ticket());

					if (currentPlayer == (players.size() - 1))
						currentScore = minimax(view, playersAfterMove, level + 1, 0, childNode, alpha, beta);
					else
						currentScore = minimax(view, playersAfterMove, level, currentPlayer + 1, childNode, alpha, beta);

					if (currentScore < beta) beta = currentScore;
					if (alpha <= beta) break;
				}
				return beta;
			}
		}


		private boolean secretMoveChooser(ScotlandYardView view, int roundToEvaluate) {
			double probSecretMove = 0.2;
			if (view.getCurrentRound() != 0) {
				if (view.getRounds().get(roundToEvaluate))
					probSecretMove = 0.7;
			}

			Random r = new Random();
			double randomDouble = r.nextDouble();

			return randomDouble < probSecretMove;
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

	}
}
