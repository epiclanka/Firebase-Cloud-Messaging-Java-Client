/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Messaging;

import com.nwised.firebase.FCMessageSender;
import javax.ws.rs.core.Response;

/**
 *
 * @author thilina_h
 */
public class FCM {
    public static void main(String[] args) {
//        DBChangeMessage message = new DBChangeMessage(DBChangeMessage.Operation.INSERT, "promos");
//        message.addId(455);
////         broadcastTopicHighPriority(message, 6000);
//        OptionalParams params = new OptionalParams();
//        params.setOption(OptionalParams.Options.PRIORITY, OptionalParams.Options.PRIORITY_HIGH);
//        //SENDER.broadcastNotificationToTopic("dbchange", "Test", "asdsa", params);
        String token="eG9LsUi7slo:APA91bGUCHyrTR-zu9wE-XiSR-QmIdh9H7r8MEAM7-YDhu07sKPP2jDAVtUehVyHut_fdYJq6sQezGfMifi9qsUnv6lxloZritPdpr9mRxBsoYpJJ3eDgpOuCGggnHD0Mnh7Y0riApdA";
        String title="Title";
        String text="Vola";
        FCMessageSender MESSAGE_SENDER = new FCMessageSender("AAAAeO9QyBU:APA91bECdhY8Mm-wjraabnyydfwS0i0XQ8HkRqlaeEUHbpFnaD0R3dMVh0XxdYfcYU1JLSCd8f3HdAyVoH9eXrrQPZyFPJVooDEmJ2ZX28ubxT5CtQDXBoHB9I50z1tgCheQcjygVnqY");
        Response sendNotification = MESSAGE_SENDER.sendNotification(token, title, text);
        System.out.println(sendNotification.getStatusInfo());
    }
}
