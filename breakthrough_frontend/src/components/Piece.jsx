   import { FaChessPawn } from "react-icons/fa";

   const Piece = ({ piece, movable, coordinates, onClickPiece }) => {
     return piece === 0 ? null : 
        <FaChessPawn 
            className={`piece piece-${piece} ${movable ? "movable" : ""}`}
            onClick={e => {
                if(movable) {
                    e.stopPropagation();
                    onClickPiece(coordinates);
                }
            }}
        />;
   };

   export default Piece;