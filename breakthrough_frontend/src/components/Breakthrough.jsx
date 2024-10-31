import { useState, useEffect } from "react";
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
      const [readyPlayerIds, setReadyPlayerIds] = useState([]);
      const [selectedWhitePlayerId, setSelectedWhitePlayerId] = useState("");
      const [selectedBlackPlayerId, setSelectedBlackPlayerId] = useState("");
      const [moveTimeout, setMoveTimeout] = useState(3000);

      useEffect(() => {
          // Aller chercher la liste des joueurs prêts
          const fetchReadyPlayerIds = async () => {
            const rPlayersReady = await api.get("/board/readyplayerids");
            setReadyPlayerIds(rPlayersReady.data);
            setSelectedWhitePlayerId(rPlayersReady.data[0]);
            setSelectedBlackPlayerId(rPlayersReady.data[0]);
          }

          fetchReadyPlayerIds();

          // Subscribe aux évènements
          const baseSubscribeUrl = "http://127.0.0.1:8080/board/subscribe"
          
          const boardEventSource = new EventSource(baseSubscribeUrl + "/board");
          const legalMovesEventSource = new EventSource(baseSubscribeUrl + "/legalmoves");
          const gameStatusEventSource = new EventSource(baseSubscribeUrl + "/gamestatus");
          const playersReadyEventSource = new EventSource(baseSubscribeUrl + "/readyplayerids");
  
          boardEventSource.onmessage = (event) => {
              const board = JSON.parse(event.data);
              setBoard(board);
          }
  
          legalMovesEventSource.onmessage = (event) => {
              const legalMoves = JSON.parse(event.data);
              setLegalMoves(legalMoves);
          }
  
          gameStatusEventSource.onmessage = (event) => {
              const gameStatus = JSON.parse(event.data);
              setGameStatus(gameStatus);
          }

          playersReadyEventSource.onmessage = (event) => {
            const playersReady = JSON.parse(event.data);
            setReadyPlayerIds(playersReady);
            setSelectedWhitePlayerId(playersReady[0]);
            setSelectedBlackPlayerId(playersReady[0]);
          }
  
           // terminating the connection on component unmount
          return () => { 
            boardEventSource.close();
            legalMovesEventSource.close();
            gameStatusEventSource.close();
            playersReadyEventSource.close();
          };
      }, []);

    async function startGame() {
        try {
            await api.put("/board/newgame", { 
                whitePlayerId: selectedWhitePlayerId, 
                blackPlayerId: selectedBlackPlayerId,
                moveTimeout: moveTimeout
            });
        } catch (e) {
            
        }
    }

    async function abortGame() {
        try {
            await api.patch("/board/abortgame");
        } catch (e) {
            
        }
    }

    async function makeMove(source, target) {
        await api.patch("/board/makemove", {source: source, target: target});
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
            <label>
                Choisir le joueur blanc: 
                <select name="whitePlayer" value={selectedWhitePlayerId} onChange={e => setSelectedWhitePlayerId(e.target.value)}>
                    {readyPlayerIds.map((player) => (
                        <option key={player} value={player}>{player}</option>
                    ))}
                </select>
            </label>
            <label>
                Choisir le joueur noir: 
                <select name="blackPlayer" value={selectedBlackPlayerId} onChange={e => setSelectedBlackPlayerId(e.target.value)}>
                    {readyPlayerIds.map((player) => (
                        <option key={player} value={player}>{player}</option>
                    ))}
                </select>
            </label>
            <input type="number" value={moveTimeout} onChange={e => setMoveTimeout(e.target.value)} />
            <button onClick={startGame}>
                Démarrer une nouvelle partie
            </button>
            <button onClick={abortGame}>
                Annuler la partie
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