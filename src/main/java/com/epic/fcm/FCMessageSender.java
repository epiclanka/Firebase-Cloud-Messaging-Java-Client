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
import java.util.EnumMap;
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

    public static class OptionalParams {

        /**
         * please follow
         * https://firebase.google.com/docs/cloud-messaging/http-server-ref#params
         */
        public enum Options {

            COLLAPS_KEY("collapse_key"), PRIORITY("priority"), DELAY_WHILE_IDLE("delay_while_idle"),
            TIME_TO_LIVE("time_to_live"), RESTRICTED_PKG_NAME("restricted_package_name");
            private final String param;
            public static final String PRIORITY_HIGH = "high";
            public static final String PRIORITY_NORMAL = "normal";

            private Options(String param) {
                this.param = param;
            }

            String getParamName() {
                return param;
            }
        }

        private final EnumMap<Options, Object> map = new EnumMap<>(Options.class);

        public void setOption(Options option, String value) {
            map.put(option, value);
        }

        public void setOption(Options option, boolean value) {
            map.put(option, value);
        }

        public void setOption(Options option, int value) {
            map.put(option, value);
        }

        EnumMap<Options, Object> getOptionMap() {
            return map;
        }
    }

    private void setOptions(JsonObjectBuilder builder, OptionalParams params) {
        if (params != null && builder != null) {
            EnumMap<OptionalParams.Options, Object> param_map = params.getOptionMap();

            param_map.entrySet().stream().forEach((option_value) -> {
                String param_name = option_value.getKey().getParamName();
                Object value = option_value.getValue();
                if (value instanceof String) {
                    builder.add(param_name, (String) value);
                } else if (value instanceof Integer) {
                    builder.add(param_name, (Integer) value);
                } else if (value instanceof Boolean) {
                    builder.add(param_name, (Boolean) value);
                } else if (value != null) {
                    System.out.println("Unknown type for param key :" + param_name + " Unsupported Class:" + String.valueOf(value.getClass().toString()));
                }
            });
        }
    }

    private WebTarget createTarget() {
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(URL);
        return target;
    }

    private Response createResponse(WebTarget target, JsonObjectBuilder finalObjBuilder) {
        Response res = target.request()
                .header("Authorization", "key=" + SERVER_KEY)
                .post(Entity.entity(finalObjBuilder.build(), MediaType.APPLICATION_JSON));
        return res;
    }

    public Response sendNotification(String token, String title, String text) {
        WebTarget target = createTarget();
        JsonObjectBuilder notification_body_builder = createNotificationBody(title, text);
        JsonObjectBuilder finalObjBuilder = Json.createObjectBuilder();
        finalObjBuilder.add("to", token);
        finalObjBuilder.add("notification", notification_body_builder);
        Response res = createResponse(target, finalObjBuilder);
        return res;
    }

    public Response sendNotification(String token, String title, String text, OptionalParams params) {
        WebTarget target = createTarget();
        JsonObjectBuilder notification_body_builder = createNotificationBody(title, text);
        JsonObjectBuilder finalObjBuilder = Json.createObjectBuilder();
        finalObjBuilder.add("to", token);
        finalObjBuilder.add("notification", notification_body_builder);
        setOptions(finalObjBuilder, params);
        Response res = createResponse(target, finalObjBuilder);
        return res;
    }

    private JsonObjectBuilder createNotificationBody(String title, String text) {
        JsonObjectBuilder notification = Json.createObjectBuilder();
        notification.add("title", title);
        notification.add("text", text);
        return notification;
    }

    /**
     * tokens range [1,1000]
     *
     * @param data
     * @param tokens
     * @return
     */
    public Response sendDataToTokens(JsonObject data, String... tokens) {
        WebTarget target = createTarget();
        int tokens_len = tokens.length;
        JsonObjectBuilder finalObjBuilder = createTargetRequestHeader(tokens_len, tokens);
        finalObjBuilder.add("content_available", true); //for IOS when a notification or message is sent and this is set to true, an inactive client app is awoken
        finalObjBuilder.add("data", data);

        Response res = createResponse(target, finalObjBuilder);
        return res;
    }

    /**
     * tokens range [1,1000]
     *
     * @param data
     * @param params
     * @param tokens
     * @return
     */
    public Response sendDataToTokens(JsonObject data, OptionalParams params, String... tokens) {
        WebTarget target = createTarget();
        int tokens_len = tokens.length;
        JsonObjectBuilder finalObjBuilder = createTargetRequestHeader(tokens_len, tokens);
        finalObjBuilder.add("content_available", true); //for IOS when a notification or message is sent and this is set to true, an inactive client app is awoken
        setOptions(finalObjBuilder, params);
        finalObjBuilder.add("data", data);
        Response res = createResponse(target, finalObjBuilder);
        return res;
    }

    private JsonObjectBuilder createTargetRequestHeader(int tokens_len, String[] tokens) {
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
        return finalObjBuilder;
    }

    public Response broadcastDataToSingleTopic(String topic, JsonObject data) {
        WebTarget target = createTarget();
        JsonObjectBuilder finalObjBuilder = createTopicsDataBody(topic, data);
        Response res = createResponse(target, finalObjBuilder);
        return res;
    }

    public Response broadcastDataToSingleTopic(String topic, JsonObject data, OptionalParams params) {
        WebTarget target = createTarget();
        JsonObjectBuilder finalObjBuilder = createTopicsDataBody(topic, data);
        setOptions(finalObjBuilder, params);
        Response res = createResponse(target, finalObjBuilder);
        return res;
    }

    private JsonObjectBuilder createTopicsDataBody(String topic, JsonObject data) {
        JsonObjectBuilder finalObjBuilder = Json.createObjectBuilder();
        finalObjBuilder.add("to", "/topics/" + topic);
        finalObjBuilder.add("data", data);
        finalObjBuilder.add("content_available", true); //for IOS when a notification or message is sent and this is set to true, an inactive client app is awoken
        return finalObjBuilder;
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
    public Response broadcastDataToConditionalTopic(String conditional_topic, JsonObject data) {
        WebTarget target = createTarget();
        JsonObjectBuilder finalObjBuilder = Json.createObjectBuilder();
        finalObjBuilder.add("condition", conditional_topic);
        finalObjBuilder.add("data", data);
        Response res = createResponse(target, finalObjBuilder);
        return res;
    }
    public Response broadcastDataToConditionalTopic(String conditional_topic, JsonObject data, OptionalParams params) {
        WebTarget target = createTarget();
        JsonObjectBuilder finalObjBuilder = Json.createObjectBuilder();
        finalObjBuilder.add("condition", conditional_topic);
        setOptions(finalObjBuilder, params);
        finalObjBuilder.add("data", data);
        Response res = createResponse(target, finalObjBuilder);
        return res;
    }

}
