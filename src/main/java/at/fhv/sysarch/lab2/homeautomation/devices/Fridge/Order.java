package at.fhv.sysarch.lab2.homeautomation.devices.Fridge;

import java.util.Optional;


public record Order(
    String name,
    int weight,
    double price
) 
{

    public static Optional<Order> Name(String name){
        var order = switch(name){
            case "chips" -> new Order(name, 100, 2);
            case "apple" -> new Order(name, 50, 1);
            case "bread" -> new Order(name, 200, 2);
            case "cheese" -> new Order(name, 50, 3);
            default -> null;
        };
        return Optional.ofNullable(order);

    }

}
