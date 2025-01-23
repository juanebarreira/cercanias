package org.cercanias.crawl;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

public class TelegramBot {
    private final String token;
    private long lastUpdateId = 0;

    public TelegramBot(String token) {
        this.token = token;
    }

    public void start() {
        while (true) {
            try {
                checkUpdates();
                Thread.sleep(1000); // Esperar 1 segundo entre checks
                System.out.println("Paso el minuto");
            } catch (Exception e) {
                System.err.println("Error checking updates: " + e.getMessage());
            }
        }
    }

    private void checkUpdates() throws Exception {
        String url = String.format("https://api.telegram.org/bot%s/getUpdates?offset=%d", token, lastUpdateId + 1);
        String response = makeRequest(url);

        JSONObject jsonResponse = new JSONObject(response);
        System.out.println(jsonResponse.toString());
        if (!jsonResponse.getBoolean("ok")) return;

        JSONArray updates = jsonResponse.getJSONArray("result");
        for (int i = 0; i < updates.length(); i++) {
            JSONObject update = updates.getJSONObject(i);
            lastUpdateId = update.getLong("update_id");

            if (!update.has("message")) continue;

            JSONObject message = update.getJSONObject("message");
            if (!message.has("text")) continue;

            String text = message.getString("text");
            Integer chatId = message.getJSONObject("chat").getInt("id");
            handleCommand(text, Integer.toString(chatId));
        }
    }

    private void handleCommand(String text, String chatId) {
        if (text.startsWith("/buscar")) {
            try {
                // Formato: /buscar origen destino [hora_inicio] [hora_fin] [tiempo_max]
                String[] parts = text.split(" ");
                if (parts.length < 3) {
                    sendMessage("Uso: /buscar origen destino [hora_inicio] [hora_fin] [tiempo_max]\n" +
                            "Ejemplo 1: /buscar Aravaca Recoletos\n" +
                            "Ejemplo 2: /buscar Aravaca Recoletos 08:30\n" +
                            "Ejemplo 3: /buscar Aravaca Recoletos 08:30 09:00 35", chatId);
                    return;
                }

                String origin = parts[1];
                String destination = parts[2];
                List<Train> trains;

                try {
                    switch (parts.length) {
                        case 3: // Solo origen y destino
                            trains = Main.searchTrainsInRange(origin, destination);
                            break;
                        case 4: // Con hora inicio
                            trains = Main.searchTrainsInRange(origin, destination, parts[3]);
                            break;
                        case 5: // Con hora inicio y fin
                            trains = Main.searchTrainsInRange(origin, destination, parts[3], parts[4]);
                            break;
                        case 6: // Con todos los parámetros
                            trains = Main.searchTrainsInRange(origin, destination, parts[3], parts[4], parts[5]);
                            break;
                        default:
                            sendMessage("Demasiados parámetros", chatId);
                            return;
                    }

                    StringBuilder message = new StringBuilder("🚂 Trenes disponibles:\n\n");
                    trains.forEach(train -> message.append(train.telegramFormat()));

                    if (trains.isEmpty()) {
                        message.append("No se encontraron trenes para los criterios especificados.");
                    }

                    sendMessage(message.toString(), chatId);
                } catch (IllegalArgumentException e) {
                    sendMessage("Error en el formato de los parámetros: " + e.getMessage(), chatId);
                }
            } catch (Exception e) {
                sendMessage("Error buscando trenes: " + e.getMessage(), chatId);
            }
        } else if (text.equals("/help")) {
            sendMessage("Comandos disponibles:\n" +
                    "/buscar origen destino [hora_inicio] [hora_fin] [tiempo_max]\n" +
                    "Ejemplos:\n" +
                    "1. Próxima hora: /buscar Aravaca Recoletos\n" +
                    "2. Desde hora específica: /buscar Aravaca Recoletos 08:30\n" +
                    "3. Rango de horas: /buscar Aravaca Recoletos 08:30 09:00\n" +
                    "4. Con tiempo máximo: /buscar Aravaca Recoletos 08:30 09:00 35", chatId);
        }
    }

    private void sendMessage(String text, String chatId) {
        try {
            String urlStr = String.format("https://api.telegram.org/bot%s/sendMessage?chat_id=%s&text=%s",
                    token, chatId, java.net.URLEncoder.encode(text, "UTF-8"));
            makeRequest(urlStr);
        } catch (Exception e) {
            System.err.println("Error sending message: " + e.getMessage());
        }
    }

    private String makeRequest(String urlStr) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            return response.toString();
        }
    }

    public static void main(String[] args) {
        String token = System.getenv("BOT_TOKEN");
        TelegramBot bot = new TelegramBot(token);
        bot.start();
    }
} 