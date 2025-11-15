//librerias
package com.example.Chatbotdemo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.generics.LongPollingBot;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@SpringBootApplication
public class ChatbotdemoApplication {

	public static void main(String[] args) {  // mi punto de entrada de la aplicacion
		SpringApplication.run(ChatbotdemoApplication.class, args); //sprig mantendra la aplicacion corriendo
	}

	@Bean //le dira a mi spring que cree el objeto TelegramBotsApi y que lo guarde en el sistema
	public TelegramBotsApi telegramBotsApi(LongPollingBot bot) { //spring inyecta mi bot en TelegramBotsApi
		try {
			TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class); //creara un objeto principal que manejara al bot
			telegramBotsApi.registerBot(bot); //registra mi bot en telegram
			System.out.println("Bot registrado correctamente.");
			return telegramBotsApi; //devuelve el objeto creado que queda guardado en el bean
		} catch (Exception e) {
			// Evita que la aplicación se caiga si no se puede borrar el webhook
			System.err.println("Aviso: No se pudo eliminar el webhook anterior. Detalle: "
					+ e.getMessage());
			return null;
		}
	}
}
