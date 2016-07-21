# Firebase-Cloud-Messaging-Java-Client
A FCM  Client library for java using JAX-RS Client 

Simple example to send a push message

	private static final FCMessageSender SENDER = new FCMessageSender("YourServerKey");
    private static final ExecutorService EXECUTOR = Executors.newCachedThreadPool();

	//Asynchronusly send notification
    public static Future<Response> sendLoginNotification(String token, String message) {
        Future<Response> response=EXECUTOR.submit(() -> {
            System.out.println("Sending greetings to pushID :" + token);
            return SENDER.sendNotification(token, message);
        });
        return response;
    }

	
Simple example to broadcast a data message to a topic

	public static Future<Response> broadcastTopic(String topic, JsonObject body) {
			return EXECUTOR.submit(()->{
				
			System.out.println("Broadcasting topic :" + topic + " " + body);
			return SENDER.broadcastDataToTopic(topic, body);
			});
	}
	
How to broadcast a data message with optional parameters

	public static Future<Response> broadcastTopicWith_TTL(String topic, JsonObject body, int time_to_live_seconds) {
			return EXECUTOR.submit(()->{
			OptionalParams params= new OptionalParams();
			params.setOption(FCMessageSender.OptionalParams.Options.TIME_TO_LIVE, ttl_in_seconds);	
			System.out.println("Broadcasting topic :" + topic + " " + body);
			return SENDER.broadcastDataToTopic(topic, body, params);
			});
	}