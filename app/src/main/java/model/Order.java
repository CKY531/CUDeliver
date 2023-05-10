package model;

public class Order {

    //Starting point latitude
    private double startLat;

    //Starting point longitude
    private double startLong;

    //Destination point latitude
    private double destinationLat;

    //Destination point longitude
    private double destinationLong;

    //Starting point name
    private String startName;

    //Destination name
    private String destinationName;

    //Starting time
    private String startTime;

    //Arrival Time
    private String arrTime;

    //Price offer
    private double price;

    //Contact
    private String contact;

    //The user who creates the order
    private String orderCreator;

    //The user who delivers the order
    private String orderDeliver;

    //Status of order: finish, delivering, expired, or pending
    private String status;

    public double getStartLat() {
        return startLat;
    }

    public void setStartLat(double startLat) {
        this.startLat = startLat;
    }

    public double getStartLong() {
        return startLong;
    }

    public void setStartLong(double startLong) {
        this.startLong = startLong;
    }

    public double getDestinationLat() {
        return destinationLat;
    }

    public void setDestinationLat(double destinationLat) {
        this.destinationLat = destinationLat;
    }

    public double getDestinationLong() {
        return destinationLong;
    }

    public void setDestinationLong(double destinationLong) {
        this.destinationLong = destinationLong;
    }

    public String getStartName() {
        return startName;
    }

    public void setStartName(String startName) {
        this.startName = startName;
    }

    public String getDestinationName() {
        return destinationName;
    }

    public void setDestinationName(String destinationName) {
        this.destinationName = destinationName;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getArrTime() {
        return arrTime;
    }

    public void setArrTime(String arrTime) {
        this.arrTime = arrTime;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getOrderCreator() {
        return orderCreator;
    }

    public void setOrderCreator(String orderCreator) {
        this.orderCreator = orderCreator;
    }

    public String getOrderDeliver() {
        return orderDeliver;
    }

    public void setOrderDeliver(String orderDeliver) {
        this.orderDeliver = orderDeliver;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Order(double startLat, double startLong, double destinationLat, double destinationLong, String startName, String destinationName, String startTime, String arrTime, double price, String contact, String orderCreator, String orderDeliver, String status) {
        this.startLat = startLat;
        this.startLong = startLong;
        this.destinationLat = destinationLat;
        this.destinationLong = destinationLong;
        this.startName = startName;
        this.destinationName = destinationName;
        this.startTime = startTime;
        this.arrTime = arrTime;
        this.price = price;
        this.contact = contact;
        this.orderCreator = orderCreator;
        this.orderDeliver = orderDeliver;
        this.status = status;
    }
}
