package org.acme.schooltimetabling.rest;

//todo add same day check and bucketlist check
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import org.acme.schooltimetabling.domain.Lesson;
import org.acme.schooltimetabling.domain.TimeTable;
import org.acme.schooltimetabling.persistence.LessonRepository;
import org.acme.schooltimetabling.persistence.RoomRepository;
import org.acme.schooltimetabling.persistence.TimeTableRepository;
import org.acme.schooltimetabling.persistence.TimeslotRepository;
import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.api.score.ScoreExplanation;
import org.optaplanner.core.api.score.ScoreManager;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.api.score.constraint.ConstraintMatchTotal;
import org.optaplanner.core.api.score.constraint.Indictment;
import org.optaplanner.core.api.solver.SolverManager;
import org.optaplanner.core.api.solver.SolverStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;
import com.google.gson.JsonArray;

import org.acme.schooltimetabling.utils.ReadWrite;

@RestController
@RequestMapping("/timeTable")
public class TimeTableController {
    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private TimeTableRepository timeTableRepository;
    @Autowired
    RoomRepository roomRepository;
    @Autowired
    TimeslotRepository timeslotRepository;
    @Autowired
    private SolverManager<TimeTable, Long> solverManager;
    @Autowired
    private ScoreManager<TimeTable, HardSoftScore> scoreManager;
    List<Lesson> subjectList = new ArrayList<>();

