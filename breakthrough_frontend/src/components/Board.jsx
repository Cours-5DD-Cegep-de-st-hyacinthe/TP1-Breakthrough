import { useState } from "react";
import api from "../api/axiosConfig";

const Board = () => {
    const [serverState, setServerState] = useState("Le serveur n'est pas connecté");

    async function tryConnect(event) {
        try{
            const response = await api.get("/", {
                signal: AbortSignal.timeout(5000)
            });
    
            setServerState(response.data);
        } catch {
            setServerState("Serveur a timeout");
        }
        
    }

    return(
        <div>
            <h1>{serverState}</h1>
            <button onClick={tryConnect}>
                Essayer de se connecter
            </button>
        </div>
    )
};

export default Board;