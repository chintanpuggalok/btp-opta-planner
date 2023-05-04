package org.acme.schooltimetabling.domain;

import java.time.DayOfWeek;
import java.time.LocalTime;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.lookup.PlanningId;
import org.optaplanner.core.api.domain.variable.PlanningVariable;

@PlanningEntity
@Entity
public class Lesson {

    @PlanningId
    @Id
    @GeneratedValue
    private Long id;

    private String subject;
    private String teacher;
    private String studentGroup;
    private String section;
    private Boolean multipleSection;
    private Integer strength;
    private String department;
    private int WeeklyFrequency;

    @PlanningVariable
    @ManyToOne
    private Timeslot timeslot;

    @PlanningVariable
    @ManyToOne
    private Room room;

    // No-arg constructor required for Hibernate and OptaPlanner
    public Lesson() {
    }

    public Lesson(String subject, String teacher, String studentGroup, int strength, String department) {
        this.subject = subject;
        this.teacher = teacher;
        this.studentGroup = studentGroup;
        this.section = "A";
        this.multipleSection = false;
        this.strength = strength;
        this.department = department;
        this.WeeklyFrequency = 2;
    }

    public Lesson(String subject, String teacher, String studentGroup, int strength, String Department,
            String section) {
        this.subject = subject;
        this.teacher = teacher;
        this.studentGroup = studentGroup;
        this.section = section;
        this.multipleSection = true;
        this.strength = strength;
        this.department = Department;
        this.WeeklyFrequency = 2;
    }

    public Boolean getMultipleSection() {
        return this.multipleSection;
    }

    public Integer getStrength() {
        return this.strength;
    }

    // public Lesson(long id, String subject, String teacher, String studentGroup,
    // Timeslot timeslot, Room room) {
    // this(subject, teacher, studentGroup);
    // this.id = id;
    // this.timeslot = timeslot;
    // this.room = room;
    // }

    @Override
    public String toString() {
        return subject + "(" + id + ")";
    }

    // ************************************************************************
    // Getters and setters
    // ************************************************************************
    public String getDepartment() {

        return this.department;
    }

    public String getSection() {
        return section;
    }

    public Long getId() {
        return id;
    }

    public String getSubject() {
        return subject;
    }

    public String getTeacher() {
        return teacher;
    }

    public String getStudentGroup() {
        return studentGroup;
    }

    public Timeslot getTimeslot() {
        return timeslot;
    }

    public void setTimeslot(Timeslot timeslot) {
        this.timeslot = timeslot;
    }

    public int getWeeklyFrequency() {
        return WeeklyFrequency;
    }

    public void setWeeklyFrequency(int weeklyFrequency) {
        this.WeeklyFrequency = weeklyFrequency;
    }
    // public LocalTime getStartTime() {
    // return timeslot.getStartTime();
    // }
    // public DayOfWeek getDayOfWeek() {
    // // System.out.println("timeslot"+timeslot.toString());
    // if(timeslot == null)
    // return null;
    // else
    // return timeslot.getDayOfWeek();
    // // return timeslot.getDayOfWeek();
    // }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }
}
