package org.acme.schooltimetabling.rest;
import org.acme.schooltimetabling.domain.EventBooking;
import org.acme.schooltimetabling.persistence.EventBookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.FileReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/eventBooking")

public class EventBookingController {

    @Autowired
    private EventBookingService eventBookingService;
    @CrossOrigin(maxAge = 3600)
	@PostMapping("/createBooking")
	public ResponseEntity createBooking(@RequestBody String body )
	{
        // String dateString = "2023-02-16T12:34:56"; // The string representation of the date and time
        System.out.println(body);
        Gson gson = new Gson();
        HashMap<String, Object> temp2 = gson.fromJson(body, HashMap.class);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"); // The format of the string
        LocalDateTime startTimeObj = null;
        LocalDateTime endTimeObj = null;

        try {
            // HashMap<String, Object> temp2 = gson.fromJson(timetable, HashMap.class);
        startTimeObj = LocalDateTime.parse( temp2.get("startTime").toString(), formatter); // Parse the string and convert it to a DateTime object
            endTimeObj = LocalDateTime.parse(temp2.get("endTime").toString(), formatter); // Parse the string and convert it to a DateTime object
            String bookedBy=temp2.get("bookedBy")==null?"":temp2.get("bookedBy").toString();
            String eventDetails=temp2.get("eventDetails")==null?"":temp2.get("eventDetails").toString();
            ArrayList<String> roomNos=(ArrayList<String>)temp2.get("roomNos");
            EventBooking eventBooking =
            eventBookingService.createBooking(roomNos,startTimeObj,endTimeObj,bookedBy,eventDetails);
            if(eventBooking==null)
            {
                return ResponseEntity.status(500).body("Booking not created as it already exists");
            }
		return ResponseEntity.ok().body("Booking created");
        } catch (Exception e) {
        e.printStackTrace(); // Handle the exception
        return ResponseEntity.status(500).body(e.toString());
        }
		// Date startTimeDate = new Date(startTime);
        

	}
    @GetMapping("/getRoomBooking")
    @CrossOrigin(maxAge = 3600)
    public ResponseEntity getRoomBooking (@RequestParam(value = "roomNo") String roomNo,@RequestParam(value="startTime") String startTimeString,@RequestParam(value="endTime") String endTimeString )
    {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"); // The format of the string
        LocalDateTime startTime = null;
        LocalDateTime endTime = null;

        try {
        startTime = LocalDateTime.parse(startTimeString, formatter); // Parse the string and convert it to a DateTime object
            endTime = LocalDateTime.parse(endTimeString, formatter); // Parse the string and convert it to a DateTime object
           List<EventBooking> bookings= eventBookingService.getBookings(roomNo, startTime, endTime);
            // eventBookingService.createBooking(roomNo,startTime,endTime,bookedBy,eventDetails);
            HashMap<String,List<EventBooking>> resp=new HashMap<>();
            resp.put("bookings", bookings);
		return ResponseEntity.ok().body(resp);
        } catch (Exception e) {
        e.printStackTrace(); // Handle the exception
        return ResponseEntity.status(500).body(e.toString());
        }

    }

    @CrossOrigin(maxAge = 3600)
    @GetMapping("/getAllRoomBookings")
    public ResponseEntity getAllRoomBookings (@RequestParam(value="startTime") String startTimeString,@RequestParam(value="endTime") String endTimeString )
    {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"); // The format of the string
        LocalDateTime startTime = null;
        LocalDateTime endTime = null;

        try {
        startTime = LocalDateTime.parse(startTimeString, formatter); // Parse the string and convert it to a DateTime object
            endTime = LocalDateTime.parse(endTimeString, formatter); // Parse the string and convert it to a DateTime object
           List<EventBooking> bookings= eventBookingService.getBookings("", startTime, endTime);
            // eventBookingService.createBooking(roomNo,startTime,endTime,bookedBy,eventDetails);
            HashMap<String,List<EventBooking>> resp=new HashMap<>();
            resp.put("bookings", bookings);
		return ResponseEntity.ok().body(resp);
        } catch (Exception e) {
        e.printStackTrace(); // Handle the exception
        return ResponseEntity.status(500).body(e.toString());
        }

    }
    @PostMapping("/deleteBooking")
    @CrossOrigin(maxAge = 3600)
    public ResponseEntity deleteBooking(@RequestParam(value = "id") String id)
    {
        eventBookingService.deleteBooking(id);
        return ResponseEntity.ok().body("Booking deleted");
    }
    
    String[] getRooms()
    {
        ArrayList<String> rooms=new ArrayList<String>();
        try {
            BufferedReader roomReader = new BufferedReader(new FileReader("rooms.csv"));
            String line = roomReader.readLine();
            while ((line = roomReader.readLine()) != null) {
                String[] roomDetails = line.split(",");
                rooms.add(roomDetails[0]);

            }
            roomReader.close();
            return rooms.toArray(new String[0]);
        } catch (Exception e) {
            // TODO: handle exception
        }
        return null;


    }

    @GetMapping("/getFreeRooms")
    @CrossOrigin(maxAge = 3600)
    public ResponseEntity getFreeRooms(@RequestParam(value = "startTime") String startTimeString,@RequestParam(value="endTime") String endTimeString )
    {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"); // The format of the string
        LocalDateTime startTime = null;
        LocalDateTime endTime = null;
        // String[] temp={"a101","a102","a103"};

        List<String> rooms= Arrays.asList(getRooms());

        try {
        startTime = LocalDateTime.parse(startTimeString, formatter); // Parse the string and convert it to a DateTime object
            endTime = LocalDateTime.parse(endTimeString, formatter); // Parse the string and convert it to a DateTime object
           List<String> freeRooms= eventBookingService.getFreeRooms(rooms,startTime, endTime);
            // eventBookingService.createBooking(roomNo,startTime,endTime,bookedBy,eventDetails);
            HashMap<String,List<String>> resp=new HashMap<>();
            resp.put("freeRooms", freeRooms);
            return ResponseEntity.ok().body(resp);
}
         catch (Exception e) {
        e.printStackTrace(); // Handle the exception
        return ResponseEntity.status(500).body(e.toString());
        }
        }

    }

