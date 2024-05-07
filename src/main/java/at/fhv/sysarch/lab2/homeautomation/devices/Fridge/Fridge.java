package at.fhv.sysarch.lab2.homeautomation.devices.Fridge;

import akka.actor.typed.javadsl.AbstractBehavior;
import java.util.HashMap;
import java.util.Map;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.Receive;
import akka.actor.typed.scaladsl.Behaviors;
import akka.actor.typed.javadsl.ActorContext;



public class Fridge extends AbstractBehavior<Fridge.FridgeCommand> {

    public interface FridgeCommand{}


    private final Map <Order, Integer> fridge = new HashMap<>();


    public static final class ConsumeCommand implements FridgeCommand{
        final Order order;

        public ConsumeCommand(Order order){
            this.order = order;
        }
    }

    @Override
    public Receive<FridgeCommand> createReceive(){
        return newReceiveBuilder()
        .onMessage(ConsumeCommand.class, this::onConsume)
        .build();
    }

    public Fridge(ActorContext<FridgeCommand> context){
        super(context);
    }

    public static Behavior<FridgeCommand> create() {
        return Behaviors.setup(Fridge::new);
    }
    

    public Behavior <FridgeCommand> onConsume(ConsumeCommand command){
        var entry = fridge.get(command.order);

        if(entry == null){
            getContext().getLog().info(command.order.name() + " is not stored in the fridge");
        }
        return this;
    }
}
