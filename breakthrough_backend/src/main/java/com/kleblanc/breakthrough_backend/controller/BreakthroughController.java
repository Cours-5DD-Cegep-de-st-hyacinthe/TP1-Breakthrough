package com.kleblanc.breakthrough_backend.controller;

import com.kleblanc.breakthrough_backend.model.Board;
import com.kleblanc.breakthrough_backend.model.Move;
import com.kleblanc.breakthrough_backend.model.Tuple;
import com.kleblanc.breakthrough_backend.model.iMove;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@RequestMapping("/board")
@RestController
public class BreakthroughController {
    private Board board;

    @Autowired
    public BreakthroughController(Board board) {
        this.board = board;
    }

    @PutMapping("/newgame")
    public void newGame() {
        this.board.resetGame();
    }

    @GetMapping("/")
    public int[][] board() {
        return this.board.getBoard();
    }

    @GetMapping("/string")
    public String boardString() {
        return this.board.toString();
    }

    @GetMapping("/legalmoves")
    public List<? extends iMove> legalMoves() {
        return this.board.getLegalMoves();
    }

    @PatchMapping("/makemove")
    public Tuple<Boolean, Object> executeMove(@RequestBody Move move) {
        boolean moveSuccess = board.executeMove(move);

        Object status;
        if (moveSuccess) {
            status = board.endTurn().makeGameStatus();
        } else {
            status = "Illegal move";
        }

        return new Tuple<>(moveSuccess, status);
    }

    @GetMapping("/playedmoves")
    public List<? extends iMove> playedMoves() {
        return this.board.getMovesPlayed();
    }
}
