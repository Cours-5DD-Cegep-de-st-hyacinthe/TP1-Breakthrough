package com.kleblanc.breakthrough_backend.controller;

import com.kleblanc.breakthrough_backend.model.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(originPatterns = "http://127.0.0.1:[*]", allowCredentials = "true")
@RequestMapping("/board")
@RestController
public class BreakthroughController {
    private Board board;

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
        } else {
            status = "Illegal move";
        }

        return new ExecutedMoveStatus(moveSuccess, status);
    }

    @GetMapping("/playedmoves")
    public List<? extends iMove> playedMoves() {
        return this.board.getMovesPlayed();
    }
}