    static List<Lesson> getSubjects(ArrayList<HashMap<String, Object>> inputSubjectList) {
        String line = "";
        String splitBy = ",";
        // ArrayList<String[]> subjectData=new ArrayList<>();
        HashMap<String, ArrayList<String[]>> subjectData = new HashMap<>();
        ArrayList<Lesson> subjectList = new ArrayList<>();
        try {
            BufferedWriter courseListWriter = new BufferedWriter(new FileWriter("courseList.csv"));
            StringBuilder courseListSb = new StringBuilder();
            courseListSb.append("CourseCode,registration,Professor\n");
            for (HashMap<String, Object> subject : inputSubjectList) {
                String[] subjectDetails = new String[3];
                subjectDetails[0] = subject.get("CourseCode").toString();
                subjectDetails[1] = subject.get("registration").toString();
                subjectDetails[2] = subject.get("Professor").toString();
                ArrayList<String[]> subjectDataList = new ArrayList<>();
                courseListSb.append(subjectDetails[0] + "," + subjectDetails[1] + "," + subjectDetails[2] + "\n");
                // subjectData.add(subjectDetails);
                if (subjectData.containsKey(subjectDetails[0])) {
                    subjectDataList = subjectData.get(subjectDetails[0]);
                }
                subjectDataList.add(subjectDetails);
                subjectData.put(subjectDetails[0], subjectDataList);
            }

            Random rand = new Random();
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
                    // char section = 'A';
                    for (int i = 0; i < 2; i++) {
                        String secString = String.valueOf(rand.nextInt());
                        for (String[] subjectDataArray : subjectDataList) {
                            Lesson subject = new Lesson(subjectDataArray[0], subjectDataArray[2], "",
                                    Integer.valueOf(subjectDataArray[1]), dept, secString);
                            subjectList.add(subject);
                        }
                    }

                }
            }
            courseListWriter.write(courseListSb.toString());
            courseListWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return subjectList;
    }

    public void createLessons(ArrayList<HashMap<String, Object>> inputSubjectList) {
        subjectList = getSubjects(inputSubjectList);
        for (Lesson subject : subjectList) {
            lessonRepository.save(subject);

        }
        Lesson lesson = lessonRepository.findAll(Sort.by("id")).iterator().next();
        lesson.setTimeslot(timeslotRepository.findAll(Sort.by("id")).iterator().next());
        lesson.setRoom(roomRepository.findAll(Sort.by("id")).iterator().next());

        lessonRepository.save(lesson);
    }

    // To try, GET http://localhost:8080/timeTable
    void readCourseList() {
        try {
            BufferedReader courseListReader = new BufferedReader(new FileReader("courseList.csv"));
            String line = courseListReader.readLine();
            while ((line = courseListReader.readLine()) != null) {
                String[] subjectDetails = line.split(",");
                Lesson subject = new Lesson(subjectDetails[0], subjectDetails[2], "",
                        Integer.valueOf(subjectDetails[1]), "");
                Lesson subjectDup = new Lesson(subjectDetails[0], subjectDetails[2], "",
                        Integer.valueOf(subjectDetails[1]), "");
                subjectList.add(subject);
                subjectList.add(subjectDup);
            }
            courseListReader.close();
        } catch (Exception e) {
            // TODO: handle exception
        }

    }

    void WriteTT(TimeTable solution) {
        HashSet<Lesson>[][] lessons = new HashSet[5][4];
        HashSet<String>[][] occupiedRoomSet = new HashSet[5][4];
        HashSet<String> allRoomSet = new HashSet<>(
                roomRepository.findAll().stream().map(r -> r.getName()).collect(Collectors.toList()));
        try {
            for (Lesson l : solution.getLessonList()) {
                if (l.getTimeslot() == null)
                    continue;
                long slotId = l.getTimeslot().getId() - 1;
                // System.out.println(slotId);
                int day = (int) (slotId / 4);
                int slot = (int) (slotId) % 4;
                // slot = slot ;
                int index = 0;
                // jsonArray.get(day).get(slot).add(l);
                // for (; index < 10 && lessons[day][slot][index] != null; index++)
                    ;
                // System.out.println("day: "+day+" slot: "+slot+" index: "+index);
                HashSet<String> occupiedRooms = new HashSet<>();
                HashSet<Lesson> lessonSlot1=new HashSet<>();
                // HashSet<Lesson> lessonSlot2=new HashSet<>();
                // HashSet<Lesson> lessonSlot3=new HashSet<>();
                if (occupiedRoomSet[day][slot] != null)
                    occupiedRooms = occupiedRoomSet[day][slot];
                if(lessons[day][slot]!=null)
                    lessonSlot1=lessons[day][slot];
                // if(lessons[day][slot+1]!=null)
                //     lessonSlot2=lessons[day][slot];
                // if(lessons[day][slot+2]!=null)
                //     lessonSlot3=lessons[day][slot];

                occupiedRooms.add(l.getRoom().getName());
                lessonSlot1.add(l);
                // lessonSlot2.add(l);
                // lessonSlot3.add(l);
                lessons[day][slot] = lessonSlot1;
                // lessons[day][slot+1] = lessonSlot2;
                // lessons[day][slot+2] = lessonSlot3;

            }
        } catch (Exception e) {
            System.out.println(e);
            // TODO: handle exception
        }

        try {
            BufferedWriter bWriter = new BufferedWriter(new FileWriter("finalTT.csv"));

            StringBuilder sb = new StringBuilder();

            for (int i = 0; i < lessons.length; i++) {
                for (int j = 0; j < lessons[0].length; j++) {
                    // int l = lessons[i][j].length;
                    if(lessons[i][j]==null)
                    {
                        for(int k=0;k<10;k++)
                            sb.append(",");
                        sb.append("|");

                        continue;
                    }
                    for (Lesson lesson : lessons[i][j]) {
                        if (lesson != null)
                            if (lesson.getMultipleSection())
                                sb.append(lesson.getSubject() + "(" + lesson.getSection() + ")"
                                        + ":" + lesson.getRoom());
                            else
                                sb.append(lesson.getSubject() + ":" + lesson.getRoom());

                        sb.append(",");
                    }
                    // if (j != lessons[0].length - 1)
                    sb.append("|");
                }
                if (i != 4)
                    sb.append("\n");
            }
            bWriter.write(sb.toString());
            bWriter.close();

        } catch (Exception e) {
            // TODO: handle exception
            System.out.println(e);
        }

        try {
            BufferedWriter freeRoomWriter = new BufferedWriter(new FileWriter("FreeRoom.csv"));
            StringBuilder freeRoomSb = new StringBuilder();
            for (int i = 0; i < occupiedRoomSet.length; i++) {
                for (int j = 0; j < occupiedRoomSet[0].length; j++) {
                    HashSet<String> occupiedRooms = occupiedRoomSet[i][j];
                    if (occupiedRooms == null)
                        occupiedRooms = new HashSet<>();
                    HashSet<String> freeRooms = new HashSet<>(allRoomSet);
                    freeRooms.removeAll(occupiedRooms);
                    for (String room : freeRooms) {
                        freeRoomSb.append(room + ",");
                    }
                    freeRoomSb.append("|");
                }
                if (i != 4)
                    freeRoomSb.append("\n");
            }
            freeRoomWriter.write(freeRoomSb.toString());
            freeRoomWriter.close();
        } catch (Exception e) {
            System.out.println(e);

        }
    }
    // @GetMapping()
    // public TimeTable getTimeTable() {
    // // Get the solver status before loading the solution
    // // to avoid the race condition that the solver terminates between them
    // SolverStatus solverStatus = getSolverStatus();
    // TimeTable solution =
    // timeTableRepository.findById(TimeTableRepository.SINGLETON_TIME_TABLE_ID);
    // scoreManager.updateScore(solution); // Sets the score
    // ScoreExplanation<TimeTable, HardSoftScore> scoreExplanation =
    // scoreManager.explainScore(solution);

    // Map<String,ConstraintMatchTotal<HardSoftScore>> constraintMatchTotals =
    // scoreExplanation.getConstraintMatchTotalMap();
    // for (String constraintMatchTotal : constraintMatchTotals.keySet()) {
    // System.out.println("ConstraintMatchTotal: " + constraintMatchTotal);
    // System.out.println("Score: " +
    // constraintMatchTotals.get(constraintMatchTotal).getScore());
    // }
    // Map<Object,Indictment<HardSoftScore>>
    // constraintIndicTotals=scoreExplanation.getIndictmentMap();
    // for (Object ind : constraintIndicTotals.keySet()) {
    // System.out.println("Constraint break " + ind);
    // System.out.println("Score: " + constraintIndicTotals.get(ind));
    // }
    // Lesson[][][] lessons=new Lesson[5][12][9];
    // // JsonArray jsonArray=new JsonArray();
    // // for(int i=0;i<5;i++){
    // // for(int j=0;j<12;j++){
    // // JsonArray jsonArray2=new JsonArray();
    // // jsonArray.add(jsonArray2);
    // // }
    // // }

    // // Map<String,Object> responseMap=new HashMap<>();
    // // responseMap.put("tt obj", solution);
    // // responseMap.put("score", scoreExplanation.getScore());
    // // // lessons[0][0][0]=solution.getLessonList().get(0);
    // try {
    // for(Lesson l:solution.getLessonList()){
    // if(l.getTimeslot()==null)
    // continue;
    // long slotId=l.getTimeslot().getId()-1;
    // // System.out.println(slotId);
    // int day=(int)(slotId/4) ;
    // int slot=(int)(slotId)%4;
    // slot=slot*3;
    // int index=0;
    // // jsonArray.get(day).get(slot).add(l);
    // for(;index<9&&lessons[day][slot][index]!=null;index++);
    // // System.out.println("day: "+day+" slot: "+slot+" index: "+index);
    // lessons[day][slot][index]=l;
    // lessons[day][slot+1][index]=l;
    // lessons[day][slot+2][index]=l;

    // }
    // } catch (Exception e) {
    // System.out.println(e);
    // // TODO: handle exception
    // }

    // try {
    // BufferedWriter bWriter = new BufferedWriter(new FileWriter("finalTT.csv"));
    // StringBuilder sb = new StringBuilder();
    // for (int i = 0; i < lessons.length; i++) {
    // for (int j = 0; j < lessons[0].length; j++) {
    // int l = lessons[i][j].length;
    // for (int p = 0; p < l; p++) {
    // if (lessons[i][j][p] != null)
    // if(lessons[i][j][p].getMultipleSection())
    // sb.append(lessons[i][j][p].getSubject()+"("+lessons[i][j][p].getSection()+")"
    // + ":" + lessons[i][j][p].getRoom());
    // else
    // sb.append(lessons[i][j][p].getSubject() + ":" + lessons[i][j][p].getRoom());

    // sb.append(",");
    // }
    // // if (j != lessons[0].length - 1)
    // sb.append("|");
    // }
    // if (i != 4)
    // sb.append("\n");
    // }
    // bWriter.write(sb.toString());
    // bWriter.close();

    // } catch (Exception e) {
    // // TODO: handle exception
    // System.out.println(e);
    // }
    // // responseMap.put("tt matrix", lessons);

    // solution.setSolverStatus(solverStatus);
    // // return responseMap;
    // return solution;
    // }
    public static String checkBucketClash(HashSet<String> currentSlot,
            int maxClashes) {
        List<Set<String>> bucketList = ReadWrite.getBucketList();
        for (Set<String> bucket : bucketList) {
            Set<String> currentSlotClone = (Set<String>) currentSlot.clone();
            currentSlotClone.retainAll(bucket);

            if (currentSlotClone.size() >= maxClashes) {
                return bucket.toString();
            }
        }
        return "No clash";

    }
    @CrossOrigin(maxAge = 3600)
    @PostMapping("/saveTimeTable")
    public Map<String, String> save(@RequestBody Map<String, Object> body) {
        // System.out.println(body);
        
            return Map.of("status", "saved");
        
    }
    @CrossOrigin(maxAge = 3600)
    @PostMapping("/createTimeTable")
    public Map<String, String> solve(@RequestBody Map<String, Object> body) {
        // System.out.println(body);
        if (getSolverStatus() == SolverStatus.NOT_SOLVING) {

            createLessons((ArrayList<HashMap<String, Object>>) body.get("tt"));
            solverManager.solveAndListen(TimeTableRepository.SINGLETON_TIME_TABLE_ID,
                    timeTableRepository::findById,
                    (TimeTable t) -> {
                        System.out.println("Solving");
                        System.out.println(t.getScore());
                        timeTableRepository.save(t);
                        WriteTT(t);

                    });
            return Map.of("status", "started");
        } else
            return Map.of("status", "already started");
    }

    @CrossOrigin(maxAge = 3600)
    @GetMapping("/continueSolving")
    public Map<String, String> continueSolving() {
        if (getSolverStatus() == SolverStatus.NOT_SOLVING) {
            solverManager.solveAndListen(TimeTableRepository.SINGLETON_TIME_TABLE_ID,
                    timeTableRepository::findById,
                    (TimeTable t) -> {
                        System.out.println("Solving");
                        System.out.println(t.getScore());
                        timeTableRepository.save(t);
                        WriteTT(t);

                    });
            return Map.of("status", "started");
        } else
            return Map.of("status", "already started");
    }

    public SolverStatus getSolverStatus() {
        return solverManager.getSolverStatus(TimeTableRepository.SINGLETON_TIME_TABLE_ID);
    }

    @PostMapping("/stopSolving")
    public void stopSolving() {
        solverManager.terminateEarly(TimeTableRepository.SINGLETON_TIME_TABLE_ID);
    }

    public static String getFile(String path) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(new File(path)));
        return reader.lines().collect(Collectors.joining("\n"));
    }

    @CrossOrigin(maxAge = 3600)
    @GetMapping(value = "/getTimeTable")
    public ResponseEntity exportCSV() {
        SolverStatus solverStatus = getSolverStatus();
        if (solverStatus == SolverStatus.NOT_SOLVING) {
            TimeTable solution = timeTableRepository.findById(TimeTableRepository.SINGLETON_TIME_TABLE_ID);
            scoreManager.updateScore(solution); // Sets the score
            ScoreExplanation<TimeTable, HardSoftScore> scoreExplanation = scoreManager.explainScore(solution);

            Map<String, ConstraintMatchTotal<HardSoftScore>> constraintMatchTotals = scoreExplanation
                    .getConstraintMatchTotalMap();
            for (String constraintMatchTotal : constraintMatchTotals.keySet()) {
                System.out.println("ConstraintMatchTotal: " + constraintMatchTotal);
                System.out.println("Score: " + constraintMatchTotals.get(constraintMatchTotal).getScore());
            }
            Map<Object, Indictment<HardSoftScore>> constraintIndicTotals = scoreExplanation.getIndictmentMap();
            for (Object ind : constraintIndicTotals.keySet()) {
                System.out.println("Constraint break " + ind);
                System.out.println("Score: " + constraintIndicTotals.get(ind));
            }

            // JsonArray jsonArray=new JsonArray();
            // for(int i=0;i<5;i++){
            // for(int j=0;j<12;j++){
            // JsonArray jsonArray2=new JsonArray();
            // jsonArray.add(jsonArray2);
            // }
            // }

            // Map<String,Object> responseMap=new HashMap<>();
            // responseMap.put("tt obj", solution);
            // responseMap.put("score", scoreExplanation.getScore());
            // // lessons[0][0][0]=solution.getLessonList().get(0);

            // responseMap.put("tt matrix", lessons);

            solution.setSolverStatus(solverStatus);

            HashMap<String, String> map = new HashMap<>();
            try {
                // if (isCreating.get() == false) {
                String reportName = "finalTT.csv";
                // File file = new File(reportName);
                // System.out.println(file.exists());
                map.put("status", "done");
                map.put("timetable", getFile(reportName));
                map.put("free Rooms", getFile("FreeRoom.csv"));
                return ResponseEntity.ok()
                        .body(map);

                // }
                // map.put("status", "creating timetable");
                // return ResponseEntity.ok()
                // .body(map);

            } catch (Exception ex) {
                System.out.println(ex);
            }
        } else {
            HashMap<String, String> map = new HashMap<>();
            map.put("status", "creating timetable");
            return ResponseEntity.ok()
                    .body(map);
        }
        return null;
    }
    Map<String,String> checkCourseRoomProfSlot(Map<String,String> errorMap,Map<String,Lesson> subMap, String[][][] courseCodes,String[][][] roomCodes)
    {
        
        for (int i = 0; i < courseCodes.length; i++) {
            for (int j = 0; j < courseCodes[0].length; j++) {
                
                String key = Integer.toString(i) + "," + Integer.toString(j);
                HashMap<String, String> codeHashMap = new HashMap<>();
                HashMap<String, String> roomHashMap = new HashMap<>();
                HashMap<String, String> profHashMap = new HashMap<>();
                for (int k=0;k<courseCodes[i][j].length;k++) {
                    
                    String courseCode=courseCodes[i][j][k];
                    String roomCode=roomCodes[i][j][k];
                    // System.out.println(courseCode[0]);
                    if (courseCode.strip().length() == 0) {
                        continue;
                    }
                    Lesson tmp = subMap.get(courseCode);
                    if (tmp == null) {
                        System.out.println(courseCode);
                    }
                    if (codeHashMap.containsKey(tmp.getSubject())) {
                        if (!errorMap.containsKey(key))
                            errorMap.put(key, "Course Common: " + codeHashMap.get(tmp.getSubject()) + "-" + courseCode+":"+roomCode);
                        else
                            errorMap.put(key,
                                    errorMap.get(key) + ", Course Common: " + codeHashMap.get(tmp.getSubject()) + "-" + courseCode+":"+roomCode);
                    } else {
                        codeHashMap.put(tmp.getSubject(), courseCode+":"+roomCode);
                    }

                    if (roomHashMap.containsKey(roomCode.strip())) {
                        if (!errorMap.containsKey(key))
                            errorMap.put(key, "Room Common: " + roomHashMap.get(roomCode.strip()) + "-" + courseCode+":"+roomCode);
                        else
                            errorMap.put(key, errorMap.get(key) + ", Room Common: " + roomHashMap.get(roomCode.strip()) + "-"
                                    + courseCode+":"+roomCode);
                    } else {
                        roomHashMap.put(roomCode.strip(), courseCode+":"+roomCode);
                    }

                    if (profHashMap.containsKey(tmp.getTeacher())) {
                        if (!errorMap.containsKey(key))
                            errorMap.put(key, "Prof Common: " + profHashMap.get(tmp.getTeacher()) + "-" + courseCode+":"+roomCode);
                        else
                            errorMap.put(key,
                                    errorMap.get(key) + ", Prof Common: " + profHashMap.get(tmp.getTeacher()) + "-" + courseCode+":"+roomCode);
                    } else {
                        profHashMap.put(tmp.getTeacher(), courseCode+":"+roomCode);
                    }

                }
            }

            // System.out.println(mpp.values());
        }
        
        
        return null;

    }
    void checkSameDaySameSubject(String[][][] courseCodes,Map<String,String> errMap)
    {
        for(int i=0;i<courseCodes.length;i++)
        {
            HashMap<String,Integer> courseCodeFreq=new HashMap<>();
            for(int j=0;j<courseCodes[i].length;j++)
            {
                for(int k=0;k<courseCodes[i][j].length;k++)
                {
                    String courseCode=courseCodes[i][j][k];
                    if(courseCode.strip().length()==0)
                    {
                        continue;
                    }
                    if(courseCodeFreq.containsKey(courseCode))
                    {
                        courseCodeFreq.put(courseCode, courseCodeFreq.get(courseCode)+1);
                    }
                    else
                    {
                        courseCodeFreq.put(courseCode, 1);
                    }
                }

            }
            for(String key:courseCodeFreq.keySet())
            {
                
                if(courseCodeFreq.get(key)>3)
                {
                    if(!errMap.containsKey(String.valueOf(i)))
                    {
                        errMap.put(String.valueOf(i), "Course "+key+" is more than 1 time in a day");
                    }
                    else
                    {
                        errMap.put(String.valueOf(i), errMap.get(String.valueOf(i))+" ,Course "+key+" is more than 3 times in a day");
                    }

                    // System.out.println(key+" "+courseCodeFreq.get(key));
                }
            }

        }

    }

    void checkSlotClash(String[][][] courseCodes,Map<String,String> errMap)
    {
        for (int i = 0; i < courseCodes.length; i++) {
            for (int j = 0; j < courseCodes[i].length; j++) {
                HashSet<String> courseCodeSet=new HashSet<>();
                for (int j2 = 0; j2 < courseCodes[i][j].length; j2++) {
                    if(courseCodes[i][j][j2].strip().length()==0)
                    {
                        continue;
                    }
                    courseCodeSet.add(courseCodes[i][j][j2]);
                }
                String result=checkBucketClash(courseCodeSet, 2);
                if(result!="No clash")
                {
                    String key = Integer.toString(i) + "," + Integer.toString(j);
                    if(!errMap.containsKey(key))
                    {
                        errMap.put(key, "Slot violates regulation" + result);
                    }
                    else
                    {
                        errMap.put(key, errMap.get(key)+" ,Slot violates regulation" +result);
                    }
                }
            }
        }
    }
    
    @CrossOrigin(maxAge = 3600)
    @PostMapping("/checkTimeTable")
    // @JsonProperty(value = "timetable")
    public ResponseEntity getTT(@RequestBody String timetable) {
        // HashMap<String, String> map = new HashMap<>();
        // JSONObject jsono = new JSONObject(timetable);
        Gson gson = new Gson();
        HashMap<String, ArrayList<ArrayList<String>>> temp2 = gson.fromJson(timetable, HashMap.class);
        // System.out.println(timetable);
        // JSONArray t = new JSONArray(timetable.get("timetable"));
        ArrayList<ArrayList<String>> tempt = temp2.get("timetable");
        if (subjectList == null || subjectList.size() == 0) {
            readCourseList();
        }
        // String[][].class);
        // System.out.println(jsono.get("tiemtable"));
        // JSONArray json = new JSONArray(timetable.get("timetable"));
        // String[][] array = new String[json.length()][];
        // JSONArray.

        // map.put("status", "");
        String[][] ttvalues = tempt.stream().map(u -> u.toArray(new String[0])).toArray(String[][]::new);
        // System.out.println(ttvalues[0][0]);
        // ttvalues = temp;
        HashMap<String, Lesson> subMap = new HashMap<>();
        HashMap<String, String> map = new HashMap<>();

        for (Lesson c : subjectList) {
            subMap.put(c.getSubject(), c);
        }
        String[][][] courseCodes=new String[5][4][];
        String[][][] roomCodes=new String[5][4][];
        for(int i=0;i<5;i++){
            for(int j=0;j<4;j++){

                String[] arrOfStr = ttvalues[i][j].split("\n");
                courseCodes[i][j]=new String[arrOfStr.length];
                roomCodes[i][j]=new String[arrOfStr.length];
                for(int k=0;k<arrOfStr.length;k++){
                    if(arrOfStr[k].strip().length()<3)
                    {
                        continue;
                    }
                    String[] courseCode = arrOfStr[k].split(":");
                    courseCodes[i][j][k]=courseCode[0].strip();
                    roomCodes[i][j][k]=courseCode[1].strip();
                }
            }
        }
        checkCourseRoomProfSlot(map,subMap,courseCodes,roomCodes);
        checkSameDaySameSubject(courseCodes,map);
        checkSlotClash(courseCodes,map);

        
        for (String objectName : map.keySet()) {
            System.out.println(objectName + "-> " + map.get(objectName));
        }
        // System.out.println("saaaaaaaaaaaaaaaaaaaaaaaaa");
        return ResponseEntity.ok()
                .body(map);
    }

}
