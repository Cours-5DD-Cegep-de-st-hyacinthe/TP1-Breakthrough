package com.kleblanc.breakthrough_backend.controller;

import com.kleblanc.breakthrough_backend.model.*;
import com.kleblanc.breakthrough_backend.model.message.IAStatusMessage;
import com.kleblanc.breakthrough_backend.service.BreakthroughKafkaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@CrossOrigin(originPatterns = "http://127.0.0.1:[*]", allowCredentials = "true")
@RequestMapping("/board")
@RestController
public class BreakthroughController {
    private final Board board;
    private final Lock boardLock = new ReentrantLock();
    private int timeoutCheckCount;

    private final BreakthroughKafkaService breakthroughKafkaService;

    private final Set<String> readyPlayerIds = new CopyOnWriteArraySet<>();

    private final List<SseEmitter> boardEmitters = new CopyOnWriteArrayList<>();
    private final List<SseEmitter> legalMovesEmitters = new CopyOnWriteArrayList<>();
    private final List<SseEmitter> gameStatusEmitters = new CopyOnWriteArrayList<>();
    private final List<SseEmitter> playersReadyEmitters = new CopyOnWriteArrayList<>();

    @Autowired
    public BreakthroughController(Board board, BreakthroughKafkaService breakthroughKafkaService) {
        this.board = board;
        this.breakthroughKafkaService = breakthroughKafkaService;

        this.readyPlayerIds.add(Constants.HUMAN_PLAYER_ID);
    }

    // REST routes ---------------------------------------------------------------------------------

    @GetMapping("/")
    public int[][] board() {
        return this.board.getBoard();
    }

    @PutMapping("/newgame")
    public void newGame(@RequestBody StartGameRequest startGameRequest) {
        boardLock.lock();
        try {
            this.board.resetGame();
            this.timeoutCheckCount = 0;

            this.breakthroughKafkaService.sendColorAssignationMessage(
                    startGameRequest.whitePlayerId(),
                    startGameRequest.blackPlayerId());

            this.board.setPlayerId(GameStatusId.TURN_WHITE.getPlayerPawn(),
                    startGameRequest.whitePlayerId());
            this.board.setPlayerId(GameStatusId.TURN_BLACK.getPlayerPawn(),
                    startGameRequest.blackPlayerId());
            this.board.setMoveTimeout(startGameRequest.moveTimeout());

            if(board.getPlayerId(GameStatusId.TURN_WHITE.getPlayerPawn()) != Constants.HUMAN_PLAYER_ID) {
                // Wait a few seconds to allow time for IAs to register properly
                ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
                executorService.schedule(this::sendMoveRequestIfIATurn, 3, TimeUnit.SECONDS);
            }

            dispatchGameToClients();
        } finally {
            boardLock.unlock();
        }
    }

    @PatchMapping("/abortgame")
    public void abortGame() {
        boardLock.lock();
        try {
            this.board.abortGame();

            dispatchGameToClients();
        } finally {
            boardLock.unlock();
        }
    }

    @GetMapping("/string")
    public String boardString() {
        return this.board.toString();
    }

    @GetMapping("/legalmoves")
    public List<? extends iMove> legalMoves() {
        return this.board.getLegalMoves();
    }

    @GetMapping("/gamestatus")
    public GameStatus gameStatus() {
        return this.board.getCurrentGameStatus().makeGameStatus();
    }

    @PatchMapping("/makemove")
    public ExecutedMoveStatus executeMove(@RequestBody Move move) {
        boardLock.lock();
        try {
            boolean moveSuccess = board.executeMove(move);

            Object status;
            if (moveSuccess) {
                // Pas de timeout pour un joueur humain
                timeoutCheckCount++;

                status = board.endTurn().makeGameStatus();

                dispatchGameToClients();
                sendMoveRequestIfIATurn();
            } else {
                status = "Illegal move";
            }

            return new ExecutedMoveStatus(moveSuccess, status);
        } finally {
            boardLock.unlock();
        }
    }

    @GetMapping("/playedmoves")
    public List<? extends iMove> playedMoves() {
        return this.board.getMovesPlayed();
    }

    @GetMapping("/readyplayerids")
    public Set<String> readyPlayerIds() {
        return this.readyPlayerIds;
    }

    @GetMapping("/isplayerhuman/{playerColor}")
    public boolean isPlayerHuman(@PathVariable int playerColor) {
        return this.board.getIsPlayerHuman(playerColor);
    }

    @GetMapping("/getplayerid/{playerColor}")
    public String getPlayerId(@PathVariable int playerColor) {
        return this.board.getPlayerId(playerColor);
    }

    @PutMapping("/setplayerid/{playerColor}")
    public void setPlayerId(@PathVariable int playerColor, @RequestBody String playerId) {
        this.board.setPlayerId(playerColor, playerId);
    }

    // PUB/SUB ----------------------------------------------------------------------------------------------

