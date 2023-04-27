package org.acme.schooltimetabling.persistence;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.acme.schooltimetabling.domain.EventBooking;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

@Service
public class EventBookingService {
    @Autowired
    private EventBookingRepository repository;
    @Autowired
     MongoTemplate mongoTemplate;

    
    public EventBooking createBooking(ArrayList<String> roomNos,LocalDateTime startTime,LocalDateTime endTime,String bookedBy,String eventDetails)
    {
        List<EventBooking> bookings= new ArrayList<EventBooking>();
        for(String roomNo:roomNos )
        {
        
             bookings.addAll( getBookings(roomNo, startTime, endTime));
            if(bookings.size()>0)
            {
                return null;
            }
            
        }
        EventBooking eventBooking=null;
        for(String roomNo:roomNos )
        {
            eventBooking = new EventBooking(startTime,endTime,roomNo,bookedBy,eventDetails);
            repository.save(eventBooking);
        }
        // EventBooking eventBooking = new EventBooking(startTime,endTime,roomNo,bookedBy,eventDetails);
        return eventBooking;
    }
    public List<String> getFreeRooms(List<String> roomNos,LocalDateTime startTime,LocalDateTime endTime)
    {
        List<String> freeRooms=new ArrayList<String>(roomNos.size());
        for(String roomNo:roomNos)
        {
            List<EventBooking> bookings= getBookings(roomNo, startTime, endTime);
            if(bookings.size()==0)
            {
                freeRooms.add(roomNo);
            }
        }
        return freeRooms;
    }
    public List<EventBooking> getBookings(String roomNo,LocalDateTime startTime,LocalDateTime endTime)
    {
        System.out.println(startTime);
        System.out.println(endTime);
        Criteria criteria1 = Criteria.where("startTime").gte(startTime);
        Criteria criteria2 = Criteria.where("startTime").lte(endTime);
        criteria2.andOperator(criteria1);
        Criteria criteria3 = Criteria.where("endTime").gte(startTime);
        Criteria criteria4= Criteria.where("endTime").lte(endTime);
        criteria4.andOperator(criteria3);
        Criteria criteria5= Criteria.where("startTime").lte(startTime);
        Criteria criteria6= Criteria.where("endTime").gte(startTime);
        criteria6.andOperator(criteria5);
        Criteria criteria8= Criteria.where("startTime").lte(endTime);
        Criteria criteria9= Criteria.where("endTime").gte(endTime);
        criteria9.andOperator(criteria8);
        Criteria criteria7=new Criteria();
        criteria7.orOperator(criteria6,criteria2,criteria9);
        if(roomNo!="")
        {
            Criteria roomCriteria=Criteria.where("roomNo").is(roomNo);
            criteria7.andOperator(roomCriteria);

        }
            
        

        Query query=new Query(criteria7);
        // findAll()
        
        return mongoTemplate.find(query, EventBooking.class);
        // return repository.find
    }

    
public EventBooking deleteBooking(String id)
{
    EventBooking eventBooking = repository.findById(id).orElse(null);
    repository.delete(eventBooking);
    return eventBooking;

    
}
}
