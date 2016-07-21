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