package com.marstracker.ttc;

import com.marstracker.ttc.model.Word;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.core.annotation.HandleAfterCreate;
import org.springframework.data.rest.core.annotation.HandleAfterDelete;
import org.springframework.data.rest.core.annotation.HandleAfterSave;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.hateoas.server.EntityLinks;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import static com.marstracker.ttc.WebSocketConfiguration.MESSAGE_PREFIX;

@Component
@RepositoryEventHandler(Word.class)
public class EventHandler {
    private final SimpMessagingTemplate websocket;

    private final EntityLinks entityLinks;

    @Autowired
    public EventHandler(SimpMessagingTemplate websocket, EntityLinks entityLinks) {
        this.websocket = websocket;
        this.entityLinks = entityLinks;
    }

    @HandleAfterCreate
    public void newWord(Word word) {
        this.websocket.convertAndSend(
                MESSAGE_PREFIX + "/newWord", getPath(word));
    }

    @HandleAfterDelete
    public void deleteWord(Word word) {
        this.websocket.convertAndSend(
                MESSAGE_PREFIX + "/deleteWord", getPath(word));
    }

    @HandleAfterSave
    public void updateWord(Word word) {
        this.websocket.convertAndSend(
                MESSAGE_PREFIX + "/updateWord", getPath(word));
    }

    /**
     * Take an {@link Word} and get the URI using Spring Data REST's {@link EntityLinks}.
     *
     * @param word
     */
    private String getPath(Word word) {
        return this.entityLinks.linkForItemResource(word.getClass(),
                word.getId()).toUri().getPath();
    }

}
