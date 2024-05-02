package at.fhv.sysarch.lab2.homeautomation.devices;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import at.fhv.sysarch.lab2.homeautomation.devices.Environment.WeatherType;


public class Blinds extends AbstractBehavior<Blinds.BlindsCommand>{

    public interface BlindsCommand {}

    private Boolean blindsClosed;
    private final String groupId;
    private final String deviceId;


    private Blinds(ActorContext<BlindsCommand> context, String groupId, String deviceId) {
        super(context);
        this.groupId = groupId;
        this.deviceId = deviceId;
        getContext().getLog().info("Blinds ready");
    }

    public static Behavior<BlindsCommand> create(String groupId, String deviceId) {
        return Behaviors.setup(context -> new Blinds(context, groupId, deviceId));
    }

    

    public static final class AdjustBlinds implements BlindsCommand{
        public final WeatherType weatherType;

        public AdjustBlinds(WeatherType weatherType){
            this.weatherType = weatherType;
        }
    }


    public static final class AdjustBlindsMovie implements BlindsCommand{

        public final Boolean isMoviePlaying;

        public AdjustBlindsMovie(Boolean isMoviePlaying){
            this.isMoviePlaying = isMoviePlaying;
        }
    }

    @Override
    public Receive<BlindsCommand> createReceive(){
        return newReceiveBuilder()
        .onMessage(AdjustBlinds.class, this::onAdjustBehavior)
        .onMessage(AdjustBlindsMovie.class, this::mediastationBehavior)
        .build();
    }


    private Behavior<BlindsCommand> onAdjustBehavior(AdjustBlinds command){
        switch(command.weatherType){
            case SUNNY:
                getContext().getLog().info("It´s sunny, Blinds going down");
                blindsClosed = true;
                break;
            case FOGGY:
            case RAINY:
                getContext().getLog().info("Its " + command.weatherType + " Blinds going up");
                blindsClosed = false;
                break;
        }   
        return this;
    }


    private Behavior<BlindsCommand> mediastationBehavior(AdjustBlindsMovie command){
            if(command.isMoviePlaying == true){
                blindsClosed = true;
                getContext().getLog().info("A movie is starting the blinds are going down");
            }else{
                blindsClosed = false;
            }
        return this;
    }




    
}
