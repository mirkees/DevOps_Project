package at.fhv.sysarch.lab2.homeautomation.devices;

import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.javadsl.*;
import java.util.Random;
import java.time.Duration;
import java.util.Optional;
import akka.actor.typed.ActorRef;

public class Environment extends AbstractBehavior<Environment.EnvironmentCommand> {

    public interface EnvironmentCommand {}

    public static final class TemperatureChanger implements EnvironmentCommand {
        

    }

    public enum WeatherType{
        SUNNY, RAINY, FOGGY
    }

    public static final class WeatherConditionsChanger implements EnvironmentCommand {
        final Optional<WeatherType> weatherType;

        public WeatherConditionsChanger(Optional<WeatherType> weatherType) {
            this.weatherType = weatherType;
        }
    }

    private double temperature = 19.0;
    private boolean isSunny = false;

    private final TimerScheduler<EnvironmentCommand> temperatureTimeScheduler;
    private final TimerScheduler<EnvironmentCommand> weatherTimeScheduler;
    private final ActorRef<TemperatureSensor.TemperatureCommand> temperatureSensor; //--
    private final ActorRef<WeatherSensor.WeatherCommand> weatherSensor; //--


    // TODO: Provide the means for manually setting the temperature //--
    public static final class SetTemperature implements EnvironmentCommand{
        final double newTemperature;

        public SetTemperature(double newTemperature){
            this.newTemperature = newTemperature;
        }
    } 
    // TODO: Provide the means for manually setting the weather //--
    public static final class SetWeather implements EnvironmentCommand {
        final WeatherType weatherType;
    
        public SetWeather(WeatherType weatherType) {
            this.weatherType = weatherType;
        }
    } 
    

    private Behavior<EnvironmentCommand> onSetTemperature(SetTemperature command){
        temperature = command.newTemperature;
        getContext().getLog().info("Manually set temperature to {}", temperature);
        return this;
    } //--

    private Behavior<EnvironmentCommand> onSetWeather(SetWeather command){
        currentWeather = command.weatherType;
        getContext().getLog().info("Manually set the weather to {}", isSunny);
        return this;
    } //--

    

    public static Behavior<EnvironmentCommand> create(ActorRef<TemperatureSensor.TemperatureCommand> temperatureSensor, ActorRef<WeatherSensor.WeatherCommand> weatherSensor){
        return Behaviors.setup(context ->  Behaviors.withTimers(timers -> new Environment(context, timers, timers, temperatureSensor, weatherSensor)));
    }


    private Environment(ActorContext<EnvironmentCommand> context,TimerScheduler<EnvironmentCommand> tempTimer, TimerScheduler<EnvironmentCommand> weatherTimer, ActorRef<TemperatureSensor.TemperatureCommand> temperatureSensor, ActorRef<WeatherSensor.WeatherCommand> weatherSensor ) {
        super(context);
        this.temperatureTimeScheduler = tempTimer;
        this.weatherTimeScheduler = weatherTimer;
        this.temperatureSensor = temperatureSensor;
        this.weatherSensor = weatherSensor;
        this.temperatureTimeScheduler.startTimerAtFixedRate(new TemperatureChanger(), Duration.ofSeconds(5));
        this.weatherTimeScheduler.startTimerAtFixedRate(new WeatherConditionsChanger(Optional.empty()), Duration.ofSeconds(10));
    }

    @Override
    public Receive<EnvironmentCommand> createReceive() {
        return newReceiveBuilder()
                .onMessage(TemperatureChanger.class, this::onChangeTemperature)
                .onMessage(WeatherConditionsChanger.class, this::onChangeWeather)
                .onMessage(SetTemperature.class, this::onSetTemperature) //-- 
                .onMessage(SetWeather.class, this::onSetWeather) //--
                .onSignal(PostStop.class, signal -> onPostStop())
                .build();
    }
    private final Random random = new Random(); // -- 

    private Behavior<EnvironmentCommand> onChangeTemperature(TemperatureChanger t) {
        // TODO: Implement behavior for random changes to temperature //--
        double change = +0.5 + random.nextDouble();

        temperature+= change;
        getContext().getLog().info("Environment received {}", temperature);
        // TODO: Handling of temperature change. Are sensors notified or do they read the temperature? //--
        temperatureSensor.tell(new TemperatureSensor.ReadTemperature(Optional.of(temperature)));
        return this;
    }

    private WeatherType currentWeather = WeatherType.SUNNY;  // Default-Wetterbedingung

    private Behavior<EnvironmentCommand> onChangeWeather(WeatherConditionsChanger command) {
        // TODO: Implement behavior for random changes to weather. Include more than just sunny and not sunny //-- 
        command.weatherType.ifPresentOrElse(
            weather -> {
                currentWeather = weather;
                getContext().getLog().info("Weather changed to: {}", currentWeather);
            },
            () -> {
                WeatherType[] weatherValues = WeatherType.values();
                currentWeather = weatherValues[new Random().nextInt(weatherValues.length)];
                getContext().getLog().info("Randomly changed weather to: {}", currentWeather);
            }
        );
        // TODO: Handling of weather change. Are sensors notified or do they read the weather information? //--
        weatherSensor.tell(new WeatherSensor.ReadWeather(Optional.of(currentWeather)));
        return this;
    }
        


    private Environment onPostStop(){
        getContext().getLog().info("Environment actor stopped");
        return this;
    }
}


