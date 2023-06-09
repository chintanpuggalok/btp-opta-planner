package org.acme.schooltimetabling;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.acme.schooltimetabling.domain.Lesson;
import org.acme.schooltimetabling.domain.Room;
import org.acme.schooltimetabling.domain.Timeslot;
import org.acme.schooltimetabling.persistence.LessonRepository;
import org.acme.schooltimetabling.persistence.RoomRepository;
import org.acme.schooltimetabling.persistence.TimeslotRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.Sort;

@SpringBootApplication
public class TimeTableSpringBootApp {
    static List<Set<String>> bucketListFinal = new ArrayList<>();

    public static void main(String[] args) {
        SpringApplication.run(TimeTableSpringBootApp.class, args);
    }

    public static List<Set<String>> getbucketList() {

        return bucketListFinal;
    }

    static List<Lesson> getSubjects() {
        String line = "";
        String splitBy = ",";
        // ArrayList<String[]> subjectData=new ArrayList<>();
        HashMap<String, ArrayList<String[]>> subjectData = new HashMap<>();
        ArrayList<Lesson> subjectList = new ArrayList<>();
        try {
            // parsing a CSV file into BufferedReader class constructor
            BufferedReader br = new BufferedReader(new FileReader("coursecodes.csv"));
            br.readLine();
            while ((line = br.readLine()) != null) // returns a Boolean value
            {
                String[] subjectDetails = line.split(splitBy);

                ArrayList<String[]> subjectDataList = new ArrayList<>();
                // subjectData.add(subjectDetails);
                if (subjectData.containsKey(subjectDetails[0])) {
                    subjectDataList = subjectData.get(subjectDetails[0]);
                }
                subjectDataList.add(subjectDetails);
                subjectData.put(subjectDetails[0], subjectDataList);
                // Lesson subject = new
                // Lesson(subjectDetails[0],subjectDetails[2],"",Integer.valueOf(subjectDetails[1]),dept);
                // Lesson subjectDup=new
                // Lesson(subjectDetails[0],subjectDetails[2],"",Integer.valueOf(subjectDetails[1]),dept);
                // subjectList.add(subject);
                // subjectList.add(subjectDup);
            }
            br.close();
            for (String subjectCode : subjectData.keySet()) {
                if (subjectData.get(subjectCode).size() == 1) {
                    String[] subjectDetails = subjectData.get(subjectCode).get(0);
                    String dept = "";
                    if (subjectDetails[0].startsWith("CSE"))
                        dept = "cse";
                    else if (subjectDetails[0].startsWith("ECE"))
                        dept = "ece";
                    else if (subjectDetails[0].startsWith("MTH"))
                        dept = "math";
                    else if (subjectDetails[0].startsWith("BIO"))
                        dept = "bio";
                    else if (subjectDetails[0].startsWith("DES"))
                        dept = "des";
                    else
                        dept = "other";
                    Lesson subject = new Lesson(subjectDetails[0], subjectDetails[2], "",
                            Integer.valueOf(subjectDetails[1]), dept);
                    Lesson subjectDup = new Lesson(subjectDetails[0], subjectDetails[2], "",
                            Integer.valueOf(subjectDetails[1]), dept);
                    subjectList.add(subject);
                    subjectList.add(subjectDup);
                } else {
                    ArrayList<String[]> subjectDataList = subjectData.get(subjectCode);
                    String[] subjectDetails = subjectData.get(subjectCode).get(0);
                    String dept = "";
                    if (subjectDetails[0].startsWith("CSE"))
                        dept = "cse";
                    else if (subjectDetails[0].startsWith("ECE"))
                        dept = "ece";
                    else if (subjectDetails[0].startsWith("MTH"))
                        dept = "math";
                    else if (subjectDetails[0].startsWith("BIO"))
                        dept = "bio";
                    else if (subjectDetails[0].startsWith("DES"))
                        dept = "des";
                    else
                        dept = "other";
                    char section = 'A';
                    for(int i=0;i<2;i++){
                        String secString=String.valueOf(section+i);
                        for(String[] subjectDataArray:subjectDataList){
                            Lesson subject = new Lesson(subjectDataArray[0], subjectDataArray[2], "",
                            Integer.valueOf(subjectDataArray[1]), dept,secString);
                            subjectList.add(subject);
                        }
                    }

                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return subjectList;
    }

    static List<Room> getRooms() {
        String line = "";
        String splitBy = ",";
        ArrayList<Room> roomList = new ArrayList<>();
        try {
            // parsing a CSV file into BufferedReader class constructor
            BufferedReader br = new BufferedReader(new FileReader("rooms.csv"));
            br.readLine();
            while ((line = br.readLine()) != null) // returns a Boolean value
            {
                String[] roomDetails = line.split(splitBy);
                Room room = new Room(roomDetails[0], Integer.valueOf(roomDetails[1]));
                roomList.add(room);
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return roomList;

    }

    public void createSlots(TimeslotRepository timeslotRepository) {
        List<LocalTime> startTimeList = List.of(LocalTime.of(9, 30), LocalTime.of(11, 0), LocalTime.of(15, 0),
                LocalTime.of(16, 30));
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                Timeslot s= timeslotRepository.save(new Timeslot(DayOfWeek.of(i + 1), startTimeList.get(j),
                        startTimeList.get(j).plusHours(1).plusMinutes(30)));
                // System.out.println(s.getId());
                    }
        }
        timeslotRepository.save(new Timeslot(DayOfWeek.of(5), LocalTime.of(9, 30), LocalTime.of(11, 0)));
        timeslotRepository.save(new Timeslot(DayOfWeek.of(5), LocalTime.of(11, 0), LocalTime.of(12, 30)));

    }

    public void createLessons(LessonRepository lessonRepository) {
        List<Lesson> subjectList = getSubjects();
        for (Lesson subject : subjectList) {
            lessonRepository.save(subject);

        }
    }

    public void createRooms(RoomRepository roomRepository) {
        List<Room> roomList = getRooms();
        for (Room room : roomList) {
            roomRepository.save(room);
        }
    }

    @Value("${timeTable.demoData:SMALL}")
    private DemoData demoData;

    @Bean
    public CommandLineRunner demoData(
            TimeslotRepository timeslotRepository,
            RoomRepository roomRepository,
            LessonRepository lessonRepository) {
        return (args) -> {
            if (demoData == DemoData.NONE) {
                return;
            }
            createSlots(timeslotRepository);
            createRooms(roomRepository);
            // createLessons(lessonRepository);
            // setBucketList();

            // Lesson lesson = lessonRepository.findAll(Sort.by("id")).iterator().next();
            // lesson.setTimeslot(timeslotRepository.findAll(Sort.by("id")).iterator().next());
            // lesson.setRoom(roomRepository.findAll(Sort.by("id")).iterator().next());

            // lessonRepository.save(lesson);
        };
    }

    public enum DemoData {
        NONE,
        SMALL,
        LARGE
    }

}
