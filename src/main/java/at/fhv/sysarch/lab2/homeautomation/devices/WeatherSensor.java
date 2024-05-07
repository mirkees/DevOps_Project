package at.fhv.sysarch.lab2.homeautomation.devices;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import at.fhv.sysarch.lab2.homeautomation.devices.Environment.WeatherType;

import java.util.Optional;


public class WeatherSensor extends AbstractBehavior<WeatherSensor.WeatherCommand> {

    public interface WeatherCommand{}


    private final String groupId;
    private final String deviceId;
    private final ActorRef<Blinds.BlindsCommand> blinds;

    

    private WeatherSensor(ActorContext<WeatherCommand> context, ActorRef<Blinds.BlindsCommand> blinds, String groupId, String deviceId){
        super(context);
        this.blinds = blinds;
        this.deviceId = deviceId;
        this.groupId = groupId;
    }

    public static Behavior<WeatherCommand> create(ActorRef<Blinds.BlindsCommand> blinds, String groupId, String deviceId){
        return Behaviors.setup(context -> new WeatherSensor(context, blinds, groupId, deviceId));

    }

    public static final class ReadWeather implements WeatherCommand{
        final Optional<WeatherType> value;

        public ReadWeather(Optional<WeatherType> value){
            this.value = value;
        }
    }

   

    @Override
    public Receive<WeatherCommand> createReceive() {
        return newReceiveBuilder()
            .onMessage(
                ReadWeather.class, this::onReadWeather)
            .build();
    }
    private Behavior<WeatherCommand> onReadWeather(ReadWeather command) {
        command.value.ifPresent(weatherType -> {
            getContext().getLog().info("Received weather update: {}", weatherType);
            this.blinds.tell(new Blinds.AdjustBlinds(weatherType));
        });
        return this;
    }
    



}
