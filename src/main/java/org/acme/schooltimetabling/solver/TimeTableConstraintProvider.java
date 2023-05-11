package org.acme.schooltimetabling.solver;

import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.api.score.stream.Constraint;
import org.optaplanner.core.api.score.stream.ConstraintCollectors;
import org.optaplanner.core.api.score.stream.ConstraintFactory;
import org.optaplanner.core.api.score.stream.ConstraintProvider;
import org.optaplanner.core.api.score.stream.Joiners;
import org.optaplanner.core.api.score.stream.uni.UniConstraintCollector;
import java.time.DayOfWeek;

import org.acme.schooltimetabling.domain.Lesson;
import org.acme.schooltimetabling.domain.Timeslot;
import org.acme.schooltimetabling.utils.ReadWrite;

public class TimeTableConstraintProvider implements ConstraintProvider {
        private List<Set<String>> subjectSets = ReadWrite.getBucketList();

        @Override
        public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {

                String y1 = "Year 1";
                String y2 = "Year 2";
                String y3 = "UG/PG";

                // subjectSets = TimeTableSpringBootApp.getbucketList();
                // subjectSets.add(Set.of("Math","Chemistry","Biology"));
                // Map<String, Integer> minLessonCount = Map.of("cse",2,"ece",1);
                List<LocalTime> startTimeList = List.of(LocalTime.of(9, 00), LocalTime.of(10, 30), LocalTime.of(12, 00),
                                LocalTime.of(14, 30), LocalTime.of(16, 00));
                List<LocalTime> endTimeList = List.of(LocalTime.of(10, 30), LocalTime.of(12, 00), LocalTime.of(13, 30),
                                LocalTime.of(16, 00), LocalTime.of(17, 30));
                Constraint[] slotConstraints = new Constraint[22];
                // for (int i = 0; i < 5; i++) {
                // slotConstraints[i] = new Timeslot(DayOfWeek.MONDAY, startTimeList.get(i),
                // endTimeList.get(i));
                // timeslotRepository.save(new Timeslot(DayOfWeek.TUESDAY, startTimeList.get(i),
                // endTimeList.get(i)));
                // timeslotRepository.save(new Timeslot(DayOfWeek.WEDNESDAY,
                // startTimeList.get(i), endTimeList.get(i)));
                // timeslotRepository.save(new Timeslot(DayOfWeek.THURSDAY,
                // startTimeList.get(i), endTimeList.get(i)));
                // timeslotRepository.save(new Timeslot(DayOfWeek.FRIDAY, startTimeList.get(i),
                // endTimeList.get(i)));
                // }
                for (int k = 0; k < 10; k++) {
                        int i = k / 2;
                        DayOfWeek nextDay = DayOfWeek.WEDNESDAY;
                        if (i == 0)
                                nextDay = DayOfWeek.THURSDAY;
                        slotConstraints[2 * i] = ensureTimeSlotBIsCopyOfTimeSlotAWithDifferentSubjectPenalty(
                                        constraintFactory,
                                        new Timeslot(DayOfWeek.MONDAY, startTimeList.get(i), endTimeList.get(i)),
                                        new Timeslot(nextDay, startTimeList.get(i), endTimeList.get(i)));
                        slotConstraints[2 * i + 1] = ensureTimeSlotBIsCopyOfTimeSlotAWithDifferentSubjectPenalty(
                                        constraintFactory,
                                        new Timeslot(nextDay, startTimeList.get(i), endTimeList.get(i)),
                                        new Timeslot(DayOfWeek.MONDAY, startTimeList.get(i), endTimeList.get(i)));

                }
                for (int k = 0; k < 10; k++) {
                        DayOfWeek nextDay = DayOfWeek.THURSDAY;
                        int i = k / 2;
                        if (i == 0)
                                nextDay = DayOfWeek.FRIDAY;

                        slotConstraints[2 * i + 10] = ensureTimeSlotBIsCopyOfTimeSlotAWithDifferentSubjectPenalty(
                                        constraintFactory,
                                        new Timeslot(DayOfWeek.TUESDAY, startTimeList.get(i), endTimeList.get(i)),
                                        new Timeslot(nextDay, startTimeList.get(i), endTimeList.get(i)));
                        slotConstraints[2 * i + 11] = ensureTimeSlotBIsCopyOfTimeSlotAWithDifferentSubjectPenalty(
                                        constraintFactory,
                                        new Timeslot(nextDay, startTimeList.get(i), endTimeList.get(i)),
                                        new Timeslot(DayOfWeek.TUESDAY, startTimeList.get(i), endTimeList.get(i)));

                }
                slotConstraints[20] = ensureTimeSlotBIsCopyOfTimeSlotAWithDifferentSubjectPenalty(constraintFactory,
                                new Timeslot(DayOfWeek.WEDNESDAY, startTimeList.get(0), endTimeList.get(0)),
                                new Timeslot(DayOfWeek.FRIDAY, startTimeList.get(1), endTimeList.get(1)));
                slotConstraints[21] = ensureTimeSlotBIsCopyOfTimeSlotAWithDifferentSubjectPenalty(constraintFactory,
                                new Timeslot(DayOfWeek.FRIDAY, startTimeList.get(1), endTimeList.get(1)),
                                new Timeslot(DayOfWeek.WEDNESDAY, startTimeList.get(0), endTimeList.get(0)));

                Constraint[] otherConstraints = new Constraint[] {
                                // Hard constraints
                                // sameTimeslotSubjectSetConstraint(constraintFactory),
                                roomConflict(constraintFactory),
                                teacherConflict(constraintFactory),
                                noCourseRepeatOnSameDay(constraintFactory),
                                ensureSubjectRoomIsSamePenalty(constraintFactory, y1),
                                ensureSubjectRoomIsSamePenalty(constraintFactory, y2),
                                ensureSubjectRoomIsSamePenalty(constraintFactory, y3),
                                ensureSameRoomForFreq3Courses(constraintFactory),
                                sameSubjectDifferentSectionConstraint(constraintFactory),
                                sameTimeslotSubjectSetConstraint(constraintFactory, y1),
                                roomCapacityConstraint(constraintFactory),
                                // Year 1
                                maxLessonsPerTimeslot(constraintFactory, 4, y1),
                                maxLessonsPerTimeslot(constraintFactory, 4, y2),
                                maxLessonsPerTimeslot(constraintFactory, 10, y3),
                                // minLessonsPerDepartmentPerTimeslot(constraintFactory, "cse", 2),
                                maxLessonsPerDepartmentPerTimeslot(constraintFactory, "cse", 2, y1),
                                maxLessonsPerDepartmentPerTimeslot(constraintFactory, "ece", 1, y1),
                                maxLessonsPerDepartmentPerTimeslot(constraintFactory, "other", 3, y1),
                                maxLessonsPerDepartmentPerTimeslot(constraintFactory, "math", 2, y1),
                                maxLessonsPerDepartmentPerTimeslot(constraintFactory, "des", 1, y1),
                                maxLessonsPerDepartmentPerTimeslot(constraintFactory, "bio", 1, y1),
                                // Year2
                                // minLessonsPerDepartmentPerTimeslot(constraintFactory, "cse", 2),
                                maxLessonsPerDepartmentPerTimeslot(constraintFactory, "cse", 3, y2),
                                maxLessonsPerDepartmentPerTimeslot(constraintFactory, "ece", 1, y2),
                                maxLessonsPerDepartmentPerTimeslot(constraintFactory, "other", 3, y2),
                                maxLessonsPerDepartmentPerTimeslot(constraintFactory, "math", 2, y2),
                                maxLessonsPerDepartmentPerTimeslot(constraintFactory, "des", 1, y2),
                                maxLessonsPerDepartmentPerTimeslot(constraintFactory, "bio", 1, y2),
                                // avoidHighStrengthLessonsInSameTimeSlot(constraintFactory),
                                // minLessonsPerTimeslot(constraintFactory, 7),
                                maxLessonsPerDepartmentPerTimeslot(constraintFactory, "cse", 3, y3),
                                maxLessonsPerDepartmentPerTimeslot(constraintFactory, "ece", 2, y3),
                                maxLessonsPerDepartmentPerTimeslot(constraintFactory, "other", 3, y3),
                                maxLessonsPerDepartmentPerTimeslot(constraintFactory, "math", 3, y3),
                                maxLessonsPerDepartmentPerTimeslot(constraintFactory, "des", 2, y3),
                                maxLessonsPerDepartmentPerTimeslot(constraintFactory, "bio", 2, y3)

                };
                return Stream.concat(Stream.of(slotConstraints), Stream.of(otherConstraints))
                                .toArray(Constraint[]::new);
        }

