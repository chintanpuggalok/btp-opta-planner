package org.acme.schooltimetabling.solver;

import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.api.score.stream.Constraint;
import org.optaplanner.core.api.score.stream.ConstraintCollectors;
import org.optaplanner.core.api.score.stream.ConstraintFactory;
import org.optaplanner.core.api.score.stream.ConstraintProvider;
import org.optaplanner.core.api.score.stream.Joiners;
import org.optaplanner.core.api.score.stream.bi.BiConstraintCollector;
import org.optaplanner.core.api.score.stream.uni.UniConstraintCollector;
import org.optaplanner.core.impl.util.Pair;
import org.acme.schooltimetabling.TimeTableSpringBootApp;

import java.io.BufferedReader;
import java.io.FileReader;
import java.time.DayOfWeek;

import org.acme.schooltimetabling.domain.Lesson;
import org.acme.schooltimetabling.domain.Timeslot;

public class TimeTableConstraintProvider implements ConstraintProvider {
    private List<Set<String>> subjectSets;
    List<HashSet<String>> setBucketList()
    {
        String line = "";
        String splitBy = ",";
        BufferedReader br;
        try {
             br = new BufferedReader(new FileReader("subject_bucket_list.csv"));
            br.readLine();
            List<Set<String>> bucketList = new ArrayList<>();
            HashSet<String> bucket = new HashSet<>();
            List<HashSet<String>> seriesBuckets = new ArrayList<>();
            seriesBuckets.add(bucket);
            while ((line = br.readLine()) != null) // returns a Boolean value
            {
                String[] subjectDetails = line.split(splitBy);
                if(subjectDetails.length==1)
                {
                    // bucketList.add(bucket);
                    for(HashSet<String> set: seriesBuckets)
                    {
                        if(set.size()>0)
                        {

                            bucketList.add(set);
                        }
                    }
                    bucket = new HashSet<>();
                    seriesBuckets= new ArrayList<>();
                    seriesBuckets.add(bucket);
                    continue;
                }
                else if(subjectDetails.length>=2)
                {
                    if(subjectDetails.length==2)
                    {
                        for(HashSet<String> set: seriesBuckets)
                        {
                            set.add(subjectDetails[1]);
                        }
                    }
                    else
                    {
                        int multiplier=subjectDetails.length-1;
                        List<HashSet<String>> newSeriesBuckets = new ArrayList<>();
                        for(int i=0;i<multiplier;i++)
                        {
                            
                            for(HashSet<String> set: seriesBuckets)
                            {
                                newSeriesBuckets.add((HashSet<String>) set.clone());
                            }
                        }
                        // for(int i=0;i<multiplier;i++)
                        // {
                        //     for(int j=1;j<subjectDetails.length;j++)
                        //     {
                        //         newSeriesBuckets.get(i*multiplier+j-1 ).add(subjectDetails[j]);
                        //     }
                        // }
                        int sub=0;
                        for(int i=0;i<newSeriesBuckets.size();i++,sub++)
                        {
                            sub=sub%multiplier;
                            newSeriesBuckets.get(i).add(subjectDetails[sub+1]);
                        }
                        seriesBuckets=newSeriesBuckets;

                    }
                
            }
        }

            br.close();
            // return bucketList;
            this.subjectSets=bucketList;
            
        } catch (Exception e) {
            
            System.out.println(e.getMessage());
            // TODO: handle exception
        }
        return null; 
        


    }

    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {

        // subjectSets =  TimeTableSpringBootApp.getbucketList();
        // subjectSets.add(Set.of("Math","Chemistry","Biology"));
        // Map<String, Integer> minLessonCount = Map.of("cse",2,"ece",1);
        setBucketList();
        List<LocalTime> startTimeList =List.of(LocalTime.of(9, 30),LocalTime.of(11,0),LocalTime.of(15,0),LocalTime.of(16,30));
        List<LocalTime> endTimeList =List.of(LocalTime.of(11, 0),LocalTime.of(12,30),LocalTime.of(16,30),LocalTime.of(18,0));
        Constraint[] slotConstraints = new Constraint[9];
        for(int i=0;i<4;i++)
        {
            DayOfWeek nextDay=DayOfWeek.WEDNESDAY;
            if(i==0)
                nextDay=DayOfWeek.THURSDAY;
            slotConstraints[i]= ensureTimeSlotBIsCopyOfTimeSlotAWithDifferentSubjectPenalty(constraintFactory,
                new Timeslot(DayOfWeek.MONDAY,startTimeList.get(i),endTimeList.get(i)),
                new Timeslot(nextDay,startTimeList.get(i),endTimeList.get(i)));
        
        }
        for(int i=0;i<4;i++)
        {
            DayOfWeek nextDay=DayOfWeek.THURSDAY;
            if(i==0)
                nextDay=DayOfWeek.FRIDAY;

            slotConstraints[i+4]= ensureTimeSlotBIsCopyOfTimeSlotAWithDifferentSubjectPenalty(constraintFactory,
                new Timeslot(DayOfWeek.TUESDAY,startTimeList.get(i),endTimeList.get(i)),
                new Timeslot(nextDay,startTimeList.get(i),endTimeList.get(i)));
        
        }
        slotConstraints[8]= ensureTimeSlotBIsCopyOfTimeSlotAWithDifferentSubjectPenalty(constraintFactory,
                new Timeslot(DayOfWeek.WEDNESDAY,startTimeList.get(0),endTimeList.get(0)),
                new Timeslot(DayOfWeek.FRIDAY,startTimeList.get(1),endTimeList.get(1)));
        

        Constraint[] otherConstraints= new Constraint[] {
                // Hard constraints
                roomConflict(constraintFactory),
                teacherConflict(constraintFactory),
                noCourseRepeatOnSameDay(constraintFactory),
                sameTimeslotSubjectSetConstraint(constraintFactory),
                sameSubjectDifferentSectionConstraint(constraintFactory),
                roomCapacityConstraint(constraintFactory),
                maxLessonsPerTimeslot(constraintFactory, 9),
                // minLessonsPerDepartmentPerTimeslot(constraintFactory, "cse", 2),
                maxLessonsPerDepartmentPerTimeslot(constraintFactory, "cse", 4),
                maxLessonsPerDepartmentPerTimeslot(constraintFactory, "ece", 4),
                maxLessonsPerDepartmentPerTimeslot(constraintFactory, "other", 4),
                maxLessonsPerDepartmentPerTimeslot(constraintFactory, "math", 3),
                maxLessonsPerDepartmentPerTimeslot(constraintFactory, "des", 3),
                maxLessonsPerDepartmentPerTimeslot(constraintFactory, "bio", 3),
                // avoidHighStrengthLessonsInSameTimeSlot(constraintFactory),
                // minLessonsPerTimeslot(constraintFactory, 7),

                
                

        };
        return Stream.concat(Stream.of(slotConstraints),Stream.of(otherConstraints)).toArray(Constraint[]::new);
    }

