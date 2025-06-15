module VotingSystem {

    // Secuencia para listas de strings
    sequence<string> ListaString;

    class Candidato {
        string cedula;
        string nombre;
        string apellido;
        string partidoPolitico;
    };

    class Voto {
        string votoId;
        Candidato candidato;
        string timestamp;
    };

    class Votante {
        string cedula;
        string nombre;
        string apellido;
        string departamento;
        string ciudad;
        bool yaVoto;
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

    interface VotingService {
        bool enviarVotoVotante(string mesaId, Voto voto, Votante votante);
        int getTotalVotos();
    };

    interface LugarVotacionService {
        ConsultaLugarResponse consultarLugarVotacion(string cedula);
    };

    interface ObserverCiudadano {
        ConsultaLugarResponse notificarConsulta(string cedula);
    };

    interface DepartamentoService {
        void registrarLugarVotacion(string lugarId, LugarVotacionService* proxy);
        ListaString obtenerLugaresRegistrados();
        string asignarLugarParaConsulta(string cedula);
    };
};
