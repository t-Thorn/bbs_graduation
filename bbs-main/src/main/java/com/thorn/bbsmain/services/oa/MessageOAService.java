package com.thorn.bbsmain.services.oa;

import com.thorn.bbsmain.mapper.MessageMapper;
import com.thorn.bbsmain.mapper.entity.Message;
import com.thorn.bbsmain.utils.MsgBuilder;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MessageOAService {

    private MessageMapper messageMapper;

    private RabbitTemplate rabbitTemplate;

    public MessageOAService(MessageMapper messageMapper, RabbitTemplate rabbitTemplate) {
        this.messageMapper = messageMapper;
        this.rabbitTemplate = rabbitTemplate;
    }

    public List getList(int page, int limit, MsgBuilder builder) {
        builder.addData("count", messageMapper.getMessageNum());
        return messageMapper.getMessages((page - 1) * limit, limit);
    }


    public Object getDetail(Object id) {

        return messageMapper.getMessageDetail((int) id);
    }


    public boolean update(Object object) {
        return false;
    }


    public boolean delete(Object id) {
        return false;
    }

    public MsgBuilder addData() {
        MsgBuilder builder = new MsgBuilder();
        builder.addData("code", 0);
        builder.addData("msg", "");

        return builder;
    }

    public void broadcast(String msg) {
        Message message = new Message();
        message.setType(2);
        message.setOwner(-1);
        message.setFromUser(-1);
        message.setContent(msg);
        messageMapper.addMessage(message);
        rabbitTemplate.convertAndSend("broadcast", msg);
    }
}
