package com.nwised.firebase;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;

import javax.json.JsonObject;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by thilina_h on 12/12/2017.
 */
public class MessageSender {


    private String URL = "https://fcm.googleapis.com/v1/projects/%s/messages:send";
    private WebTarget webTarget;
    private final static String[] SCOPES = {
            "https://www.googleapis.com/auth/firebase.database",
            "https://www.googleapis.com/auth/userinfo.email",
            "https://www.googleapis.com/auth/firebase.messaging"
    };
    private GoogleCredential googleCredential;

    private String getAccessToken() {
        try {
            googleCredential.refreshToken();
        } catch (IOException e) {
            Logger.getLogger(MessageSender.class.getName()).log(Level.SEVERE, "Failed refreshing Google Auth2.0 token",e);
        }
        return googleCredential.getAccessToken();
    }

    public MessageSender(InputStream config_stream, String project_name) {
        try {
            googleCredential = GoogleCredential
                    .fromStream(config_stream)
                    .createScoped(Arrays.asList(SCOPES));
        } catch (IOException e) {
            e.printStackTrace();
            Logger.getLogger(MessageSender.class.getName()).log(Level.SEVERE, "Failed building GoogleCredential ",e);
        }
        URL = String.format(URL, project_name);
        webTarget = ClientBuilder.newClient().target(URL);

    }

    private Response postReq(WebTarget target, JsonObject obj) {
        Response res = target.request()
                .header("Authorization", "Bearer " + getAccessToken())
                .post(Entity.entity(obj.toString(), MediaType.APPLICATION_JSON));
        return res;
    }

    public Response sendMessage(String registration_id, MessageBuilder.Notification notification, MessageBuilder.Data dataMessage) {
        MessageBuilder messageBuilder = new MessageBuilder(registration_id, notification, dataMessage);
        JsonObject object = messageBuilder.build();
        System.out.println(object.toString());
        return postReq(webTarget, object);
    }

    public Response sendMessage(String registration_id, MessageBuilder.Notification notification) {
        MessageBuilder messageBuilder = new MessageBuilder(registration_id, notification);
        JsonObject object = messageBuilder.build();
        System.out.println(object.toString());
        return postReq(webTarget, object);
    }
//    public Response sendMessage(Set<String> registration_ids, MessageBuilder.Notification notification) {
//        MessageBuilder messageBuilder = new MessageBuilder(registration_ids, notification);
//        JsonObject object = messageBuilder.build();
//        System.out.println(object.toString());
//        return postReq(webTarget, object);
//    }

}
