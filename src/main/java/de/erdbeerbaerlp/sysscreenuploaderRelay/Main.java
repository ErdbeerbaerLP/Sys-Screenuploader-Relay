package de.erdbeerbaerlp.sysscreenuploaderRelay;


import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.WebhookClientBuilder;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.javalin.Javalin;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.InternalServerErrorResponse;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Scanner;
import java.util.logging.Logger;


public class Main {
    private static final Gson gson = new GsonBuilder().create();
    private static final SimpleDateFormat nintendoDateFormat = new SimpleDateFormat("yyyyMMDDHHmmss");

    private static HashMap<String, String> gameIDs = new HashMap<>();
    private static String host = "0.0.0.0";
    private static int port = 9000;

    public static void main(String[] args) {

        // Parsing command line arguments
        if (args.length > 0) {
            for (int i = 0; i < args.length; i++) {
                if (args[i].equals("--host") && i + 1 < args.length) {
                    host = args[i + 1];
                } else if (args[i].equals("--port") && i + 1 < args.length) {
                    port = Integer.parseInt(args[i + 1]);
                }
            }
        }


        Javalin app = Javalin.create().start(host, port);

        final Logger logger = Logger.getLogger("Test");

        try {
            final URL titleIDUrl = new URL("https://raw.githubusercontent.com/RenanGreca/Switch-Screenshots/master/game_ids.json");
            final URLConnection conn = titleIDUrl.openConnection();
            conn.connect();
            JsonObject e = gson.fromJson(new InputStreamReader(conn.getInputStream()),JsonElement.class).getAsJsonObject();
            logger.info(e.toString());
            e.keySet().forEach((n)->{
                if(gameIDs.containsKey(n)){
                    logger.info("Duplicate ID "+n+e.get(n).getAsString());
                }else{
                    gameIDs.put(n,e.get(n).getAsString());
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }


        app.get("/", ctx -> {
            ctx.result(getHTML("index.html"))
                    .contentType("text/html");
        });

        app.before((ctx) -> {
            logger.info(ctx.path() + " - " + ctx.ip());
            logger.info("Query Param");
            ctx.queryParamMap().forEach((a, b) -> {
                logger.info(a + " == " + b);
            });
            logger.info("Path Param");
            ctx.pathParamMap().forEach((a, b) -> {
                logger.info(a + " == " + b);
            });
        }).post("/discord/{webhookURL}", ctx -> {
            String filename = ctx.queryParam("filename");
            if (filename == null || filename.isEmpty()) {
                logger.info("ERROR no Filename");
                throw new BadRequestResponse("filename param is missing");
            }
            filename = new File(filename).getName();
            String caption = ctx.queryParam("caption");
            if (caption == null || caption.isEmpty()) {
                logger.info("no caption");
                caption = null;
            }

            String timeFormat = ctx.queryParam("timeFormat");
            if (timeFormat == null || timeFormat.isEmpty()) {
                timeFormat = "yyyy-MM-dd HH:mm:ss";
            } else
                timeFormat = URLDecoder.decode(timeFormat, StandardCharsets.UTF_8);
            final SimpleDateFormat outFormat = new SimpleDateFormat(timeFormat);

            if (caption != null) {
                final String[] splitFilename = filename.split("-");
                String timeOut;
                try {
                    final String tmpDate = splitFilename[0];
                    final Date d = nintendoDateFormat.parse(tmpDate.substring(0, tmpDate.length() - 2));
                    timeOut = outFormat.format(d);
                }catch (Exception e){
                    timeOut = "Unknown Date "+e.getMessage();
                }
                final String titleID = (splitFilename.length < 2)?"null":splitFilename[1].split("\\.")[0];
                final String titleName = gameIDs.getOrDefault(titleID, "Unknown Title ("+titleID+")");

                caption = caption.replace("{title}", titleName);
                caption = caption.replace("{titleid}", titleID);
                caption = caption.replace("{time}", timeOut);

            }
            // Retrieving webhook URL from web parameters
            String encodedWebhookURL = ctx.pathParam("webhookURL");
            if (encodedWebhookURL.isEmpty()) {
                logger.info("No Webhook URL");
                throw new BadRequestResponse("webhookURL param is missing");
            }
            System.out.println(encodedWebhookURL);
            String decodedWebhookURL = URLDecoder.decode(encodedWebhookURL, StandardCharsets.UTF_8);

            // Building WebhookClient with the decoded URL
            try (final WebhookClient cli = new WebhookClientBuilder(decodedWebhookURL).build()) {
                logger.info("Sending...");
                final WebhookMessageBuilder wbm = new WebhookMessageBuilder();
                if (caption != null) wbm.setContent(caption);
                wbm.addFile(filename, ctx.req().getInputStream());
                cli.send(wbm.build());
            } catch (IOException e) {
                e.printStackTrace();
                throw new InternalServerErrorResponse();
            }
            ctx.result("OK");
            logger.info("OK");
        });
    }

    private static String getHTML(String htmlFile){
        InputStream inputStream = Main.class.getClassLoader().getResourceAsStream(htmlFile);

        if (inputStream != null) {
            // Use a Scanner to read the content of the InputStream
            final Scanner scanner = new Scanner(inputStream);
            final StringBuilder content = new StringBuilder();

            // Read line by line and append to the content StringBuilder
            while (scanner.hasNextLine()) {
                content.append(scanner.nextLine()).append("\n");
            }

            // Close the scanner and print the content
            scanner.close();
            return content.toString();
        }
        return "File not found";
    }
}