    private void dispatchGameToClients() {
        dispatchBoard();
        dispatchLegalMovesForHumanPlayers();
        dispatchGameStatus();
    }

    @GetMapping("/subscribe/board")
    public SseEmitter subscribeBoard() {
        return subscribeTo(boardEmitters);
    }

    private void dispatchBoard() {
        dispatchTo(boardEmitters, this.board.getBoard());
    }

    @GetMapping("/subscribe/legalmoves")
    public SseEmitter subscribeLegalMoves() {
        return subscribeTo(legalMovesEmitters);
    }

    private void dispatchLegalMovesForHumanPlayers() {
        List<? extends iMove> legalMoves = this.board.getIsPlayerHuman(this.board.getCurrentGameStatus().getPlayerPawn())
                                                    ? this.board.getLegalMoves()
                                                    : List.of();
        dispatchTo(legalMovesEmitters, legalMoves);
    }

    @GetMapping("/subscribe/gamestatus")
    public SseEmitter subscribeGameStatus() {
        return subscribeTo(gameStatusEmitters);
    }

    private void dispatchGameStatus() {
        dispatchTo(gameStatusEmitters, this.board.getCurrentGameStatus().makeGameStatus());
    }

    @GetMapping("/subscribe/readyplayerids")
    public SseEmitter subscribeReadyPlayerIds() {
        return subscribeTo(playersReadyEmitters);
    }

    private void dispatchReadyPlayerIds() {
        dispatchTo(playersReadyEmitters, this.readyPlayerIds);
    }

    private SseEmitter subscribeTo(List<SseEmitter> emitters) {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        emitters.add(emitter);

        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError(e -> emitters.remove(emitter));

        return emitter;
    }

    private void dispatchTo(List<SseEmitter> emitters, Object data) {
        List<SseEmitter> deadEmitters = new ArrayList<>();

        emitters.forEach(emitter -> {
            try {
                emitter.send(SseEmitter.event().name("message").data(data));
            } catch (Exception e) {
                deadEmitters.add(emitter);
            }
        });

        emitters.removeAll(deadEmitters);
    }

    // Kafka consumers --------------------------------------------------------------------------------------

    @KafkaListener(topics = "${spring.kafka.topic.iaStatusTopic}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "iaStatusKafkaListenerContainerFactory")
    public void listenIaStatusTopic(IAStatusMessage iaStatus) {
        if (iaStatus.isReady()) {
            readyPlayerIds.add(iaStatus.id());
        } else {
            readyPlayerIds.remove(iaStatus.id());

            boardLock.lock();
            try {
                if(board.getPlayerId(GameStatusId.TURN_WHITE.getPlayerPawn()) == iaStatus.id()) {
                    board.setWhitePlayerNotReady();
                    dispatchGameToClients();
                } else if (board.getPlayerId(GameStatusId.TURN_BLACK.getPlayerPawn()) == iaStatus.id()) {
                    board.setBlackPlayerNotReady();
                    dispatchGameToClients();
                }
            } finally {
                boardLock.unlock();
            }
        }
        dispatchReadyPlayerIds();
    }

    @KafkaListener(topics = "${spring.kafka.topic.moveResponseWhiteTopic}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "moveResponseKafkaListenerContainerFactory")
    public void listenWhiteMoveResponseTopic(Move move) {
        executeAIMove(move, GameStatusId.TURN_WHITE.getPlayerPawn());
    }

    @KafkaListener(topics = "${spring.kafka.topic.moveResponseBlackTopic}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "moveResponseKafkaListenerContainerFactory")
    public void listenBlackMoveResponseTopic(Move move) {
        executeAIMove(move, GameStatusId.TURN_BLACK.getPlayerPawn());
    }

    // AI moves functions ----------------------------------------------------------------------------------

    private void sendMoveRequestIfIATurn() {
        if(board.getPlayerId(board.getCurrentGameStatus().getPlayerPawn()) == Constants.HUMAN_PLAYER_ID)
            return;

        breakthroughKafkaService.sendMoveRequest(board);

        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.schedule(this::checkIfTimedOut, board.getMoveTimeout(), TimeUnit.MILLISECONDS);
    }

    private void executeAIMove(Move move, int topicColor) {
        boardLock.lock();
        try {
            if (board.getCurrentGameStatus().getPlayerPawn() != topicColor)
                return;

            boolean moveSuccess = board.executeMove(move);

            if (moveSuccess) {
                dispatchGameToClients();
                sendMoveRequestIfIATurn();
            }
        } finally {
            boardLock.unlock();
        }
    }

    private void checkIfTimedOut() {
        boardLock.lock();
        try {
            timeoutCheckCount++;

            if (timeoutCheckCount > board.getMovesPlayed().size()) {
                board.timeoutActivePlayer();
                dispatchGameToClients();
            }
        } finally {
            boardLock.unlock();
        }
    }
}
