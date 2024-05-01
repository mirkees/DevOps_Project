package at.fhv.sysarch.lab2.homeautomation.devices;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import at.fhv.sysarch.lab2.homeautomation.devices.Environment.WeatherType;


public class Blinds extends AbstractBehavior<Blinds.BlindsCommand>{

    public interface BlindsCommand {}


    public static final class AdjustBlinds implements BlindsCommand{
        public final WeatherType weatherType;

        public AdjustBlinds(WeatherType weatherType){
            this.weatherType = weatherType;
        }
    }

    @Override
    public Receive<BlindsCommand> createReceive(){
        return newReceiveBuilder()
        .onMessage(AdjustBlinds.class, this::onAdjustBehavior)
        .build();
    }


    private Behavior<BlindsCommand> onAdjustBehavior(AdjustBlinds command){
        switch(command.weatherType){
            case SUNNY:
                getContext().getLog().info("It´s sunny, Blinds going down");
                break;
            case FOGGY:
            case RAINY:
                getContext().getLog().info("Its " + command.weatherType + " Blinds going up");
                break;
        }   
        return this;
    }


    private final String groupId;
    private final String deviceId;

    public Blinds(ActorContext<BlindsCommand> context, String groupId, String deviceId) {
        super(context);
        this.groupId = groupId;
        this.deviceId = deviceId;
        getContext().getLog().info("Blinds ready");
    }

    public static Behavior<BlindsCommand> create(String groupId, String deviceId) {
        return Behaviors.setup(context -> new Blinds(context, groupId, deviceId));
    }


}
