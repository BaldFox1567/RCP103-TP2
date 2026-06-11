public class Message {

    private int messageID;
    private int source;
    private int destination;
    private double sendTime;
    private double arrivedTime;

    public Message() {
        this.messageID = 0;
        this.source = 0;
        this.destination = 0;
    }

    public Message(int messageID, int source, int destination, double sendTime, double arrivedTime) {
        this.messageID = messageID;
        this.source = source;
        this.destination = destination;
        this.sendTime = sendTime;
        this.arrivedTime = arrivedTime;

    }

    // Getters
    public int GetMessageID() {
        return this.messageID;
    }

    public int GetSource() {
        return this.source;
    }

    public int GetDestination() {
        return this.destination;
    }

    public double GetSendTime() {
        return this.sendTime;
    }

    public double GetArrivedTime() {
        return this.arrivedTime;
    }

    // Setters
    public void SetMessageID(int messageID) {
        this.messageID = messageID;
    }

    public void SetSource(int source) {
        this.source = source;
    }

    public void SetDestination(int destination) {
        this.destination = destination;
    }

    public void serviceStart() {
        this.arrivedTime = 0.;
        this.sendTime = 0.0;
    }

    // Affichage
    public void PrintMessage() {
        System.out.println("Message");
        System.out.println("  messageID  : " + this.messageID);
        System.out.println("  source     : " + this.source);
        System.out.println("  destination: " + this.destination);
        System.out.println("  arrivedTime: " + this.arrivedTime);
        System.out.println("  sendTime   : " + this.sendTime);
    }
}
