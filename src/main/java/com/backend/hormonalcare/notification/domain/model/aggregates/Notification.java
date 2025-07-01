package com.backend.hormonalcare.notification.domain.model.aggregates;

import com.backend.hormonalcare.notification.domain.model.commands.CreateNotificationCommand;
import com.backend.hormonalcare.notification.domain.model.commands.UpdateNotificationStateCommand;
import com.backend.hormonalcare.notification.domain.model.valueobjects.State;
import com.backend.hormonalcare.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import jakarta.persistence.*;
import lombok.Getter;

@Getter
@Entity
public class Notification extends AuditableAbstractAggregateRoot<Notification> {

    @Column(name = "title")
    private String title;

    @Column(name = "message")
    private String message;

    @Enumerated(EnumType.STRING)
    private State state;

    @Getter
    @Column(name = "recipient_id")
    private Long recipientId;

    public Notification() {
        this.title = "";
        this.message = "";
        this.state = State.UNREAD;
        this.recipientId = null;
    }

    public Notification(CreateNotificationCommand command) {
        this.title = command.title();
        this.message = command.message();
        this.state = command.state();
        this.recipientId = command.recipientId();
    }

    public Notification(UpdateNotificationStateCommand command) {
        this.state = command.state();
    }

    public void updateState(State state) {
        this.state = state;
    }

}