package com.kleblanc.breakthrough_backend.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class Board {
    // Blanc est 1, noir est 2
    // X est la première dimension, Y est la deuxième
    @Getter
    private int[][] board = new int[8][8];
    @Getter
    private GameStatusId currentGameStatus = GameStatusId.NON_INITIALIZED;
    private Map<Integer, String> playerIds = new HashMap<>();
    private ArrayList<ExecutedMove> movesPlayed;
    @Getter
    @Setter
    private int moveTimeout;

    private static final List<GameStatusId> gameInProgressStatuses = List.of(
            GameStatusId.TURN_WHITE,
            GameStatusId.TURN_BLACK);

    public Board() {
        playerIds.put(GameStatusId.TURN_WHITE.getPlayerPawn(), "");
        playerIds.put(GameStatusId.TURN_BLACK.getPlayerPawn(), "");
    }

    public boolean getIsPlayerHuman(int pawnColor) {
        return Objects.equals(playerIds.get(pawnColor), Constants.HUMAN_PLAYER_ID);
    }

    public boolean isGameInProgress() {
        return gameInProgressStatuses.contains(currentGameStatus);
    }

    public boolean isGameOver() {
        return !isGameInProgress();
    };

    public String getPlayerId(int pawnColor) {
        return playerIds.get(pawnColor);
    }
    
    public void setPlayerId(int pawnColor, String playerId) {
        playerIds.replace(pawnColor, playerId);
    }

    public void resetGame() {
        this.board = new int[][]{
                {2, 2, 2, 2, 2, 2, 2, 2}, // x = 0
                {2, 2, 2, 2, 2, 2, 2, 2}, // x = 1
                {0, 0, 0, 0, 0, 0, 0, 0}, // x = 2
                {0, 0, 0, 0, 0, 0, 0, 0}, // x = 3
                {0, 0, 0, 0, 0, 0, 0, 0}, // x = 4
                {0, 0, 0, 0, 0, 0, 0, 0}, // x = 5
                {1, 1, 1, 1, 1, 1, 1, 1}, // x = 6
                {1, 1, 1, 1, 1, 1, 1, 1}  // x = 7
        };

        currentGameStatus = GameStatusId.TURN_WHITE;
        movesPlayed = new ArrayList<>();
    }

    public ArrayList<? extends iMove> getLegalMoves() {
        return getLegalMoves(currentGameStatus);
    }

    private ArrayList<? extends iMove> getLegalMoves(GameStatusId gameStatus) {
        ArrayList<Move> legalMoves = new ArrayList<>();

        if(isGameOver()) {
            return legalMoves;
        }

        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                if(board[x][y] == gameStatus.getPlayerPawn()) {
                    addLegalMoves(legalMoves, x, y, gameStatus);
                }
            }
        }

        return legalMoves;
    }

    private void addLegalMoves(ArrayList<Move> legalMoves, int x, int y, GameStatusId gameStatus) {
        for(int yMoveDirection = -1; yMoveDirection <= 1; yMoveDirection++) {
            Move move = new Move(
                    new Coordinate(x,y),
                    new Coordinate(x + gameStatus.getXMoveDirection(), y + yMoveDirection));

            if(isMoveLegal(move, gameStatus)) legalMoves.add(move);
        }
    }

    public boolean isMoveLegal(iMove move) {
        return isMoveLegal(move, currentGameStatus);
    }

    private boolean isMoveLegal(iMove move, GameStatusId gameStatus) {
        if(isGameOver()) return false;
        if(move.source().isOffBoard() || move.target().isOffBoard()) return false;

        // On peut bouger seulement son propre pion
        if(board[move.source().x()][move.source().y()] != gameStatus.getPlayerPawn())
            return false;

        // On peut seulement avancer d'une case
        if(move.source().x() + gameStatus.getXMoveDirection() != move.target().x())
            return false;

        // On peut bouger latéralement un maximum d'une case (donc en avant + diagonale)
        if(Math.abs(move.source().y() - move.target().y()) > 1)
            return false;

        // On ne peut pas manger un pion en avant de nous
        if(move.source().y() == move.target().y() &&
                board[move.target().x()][move.target().y()] != 0) return false;

        // On ne peut pas manger son propre pion
        if(board[move.target().x()][move.target().y()] == gameStatus.getPlayerPawn())
            return false;

        return true;
    }

    public boolean executeMove(iMove move) {
        if (!isMoveLegal(move)) return false;

        ExecutedMove moveToExecute = new ExecutedMove(
                move.source(),
                move.target(),
                board[move.target().x()][move.target().y()]);

        board[move.target().x()][move.target().y()] = board[move.source().x()][move.source().y()];
        board[move.source().x()][move.source().y()] = 0;

        movesPlayed.add(moveToExecute);

        return true;
    }

    public void undoLastMove() {
        ExecutedMove moveToUndo = movesPlayed.getLast();

        board[moveToUndo.source().x()][moveToUndo.source().y()] =
                board[moveToUndo.target().x()][moveToUndo.target().y()];

        board[moveToUndo.target().x()][moveToUndo.target().y()] = moveToUndo.targetContent();

        movesPlayed.removeLast();
    }

    public GameStatusId endTurn() {
        // Si blanc gagne la partie
        if(Arrays.stream(board[0]).anyMatch(p -> p == GameStatusId.TURN_WHITE.getPlayerPawn()) ||
                Arrays.stream(board).noneMatch(r ->
                        Arrays.stream(r).anyMatch(p -> p == GameStatusId.TURN_BLACK.getPlayerPawn()))) {
            currentGameStatus = GameStatusId.WIN_WHITE;
        }
        // Si noir gagne la partie
        else if(Arrays.stream(board[7]).anyMatch(p -> p == GameStatusId.TURN_BLACK.getPlayerPawn()) ||
                Arrays.stream(board).noneMatch(r ->
                        Arrays.stream(r).anyMatch(p -> p == GameStatusId.TURN_WHITE.getPlayerPawn()))){
            currentGameStatus = GameStatusId.WIN_BLACK;
        }
        else if(currentGameStatus == GameStatusId.TURN_WHITE){
            currentGameStatus = GameStatusId.TURN_BLACK;
        }
        else if(currentGameStatus == GameStatusId.TURN_BLACK){
            currentGameStatus = GameStatusId.TURN_WHITE;
        }

        return currentGameStatus;
    }

    public GameStatusId abortGame() {
        if (currentGameStatus == GameStatusId.TURN_WHITE || currentGameStatus == GameStatusId.TURN_BLACK ) {
            currentGameStatus = GameStatusId.GAME_ABORTED;
        }

        return currentGameStatus;
    }

    public void timeoutActivePlayer() {
        if (currentGameStatus == GameStatusId.TURN_WHITE) {
            currentGameStatus = GameStatusId.TIMEOUT_WHITE;
        } else if (currentGameStatus == GameStatusId.TURN_BLACK) {
            currentGameStatus = GameStatusId.TIMEOUT_BLACK;
        }
    }

    public void setWhitePlayerNotReady() {
        currentGameStatus = GameStatusId.WHITE_NOT_READY;
    }

    public void setBlackPlayerNotReady() {
        currentGameStatus = GameStatusId.BLACK_NOT_READY;
    }

    public List<? extends iMove> getMovesPlayed() {
        return movesPlayed;
    }

    @Override
    public String toString() {
        StringBuilder boardString = new StringBuilder();

        for(int i = 0; i < 8; i++) {
            for(int cell : board[i]) {
                boardString.append(cell).append(" ");
            }
            boardString.append("\n");
        }

        return boardString.toString();
    }

    private record ExecutedMove(Coordinate source, Coordinate target, int targetContent) implements iMove  {}
}