        private Constraint ensureSameRoomForFreq3Courses(ConstraintFactory constraintFactory) {
                return constraintFactory.forEachUniquePair(Lesson.class,
                                Joiners.equal(Lesson::getSubject),
                                Joiners.equal(Lesson::getTeacher))
                                .filter((lesson1, lesson2) -> lesson1.getWeeklyFrequency() == 3
                                                && lesson2.getWeeklyFrequency() == 3)
                                .filter((lesson1, lesson2) -> lesson1.getTimeslot().equals(lesson2.getTimeslot()))
                                .filter((lesson1, lesson2) -> lesson1.getRoom().equals(lesson2.getRoom()))
                                .penalize(HardSoftScore.ofHard(3))
                                .asConstraint("freq3 invalid");
        }

        private Constraint ensureTimeSlotBIsCopyOfTimeSlotAWithDifferentSubjectPenalty(
                        ConstraintFactory constraintFactory,
                        Timeslot a, Timeslot b) {
                return constraintFactory.forEach(Lesson.class)
                                .filter(lesson -> lesson.getTimeslot().equals(a))
                                .filter(lesson -> lesson.getWeeklyFrequency() == 2)
                                .ifNotExists(Lesson.class,
                                                Joiners.equal(Lesson::getSubject, Lesson::getSubject),
                                                Joiners.equal(Lesson::getTeacher, Lesson::getTeacher),
                                                Joiners.filtering((lessonA, lessonB) -> lessonB.getTimeslot().equals(b)
                                                                && lessonA.getRoom().equals(lessonB.getRoom())))
                                .penalize(HardSoftScore.ofHard(2))
                                .asConstraint("Ensure time slot B is copy of time slot A with different subject penalty"
                                                + a.getDayOfWeek() + a.getStartTime() + a.getEndTime());
        }

