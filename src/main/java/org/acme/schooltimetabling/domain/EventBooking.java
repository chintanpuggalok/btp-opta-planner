package org.acme.schooltimetabling.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

import javax.annotation.Generated;
import javax.persistence.GeneratedValue;


@Document(collection = "eventBooking")
public class EventBooking {
    

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private String roomNo;
    private String bookedBy;
    private String eventDetails;

    public EventBooking( LocalDateTime startTime, LocalDateTime endTime, String roomNo, String bookedBy,
            String eventDetails) {
        
        this.startTime = startTime;
        this.endTime = endTime;
        this.roomNo = roomNo;
        this.bookedBy = bookedBy;
        this.eventDetails = eventDetails;
    }

    // public int getId() {
    //     return id;
    // }

    // public void setId(int id) {
    //     this.id = id;
    // }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public String getBookedBy() {
        return bookedBy;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public String getEventDetails() {
        return eventDetails;
    }

    public String getRoomNo() {
        return roomNo;
    }

    @Override
    public String toString() {
        return "eventBooking [ startTime=" + startTime + ", endTime=" + endTime + ", roomNo=" + roomNo
                + ", bookedBy=" + bookedBy + ", eventDetails=" + eventDetails + "]";
    }
}