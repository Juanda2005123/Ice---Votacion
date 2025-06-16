module VotingSystem {

    dictionary<int, int> MapConteoVotos;

    class DeltaConteo {
        string nodoId;
        long timestamp;
        int totalVotos;
        MapConteoVotos conteo; // candidatoId → cantidad
    };

    // Clase Candidato
    class Candidato {
        int id;
        string nombre;
        string partidoPolitico;
    };

    // Clase Ciudadano (adaptada a tu clase Java)
    class Ciudadano {
        int id;
        string documento;
        string nombre;
        string apellido;
        string mesaId;
        bool yaVoto;
    };

    // Clase Voto (adaptada a tu clase Java)
    class Voto {
        int id;                   
        Candidato candidato;
    };    
    
    // Interface para enviar votos al Broker
    interface BrokerService {
        bool recibirDeltaConteo(DeltaConteo delta);
        int recibirValidacionVotante(string documento, int candidatoId);
        bool ping();
    };

    // Interface para que Broker envíe votos  
    interface ReceptorVotos {
        bool recibirDeltaConteo(DeltaConteo delta);
        int recibirValidacionVotante(string documento, int candidatoId);
        bool ping();
    };
    
};