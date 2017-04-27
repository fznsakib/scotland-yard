package uk.ac.bris.cs.scotlandyard.ui.ai;


import uk.ac.bris.cs.scotlandyard.model.Colour;
import uk.ac.bris.cs.scotlandyard.model.Player;
import uk.ac.bris.cs.scotlandyard.model.Ticket;

import java.util.HashMap;
import java.util.Map;

public class GameTreePlayer {

    private final Colour colour;
    private int location;
    private final Map<Ticket, Integer> tickets;

    public GameTreePlayer(Colour colour, int location, Map<Ticket, Integer> tickets) {
        this.colour = colour;
        this.location = location;
        this.tickets = new HashMap(tickets);
    }


    public Colour colour() {
        return this.colour;
    }

    public boolean isMrX() {
        return this.colour.isMrX();
    }

    public boolean isDetective() {
        return this.colour.isDetective();
    }

    public void location(int location) {
        this.location = location;
    }

    public int location() {
        return this.location;
    }

    public Map<Ticket, Integer> tickets() {
        return this.tickets;
    }

    public void addTicket(Ticket ticket) {
        this.adjustTicketCount(ticket, 1);
    }

    public void removeTicket(Ticket ticket) {
        this.adjustTicketCount(ticket, -1);
    }

    private void adjustTicketCount(Ticket ticket, int by) {
        Integer ticketCount = (Integer)this.tickets.get(ticket);
        ticketCount = ticketCount + by;
        this.tickets.remove(ticket);
        this.tickets.put(ticket, ticketCount);
    }

    public boolean hasTickets(Ticket ticket) {
        return this.tickets.get(ticket) != 0;
    }

    public boolean hasTickets(Ticket ticket, int quantityInclusive) {
        return (Integer) this.tickets.get(ticket) >= quantityInclusive;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("ScotlandYardPlayer{");
        sb.append(", colour=").append(this.colour);
        sb.append(", location=").append(this.location);
        sb.append(", tickets=").append(this.tickets);
        sb.append('}');
        return sb.toString();
    }
}

