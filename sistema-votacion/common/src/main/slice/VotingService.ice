module VotingSystem {
    
    // Definir las clases que se van a transmitir
    class Candidato {
        string cedula;
        string nombre;
        string apellido;
        string partidoPolitico;
    };
    
    class Voto {
        Candidato candidato;
        string timestamp;
        // Agregar otros campos que necesites
    };
    
    class Votante {
        string cedula;
        string nombre;
        string apellido;
        string departamento;
        string ciudad;
        bool yaVoto;
        // Agregar otros campos que necesites
    };
    
    // Interfaz del servicio
    interface VotingService {
        // Enviar voto completo (voto + votante)
        bool enviarVotoVotante(string mesaId, Voto voto, Votante votante);
        
        // Método para testing - obtener total de votos
        int getTotalVotos();
    };
};