    private Constraint ensureTimeSlotBIsCopyOfTimeSlotAWithDifferentSubjectPenalty(ConstraintFactory constraintFactory,Timeslot a,Timeslot b) {
        return constraintFactory.forEach(Lesson.class)
            .filter(lesson -> lesson.getTimeslot().equals(a))
            .ifNotExists(Lesson.class,
                Joiners.equal(Lesson::getSubject, Lesson::getSubject),
                Joiners.filtering((lessonA, lessonB) -> lessonB.getTimeslot().equals(b) &&
                                                        lessonA.getRoom().equals(lessonB.getRoom())))
            .penalize( HardSoftScore.ONE_HARD).asConstraint("Ensure time slot B is copy of time slot A with different subject penalty"+a.getDayOfWeek()+a.getStartTime()+a.getEndTime());
    }
    
    
    
    

    Constraint roomConflict(ConstraintFactory constraintFactory) {
        // A room can accommodate at most one lesson at the same time.
        return constraintFactory
                // Select each pair of 2 different lessons ...
                .forEachUniquePair(Lesson.class,
                        // ... in the same timeslot ...
                        Joiners.equal(Lesson::getTimeslot),
                        // ... in the same room ...
                        Joiners.equal(Lesson::getRoom))

                // ... and penalize each pair with a hard weight.
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("Room conflict");
    }

    Constraint teacherConflict(ConstraintFactory constraintFactory) {
        // A teacher can teach at most one lesson at the same time.
        return constraintFactory
                .forEachUniquePair(Lesson.class,
                        Joiners.equal(Lesson::getTimeslot),
                        Joiners.equal(Lesson::getTeacher))
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("Teacher conflict");
    }

