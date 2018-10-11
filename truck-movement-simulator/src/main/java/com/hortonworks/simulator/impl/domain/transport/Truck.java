package com.hortonworks.simulator.impl.domain.transport;

import akka.actor.ActorRef;
import com.hortonworks.simulator.impl.domain.AbstractEventEmitter;
import com.hortonworks.simulator.impl.domain.gps.Location;
import com.hortonworks.simulator.impl.domain.transport.route.Route;
import com.hortonworks.simulator.impl.messages.EmitEvent;
import org.apache.log4j.Logger;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class Truck extends AbstractEventEmitter {

  private static final long serialVersionUID = 9157180698115417087L;
  private static final Logger LOG = Logger.getLogger(Truck.class);

  private final boolean minimal = true;
  
  private Driver driver;
  private int truckId;
  private int messageCount = 0;

  private List<MobileEyeEventTypeEnum> eventTypes;

  private int numberOfEventsToGenerate;
  private long demoId;
  private int messageDelay;

  private Random rand = new Random();

  public Truck(int numberOfEvents, long demoId, int messageDelay) {
    this.messageDelay = messageDelay;
    driver = TruckConfiguration.getNextDriver();
    truckId = TruckConfiguration.getNextTruckId();
    eventTypes = Arrays.asList(MobileEyeEventTypeEnum.values());

    this.numberOfEventsToGenerate = numberOfEvents;
    this.demoId = demoId;

    LOG.info("New Truck Instance[" + truckId + "] with Driver[" + driver
        .getDriverName() + "] " +
        "has started  new Route[" + driver.getRoute().getRouteName() + "], " +
        "RouteId[" +
        driver.getRoute().getRouteId() + "]");
  }


  public MobileEyeEvent generateEvent() {

		/* If the route has ended, then assign a new truck to the driver. */
    changeTruckIfRequired();

		/* Change the route for driver after a period of time */
    changeDriverRouteIfRequired();


    Location nextLocation = getDriver().getRoute().getNextLocation();
    if (messageCount % driver.getRiskFactor() == 0)
      return new MobileEyeEvent(demoId, nextLocation, getRandomUnsafeEvent(),
          this);
    else
      return new MobileEyeEvent(demoId, nextLocation,
          MobileEyeEventTypeEnum.NORMAL, this);
  }


  private void changeDriverRouteIfRequired() {
    try {
      if (getDriver().getRouteTraversalCount() > TruckConfiguration
          .MAX_ROUTE_TRAVERSAL_COUNT) {
        LOG.info("The Driver[" + getDriver().getDriverName() + "] for Truck["
            + truckId + "] needs to be have its Route[" + getDriver()
            .getRoute().getRouteName() + "] changed.");
        Route newRoute = TruckConfiguration.freeRoutePool.poll();
        while (newRoute == null) {
          LOG.info("The Driver[" + getDriver().getDriverName() + "] for " +
              "Truck[" + truckId + "] is going to wait 5 seconds for a new " +
              "route to be abailable");
          Thread.sleep(5000);
          newRoute = TruckConfiguration.freeRoutePool.poll();
        }
        Route oldRoute = getDriver().getRoute();
        TruckConfiguration.freeRoutePool.offer(oldRoute);
        LOG.info("The Driver[" + getDriver().getDriverName() + "] for Truck["
            + truckId + "] releasing old Route[" + oldRoute.getRouteName() +
            "], RouteId[" + oldRoute.getRouteId() + "].");

        getDriver().provideRoute(newRoute);
        LOG.info("The Driver[" + getDriver().getDriverName() + "] for Truck["
            + truckId + "] found a new Route[" + getDriver().getRoute()
            .getRouteName() + "], RouteId[" + getDriver().getRoute()
            .getRouteId() + "].");
      }
    } catch (Exception e) {
      LOG.error("Error Changing route for Driver[" + getDriver()
          .getDriverName() + "] for Truck[" + truckId + "]");
    }
  }


  private void changeTruckIfRequired() {
    if (getDriver().getRoute().routeEnded()) {

      LOG.info("Route has ended for Driver[" + getDriver().getDriverId() + "]" +
          " on Truck[" + truckId + "]");
      Integer lastTruckId = new Integer(truckId);
      Integer nextFreeTruck = TruckConfiguration.freeTruckPool.poll();

      //Pick up a new Truck
      if (nextFreeTruck != null)
        truckId = nextFreeTruck.intValue();
      else
        truckId = TruckConfiguration.getNextTruckId();

      TruckConfiguration.freeTruckPool.offer(lastTruckId);


      //increment the routeTraversal count
      getDriver().incrementRootTraversalCount();

      LOG.info("The Driver[" + getDriver().getDriverName() + "] has new " +
          "Truck[" + truckId + "] with[" + getDriver().getRoute()
          .getRouteName() + "] traversed " + getDriver()
          .getRouteTraversalCount() + " times.");
    }
  }

  private MobileEyeEventTypeEnum getRandomUnsafeEvent() {
    return eventTypes.get(rand.nextInt(eventTypes.size() - 1));
  }

  @Override
  public String toString() {
	  SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");  
	  
    return sdf.format(new Date().getTime()) + "," + truckId + ","
        + driver.getDriverId() + "," + driver.getDriverName() + "," + driver
        .getRoute().getRouteId() + "," + driver.getRoute().getRouteName() + ",";
  }

  public String toJson() {
	  return "{ timestamp = " + new Timestamp(new Date().getTime()) + "\"" + " truckId = \"" + truckId + "\"" + "}";
  }

  public String toCSV(Integer timeFactor) {
	  SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");  
	  String driverName = (minimal) ? "" : "," + driver.getDriverName();
	  
	  return (new Date().getTime() * timeFactor) 
	    				+ "," + truckId 
	    				+ "," + driver.getDriverId() 
	    				+ driverName 
	    				+ "," + driver.getRoute().getRouteId()  + ",";
  }

  @Override
  public void onReceive(Object message) throws Exception {
    if (message instanceof EmitEvent) {
      ActorRef actor = this.context().system()
          .actorFor("akka://EventSimulator/user/eventCollector");
      Random rand = new Random();
      long timeline = 0;
      if (numberOfEventsToGenerate == -1) {
        while (true) {
          //The next message will be sent roughly after a delay of  "messageDelay" milliseconds.
          //This allows for streams to deviate from one another instead of moving in lock-step.
          double offset_factor = rand.nextDouble() * 0.25; // shave off approx 25% of the delay...
          timeline += messageDelay - (offset_factor * messageDelay);
          messageCount++;
          this.context().system().scheduler().scheduleOnce(scala.concurrent
                  .duration.Duration.create(timeline, TimeUnit.MILLISECONDS),
              actor, generateEvent(), this.context().system().dispatcher(),
              this.getSender());
        }

      } else {
        while (messageCount < numberOfEventsToGenerate) {
          double offset_factor = rand.nextDouble() * 0.25;
          timeline += messageDelay - (offset_factor * messageDelay);
          messageCount++;
          context().system().scheduler().scheduleOnce(scala.concurrent
                  .duration.Duration.create(timeline, TimeUnit.MILLISECONDS),
              actor, generateEvent(), this.context().system().dispatcher(),
              this.getSender());
        }
        LOG.info("Truck[" + truckId + "] with Driver[" + driver.getDriverName
            () + " ] has stopped its route");
      }

    }
  }

  public int getTruckId() {
    return truckId;
  }

  public Driver getDriver() {
    return driver;
  }

  public void setDriver(Driver driver) {
    this.driver = driver;
  }
}
