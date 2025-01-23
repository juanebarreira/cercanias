package org.cercanias.crawl;

import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import org.telegram.telegrambots.meta.generics.BotSession;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class TelegramNotifier extends TelegramLongPollingBot {
    private final String BOT_TOKEN;
    private final String CHAT_ID;

    public TelegramNotifier(String botToken, String chatId) {
        this.BOT_TOKEN = botToken;
        this.CHAT_ID = chatId;
    }

    public void sendMessage(String message) {
        try {
            // Codificar los parámetros de la URL
            String encodedMessage = URLEncoder.encode(message, StandardCharsets.UTF_8.toString());
            String encodedChatId = URLEncoder.encode(CHAT_ID, StandardCharsets.UTF_8.toString());
            
            String urlString = String.format("https://api.telegram.org/bot%s/sendMessage?chat_id=%s&text=%s",
                BOT_TOKEN, encodedChatId, encodedMessage);

            URL url = new URL(urlString);
            URLConnection conn = url.openConnection();
            
            // Configurar timeouts
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            
            try (InputStream is = new BufferedInputStream(conn.getInputStream());
                 BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
                
                String inputLine;
                StringBuilder response = new StringBuilder();
                while ((inputLine = br.readLine()) != null) {
                    response.append(inputLine);
                }
                System.out.println("Telegram API response: " + response);
            }
            
        } catch (Exception e) {
            System.err.println("Error sending message to Telegram: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public String getBotUsername() {
        return "CercaniasRenfe_bot"; // Asegúrate que este es exactamente el nombre de tu bot
    }

    @Override
    public String getBotToken() {
        return BOT_TOKEN;
    }

    @Override
    public void onUpdateReceived(Update update) {
        // No necesitamos procesar mensajes entrantes
    }
}

