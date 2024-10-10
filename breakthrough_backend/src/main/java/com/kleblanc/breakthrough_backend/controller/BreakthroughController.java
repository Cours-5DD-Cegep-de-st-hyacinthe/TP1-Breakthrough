package com.kleblanc.breakthrough_backend.controller;

import com.kleblanc.breakthrough_backend.model.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@CrossOrigin(originPatterns = "http://127.0.0.1:[*]", allowCredentials = "true")
@RequestMapping("/board")
@RestController
public class BreakthroughController {
    private final Board board;

    private final List<SseEmitter> boardEmitters = new CopyOnWriteArrayList<>();
    private final List<SseEmitter> legalMovesEmitters = new CopyOnWriteArrayList<>();
    private final List<SseEmitter> gameStatusEmitters = new CopyOnWriteArrayList<>();

    @Autowired
    public BreakthroughController(Board board) {
        this.board = board;
    }

    @GetMapping("/")
    public int[][] board(HttpSession session, HttpServletRequest request) {
        return this.board.getBoard();
    }

    @PutMapping("/newgame")
    public void newGame(HttpSession session, HttpServletRequest request) {
        this.board.resetGame();

        dispatchBoard();
        dispatchLegalMoves();
        dispatchGameStatus();
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
        boolean moveSuccess = board.executeMove(move);

        Object status;
        if (moveSuccess) {
            status = board.endTurn().makeGameStatus();

            dispatchBoard();
            dispatchLegalMoves();
            dispatchGameStatus();
        } else {
            status = "Illegal move";
        }

        return new ExecutedMoveStatus(moveSuccess, status);
    }

    @GetMapping("/playedmoves")
    public List<? extends iMove> playedMoves() {
        return this.board.getMovesPlayed();
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

    private void dispatchLegalMoves() {
        dispatchTo(legalMovesEmitters, this.board.getLegalMoves());
    }

    @GetMapping("/subscribe/gamestatus")
    public SseEmitter subscribeGameStatus() {
        return subscribeTo(gameStatusEmitters);
    }

    private void dispatchGameStatus() {
        dispatchTo(gameStatusEmitters, this.board.getCurrentGameStatus().makeGameStatus());
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
}
