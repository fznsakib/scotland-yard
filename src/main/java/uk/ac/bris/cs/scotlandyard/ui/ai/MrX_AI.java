package uk.ac.bris.cs.scotlandyard.ui.ai;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;
import java.util.Set;
import java.util.function.Consumer;

import uk.ac.bris.cs.gamekit.graph.Edge;
import uk.ac.bris.cs.scotlandyard.ai.AIPool;
import uk.ac.bris.cs.scotlandyard.ai.ManagedAI;
import uk.ac.bris.cs.scotlandyard.ai.PlayerFactory;
import uk.ac.bris.cs.scotlandyard.ai.Visualiser;
import uk.ac.bris.cs.scotlandyard.model.Colour;
import uk.ac.bris.cs.scotlandyard.model.Move;
import uk.ac.bris.cs.scotlandyard.model.Player;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYardView;


import uk.ac.bris.cs.scotlandyard.model.*;

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
			// TODO do something interesting here; find the best move

			ArrayList<Integer> scoreForMoves = new ArrayList<>();

			// Add a score to an array for each move possible
			for (Move move : moves)
			{
				if (move instanceof TicketMove)
					scoreForMoves.add(score(view, ((TicketMove) move).destination()));
				else if (move instanceof DoubleMove)
					scoreForMoves.add(score(view, ((DoubleMove) move).finalDestination()));
			}

			// If no better scored moves are found, use least worst move
			int bestScore = scoreForMoves.get(0);
			for (int i = 1; i < scoreForMoves.size(); i++)
			{
				if (scoreForMoves.get(i) < bestScore)
					bestScore = scoreForMoves.get(i);
			}
			for (int i = 0; i < scoreForMoves.size(); i++)
			{
				if (scoreForMoves.get(i) == bestScore)
					callback.accept(new ArrayList<>(moves).get(i));
			}
		}

		private int score(ScotlandYardView view, int location)
		{
			// Calculates a score according to where the detectives are
			int score = 0;
			int detectiveLocation;

			// Get all nodes that are connected to MrX's current location
			Collection<Edge<Integer, Transport>> accessibleNodes = view.getGraph().getEdgesFrom(view.getGraph().getNode(location));

			// Cycles through each connected node to see if there is a detective there. If there is, add to the score
			for (int i = 1; i < view.getPlayers().size(); i ++)
			{
				detectiveLocation = view.getPlayerLocation(view.getPlayers().get(i));
				for (Edge <Integer, Transport> edge : accessibleNodes)
				{
					// Check if the connected node contains a detective
					if (edge.destination().equals(view.getGraph().getNode(detectiveLocation)))
					{
						score++;
					}
				}
			}
			return score;
		}
	}
}
