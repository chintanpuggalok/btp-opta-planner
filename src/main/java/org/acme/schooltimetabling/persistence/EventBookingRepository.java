package org.acme.schooltimetabling.persistence;

import org.springframework.stereotype.Repository;

import java.security.Key;
import java.time.LocalDateTime;
import java.util.List;
import org.acme.schooltimetabling.domain.EventBooking;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.repository.MongoRepository;
// import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.core.query.Query;
// import org.springframework.data.mongodb.repository.Query;
@Repository
public interface EventBookingRepository extends MongoRepository<EventBooking, String> {
    
    // List<EventBooking> findAll(Query query);
    
    
    
}
