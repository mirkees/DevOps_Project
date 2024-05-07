package at.fhv.sysarch.lab2.homeautomation.devices.Fridge;

import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class WeightSensor extends AbstractBehavior<WeightSensor.WeightSensorCommand>{

    public interface WeightSensorCommand{}

    public static final class AddWeightCommand implements WeightSensorCommand{
        private int weight;

        public AddWeightCommand(int weight){
            this.weight = weight;
        }


    }
    public static final class RemoveWeightCommand implements WeightSensorCommand{
        private int weight;

        public RemoveWeightCommand(int weight){
            this.weight = weight;
        }


    }

    private int weight = 0;
    private int maxWeight = 10000;


    public WeightSensor(ActorContext<WeightSensorCommand> context){
        super(context);
    }
        
    @Override
    public Receive<WeightSensorCommand> createReceive(){
        return newReceiveBuilder()
        .onMessage(RemoveWeightCommand.class, this::onRemoveItem)
        .onMessage(AddWeightCommand.class, this::onAddItem)
        .build();
    }

    public static Behavior<WeightSensorCommand> create(){
        return Behaviors.setup(WeightSensor::new);
    }

    private Behavior<WeightSensorCommand> onAddItem (AddWeightCommand command){
        weight += command.weight;
        getContext().getLog().info(command.weight + " was added to the fridge");
        return this;
    }

    private Behavior<WeightSensorCommand> onRemoveItem(RemoveWeightCommand command){
        weight -= command.weight;
        getContext().getLog().info(command.weight + " was removed from the fridge");
        return this;
    }   





}