        private Constraint sameSubjectDifferentSectionConstraint(ConstraintFactory factory) {
                return factory.forEachUniquePair(Lesson.class,
                                Joiners.equal(Lesson::getSubject),
                                Joiners.equal(Lesson::getSection)

                ).filter((lesson1, lesson2) -> lesson1.getStudentGroup().equals(lesson2.getStudentGroup()))
                                .filter((lesson1, lesson2) -> lesson1.getMultipleSection()
                                                && lesson2.getMultipleSection())
                                .filter((lesson1, lesson2) -> !lesson1.getTimeslot().equals(lesson2.getTimeslot()))
                                .penalize(HardSoftScore.ofHard(2))
                                .asConstraint("Same subject different section constraint");
        }

        private Constraint sameTimeslotSubjectSetConstraint(ConstraintFactory factory, String studentGroup) {
                return factory.forEach(Lesson.class)
                                .filter(lesson -> lesson.getStudentGroup().equals(studentGroup))
                                .filter(lesson -> lesson.getTimeslot() != null && lesson.getSubject() != null)
                                .join(Lesson.class,
                                                Joiners.equal(Lesson::getTimeslot),
                                                Joiners.lessThan(Lesson::getId))
                                .filter((lesson1, lesson2) -> !lesson1.getSubject().equals(lesson2.getSubject()))
                                .filter((lesson1, lesson2) -> areSubjectsInSameSet(lesson1.getSubject(),
                                                lesson2.getSubject()))
                                .penalize(HardSoftScore.ONE_HARD)
                                .asConstraint("Same timeslot subject set constraint" + studentGroup);
        }

