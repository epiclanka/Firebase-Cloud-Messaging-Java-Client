package com.epic.firebase;

import javax.annotation.Nonnull;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by thilina_h on 12/11/2017.
 */
public class MessageBuilder {

    private String registration_id;
    private boolean isTopic;
    private Notification notification;
    private Data dataMessage;
    private Set<String> registration_ids = new HashSet<>();
    private JsonObjectBuilder messageBuilder = Json.createObjectBuilder();


    public MessageBuilder(String registration_id, Notification notification) {
        this.registration_id = registration_id;
        this.notification = notification;
    }

    public MessageBuilder(String registration_id, Notification notification, Data dataMessage) {
        this.registration_id = registration_id;
        this.notification = notification;
        this.dataMessage = dataMessage;
    }

    public boolean isTopic() {
        return isTopic;
    }

    public void setTopic(boolean topic) {
        this.isTopic = topic;
    }

    public MessageBuilder(Set<String> registration_ids, Notification notification) {
        this.registration_ids = registration_ids;
        this.notification = notification;
    }

    public MessageBuilder(Set<String> registration_ids, Notification notification, Data dataMessage) {
        this.registration_ids = registration_ids;
        this.notification = notification;
        this.dataMessage = dataMessage;
    }

    public String getRegistration_id() {
        return registration_id;
    }

    public Notification getMessage() {
        return notification;
    }

    public void setMessage(Notification notification) {
        this.notification = notification;
    }

    /**
     * @return an unmodifiable set
     */
    public Set<String> getRegistration_ids() {
        return Collections.unmodifiableSet(registration_ids);
    }


    public JsonObject build() {
        JsonObjectBuilder payload_builder = Json.createObjectBuilder();

        if (registration_id != null) {
            if (isTopic) {
                payload_builder.add("topic", registration_id);
            } else {
                payload_builder.add("token", registration_id);
            }
        }

        if (notification != null) {
            payload_builder.add("notification", notification.build());
        }
        if (dataMessage != null) {
            payload_builder.add("data", dataMessage.build());
        }

        messageBuilder.add("message", payload_builder);
        return messageBuilder.build();
    }


    public static class Notification {
        @Nonnull
        private String body;
        @Nonnull
        private String title;

        public Notification(@Nonnull String title, @Nonnull String body) {
            this.body = body;
            this.title = title;
        }

        public String getBody() {
            return body;
        }

        public void setBody(String body) {
            this.body = body;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public JsonObject build() {
            JsonObjectBuilder builder = Json.createObjectBuilder();
            builder.add("title", title)
                    .add("body", body);
            return builder.build();
        }
    }

    public static class Data {
        private JsonObject obj;

        public JsonObject getObj() {
            return obj;
        }

        public void setObj(JsonObject obj) {
            this.obj = obj;
        }

        JsonObject build() {
            return obj;
        }
    }


}
