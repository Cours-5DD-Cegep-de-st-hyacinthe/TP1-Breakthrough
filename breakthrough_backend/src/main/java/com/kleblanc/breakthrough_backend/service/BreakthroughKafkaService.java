package com.kleblanc.breakthrough_backend.service;

import com.kleblanc.breakthrough_backend.model.Board;
import com.kleblanc.breakthrough_backend.model.Constants;
import com.kleblanc.breakthrough_backend.model.GameStatusId;
import com.kleblanc.breakthrough_backend.model.message.ColorAssignationMessage;
import com.kleblanc.breakthrough_backend.model.message.MoveRequestMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class BreakthroughKafkaService {
    private final KafkaTemplate<String, ColorAssignationMessage> colorAssignationTemplate;
    private final KafkaTemplate<String, MoveRequestMessage> moveRequestMessageTemplate;

    @Value(value = "${spring.kafka.topic.colorAssignationTopic}")
    private String colorAssignationTopic;
    @Value(value = "${spring.kafka.topic.moveRequestWhiteTopic}")
    private String moveRequestWhiteTopic;
    @Value(value = "${spring.kafka.topic.moveRequestBlackTopic}")
    private String moveRequestBlackTopic;

    @Autowired
    public BreakthroughKafkaService(
            KafkaTemplate<String, ColorAssignationMessage> colorAssignationTemplate,
            KafkaTemplate<String, MoveRequestMessage> moveRequestMessageTemplate) {
        this.colorAssignationTemplate = colorAssignationTemplate;
        this.moveRequestMessageTemplate = moveRequestMessageTemplate;
    }

    public void sendColorAssignationMessage (String whiteColorId, String blackColorId) {
        if(whiteColorId != Constants.HUMAN_PLAYER_ID) {
            colorAssignationTemplate.send(colorAssignationTopic, new ColorAssignationMessage(
                    whiteColorId,
                    GameStatusId.TURN_WHITE.getPlayerPawn()
            ));
        }

        if(blackColorId != Constants.HUMAN_PLAYER_ID) {
            colorAssignationTemplate.send(colorAssignationTopic, new ColorAssignationMessage(
                    blackColorId,
                    GameStatusId.TURN_BLACK.getPlayerPawn()
            ));
        }
    }

    public void sendMoveRequest (Board board) {
        String moveRequestTopic;

        if (board.getPlayerId(board.getCurrentGameStatus().getPlayerPawn()) == Constants.HUMAN_PLAYER_ID) {
            throw new IllegalArgumentException("This is a human player turn");
        }

        if (board.getCurrentGameStatus().getPlayerPawn() == GameStatusId.TURN_WHITE.getPlayerPawn()) {
            moveRequestTopic = moveRequestWhiteTopic;
        } else if (board.getCurrentGameStatus().getPlayerPawn() == GameStatusId.TURN_BLACK.getPlayerPawn()) {
            moveRequestTopic = moveRequestBlackTopic;
        } else {
            throw new IllegalArgumentException("Game is not in progress");
        }

        moveRequestMessageTemplate.send(moveRequestTopic, new MoveRequestMessage(
                board.getMoveTimeout(),
                board.getBoard(),
                board.getLegalMoves()
        ));
    }
}
