package com.ideas.micro.jasonapp102;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class FCMService  extends FirebaseMessagingService {
    // https://fightwennote.blogspot.com/2019/03/android-firebase-fcm.html
    //  https://ironglion.com/archives/firebasecloudmessaging/
    // https://docs.microsoft.com/zh-tw/xamarin/android/data-cloud/google-messaging/remote-notifications-with-fcm?tabs=windows
    // 碼農日常-『Android studio』FCM雲端推播與通知系統(上)-建立第一個基本推播通知APP
    // https://thumbb13555.pixnet.net/blog/post/333378541-notification

    private final String TAG = "推播服務";
    public FCMService() {

    }

    @Override
    // onMessageReceived 收到的訊息都在這接收，請注意是在背景執行緒執行，BUT APP 必須是在運行的狀態
    // 官方建議不要做超過十秒的事情，否則要另開 Job 處理
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // 如果APP在前景 會觸發 onMessageReceived
        // 如果APP在背景 不會觸發 onMessageReceived 但是 會收到推播
        // 如果APP關閉 不會收到推播
        Log.e(TAG, "onMessageReceived " + remoteMessage.getData());
        super.onMessageReceived(remoteMessage);
        if (remoteMessage.getNotification() != null) {
            Log.e(TAG,"title "+remoteMessage.getNotification().getTitle());
            Log.e(TAG,"body "+remoteMessage.getNotification().getBody());
            // 那如果想要自訂Notification的話，那就用data messages，然後在收到訊息後去建一個Notification就行了。
            sendNotification(remoteMessage.getNotification().getTitle(),
                    remoteMessage.getNotification().getBody());
        }
    }

    @Override
    // onNewToken 會產一個手機裝置的 token ，傳送訊息識別用
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
        Log.i(TAG,"裝置 token = "+s);
    }


    private void sendNotification(String messageTitle,String messageBody) {
        Log.e(TAG, "sendNotification");
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT);

        String channelId = "default_notification_channel_id";
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setContentTitle(messageTitle)
                        .setSmallIcon(R.drawable.heartbeat)
                        .setContentText(messageBody)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent);


        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(0, notificationBuilder.build());
    }

    /*
 因為前述步驟我們都是送通知名稱跟通知文字，在 Firebase 的文件提到，
如果我們的 App 放到背景，甚至是關閉，推播的  Notification 會變成系統預設樣式，
然而預設系統樣式能夠變化的只有 small icon 跟 文字顏色。
不會 call onMessageReceived 且如果點擊進 App 收資料會從 Activity Intent 接收。
(詳細連結: https://firebase.google.com/docs/cloud-messaging/android/receive)

如果希望保持一致，都在 onMessageReceived 接收，並且不會跑去系統，
那麼就必須把資料放在 data (自訂資料) 而不要送通知標題跟通知文字。
但 Firebase 後台不允許這麼做，故要做的話需與自家後台配合。

若要測試則可以到 http://pushtry.com/
MyFirebaseService 會改成如下，getData 後面的 Key 值自訂。
@Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        if (remoteMessage.getData()!=null) {
            sendNotification(remoteMessage.getData().get("title"),
　　　　　　　　　　　　　　　　　remoteMessage.getData().get("msg"));
        }
    }
    伺服器金鑰可以從 Firebase 後台左邊的專案設定裡找到，以下為發送訊息的 json 格式
   {"to":"你的裝置token","data":{"title":"測試 data 標題","msg":"測試 data 文字"},"priority":"high"}
     */
}