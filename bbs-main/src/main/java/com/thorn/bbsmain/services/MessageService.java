package com.thorn.bbsmain.services;

import com.thorn.bbsmain.mapper.MessageMapper;
import com.thorn.bbsmain.mapper.entity.Message;
import org.springframework.stereotype.Service;

@Service
public class MessageService {
    private MessageMapper messageMapper;

    public MessageService(MessageMapper messageMapper) {
        this.messageMapper = messageMapper;
    }

    void checkMessage(int id) {
        messageMapper.checkMessage(id);
    }

    void addMessage(Message message) {
        messageMapper.addMessage(message);
    }
}
