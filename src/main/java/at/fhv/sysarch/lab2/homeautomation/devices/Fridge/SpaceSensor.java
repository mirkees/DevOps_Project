package at.fhv.sysarch.lab2.homeautomation.devices.Fridge;

import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.actor.typed.ActorRef;



public class SpaceSensor extends AbstractBehavior<SpaceSensor.SpaceSensorCommand> {

    public interface SpaceSensorCommand{}

    public static final class AddSpaceCommand implements SpaceSensorCommand {}
    public static final class RemoveSpaceCommand implements SpaceSensorCommand {}


    public static final class GetCurrentSpaceCommand implements SpaceSensorCommand{
        final ActorRef<CurrentSpaceResponse> replyTo;

        public GetCurrentSpaceCommand(ActorRef<CurrentSpaceResponse> replyTo){
            this.replyTo = replyTo;
        }


    }


    public static final class CurrentSpaceResponse implements SpaceSensorCommand{
        final int space;

        public CurrentSpaceResponse(int space){
            this.space = space;
        }
    }



    private int space = 0;
    private final int maxSpace = 10;

    public SpaceSensor(ActorContext<SpaceSensorCommand> context){
        super(context);
    }






    public static Behavior<SpaceSensorCommand> create(){
        return Behaviors.setup(SpaceSensor::new);
    }


    @Override
    public Receive<SpaceSensorCommand> createReceive(){
        return newReceiveBuilder()
            .onMessage(AddSpaceCommand.class, this::onAddItem)
            .onMessage(RemoveSpaceCommand.class, this::onRemoveItem)
            .onMessage(GetCurrentSpaceCommand.class, this::onGetCurrentSpace)
            .build();
            
    }


    private Behavior<SpaceSensorCommand> onAddItem (AddSpaceCommand command){
        space += 1;
        getContext().getLog().info("Item added to the fridge, " + (maxSpace - space) + " is left");
        return this;
    }

    private Behavior<SpaceSensorCommand> onRemoveItem(RemoveSpaceCommand command){
        space -= 1;
        getContext().getLog().info("Item was removed from the fridge, " + (maxSpace - space) + " is left");
        return this;
    }

    private Behavior<SpaceSensorCommand> onGetCurrentSpace(GetCurrentSpaceCommand command){
        command.replyTo.tell(new CurrentSpaceResponse(space));
        return this;
    }


}
