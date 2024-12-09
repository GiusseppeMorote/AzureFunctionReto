package org.morote.functions;

import java.util.*;

import com.microsoft.azure.functions.annotation.*;
import com.microsoft.azure.functions.*;
import org.morote.functions.model.Persona;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;


/**
 * Azure Functions with HTTP Trigger.
 */
public class HttpTriggerJava {

    // Cadena de conexión a SQL Server
    private static final String CONNECTION_STRING = "jdbc:sqlserver://serverggmc28.database.windows.net:1433;database=bd-morote;user=adminggmc@serverggmc28;password=Morote123;encrypt=true;trustServerCertificate=false;hostNameInCertificate=*.database.windows.net;loginTimeout=30;";

    private JdbcTemplate jdbcTemplate;

    public HttpTriggerJava() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setUrl(CONNECTION_STRING);
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }


    @FunctionName("getAllPersonas")
    public HttpResponseMessage run(
            @HttpTrigger(name = "getAllPersonas", methods = {HttpMethod.GET}
                    , route = "personas"
                    , authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<Persona>> request,
            final ExecutionContext context) {

        context.getLogger().info("Java HTTP trigger processed a request.");

        List<Persona> personas = jdbcTemplate.query("SELECT * FROM [dbo].[persona]", (rs, rowNum) -> new Persona(
                rs.getInt("personaId"),
                rs.getString("nombre"),
                rs.getString("apellidoPaterno"),
                rs.getString("apellidoMaterno"),
                rs.getInt("edad"),
                rs.getString("sexo")
        ));

        if (personas.isEmpty()) {
            return request.createResponseBuilder(HttpStatus.NOT_FOUND)
                    .body("{\"message\": \"No se encontraron personas\"}")
                    .build();
        }

        return request.createResponseBuilder(HttpStatus.OK)
                .header("Content-Type", "application/json")
                .body(personas)
                .build();
    }

    @FunctionName("addPersona")
    public HttpResponseMessage addPersona(
            @HttpTrigger(name = "addPersona", methods = {HttpMethod.POST}, route = "personas", authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<Persona>> request,
            final ExecutionContext context) {

        context.getLogger().info("Java HTTP trigger processed a request to add a persona.");

        Persona persona = request.getBody().orElse(null);

        if (persona == null) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .body("{\"message\": \"Invalid input\"}")
                    .build();
        }

        String sql = "INSERT INTO [dbo].[persona] (nombre, apellidoPaterno, apellidoMaterno, edad, sexo) VALUES (?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, persona.getNombre(), persona.getApellidoPaterno(), persona.getApellidoMaterno(), persona.getEdad(), persona.getSexo());

        return request.createResponseBuilder(HttpStatus.CREATED)
                .header("Content-Type", "application/json")
                .body("{\"message\": \"Persona added successfully\"}")
                .build();
    }
}
