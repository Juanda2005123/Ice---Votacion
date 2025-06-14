module VotingSystem {

    // Clase Candidato
    class Candidato {
        int id;
        string nombre;
        string partidoPolitico;
    };

    // Clase Voto (adaptada a tu clase Java)
    class Voto {
        int id;                   
        Candidato candidato;
    };

   // Interface para enviar votos al Broker
    interface BrokerService {
        bool recibirVoto(Voto voto);
        bool ping();
    };

    // Interface para que Broker envíe votos
    interface ReceptorVotos {
        bool recibirVoto(Voto voto);
        bool ping();
    };
};
