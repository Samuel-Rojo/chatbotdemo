//librerias
package com.example.Chatbotdemo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import java.util.HashMap;
import java.util.Map;

@Service
public class GroqService {

    @Value("${groq.api.key:${GROQ_API_KEY:}}") //mi apikey de groq que esta ubicada en mi application.properties
    private String apiKey;

    private final String baseUrl = "https://api.groq.com/openai/v1/chat/completions";  //url de groq que si recibe las peticiones http

    public String chatConGroq(String mensajeUsuario) { //funcion que recibira un texto que mando el usuario y devuelve un texto generado por groq
        try {
            RestTemplate restTemplate = new RestTemplate(); //creara un cliente http de Spring (para realizar peticiones Post/Gets)
            //prepararemos los headers de la peticion,esto hara que le enviemos un JSON a groq
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);
            //contruimos el cuerpo del JSON que groq necesitara, en este caso el modelo de ia que usara es "llama.3.1-8b-instant"
            Map<String, Object> body = new HashMap<>();
            body.put("model", "llama-3.1-8b-instant");
            //mensaje que se le envia a groq
            Map<String, String> message = new HashMap<>();
            message.put("role", "user"); //el mensaje viene del usuario
            message.put("content", mensajeUsuario);
            body.put("messages", new Map[]{message});
            //combinaremos el cuerpo del JSON con los headers y este lo metera dentro de un objeto HttpEntity listo para enviar.
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(baseUrl, request, Map.class); //hace la peticion POST a la api de groq
            //entra al array choices y agarrara la primera respuesta
            Map<String, Object> choices = (Map<String, Object>) ((java.util.List<?>) response.getBody().get("choices")).get(0);
            //dentro del objeto "choice", entra al campo "message"
            Map<String, Object> messageData = (Map<String, Object>) choices.get("message");
            return (String) messageData.get("content"); //extrae el texto generado por la ia y lo devuelve asi el bot pueda enviarlo por el chat
        //excepcion por si ocurre un error, el error se mostrara en la consola
        } catch (Exception e) {
            e.printStackTrace();
            return "Error al comunicarse con Groq: " + e.getMessage();
        }
    }
}