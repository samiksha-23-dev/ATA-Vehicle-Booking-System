package com.ata.admin.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

@Entity
@Table(name = "routes")
public class Route {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long routeId;

    @NotBlank
    @Column(nullable = false)
    private String source;

    @NotBlank
    @Column(nullable = false)
    private String destination;

    @Positive(message = "Distance must be positive")
    private double distance;

    @NotBlank
    private String duration;

    public Route() {}

    public Long getRouteId() { return routeId; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }
    public double getDistance() { return distance; }
    public void setDistance(double distance) { this.distance = distance; }
    public String getDuration() { return duration; }
    public void setDuration(String duration) { this.duration = duration; }
}