        private Constraint ensureSubjectRoomIsSamePenalty(ConstraintFactory constraintFactory, String studentGroup) {
                return constraintFactory.forEach(Lesson.class)
                                .filter(lesson -> lesson.getStudentGroup().equals(studentGroup))
                                .filter(lesson -> lesson.getTimeslot() != null && lesson.getSubject() != null)
                                .ifNotExists(Lesson.class,
                                                Joiners.equal(Lesson::getSubject, Lesson::getSubject),
                                                Joiners.equal(Lesson::getTeacher, Lesson::getTeacher),
                                                Joiners.filtering((lessonA, lessonB) -> lessonA.getRoom()
                                                                .equals(lessonB.getRoom())),
                                                Joiners.filtering(
                                                                (lesson1, lesson2) -> areSubjectsInSameSet(
                                                                                lesson1.getSubject(),
                                                                                lesson2.getSubject())))
                                .penalize(HardSoftScore.ofHard(3))
                                .asConstraint("Ensure room-subject penalty" + studentGroup);
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

        private Constraint roomCapacityConstraint(ConstraintFactory factory) {
                return factory.forEach(Lesson.class)
                                .filter(lesson -> lesson.getRoom() != null && lesson.getStrength() != null)
                                .filter(lesson -> lesson.getRoom().getCapacity() < lesson.getStrength())
                                .penalize(HardSoftScore.ONE_HARD).asConstraint("room capacity constraint");
        }

        public Constraint maxLessonsPerTimeslot(ConstraintFactory factory, int max, String studentGroup) {
                UniConstraintCollector<Lesson, ?, Integer> slotCollector = ConstraintCollectors.count();
                return factory.forEach(Lesson.class)
                                .filter(lesson -> lesson.getStudentGroup().equals(studentGroup))
                                .groupBy(Lesson::getTimeslot, slotCollector)
                                .filter((timeslot, count) -> count > max)
                                .penalize(HardSoftScore.ONE_SOFT)
                                .asConstraint("Max lessons per timeslot" + studentGroup);
        }

        public Constraint minLessonsPerTimeslot(ConstraintFactory factory, int min) {
                UniConstraintCollector<Lesson, ?, Integer> slotCollector = ConstraintCollectors.count();
                return factory.forEach(Lesson.class)
                                .groupBy(Lesson::getTimeslot, slotCollector)
                                .filter((timeslot, count) -> count < min)
                                .penalize(HardSoftScore.ONE_SOFT).asConstraint("Min lessons per timeslot");
        }

        public Constraint maxLessonsPerDepartmentPerTimeslot(ConstraintFactory factory, String department, int max,
                        String studentGroup) {
                UniConstraintCollector<Lesson, ?, Integer> countCollector = ConstraintCollectors
                                .countDistinct(Lesson::getSubject);

                return factory.forEach(Lesson.class)
                                .filter(lesson -> lesson.getStudentGroup().equals(studentGroup))
                                .filter(lesson -> lesson.getDepartment().equals(department))
                                .groupBy(Lesson::getTimeslot, countCollector)
                                .filter((timeslot, count) -> count >= max)
                                .penalize(HardSoftScore.ONE_SOFT)
                                .asConstraint("Max lessons per department per timeslot in" + department + studentGroup);
        }

        public Constraint noCourseRepeatOnSameDay(ConstraintFactory factory) {

                return factory.forEachUniquePair(Lesson.class,
                                Joiners.equal(Lesson::getSubject),
                                Joiners.equal((L1) -> L1.getTimeslot().getDayOfWeek()),
                                Joiners.equal(Lesson::getTeacher))
                                .filter((lesson1, lesson2) -> areSubjectsInSameSet(lesson1.getSubject(),
                                                lesson2.getSubject()))
                                .penalize(HardSoftScore.ONE_HARD).asConstraint("No course repeat on same day");
        }

        private boolean areSubjectsInSameSet(String subject1, String subject2) {
                for (Set<String> subjectSet : subjectSets) {
                        if (subjectSet.contains(subject1) && subjectSet.contains(subject2)) {
                                return true;
                        }
                }
                return false;
        }

        private Constraint avoidHighStrengthLessonsInSameTimeSlot(ConstraintFactory constraintFactory,
                        String studentGroup) {
                return constraintFactory.forEach(Lesson.class)
                                .filter(lesson -> lesson.getStudentGroup().equals(studentGroup))
                                .filter(lesson -> lesson.getStrength() > 100)
                                .join(Lesson.class,
                                                Joiners.equal(Lesson::getTimeslot),
                                                Joiners.lessThan(Lesson::getId),
                                                Joiners.filtering((lesson,
                                                                otherLesson) -> otherLesson.getStrength() > 90))
                                .penalize(HardSoftScore.ONE_SOFT)
                                .asConstraint("avoidHighStrengthLessonsInSameTimeSlot" + studentGroup);
        }

}