    private Constraint sameSubjectDifferentSectionConstraint(ConstraintFactory factory) {
        return factory.forEachUniquePair(Lesson.class,
                Joiners.equal(Lesson::getSubject),
                Joiners.equal(Lesson::getSection)

        )
                .filter((lesson1, lesson2) -> lesson1.getMultipleSection() && lesson2.getMultipleSection())
                .filter((lesson1, lesson2) -> !lesson1.getTimeslot().equals(lesson2.getTimeslot()))
                .penalize(HardSoftScore.ONE_HARD).asConstraint("Same subject different teacher constraint");
    }

    private Constraint roomCapacityConstraint(ConstraintFactory factory) {
        return factory.forEach(Lesson.class)
                .filter(lesson -> lesson.getRoom() != null && lesson.getStrength() != null)
                .filter(lesson -> lesson.getRoom().getCapacity() < lesson.getStrength())
                .penalize(HardSoftScore.ONE_HARD).asConstraint("room capacity constraint");
    }

    public Constraint maxLessonsPerTimeslot(ConstraintFactory factory, int max) {
        UniConstraintCollector<Lesson, ?, Integer> slotCollector = ConstraintCollectors.count();
        return factory.forEach(Lesson.class)
                .groupBy(Lesson::getTimeslot, slotCollector)
                .filter((timeslot, count) -> count > max)
                .penalize(HardSoftScore.ONE_HARD).asConstraint("Max lessons per timeslot");
    }
    public Constraint minLessonsPerTimeslot(ConstraintFactory factory, int min) {
        UniConstraintCollector<Lesson, ?, Integer> slotCollector = ConstraintCollectors.count();
        return factory.forEach(Lesson.class)
                .groupBy(Lesson::getTimeslot, slotCollector)
                .filter((timeslot, count) -> count < min)
                .penalize(HardSoftScore.ONE_HARD).asConstraint("Min lessons per timeslot");
    }

    public Constraint maxLessonsPerDepartmentPerTimeslot(ConstraintFactory factory, String department, int max) {
        UniConstraintCollector<Lesson, ?, Integer> countCollector = ConstraintCollectors.count();

        return factory.forEach(Lesson.class)
                .filter(lesson -> lesson.getDepartment().equals(department))
                .groupBy(Lesson::getTimeslot, countCollector)
                .filter((timeslot, count) -> count >= max)
                .penalize(HardSoftScore.ONE_SOFT)
                .asConstraint("Max lessons per department per timeslot in" + department);
    }

    public Constraint noCourseRepeatOnSameDay(ConstraintFactory factory) {

        return factory.forEachUniquePair(Lesson.class,
                Joiners.equal(Lesson::getSubject),
                Joiners.equal((L1) -> L1.getTimeslot().getDayOfWeek()),
                Joiners.equal(Lesson::getTeacher))
                .penalize(HardSoftScore.ONE_HARD).asConstraint("No course repeat on same day");
    }

    private Constraint sameTimeslotSubjectSetConstraint(ConstraintFactory factory) {
        return factory.forEach(Lesson.class)
                .filter(lesson -> lesson.getTimeslot() != null && lesson.getSubject() != null)
                .join(Lesson.class,
                        Joiners.equal(Lesson::getTimeslot),
                        Joiners.lessThan(Lesson::getId))
                .filter((lesson1, lesson2) -> areSubjectsInSameSet(lesson1.getSubject(), lesson2.getSubject()))
                .penalize(HardSoftScore.ONE_HARD).asConstraint("Same timeslot subject set constraint1");
    }

    private boolean areSubjectsInSameSet(String subject1, String subject2) {
        for (Set<String> subjectSet : subjectSets) {
            if (subjectSet.contains(subject1) && subjectSet.contains(subject2)) {
                return true;
            }
        }
        return false;
    }
    private Constraint avoidHighStrengthLessonsInSameTimeSlot(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Lesson.class)
            .filter(lesson -> lesson.getStrength() > 100)
            .join(Lesson.class,
                Joiners.equal(Lesson::getTimeslot),
                Joiners.lessThan(Lesson::getId),
                Joiners.filtering((lesson, otherLesson) -> otherLesson.getStrength() > 90))
            .penalize( HardSoftScore.ONE_SOFT).asConstraint("avoidHighStrengthLessonsInSameTimeSlot");
    }

}
