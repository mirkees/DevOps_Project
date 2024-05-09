package at.fhv.sysarch.lab2.homeautomation.devices.Fridge;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;

import java.util.concurrent.CompletionStage;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;


public class Fridge extends AbstractBehavior<Fridge.FridgeCommand> {

    public interface FridgeCommand {}

    public static final class ConsumeCommand implements FridgeCommand {
        final Order order;

        public ConsumeCommand(Order order) {
            this.order = order;
        }
    }

    public static final class PlaceOrderCommand implements FridgeCommand {
        final Order order;

        public PlaceOrderCommand(Order order){
            this.order = order;
        }
    }

    public static final class QueryInventoryCommand implements FridgeCommand {}
    public static final class QueryOrderHistoryCommand implements FridgeCommand {}

    private final Map<Order, Integer> fridge = new HashMap<>();
    private final Map<Order, Integer> orderHistory = new HashMap<>();
    private ActorRef<SpaceSensor.SpaceSensorCommand> spaceSensor;
    private ActorRef<WeightSensor.WeightSensorCommand> weightSensor;
    private static final int MAX_WEIGHT = 10000; 
    private final int MAX_SPACE = 10;
    private final String groupId;
    private final String deviceId;


    private Fridge(ActorContext<FridgeCommand> context, String groupId, String deviceId) {
        super(context);
        spaceSensor = context.spawn(SpaceSensor.create(), "spaceSensor");
        weightSensor = context.spawn(WeightSensor.create(), "weightSensor");
        this.deviceId = deviceId;
        this.groupId = groupId;
        getContext().getLog().info("Fridge ready");
    }

    public static Behavior<FridgeCommand> create(String groupId, String deviceId) {
        return Behaviors.setup(context -> new Fridge(context, groupId, deviceId));
    }

    @Override
    public Receive<FridgeCommand> createReceive() {
        return newReceiveBuilder()
            .onMessage(ConsumeCommand.class, this::onConsume)
            .onMessage(PlaceOrderCommand.class, this::onPlaceOrder)
            .onMessage(QueryInventoryCommand.class, this::onQueryInventory)
            .onMessage(QueryOrderHistoryCommand.class, this::onQueryOrderHistory)
            .build();
    }

    private Behavior<FridgeCommand> onPlaceOrder(PlaceOrderCommand command) {
        CompletionStage<Behavior<FridgeCommand>> result = getCurrentWeight().thenCompose(currentWeight -> {
            int availableWeight = MAX_WEIGHT - currentWeight;
            return getCurrentSpace().thenApply(currentSpace -> {
                int availableSpace = MAX_SPACE - currentSpace;
                if (availableSpace >= 1 && availableWeight >= command.order.weight()) {
                    int count = fridge.getOrDefault(command.order, 0);
                    fridge.put(command.order, count + 1);
                    updateSensors(command.order, count + 1);
                    orderHistory.put(command.order, orderHistory.getOrDefault(command.order, 0) + 1);
                    getContext().getLog().info("Order placed for " + command.order.name());
                    return this;
                } else {
                    getContext().getLog().info("Cannot place order for " + command.order.name() + ". Not enough space or weight capacity.");
                    return this;
                }
            });
        });
        return Behaviors.same(); 
    }
    
    private void updateSensors(Order order, int increment){
        spaceSensor.tell(new SpaceSensor.AddSpaceCommand());
        weightSensor.tell(new WeightSensor.AddWeightCommand(order.weight()));
    }



    private CompletionStage<Integer> getCurrentWeight() {
        return AskPattern.ask(
            weightSensor,
            (ActorRef<WeightSensor.CurrentWeightResponse> replyTo) -> new WeightSensor.GetCurrentWeightCommand(replyTo),
            Duration.ofSeconds(3), 
            getContext().getSystem().scheduler()).thenApply(response -> response.currentWeight);
    }


    private CompletionStage<Integer> getCurrentSpace(){
        return AskPattern.ask(
            spaceSensor,
            (ActorRef<SpaceSensor.CurrentSpaceResponse> replyTo) -> new SpaceSensor.GetCurrentSpaceCommand(replyTo),
            Duration.ofSeconds(3),
            getContext().getSystem().scheduler()).thenApply(response -> response.space);
    }


    private Behavior<FridgeCommand> onQueryInventory(QueryInventoryCommand command){
        getContext().getLog().info("Current inventory: " + fridge.toString());
        return this;
    }

    private Behavior<FridgeCommand> onQueryOrderHistory(QueryOrderHistoryCommand command){
        getContext().getLog().info("Order history: " + orderHistory.toString());
        return this;
    }

    

    public Behavior <FridgeCommand> onConsume(ConsumeCommand command){
        Integer count = fridge.get(command.order);
        if(count == null || count == 0){
            getContext().getLog().info(command.order.name() + " is not stored in the fridge");
        }else{
            fridge.put(command.order, count -1);
            updateSensors(command.order, -1);
            getContext().getLog().info("Consumend " + command.order.name());
            if(count -1 <= 0){
                getContext().getLog().info(command.order.name() + "has run out, reordering");
                getContext().getSelf().tell(new PlaceOrderCommand(command.order)); 
            }
        }
        return this;
        
    }

    

    
}
