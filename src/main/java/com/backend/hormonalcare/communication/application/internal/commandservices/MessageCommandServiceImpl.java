package com.backend.hormonalcare.communication.application.internal.commandservices;

import java.util.Date;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.backend.hormonalcare.communication.application.internal.outboundservices.acl.CommunicationExternalProfileService;
import com.backend.hormonalcare.communication.domain.events.MessageSentEvent;
import com.backend.hormonalcare.communication.domain.model.aggregates.Conversation;
import com.backend.hormonalcare.communication.domain.model.aggregates.Message;
import com.backend.hormonalcare.communication.domain.model.commands.DeleteMessageCommand;
import com.backend.hormonalcare.communication.domain.model.commands.SendMessageCommand;
import com.backend.hormonalcare.communication.domain.model.commands.UpdateMessageStatusCommand;
import com.backend.hormonalcare.communication.domain.services.MessageCommandService;
import com.backend.hormonalcare.communication.infrastructure.persistence.mongodb.repositories.ConversationRepository;
import com.backend.hormonalcare.communication.infrastructure.persistence.mongodb.repositories.MessageRepository;
import com.backend.hormonalcare.shared.infrastructure.events.AsyncEventPublisher;

@Service
public class MessageCommandServiceImpl implements MessageCommandService {

    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    private final AsyncEventPublisher asyncEventPublisher;

    public MessageCommandServiceImpl(
            MessageRepository messageRepository,
            ConversationRepository conversationRepository,
            CommunicationExternalProfileService externalProfileService,
            AsyncEventPublisher asyncEventPublisher) {
        this.messageRepository = messageRepository;
        this.conversationRepository = conversationRepository;
        this.asyncEventPublisher = asyncEventPublisher;
    }

    @Override
    public Optional<Message> handle(SendMessageCommand command) {
        String conversationId = command.conversationId();

        // Check if conversation exists
        Optional<Conversation> conversationOptional;
        if (conversationId == null || conversationId.isEmpty()) {
            conversationOptional = conversationRepository.findByParticipants(
                    command.senderId(),
                    command.recipientId());

            if (conversationOptional.isEmpty()) {
                throw new IllegalArgumentException("Conversation not found");
            }
            conversationId = conversationOptional.get().getId();
        } else {
            conversationOptional = conversationRepository.findById(conversationId);
            if (conversationOptional.isEmpty()) {
                throw new IllegalArgumentException("Conversation not found");
            }
            // Verify sender belongs to the conversation
            if (!conversationOptional.get().hasParticipant(command.senderId())) {
                throw new IllegalArgumentException("Sender is not a participant in this conversation");
            }
        }

        // Create and save message
        var message = new Message(command, conversationId);
        messageRepository.save(message);

        // Update the conversation with last message info
        Conversation conversation = conversationOptional.get();
        conversation.updateLastMessage(command.content(), new Date());
        conversationRepository.save(conversation);

        // Publish event asíncronamente
        asyncEventPublisher.publishEvent(new MessageSentEvent(
                message.getId(),
                message.getSenderId(),
                message.getRecipientId(),
                message.getConversationId()));

        return Optional.of(message);
    }

    @Override
    public Optional<Message> handle(UpdateMessageStatusCommand command) {
        Optional<Message> messageOptional = messageRepository.findById(command.messageId());
        if (messageOptional.isEmpty()) {
            return Optional.empty();
        }

        Message message = messageOptional.get();

        switch (command.status()) {
            case DELIVERED -> message.markAsDelivered();
            case READ -> message.markAsRead();
            default -> {
                /* No action needed */ }
        }

        messageRepository.save(message);
        return Optional.of(message);
    }

    @Override
    public void handle(DeleteMessageCommand command) {
        Optional<Message> messageOptional = messageRepository.findById(command.messageId());
        if (messageOptional.isEmpty()) {
            return;
        }

        Message message = messageOptional.get();
        message.markAsDeleted();
        messageRepository.save(message);
    }
}