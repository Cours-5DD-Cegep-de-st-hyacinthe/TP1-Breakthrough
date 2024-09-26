import Piece from './Piece';

const Square = ({ piece, movable, coordinates, highlighted, onClickPiece, onClickSquare }) => {
    const color = (coordinates.x + coordinates.y) % 2 === 0 ? "black" : "white"
    
    return (
    <div className={`square square-${color} ${highlighted ? "highlighted" : ""}`}
            onClick={() => onClickSquare(coordinates)}>
        <Piece piece={piece} movable={movable} coordinates={coordinates} onClickPiece={onClickPiece} />
    </div>
    );
};

export default Square;