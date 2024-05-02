package at.fhv.sysarch.lab2.homeautomation.devices;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.actor.typed.ActorRef;


public class MediaStation extends AbstractBehavior<MediaStation.MediaStationCommand> {

    public interface MediaStationCommand{}

    public static final class StartMovie implements MediaStationCommand{}
    public static final class StopMovie implements MediaStationCommand{}




    private final String groupId;
    private final String deviceId;
    private ActorRef<Blinds.BlindsCommand> blinds;
    private Boolean isMoviePlaying = false;


    private MediaStation(ActorContext<MediaStationCommand> context, String groupId, String deviceId, ActorRef<Blinds.BlindsCommand> blinds) {
        super(context);
        this.groupId = groupId;
        this.deviceId = deviceId;
        this.blinds = blinds;
        getContext().getLog().info("Media Station ready");
    }

    public static Behavior<MediaStationCommand> create(String groupId, String deviceId, ActorRef<Blinds.BlindsCommand> blinds) {
        return Behaviors.setup(context -> new MediaStation(context, groupId, deviceId, blinds));
    }

    @Override
    public Receive<MediaStationCommand> createReceive(){
        return newReceiveBuilder()
        .onMessage(StartMovie.class,this::onStartMovie)
        .onMessage(StopMovie.class, this::onStopMovie)
        .build();
    } 





    private Behavior<MediaStationCommand> onStartMovie(StartMovie command){
        if(!isMoviePlaying){
            getContext().getLog().info("Movie started, lowering blinds");
            isMoviePlaying = true;
            blinds.tell(new Blinds.AdjustBlindsMovie(isMoviePlaying));

        }else{
            getContext().getLog().info("Movie is already playing, permission denied");
        }
        return this;
    }

    private Behavior<MediaStationCommand> onStopMovie(StopMovie command){
        if(isMoviePlaying){
            getContext().getLog().info("Movie stopped, raising blinds");
            isMoviePlaying = false;
            blinds.tell(new Blinds.AdjustBlindsMovie(isMoviePlaying));
        }
        return this;
    }



}
