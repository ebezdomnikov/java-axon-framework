package io.axoniq.labs.chat.commandmodel;

import io.axoniq.labs.chat.coreapi.*;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.spring.stereotype.Aggregate;

import java.util.HashSet;
import java.util.Set;

import static org.axonframework.modelling.command.AggregateLifecycle.apply;

@Aggregate
public class ChatRoom {

    @AggregateIdentifier
    private String roomId;

    private Set<String> participants = new HashSet<>();

    @CommandHandler
    public ChatRoom(CreateRoomCommand cmd) {
        apply(new RoomCreatedEvent(cmd.getRoomId(), cmd.getName()));
    }

    @CommandHandler
    public void handle(JoinRoomCommand cmd) {
        if (!participants.contains(cmd.getParticipant())) {
            apply(new ParticipantJoinedRoomEvent(cmd.getParticipant(), cmd.getRoomId()));
        }
    }

    @CommandHandler
    public void handle(LeaveRoomCommand cmd) {
        if (participants.contains(cmd.getParticipant())) {
            apply(new ParticipantLeftRoomEvent(cmd.getParticipant(), cmd.getRoomId()));
        }
    }

    @CommandHandler
    public void handle(PostMessageCommand cmd) {
        if (!participants.contains(cmd.getParticipant())) {
            throw new IllegalStateException("Please join the room before posting the message");
        }
        apply(new MessagePostedEvent(cmd.getParticipant(), cmd.getRoomId(), cmd.getMessage()));
    }

    public ChatRoom() {
    }

    @EventSourcingHandler
    public void on(ParticipantJoinedRoomEvent event) {
        participants.add(event.getParticipant());
    }

    @EventSourcingHandler
    public void on(ParticipantLeftRoomEvent event) {
        participants.remove(event.getParticipant());
    }

    @EventSourcingHandler
    public void on(RoomCreatedEvent event) {
        roomId = event.getRoomId();
    }
}
