package com.thorn.bbsmain.services;

import com.thorn.bbsmain.mapper.MessageMapper;
import org.springframework.stereotype.Service;

@Service
public class MessageService {
    private MessageMapper messageMapper;

    public MessageService(MessageMapper messageMapper) {
        this.messageMapper = messageMapper;
    }

    public void checkMessage(int id) {
        messageMapper.checkMessage(id);
    }
}
