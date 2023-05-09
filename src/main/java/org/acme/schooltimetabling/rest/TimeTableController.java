package org.acme.schooltimetabling.rest;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.acme.schooltimetabling.domain.Lesson;
import org.acme.schooltimetabling.domain.TimeTable;
import org.acme.schooltimetabling.persistence.TimeTableRepository;
import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.api.score.ScoreExplanation;
import org.optaplanner.core.api.score.ScoreManager;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.api.score.constraint.ConstraintMatchTotal;
import org.optaplanner.core.api.score.constraint.Indictment;
import org.optaplanner.core.api.solver.SolverManager;
import org.optaplanner.core.api.solver.SolverStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.JsonArray;

@RestController
@RequestMapping("/timeTable")
public class TimeTableController {

    @Autowired
    private TimeTableRepository timeTableRepository;
    @Autowired
    private SolverManager<TimeTable, Long> solverManager;
    @Autowired
    private ScoreManager<TimeTable, HardSoftScore> scoreManager;

    // To try, GET http://localhost:8080/timeTable
    @GetMapping()
    public TimeTable getTimeTable() {
        // Get the solver status before loading the solution
        // to avoid the race condition that the solver terminates between them
        SolverStatus solverStatus = getSolverStatus();
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
        Lesson[][][] lessons = new Lesson[5][12][9];
        JsonArray jsonArray = new JsonArray();
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 12; j++) {
                JsonArray jsonArray2 = new JsonArray();
                jsonArray.add(jsonArray2);
            }
        }
        // Map<String,Object> responseMap=new HashMap<>();
        // responseMap.put("tt obj", solution);
        // responseMap.put("score", scoreExplanation.getScore());
        // // lessons[0][0][0]=solution.getLessonList().get(0);
        // for(Lesson l:solution.getLessonList()){
        // if(l.getTimeslot()==null)
        // continue;
        // long slotId=l.getTimeslot().getId();
        // System.out.println(slotId);
        // int day=(int)(slotId/5) ;
        // int slot=(int)slotId%5;
        // // int index=0;
        // jsonArray.get(day).get(slot).add(l);

        // }

        // responseMap.put("tt matrix", lessons);

        solution.setSolverStatus(solverStatus);
        // return responseMap;
        return solution;
    }

    @PostMapping("/solve")
    public Map<String, String> solve() {
        solverManager.solveAndListen(TimeTableRepository.SINGLETON_TIME_TABLE_ID,
                timeTableRepository::findById,
                timeTableRepository::save);
        return Map.of("status", "started");
    }

    public SolverStatus getSolverStatus() {
        return solverManager.getSolverStatus(TimeTableRepository.SINGLETON_TIME_TABLE_ID);
    }

    @PostMapping("/stopSolving")
    public void stopSolving() {
        solverManager.terminateEarly(TimeTableRepository.SINGLETON_TIME_TABLE_ID);
    }

}
