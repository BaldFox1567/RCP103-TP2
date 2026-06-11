public class Event {

    public static final String SEND_MSG = "SEND_MSG";
    public static final String RECV_MSG = "RECV_MSG";
    public static final String MSG_DEPT = "MSG_DEPT";

    private int eventID;
    private String eventType;
    private double eventTime;
    private Message message;

    public Event() {
        this.eventID = 0;
        this.eventType = "";
        this.eventTime = 0.0;
        this.message = null;
    }

    public Event(int eventID, String eventType, double eventTime, Message message) {
        this.eventID = eventID;

        if (IsValidEventType(eventType)) {
            this.eventType = eventType;
        } else {
            this.eventType = "";
        }

        this.eventTime = eventTime;
        this.message = message;
    }

    private boolean IsValidEventType(String eventType) {
        return eventType.equals(SEND_MSG)
                || eventType.equals(RECV_MSG)
                || eventType.equals(MSG_DEPT);
    }

    // Getters
    public int GetEventID() {
        return this.eventID;
    }

    public String GetEventType() {
        return this.eventType;
    }

    public double GetEventTime() {
        return this.eventTime;
    }

    public Message GetMessage() {
        return this.message;
    }

    // Setters
    public void SetEventID(int eventID) {
        this.eventID = eventID;
    }

    public void SetEventType(String eventType) {
        if (IsValidEventType(eventType)) {
            this.eventType = eventType;
        } else {
            this.eventType = "";
        }
    }

    public void SetEventTime(double eventTime) {
        this.eventTime = eventTime;
    }

    public void SetMessage(Message message) {
        this.message = message;
    }

    // Affichage
    public void PrintEvent() {
        System.out.println("Event");
        System.out.println("  eventID   : " + this.eventID);
        System.out.println("  eventType : " + this.eventType);
        System.out.println("  eventTime : " + this.eventTime);

        if (this.message != null) {
            System.out.println("  messageID : " + this.message.GetMessageID());
        } else {
            System.out.println("  messageID : None");
        }
    }
}