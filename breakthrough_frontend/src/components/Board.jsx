import './Board.css'
import Square from './Square';
import { Set, Map } from 'immutable';

const Chessboard = ({ board, legalMoves, highlightedSquare, onClickPiece, onClickSquare }) => {
  let movablePieces = Set();

  for (const move of legalMoves) {
    movablePieces = movablePieces.add(Map({x: move.source.x, y: move.source.y}));
  }

  return (
    <div className='layout-board'>
        <div className='spacer'/>
        <div className="chessboard">
            {board.map((row, rowIndex) => (
                <div key={rowIndex} className="board-row">
                    {row.map((piece, colIndex) => (
                        <Square 
                            key={colIndex} 
                            piece={piece} 
                            movable={movablePieces.has(Map({x: rowIndex, y: colIndex}))} 
                            coordinates={{x: rowIndex, y: colIndex}}
                            highlighted={highlightedSquare.has(Map({x: rowIndex, y: colIndex}))}
                            onClickPiece={onClickPiece}
                            onClickSquare={onClickSquare}
                        />
                    ))}
                </div>
            ))}
        </div>
        <div className='spacer'/>
    </div>
  );
};

export default Chessboard;