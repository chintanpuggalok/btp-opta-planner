package org.acme.schooltimetabling.rest;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

import com.google.gson.JsonArray;

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
    static List<Lesson> getSubjects(ArrayList<HashMap<String,Object>> inputSubjectList) {
        String line = "";
        String splitBy = ",";
        // ArrayList<String[]> subjectData=new ArrayList<>();
        HashMap<String, ArrayList<String[]>> subjectData = new HashMap<>();
        ArrayList<Lesson> subjectList = new ArrayList<>();
        try {
            for(HashMap<String,Object> subject:inputSubjectList){
                String[] subjectDetails = new String[3];
                subjectDetails[0]=subject.get("CourseCode").toString();
                subjectDetails[1]=subject.get("registration").toString();
                subjectDetails[2]=subject.get("Professor").toString();
                ArrayList<String[]> subjectDataList = new ArrayList<>();
                // subjectData.add(subjectDetails);
                if (subjectData.containsKey(subjectDetails[0])) {
                    subjectDataList = subjectData.get(subjectDetails[0]);
                }
                subjectDataList.add(subjectDetails);
                subjectData.put(subjectDetails[0], subjectDataList);
            }
            // parsing a CSV file into BufferedReader class constructor
            // BufferedReader br = new BufferedReader(new FileReader("coursecodes.csv"));
            // br.readLine();
            // while ((line = br.readLine()) != null) // returns a Boolean value
            // {
            //     String[] subjectDetails = line.split(splitBy);

            //     ArrayList<String[]> subjectDataList = new ArrayList<>();
            //     // subjectData.add(subjectDetails);
            //     if (subjectData.containsKey(subjectDetails[0])) {
            //         subjectDataList = subjectData.get(subjectDetails[0]);
            //     }
            //     subjectDataList.add(subjectDetails);
            //     subjectData.put(subjectDetails[0], subjectDataList);
            //     // Lesson subject = new
            //     // Lesson(subjectDetails[0],subjectDetails[2],"",Integer.valueOf(subjectDetails[1]),dept);
            //     // Lesson subjectDup=new
            //     // Lesson(subjectDetails[0],subjectDetails[2],"",Integer.valueOf(subjectDetails[1]),dept);
            //     // subjectList.add(subject);
            //     // subjectList.add(subjectDup);
            // }
            // br.close();
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
        } catch (Exception e) {
            e.printStackTrace();
        }

        return subjectList;
    }

    public void createLessons(ArrayList<HashMap<String,Object>> inputSubjectList) {
        List<Lesson> subjectList = getSubjects(inputSubjectList);
        for (Lesson subject : subjectList) {
            lessonRepository.save(subject);

        }
        Lesson lesson = lessonRepository.findAll(Sort.by("id")).iterator().next();
            lesson.setTimeslot(timeslotRepository.findAll(Sort.by("id")).iterator().next());
            lesson.setRoom(roomRepository.findAll(Sort.by("id")).iterator().next());

            lessonRepository.save(lesson);
    }
    // To try, GET http://localhost:8080/timeTable
    @GetMapping()
    public TimeTable getTimeTable() {
        // Get the solver status before loading the solution
        // to avoid the race condition that the solver terminates between them
        SolverStatus solverStatus = getSolverStatus();
        TimeTable solution = timeTableRepository.findById(TimeTableRepository.SINGLETON_TIME_TABLE_ID);
        scoreManager.updateScore(solution); // Sets the score
        ScoreExplanation<TimeTable, HardSoftScore> scoreExplanation = scoreManager.explainScore(solution);
        
        Map<String,ConstraintMatchTotal<HardSoftScore>> constraintMatchTotals = scoreExplanation.getConstraintMatchTotalMap();
        for (String constraintMatchTotal : constraintMatchTotals.keySet()) {
            System.out.println("ConstraintMatchTotal: " + constraintMatchTotal);
            System.out.println("Score: " + constraintMatchTotals.get(constraintMatchTotal).getScore());
        }
        Map<Object,Indictment<HardSoftScore>> constraintIndicTotals=scoreExplanation.getIndictmentMap();
        for (Object ind : constraintIndicTotals.keySet()) {
            System.out.println("Constraint break " + ind);
            System.out.println("Score: " + constraintIndicTotals.get(ind));
        }
        Lesson[][][] lessons=new Lesson[5][12][9];
        // JsonArray jsonArray=new JsonArray();
        // for(int i=0;i<5;i++){
        //     for(int j=0;j<12;j++){
        //         JsonArray jsonArray2=new JsonArray();
        //         jsonArray.add(jsonArray2);
        //     }
        // }
    
        // Map<String,Object> responseMap=new HashMap<>();
        // responseMap.put("tt obj", solution);
        // responseMap.put("score", scoreExplanation.getScore());
        // // lessons[0][0][0]=solution.getLessonList().get(0);
        try {
            for(Lesson l:solution.getLessonList()){
                if(l.getTimeslot()==null)
                    continue;
                long slotId=l.getTimeslot().getId()-1;
                // System.out.println(slotId);
                int day=(int)(slotId/4) ;
                int slot=(int)(slotId)%4;
                slot=slot*3;
                int index=0;
                // jsonArray.get(day).get(slot).add(l);
                for(;index<9&&lessons[day][slot][index]!=null;index++);
                // System.out.println("day: "+day+" slot: "+slot+" index: "+index);
                lessons[day][slot][index]=l;
                lessons[day][slot+1][index]=l;
                lessons[day][slot+2][index]=l;
    
                
            }    
        } catch (Exception e) {
            System.out.println(e);
            // TODO: handle exception
        }
        
        try {
            BufferedWriter bWriter = new BufferedWriter(new FileWriter("test.csv"));
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < lessons.length; i++) {
            for (int j = 0; j < lessons[0].length; j++) {
                int l = lessons[i][j].length;
                for (int p = 0; p < l; p++) {
                    if (lessons[i][j][p] != null)
                    if(lessons[i][j][p].getMultipleSection())
                        sb.append(lessons[i][j][p].getSubject()+"("+lessons[i][j][p].getSection()+")" + ":" + lessons[i][j][p].getRoom());
                    else
                    sb.append(lessons[i][j][p].getSubject() + ":" + lessons[i][j][p].getRoom());

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
        // responseMap.put("tt matrix", lessons);
        




        solution.setSolverStatus(solverStatus);
        // return responseMap;
        return solution;
    }
    
    @CrossOrigin(maxAge = 3600)
    @PostMapping("/createTimeTable")
    public Map<String,String> solve(@RequestBody Map<String,Object> body)  {
        // System.out.println(body);
        createLessons((ArrayList<HashMap<String,Object>>)body.get("tt"));
        solverManager.solveAndListen(TimeTableRepository.SINGLETON_TIME_TABLE_ID,
                timeTableRepository::findById,
                timeTableRepository::save);
        return Map.of("status","started");
    }

    public SolverStatus getSolverStatus() {
        return solverManager.getSolverStatus(TimeTableRepository.SINGLETON_TIME_TABLE_ID);
    }

    @PostMapping("/stopSolving")
    public void stopSolving() {
        solverManager.terminateEarly(TimeTableRepository.SINGLETON_TIME_TABLE_ID);
    }
    public static String getTimeTable(String path) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(new File(path)));
        return reader.lines().collect(Collectors.joining(System.lineSeparator()));
    }
    @CrossOrigin(maxAge = 3600)
	@GetMapping(value = "/getTimeTable")
	public ResponseEntity exportCSV() {
        SolverStatus solverStatus = getSolverStatus();
        TimeTable solution = timeTableRepository.findById(TimeTableRepository.SINGLETON_TIME_TABLE_ID);
        scoreManager.updateScore(solution); // Sets the score
        ScoreExplanation<TimeTable, HardSoftScore> scoreExplanation = scoreManager.explainScore(solution);
        
        Map<String,ConstraintMatchTotal<HardSoftScore>> constraintMatchTotals = scoreExplanation.getConstraintMatchTotalMap();
        for (String constraintMatchTotal : constraintMatchTotals.keySet()) {
            System.out.println("ConstraintMatchTotal: " + constraintMatchTotal);
            System.out.println("Score: " + constraintMatchTotals.get(constraintMatchTotal).getScore());
        }
        Map<Object,Indictment<HardSoftScore>> constraintIndicTotals=scoreExplanation.getIndictmentMap();
        for (Object ind : constraintIndicTotals.keySet()) {
            System.out.println("Constraint break " + ind);
            System.out.println("Score: " + constraintIndicTotals.get(ind));
        }
        Lesson[][][] lessons=new Lesson[5][12][9];
        // JsonArray jsonArray=new JsonArray();
        // for(int i=0;i<5;i++){
        //     for(int j=0;j<12;j++){
        //         JsonArray jsonArray2=new JsonArray();
        //         jsonArray.add(jsonArray2);
        //     }
        // }
    
        // Map<String,Object> responseMap=new HashMap<>();
        // responseMap.put("tt obj", solution);
        // responseMap.put("score", scoreExplanation.getScore());
        // // lessons[0][0][0]=solution.getLessonList().get(0);
        try {
            for(Lesson l:solution.getLessonList()){
                if(l.getTimeslot()==null)
                    continue;
                long slotId=l.getTimeslot().getId()-1;
                // System.out.println(slotId);
                int day=(int)(slotId/4) ;
                int slot=(int)(slotId)%4;
                slot=slot*3;
                int index=0;
                // jsonArray.get(day).get(slot).add(l);
                for(;index<9&&lessons[day][slot][index]!=null;index++);
                // System.out.println("day: "+day+" slot: "+slot+" index: "+index);
                lessons[day][slot][index]=l;
                lessons[day][slot+1][index]=l;
                lessons[day][slot+2][index]=l;
    
                
            }    
        } catch (Exception e) {
            System.out.println(e);
            // TODO: handle exception
        }
        
        try {
            BufferedWriter bWriter = new BufferedWriter(new FileWriter("test.csv"));
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < lessons.length; i++) {
            for (int j = 0; j < lessons[0].length; j++) {
                int l = lessons[i][j].length;
                for (int p = 0; p < l; p++) {
                    if (lessons[i][j][p] != null)
                    if(lessons[i][j][p].getMultipleSection())
                        sb.append(lessons[i][j][p].getSubject()+"("+lessons[i][j][p].getSection()+")" + ":" + lessons[i][j][p].getRoom());
                    else
                    sb.append(lessons[i][j][p].getSubject() + ":" + lessons[i][j][p].getRoom());

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
        // responseMap.put("tt matrix", lessons);
        




        solution.setSolverStatus(solverStatus);

		HashMap<String, String> map = new HashMap<>();
		try {
			// if (isCreating.get() == false) {
				String reportName = "test.csv";
				// File file = new File(reportName);
				// System.out.println(file.exists());
				map.put("timetable", getTimeTable(reportName));
				return ResponseEntity.ok()
						.body(map);

			// }
			// map.put("status", "creating timetable");
			// return ResponseEntity.ok()
			// 		.body(map);

		} catch (Exception ex) {
			System.out.println(ex);
		}
		return null;
	}

}
