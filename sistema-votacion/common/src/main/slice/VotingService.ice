module VotingSystem {

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


    class ConsultaLugarResponse {
        string departamento;
        string ciudad;
        string lugarNombre;
        string direccion;
        string mesaId;
        bool encontrado;
        string mensaje;
    };
 
    
    // Interface para enviar votos al Broker
    interface BrokerService {
        bool recibirVoto(Voto voto);
        int recibirValidacionVotante(string documento, int candidatoId);
        string query(string document);
        bool ping();
    };

    // Interface para que Broker envíe votos  
    interface ReceptorVotos {
        bool recibirVoto(Voto voto);
        int recibirValidacionVotante(string documento, int candidatoId);
        bool ping();
    };

    interface QueryStation {
        string query(string document);
    }

};
