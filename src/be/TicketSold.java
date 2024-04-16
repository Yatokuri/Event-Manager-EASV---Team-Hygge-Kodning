package be;

public class TicketSold {
    private final String firstName, lastName, email;
    private final int ticketID, transactionID;

    public TicketSold(String firstName, String lastName, String email, int ticketID, int transactionID){
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.ticketID = ticketID;
        this.transactionID = transactionID;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public int getTicketID() {
        return ticketID;
    }

    public int getTransactionID() {
        return transactionID;
    }

    //Custom message to the tbl view over sold ticket
    public String getCustomUserInfo() {
        return " " + firstName + " " + lastName ;
    }
}