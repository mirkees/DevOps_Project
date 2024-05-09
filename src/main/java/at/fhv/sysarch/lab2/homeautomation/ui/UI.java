package at.fhv.sysarch.lab2.homeautomation.ui;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import at.fhv.sysarch.lab2.homeautomation.devices.AirCondition;
import at.fhv.sysarch.lab2.homeautomation.devices.MediaStation;
import at.fhv.sysarch.lab2.homeautomation.devices.TemperatureSensor;
import at.fhv.sysarch.lab2.homeautomation.devices.Fridge.Fridge;
import at.fhv.sysarch.lab2.homeautomation.devices.Fridge.Order;


import java.util.Optional;
import java.util.Scanner;

public class UI extends AbstractBehavior<Void> {

    private ActorRef<TemperatureSensor.TemperatureCommand> tempSensor;
    private ActorRef<AirCondition.AirConditionCommand> airCondition;
    private ActorRef<Fridge.FridgeCommand> fridge;
    private ActorRef<MediaStation.MediaStationCommand> mediaStation;

    public static Behavior<Void> create(
        ActorRef<TemperatureSensor.TemperatureCommand> tempSensor,
        ActorRef<AirCondition.AirConditionCommand> airCondition,
        ActorRef<Fridge.FridgeCommand> fridge,
        ActorRef<MediaStation.MediaStationCommand> mediaStation) {
        return Behaviors.setup(context -> new UI(context, tempSensor, airCondition, fridge, mediaStation));
    }

    private UI(ActorContext<Void> context, 
               ActorRef<TemperatureSensor.TemperatureCommand> tempSensor,
               ActorRef<AirCondition.AirConditionCommand> airCondition,
               ActorRef<Fridge.FridgeCommand> fridge,
               ActorRef<MediaStation.MediaStationCommand> mediaStation) {
        super(context);
        this.tempSensor = tempSensor;
        this.airCondition = airCondition;
        this.fridge = fridge;
        this.mediaStation = mediaStation;
        new Thread(this::runCommandLine).start();
        getContext().getLog().info("UI started");
    }

    @Override
    public Receive<Void> createReceive() {
        return newReceiveBuilder().onSignal(PostStop.class, signal -> onPostStop()).build();
    }

    private UI onPostStop() {
        getContext().getLog().info("UI stopped");
        return this;
    }

    public void runCommandLine() {
        // TODO: Create Actor for UI Input-Handling?
        Scanner scanner = new Scanner(System.in);
        String[] input = null;
        String reader = "";


        while (!reader.equalsIgnoreCase("quit") && scanner.hasNextLine()) {
            reader = scanner.nextLine();
            // TODO: change input handling
            String[] command = reader.split(" ");
            if(command[0].equals("t")) {
                this.tempSensor.tell(new TemperatureSensor.ReadTemperature(Optional.of(Double.valueOf(command[1]))));
            }
            if(command[0].equals("a")) {
                this.airCondition.tell(new AirCondition.PowerAirCondition(Optional.of(Boolean.valueOf(command[1]))));
            }
            if(command[0].equals("startMovie")){
                this.mediaStation.tell(new MediaStation.StartMovie());
            }
            if(command[0].equals("stopMovie")){
                this.mediaStation.tell(new MediaStation.StopMovie());
            }

            if (command[0].equals("consume")) {
                if (command.length < 2) {
                    System.out.println("Please specify the product to consume.");
                    continue; 
                }
                String productName = command[1];
                Optional<Order> optionalOrder = Order.Name(productName);
                optionalOrder.ifPresentOrElse(
                    order -> {
                        this.fridge.tell(new Fridge.ConsumeCommand(order));
                        System.out.println("Attempting to consume: " + productName);
                    },
                    () -> {
                        System.out.println("Product not found: " + productName);
                    }
                );
            }

            if(command[0].equals("order")){
                if(command.length <2){
                    System.out.println("Please specify the product to order");
                    continue;
                }
                String productName = command[1];
                Optional<Order> optionalOrder = Order.Name(productName);
                optionalOrder.ifPresentOrElse(
                    order -> {
                        this.fridge.tell(new Fridge.PlaceOrderCommand(order));
                        System.out.println("Order placed for " + productName);
                    }, 
                    () -> {
                        System.out.println("Product not available to order");
                    });
            }

            if(command[0].equals("history")){
                this.fridge.tell(new Fridge.QueryOrderHistoryCommand());
            }

            if(command[0].equals("inventory")){
                this.fridge.tell(new Fridge.QueryInventoryCommand());
            }
            
        }
        getContext().getLog().info("UI done");
    }
}
