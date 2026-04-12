package model;

public class Booking {
    private int bookingId;
    private int tripId;
    private int passengerId;
    private int seatNumber;
    private int pickupStopId;
    private int dropoffStopId;
    private double farePaid;
    private String bookingStatus;

    public Booking() {}

    public Booking(int bookingId, int tripId, int passengerId, int seatNumber, int pickupStopId, int dropoffStopId, double farePaid, String bookingStatus) {
        this.bookingId = bookingId;
        this.tripId = tripId;
        this.passengerId = passengerId;
        this.seatNumber = seatNumber;
        this.pickupStopId = pickupStopId;
        this.dropoffStopId = dropoffStopId;
        this.farePaid = farePaid;
        this.bookingStatus = bookingStatus;
    }

    // For INSERT (no bookingId yet, auto-incremented by DB)
    public Booking(int tripId, int passengerId, int seatNumber, int pickupStopId, int dropoffStopId, double farePaid, String bookingStatus) {
        this.tripId = tripId;
        this.passengerId = passengerId;
        this.seatNumber = seatNumber;
        this.pickupStopId = pickupStopId;
        this.dropoffStopId = dropoffStopId;
        this.farePaid = farePaid;
        this.bookingStatus = bookingStatus;
    }

    // Getters and Setters
    public int getBookingId() { 
        return bookingId; 
    }
    public void setBookingId(int bookingId) { 
        this.bookingId = bookingId; 
    }

    public int getTripId() { 
        return tripId; 
    }
    public void setTripId(int tripId) { 
        this.tripId = tripId; 
    }

    public int getPassengerId() { 
        return passengerId; 
    }
    public void setPassengerId(int passengerId) { 
        this.passengerId = passengerId; 
    }

    public int getSeatNumber() { 
        return seatNumber; 
    }
    public void setSeatNumber(int seatNumber) { 
        this.seatNumber = seatNumber; 
    }

    public int getPickupStopId() { 
        return pickupStopId; 
    }
    public void setPickupStopId(int pickupStopId) { 
        this.pickupStopId = pickupStopId; 
    }

    public int getDropoffStopId() { 
        return dropoffStopId; 
    }
    public void setDropoffStopId(int dropoffStopId) { 
        this.dropoffStopId = dropoffStopId; 
    }

    public double getFarePaid() { 
        return farePaid; 
    }
    public void setFarePaid(double farePaid) { 
        this.farePaid = farePaid; 
    }

    public String getBookingStatus() { 
        return bookingStatus; 
    }
    public void setBookingStatus(String bookingStatus) { 
        this.bookingStatus = bookingStatus; 
    }

    public String getFormattedId() { 
        return String.format("BKG%03d", bookingId); 
    }

    @Override
    public String toString() {
        return "Booking{" +
                "bookingId=" + bookingId +
                ", tripId=" + tripId +
                ", passengerId=" + passengerId +
                ", seatNumber=" + seatNumber +
                ", pickupStopId=" + pickupStopId +
                ", dropoffStopId=" + dropoffStopId +
                ", farePaid=" + farePaid +
                ", bookingStatus='" + bookingStatus + '\'' +
                '}';
    }
}