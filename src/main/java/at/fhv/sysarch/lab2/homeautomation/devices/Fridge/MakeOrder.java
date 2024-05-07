package at.fhv.sysarch.lab2.homeautomation.devices.Fridge;

import akka.actor.typed.javadsl.AbstractBehavior;
import java.util.OptionalInt;


public class MakeOrder extends AbstractBehavior<MakeOrder.MakeOrderCommand>{

    public interface MakeOrderCommand{}



    public static final class ReceiveSpace implements MakeOrderCommand{

        final int space; 

        public ReceiveSpace(int space){
            this.space = space;
        }
    }

    public static Behavior<MakeOrderCommand> create()

    private OptionalInt availabeSpace = OptionalInt.empty();

    private Behavior<MakeOrder> onSpace(MakeOrderCommand command){
        this.availabeSpace = OptionalInt.of(command.space)

    }

}
