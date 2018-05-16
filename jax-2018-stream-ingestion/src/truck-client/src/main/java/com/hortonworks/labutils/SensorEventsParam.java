package com.hortonworks.labutils;

import java.io.Serializable;

public class SensorEventsParam implements Serializable {

  private static final long serialVersionUID = 8764713202535596728L;

  private int numberOfEventEmitters;
  private int numberOfEvents;
  String eventEmitterClassName;
  String eventCollectorClassName;

  private String routeDirectory;
  private double centerCoordinatesLat;
  private double centerCoordinatesLong;
  private int zoomLevel;
  private int truckSymbolSize;
  private int delayBetweenEvents;

  public int getZoomLevel() {
    return zoomLevel;
  }
  public void setZoomLevel(int zoomLevel) {
    this.zoomLevel = zoomLevel;
  }
  public int getNumberOfEventEmitters() {
    return numberOfEventEmitters;
  }
  public void setNumberOfEventEmitters(int numberOfEventEmitters) {
    this.numberOfEventEmitters = numberOfEventEmitters;
  }
  public int getNumberOfEvents() {
    return numberOfEvents;
  }
  public void setNumberOfEvents(int numberOfEvents) {
    this.numberOfEvents = numberOfEvents;
  }
  public String getEventEmitterClassName() {
    return eventEmitterClassName;
  }
  public void setEventEmitterClassName(String eventEmitterClassName) {
    this.eventEmitterClassName = eventEmitterClassName;
  }
  public String getEventCollectorClassName() {
    return eventCollectorClassName;
  }
  public void setEventCollectorClassName(String eventCollectorClassName) {
    this.eventCollectorClassName = eventCollectorClassName;
  }
  public String getRouteDirectory() {
    return routeDirectory;
  }
  public void setRouteDirectory(String routeDirectory) {
    this.routeDirectory = routeDirectory;
  }
  public double getCenterCoordinatesLat() {
    return centerCoordinatesLat;
  }
  public void setCenterCoordinatesLat(double centerCoordinatesLat) {
    this.centerCoordinatesLat = centerCoordinatesLat;
  }
  public double getCenterCoordinatesLong() {
    return centerCoordinatesLong;
  }
  public void setCenterCoordinatesLong(double centerCoordinatesLong) {
    this.centerCoordinatesLong = centerCoordinatesLong;
  }
  public int getTruckSymbolSize() {
    return truckSymbolSize;
  }
  public void setTruckSymbolSize(int truckSymbolSize) {
    this.truckSymbolSize = truckSymbolSize;
  }
  public int getDelayBetweenEvents() {
    return delayBetweenEvents;
  }
  public void setDelayBetweenEvents(int delayBetweenEvents) {
    this.delayBetweenEvents = delayBetweenEvents;
  }

}
