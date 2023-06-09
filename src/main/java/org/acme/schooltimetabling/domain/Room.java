package org.acme.schooltimetabling.domain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.optaplanner.core.api.domain.lookup.PlanningId;

@Entity
public class Room {

    @PlanningId
    @Id @GeneratedValue
    private Long id;

    private String name;
    private int capacity;

    // No-arg constructor required for Hibernate
    public Room() {
    }

    public Room(String name,int capacity) {
        this.name = name;
        this.capacity = capacity;
    }

    public Room(long id, String name,int capacity) {
        this(name,capacity);
        this.id = id;
        // this.capacity = capacity;
    }

    @Override
    public String toString() {
        return name;
    }

    // ************************************************************************
    // Getters and setters
    // ************************************************************************

    public Long getId() {
        return id;
    }
    public int getCapacity() {
        return capacity;
    }

    public String getName() {
        return name;
    }

}
