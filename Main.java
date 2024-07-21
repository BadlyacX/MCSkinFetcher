import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class Main {

    private static final String UUID_URL = "https://api.mojang.com/users/profiles/minecraft/%s";
    private static final String SKIN_URL = "https://sessionserver.mojang.com/session/minecraft/profile/%s?unsigned=false";

    public static void main(String[] args) {
        while (true) {
            Scanner scanner = new Scanner(System.in);
            System.out.print("\nEnter the player name:");
            String playerName = scanner.next();
            if (playerName == "stop") break;
            String uuid = getUUID(playerName);
            if (uuid != null) {
                String skinData = fetchSkinData(uuid);
                String signature = fetchSkinSignature(uuid);
                System.out.println("UUID:" + uuid);
                System.out.println("Skin Data: " + skinData);
                System.out.println("Signature: " + signature);
            } else {
                System.out.println("Player not found!");
            }
        }
    }

    public static String getUUID(String playerName) {
        try {
            URL url = new URL(String.format(UUID_URL, playerName));
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("GET");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                JSONObject jsonResponse = new JSONObject(response.toString());
                if (jsonResponse.has("id")) {
                    return jsonResponse.getString("id");
                }
            } else {
                System.out.println("Failed to get UUID: HTTP error code : " + responseCode);
            }

        } catch (Exception e) {
            System.out.println("An error occurred while getting UUID: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public static String fetchSkinData(String uuid) {
        return fetchSkinProperty(uuid, "value");
    }

    public static String fetchSkinSignature(String uuid) {
        return fetchSkinProperty(uuid, "signature");
    }

    private static String fetchSkinProperty(String uuid, String property) {
        String urlString = String.format(SKIN_URL, uuid);

        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                JSONObject jsonObject = new JSONObject(response.toString());
                return jsonObject.getJSONArray("properties").getJSONObject(0).getString(property);
            } else {
                System.out.println("GET request not worked");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
