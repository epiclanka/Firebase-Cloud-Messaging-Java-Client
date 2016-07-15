/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.epic.fcm;

/**
 *
 * @author thilina_h
 */
import javax.ws.rs.client.*;
import javax.ws.rs.core.Response;
import javax.json.*;
import javax.ws.rs.core.MediaType;

public class FCMessageSender {

    protected final String SERVER_KEY;
    private final String URL = "https://fcm.googleapis.com/fcm/send";

    public FCMessageSender(String SERVER_KEY) {
        this.SERVER_KEY = SERVER_KEY;
    }

    public Response sendMessage(String token, String title, String text) {
        Client client = ClientBuilder.newClient();

        WebTarget target = client.target(URL);
        JsonObjectBuilder notification = Json.createObjectBuilder();
        notification.add("title", title);
        notification.add("text", text);

        JsonObjectBuilder finalObjBuilder = Json.createObjectBuilder();
        finalObjBuilder.add("to", token);
        finalObjBuilder.add("notification", notification);

        Response res = target.request()
                .header("Authorization", "key=" + SERVER_KEY)
                .post(Entity.entity(finalObjBuilder.build(), MediaType.APPLICATION_JSON));

        return res;
    }

    /**
     * tokens range [1,1000]
     *
     * @param data
     * @param tokens
     * @return
     */
    public Response sendData(JsonObject data, String... tokens) {
        Client client = ClientBuilder.newClient();

        WebTarget target = client.target(URL);
        int tokens_len = tokens.length;
        JsonObjectBuilder finalObjBuilder = Json.createObjectBuilder();

        if (tokens_len > 1) {
            JsonArrayBuilder tokenArrayBuilder = Json.createArrayBuilder();
            for (int i = 0; i < 1000 && i < tokens_len; i++) {
                tokenArrayBuilder.add(tokens[i]);
            }
            finalObjBuilder.add("registration_ids", tokenArrayBuilder);

        } else {
            finalObjBuilder.add("to", tokens[0]);
        }
        finalObjBuilder.add("data", data);

        Response res = target.request()
                .header("Authorization", "key=" + SERVER_KEY)
                .post(Entity.entity(finalObjBuilder.build(), MediaType.APPLICATION_JSON));
        return res;
    }

    public Response broadcastSingleTopic(String topic, JsonObject data) {
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(URL);
        JsonObjectBuilder finalObjBuilder = Json.createObjectBuilder();
        finalObjBuilder.add("to", "/topics/" + topic);
        finalObjBuilder.add("data", data);
        Response res = target.request()
                .header("Authorization", "key=" + SERVER_KEY)
                .post(Entity.entity(finalObjBuilder.build(), MediaType.APPLICATION_JSON));
        return res;
    }

    /**
     * To send to combinations of multiple topics, the app server sets the
     * condition key with conditions expressed in the following format: 
     * 
     * 'TopicA' in topics && ('TopicB' in topics || 'TopicC' in topics)
     *
     * FCM first evaluates any conditions in parentheses, and then evaluates the
     * expression from left to right. In the above expression, a user subscribed
     * to any single topic does not receive the message. Likewise, a user who
     * does not subscribe to TopicA does not receive the message. These
     * combinations do receive it:
     *
     * TopicA and TopicB TopicA and TopicC
     *
     * Conditions for topics support two operators per expression, and
     * parentheses are supported.
     *
     * @param conditional_topic
     * @param data
     * @return
     */
    public Response broadcastConditionalTopic(String conditional_topic, JsonObject data) {
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(URL);
        JsonObjectBuilder finalObjBuilder = Json.createObjectBuilder();
        finalObjBuilder.add("condition", conditional_topic);
        finalObjBuilder.add("data", data);
        Response res = target.request()
                .header("Authorization", "key=" + SERVER_KEY)
                .post(Entity.entity(finalObjBuilder.build(), MediaType.APPLICATION_JSON));
        return res;
    }

}
