import { useState } from "react";
import { Set, Map } from 'immutable';
import api from "../api/axiosConfig";
import Board from './Board';

const Breakthrough = () => {
    const [gameStatus, setGameStatus] = useState({code: 0, description: "Non initialisé"});
    const [legalMoves, setLegalMoves] = useState([]);
    const [highlightedSquare, setHighlightedSquare] = useState(new Set());
    const [board, setBoard] = useState([
        [0,0,0,0,0,0,0,0],
        [0,0,0,0,0,0,0,0],
        [0,0,0,0,0,0,0,0],
        [0,0,0,1,2,0,0,0],
        [0,0,0,2,1,0,0,0],
        [0,0,0,0,0,0,0,0],
        [0,0,0,0,0,0,0,0],
        [0,0,0,0,0,0,0,0],
      ]);

    async function startGame() {
        try {
            await api.put("/board/newgame");

            await getPosition();

            const rGameStatus = await api.get("/board/gamestatus");
            setGameStatus(rGameStatus.data);
        } catch (e) {
            
        }
    }

    async function getPosition() {
        const rBoard = await api.get("/board/");    
        setBoard(rBoard.data);

        const rLegalMove = await api.get("/board/legalmoves");
        setLegalMoves(rLegalMove.data);
    }

    async function makeMove(source, target) {
        const response = await api.patch("/board/makemove", {source: source, target: target});

        if (response.data.moveSuccess === true) {
            setGameStatus(response.data.gameStatus);

            getPosition();
        }
    }

    function onClickPiece(coordinates) {
        const squaresToHighlight = legalMoves.reduce((squares, current) => {
            if (current.source.x === coordinates.x && current.source.y === coordinates.y) {
                squares = squares.add(Map({x: current.target.x, y: current.target.y}));
            }
            return squares;
        }, new Set().add(Map({x: coordinates.x, y: coordinates.y})));

        setHighlightedSquare(squaresToHighlight);
    }

    async function onClickSquare(coordinates) {
        const source = highlightedSquare.first();
        const target = Map({x: coordinates.x, y: coordinates.y});

        if (typeof source !== "undefined" && !source.equals(target)) {
            
            if(highlightedSquare.has(target)) {
                await makeMove({x: source.get("x"), y: source.get("y")}, coordinates);
            }

            setHighlightedSquare(new Set());
        }
    }

    return (
        <div>
            <button onClick={startGame}>
                Démarrer une nouvelle partie
            </button>
            <p>{gameStatus.description}</p>
            <Board 
                board={board} 
                legalMoves={legalMoves} 
                highlightedSquare={highlightedSquare}
                onClickPiece={onClickPiece}
                onClickSquare={onClickSquare}
            />
        </div>
    );
  };
  
  export default Breakthrough;