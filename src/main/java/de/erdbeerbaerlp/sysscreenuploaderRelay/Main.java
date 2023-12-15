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
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
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

        String htmlContent = "<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "    <title>Sys-Screenuploader to Discord</title>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <h2>Sys-Screenuploader to Discord</h2>\n" +
                "    <p>\n" +
                "        This alternative sys-screenuploader backend server allows you to directly post screenshots and videos to Discord. Videos will not be hosted on an external site.\n" +
                "    </p>\n" +
                "    <p>\n" +
                "        No images or video files will be stored on my server. It only acts as a relay between Discord and sys-screenuploader.\n" +
                "    </p>\n" +
                "    <p>\n" +
                "        Source code can be found <a href=\"https://example.com\">HERE</a>.\n" +
                "    </p>\n<br/><br/>" +
                "\n" +
                "    <label for=\"webhookUrl\">Enter Discord Webhook URL:</label>\n" +
                "    <input type=\"text\" id=\"webhookUrl\" placeholder=\"Enter your Discord webhook URL\">\n" +
                "\n" +
                "    <button onclick=\"convertUrl()\">Convert URL</button>\n" +
                "\n" +
                "    <p id=\"convertedUrl\">Converted URL will appear here</p>\n" +
                "    <button id=\"copyUrlBtn\" onclick=\"copyUrl()\" style=\"display: none;\">Copy URL</button>\n" +
                "<br/>\n" +
                "    <h3>How to Use</h3>\n" +
                "    <p>\n" +
                "        To post screenshots or videos to Discord using sys-screenuploader, follow these steps:\n" +
                "    </p>\n" +
                "    <ol>\n" +
                "        <li>Copy the Discord webhook URL of the channel you want to post to.</li>\n" +
                "        <li>Paste the webhook URL in the input field above.</li>\n" +
                "        <li>Click the \"Convert URL\" button to get the converted URL.</li>\n" +
                "        <li>Copy the converted URL using the \"Copy URL\" button.</li>\n" +
                "        <li>Use the copied URL in your sys-screenuploader configuration.</li>\n" +
                "    </ol>\n" +
                "\n" +
                "    <h3>URL Parameters:</h3>\n" +
                "    <p>\n" +
                "        <span style=\"text-decoration: underline;\">filename (required)</span> - Filename of the file, automatically added by sys-screenuploader<br>\n" +
                "        <span style=\"text-decoration: underline;\">caption</span> - Provide an optional caption for the screenshot\n" +
                "    </p>\n" +
                "\n" +
                "    <h3>Supported Placeholders for Caption:</h3>\n" +
                "    <p>\n" +
                "        <span style=\"text-decoration: underline;\">{title}</span> - Name of the played title (derived from filename)<br>\n" +
                "        <span style=\"text-decoration: underline;\">{titleid}</span> - Title ID of the played title (derived from filename)<br>\n" +
                "        <span style=\"text-decoration: underline;\">{time}</span> - Time of the screenshot (derived from filename)\n" +
                "    </p>\n" +
                "\n" +
                "    <script>\n" +
                "        function convertUrl() {\n" +
                "            // Get the input value\n" +
                "            var inputUrl = document.getElementById(\"webhookUrl\").value;\n" +
                "\n" +
                "            // Encode the input URL\n" +
                "            var encodedUrl = encodeURIComponent(inputUrl);\n" +
                "\n" +
                "            // Construct the converted URL\n" +
                "            var convertedUrl = \"https://api.erdbeerbaerlp.de/sysscreenuploader/discord/\" + encodedUrl;\n" +
                "\n" +
                "            // Display the converted URL\n" +
                "            document.getElementById(\"convertedUrl\").innerText = convertedUrl;\n" +
                "\n" +
                "            // Show the \"Copy URL\" button\n" +
                "            document.getElementById(\"copyUrlBtn\").style.display = \"block\";\n" +
                "        }\n" +
                "\n" +
                "        function copyUrl() {\n" +
                "            // Get the converted URL\n" +
                "            var convertedUrl = document.getElementById(\"convertedUrl\").innerText;\n" +
                "\n" +
                "            // Create a textarea element to temporarily hold the text\n" +
                "            var tempInput = document.createElement(\"textarea\");\n" +
                "            tempInput.value = convertedUrl;\n" +
                "\n" +
                "            // Append the textarea to the document\n" +
                "            document.body.appendChild(tempInput);\n" +
                "\n" +
                "            // Select the text in the textarea\n" +
                "            tempInput.select();\n" +
                "\n" +
                "            // Execute the copy command\n" +
                "            document.execCommand(\"copy\");\n" +
                "\n" +
                "            // Remove the textarea\n" +
                "            document.body.removeChild(tempInput);\n" +
                "\n" +
                "            // Provide feedback to the user\n" +
                "            alert(\"Copied to clipboard: \" + convertedUrl);\n" +
                "        }\n" +
                "    </script>\n" +
                "</body>\n" +
                "</html>\n";

        app.get("/", ctx -> {
            ctx.result(htmlContent)
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
}
