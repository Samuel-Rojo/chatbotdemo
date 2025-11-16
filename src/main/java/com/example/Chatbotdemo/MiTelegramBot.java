//librerias
package com.example.Chatbotdemo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import java.util.*;

@Service
public class MiTelegramBot extends TelegramLongPollingBot {   //mi clase MiTelegramBot heredara TelegramLongPollingBot que viene de su respectiva libreria de telegram
    //inyeccion de dependencia de groqservice y una lista de mapas(diccionarios) donde guardaremos informacion temporal del usuario
    @Autowired
    private GroqService groqService;
    private final Map<String, String> estadoUsuario = new HashMap<>();

    @Value("${telegram.bot.username}") //user
    private String botUsername;

    @Value("${telegram.bot.token}") //token del bot
    private String botToken;
    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }
    @Override
    //sobreescribimos el metodo onUpdateReceived que le dira al bot que hacer cuando reciba un mensaje, este metodo se va a ir ejecutando automaticamente.
    public void onUpdateReceived(Update update) {

        if (!update.hasMessage())
            return;

        // si el usuario manda una imagen/audio/sticker/documento
        if (!update.getMessage().hasText()) {
            String chatId = update.getMessage().getChatId().toString();
            enviar(chatId,
                    "No puedo procesar fotos, audios, videos o documentos 📄📸🎧.\n" +
                            "Solo puedo ayudarte mediante texto usando estos comandos:\n" +
                            "/desayuno\n/almuerzo\n/merienda\n/cena\n\nTambién podés finalizar con /finalizar.");
            return;
        }

        if (!update.hasMessage() || !update.getMessage().hasText())  //el bot solo seguira si recibe un texto inicial.
            return;

        String chatId = update.getMessage().getChatId().toString(); //identificador de usuario para devolver mensajes
        String texto = update.getMessage().getText().trim().toLowerCase(); //trae lo que escribe el usuario, le saca los espacios al inicio y al final y convierte a minusculas las palabras en mayusculas


        // Comando /start → bienvenida
        if (texto.equals("/start")) {
            enviar(chatId,
                    "¡Hola! 👋 Soy tu asistente Nutricional 🥦\n" +
                            "Puedo ayudarte a preparar comidas saludables según tus ingredientes.\n\n" +
                            "👉 *Comandos disponibles:*\n" +
                            "/desayuno\n/almuerzo\n/merienda\n/cena\n\n" +
                            "Si querés terminar la charla:\n/finalizar");
            return;
        }
        //deteccion de saludos para el bot
        String[] saludos = {
                "hola", "holaa", "hola!", "buenas", "buen día", "buen dia",
                "buenas tardes", "buenas noches", "que tal", "qué tal", "hola bot"
        };

        for (String s : saludos) {
            if (texto.startsWith(s)) {
                enviar(chatId,
                        "¡Hola! 😊 ¿Cómo estás?\n" +
                                "Estoy acá para ayudarte a preparar una comida saludable.\n" +
                                "Podés decirme qué querés cocinar usando uno de estos comandos:\n" +
                                "/desayuno\n/almuerzo\n/merienda\n/cena");
                return;
            }
        }
        // Comando para finalizar sesión con el bot
        if (texto.equals("/finalizar") || texto.equals("/finalizar charla") || texto.equals("/finalizar conversacion")) {
            estadoUsuario.remove(chatId);
            enviar(chatId, "Sesión finalizada 👋😊\nSi quieres volver a comenzar escribe /desayuno /almuerzo /merienda /cena.");
            return;
        }

        // Si el usuario envía “gracias” despues de que el bot termina la receta
        if (texto.contains("gracias") || texto.contains("muchas gracias")) {
            enviar(chatId, "¡De nada! 😊 Me alegra ayudarte. Si querés volver a preparar otra comida, usa estos comandos:\n/desayuno\n/almuerzo\n/merienda\n/cena\nSi quieres terminar la charla:\n/finalizar");
            return;
        }

        //  Si el usuario dice “quiero volver”
        if (texto.contains("quiero volver") ||
                texto.contains("volver a empezar") ||
                texto.contains("empezar de nuevo")) {
        //cualquiera de estas 3 opciones que elija el usuario el bot contestara de la siguiente forma:
            enviar(chatId, "¡Perfecto! 🙌\nSolo usa uno de estos comandos para comenzar:\n/desayuno\n/almuerzo\n/merienda\n/cena\nSi quieres terminar la charla:\n/finalizar");
            return;
        }

        //  Comandos nutricionales
        if (texto.equals("/desayuno") || texto.equals("/almuerzo") || texto.equals("/merienda") || texto.equals("/cena")) {
            manejarComandoComida(chatId, texto);
            return;
        }

        // si el usuario está enviando ingredientes
        if (estadoUsuario.containsKey(chatId)) { //"estadoUsuario formara parte de Map<String, String>, ahi guardara mi chatId(que usuario) y mi estado
            procesarIngredientes(chatId, texto); //cualquier texto que envie el usuario despues de elejir una comida lo tomara como ingredientes
            return;
        }

        //  condicional para mensajes NO nutricionales, devuelve un true si el mensaje es nutricional y si no un false(!).
        if (!esMensajeNutricional(texto)) {
            enviar(chatId, "No puedo contestar ese mensaje porque soy tu asistente Nutricional 🥦 y solo puedo ayudarte con comidas.\nUsa estos comandos:\n/desayuno\n/almuerzo\n/merienda\n/cena\n\nSi quieres terminar la charla:\n/finalizar");
            return;
        }
    }
    //recibe el texto que escribio el usuario, si tiene que ver con palabras nutricionales(como las de abajo)
    //devuelve un true, si no, devuelve un false. esto lo usara el bot para contestar que el es un bot nutricional y que solo nos ayudara con la comida.
    private boolean esMensajeNutricional(String texto) {
        String[] claves = {"comer", "cocinar", "ingrediente", "receta", "cenar", "almorzar", "desayunar", "comida"};
        for (String c : claves) {
            if (texto.contains(c)) return true;
        }
        return false;
    }

    private void manejarComandoComida(String chatId, String comando) {  //comando para la comida
        estadoUsuario.put(chatId, comando); //guarda la comida que elige el usuario (/desayuno o /almuerzo o /merienda o /cena)

        String nombre = comando.replace("/", ""); //escribe un mensaje mas comodo para el usuario
       //transicion de enviar la comida y los ingredientes, el metodo "enviar" es el que le mandara un texto al usuario
        enviar(chatId,
                "Perfecto! Vamos a preparar algo para tu *" + nombre + "*.\n" +
                        "Envíame los ingredientes que tenés disponibles.");
    }

    private void procesarIngredientes(String chatId, String ingredientesUsuario) {
        String tipoComida = estadoUsuario.get(chatId); //mi "estadiUsuario" guardara lo que el usuario eligio (/desayuno o /almuerzo o /merienda o /cena)
        //llama a contruirPrompt y este le dira a la ia los ingredientes y la comida que eligio el usuario
        String prompt = construirPrompt(tipoComida, ingredientesUsuario);
        //mandamos la pormpt completo al servicio de groq y este nos devolvera la respuesta con la receta generada
        String respuesta = groqService.chatConGroq(prompt);

        enviar(chatId, respuesta); //mandara el texto generado por la ia a telegram

        estadoUsuario.remove(chatId); //limpia el estado del usuario para evitar atascos del bot
    }
    //prompt que se le enviara al bot a la hora de que el usuario envie le envie un mensaje
    private String construirPrompt(String tipo, String ingredientes) {
        String comida = tipo.replace("/", "");

        return "Eres NutriBot, un asistente nutricional experto.\n" +
                "El usuario quiere cocinar para: *" + comida + "*.\n" +
                "Ingredientes que tiene: " + ingredientes + "\n\n" +
                "Tu tarea es:\n" +
                "1️⃣ Evaluar si los ingredientes son aptos.\n" +
                "2️⃣ Crear una receta saludable con lo que tiene.\n" +
                "3️⃣ Sugerir ingredientes faltantes.\n" +
                "4️⃣ Explicar beneficios nutricionales.\n" +
                "Responde en tono amable y profesional.";
    }

    private void enviar(String chatId, String mensaje) { //funcion enviar para que reciba un texto y lo mande al chat correspondiente
        try {
            execute(new SendMessage(chatId, mensaje)); //manda el mensaje a telegram
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
