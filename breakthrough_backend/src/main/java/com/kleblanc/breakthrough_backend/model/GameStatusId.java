package com.kleblanc.breakthrough_backend.model;

public enum GameStatusId {
    NON_INITIALIZED,

    TURN_WHITE  {
        @Override
        public GameStatus makeGameStatus() {
            return new GameStatus(1, "Tour blanc");
        }
        @Override
        public int getPlayerPawn() { return 1; }
        @Override
        public int getXMoveDirection() { return -1; }
    },

    TURN_BLACK {
        @Override
        public GameStatus makeGameStatus() {
            return new GameStatus(2, "Tour noir");
        }
        @Override
        public int getPlayerPawn() { return 2; }
        @Override
        public int getXMoveDirection() { return 1; }
    },

    WIN_WHITE  {
        @Override
        public GameStatus makeGameStatus() {
            return new GameStatus(3, "Blanc a gagné");
        }
    },

    WIN_BLACK {
        @Override
        public GameStatus makeGameStatus() {
            return new GameStatus(4, "Noir a gagné");
        }
    },

    TIMEOUT_WHITE  {
        @Override
        public GameStatus makeGameStatus() {
            return new GameStatus(5, "Blanc a timeout");
        }
    },

    TIMEOUT_BLACK {
        @Override
        public GameStatus makeGameStatus() {
            return new GameStatus(6, "Noir a timeout");
        }
    },

    WHITE_NOT_READY  {
        @Override
        public GameStatus makeGameStatus() {
            return new GameStatus(7, "Blanc n'est plus prêt");
        }
    },

    BLACK_NOT_READY  {
        @Override
        public GameStatus makeGameStatus() {
            return new GameStatus(8, "Noir n'est plus prêt");
        }
    },

    GAME_ABORTED {
        @Override
        public GameStatus makeGameStatus() {
            return new GameStatus(9, "Partie interrompue");
        }
    };

    public GameStatus makeGameStatus() {
        return new GameStatus(0, "Non initialisé");
    }

    public int getXMoveDirection() { return 0; }

    public int getPlayerPawn() { return 0; }
}
