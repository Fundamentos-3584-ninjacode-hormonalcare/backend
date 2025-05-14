package com.backend.hormonalcare;

import com.backend.hormonalcare.communication.application.internal.commandservices.MessageCommandServiceImpl;
import com.backend.hormonalcare.communication.domain.events.MessageSentEvent;
import com.backend.hormonalcare.communication.domain.model.aggregates.Conversation;
import com.backend.hormonalcare.communication.domain.model.aggregates.Message;
import com.backend.hormonalcare.communication.domain.model.commands.SendMessageCommand;
import com.backend.hormonalcare.communication.infrastructure.persistence.mongodb.repositories.ConversationRepository;
import com.backend.hormonalcare.communication.infrastructure.persistence.mongodb.repositories.MessageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MessageCommandServiceImplTest {

    @Mock
    private MessageRepository messageRepository;
    @Mock private ConversationRepository conversationRepository;
    @Mock private ApplicationEventPublisher publisher;

    private MessageCommandServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new MessageCommandServiceImpl(messageRepository, conversationRepository, null, publisher);
    }

    @Test
    void testSendMessageSuccess() {
        SendMessageCommand cmd = new SendMessageCommand(1L, 10L, "20", "abc123");

        Conversation convo = new Conversation();
        convo.getParticipants().add(1L);
        convo.getParticipants().add(2L);

        convo.updateLastMessage("Hola", new Date());

        when(conversationRepository.findById("abc123")).thenReturn(Optional.of(convo));

        Optional<Message> result = service.handle(cmd);

        assertTrue(result.isPresent());
        verify(messageRepository).save(any(Message.class));
        verify(publisher).publishEvent(any(MessageSentEvent.class));
    }
}
