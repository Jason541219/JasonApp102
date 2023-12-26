package com.ideas.micro.jasonapp102;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ViewAdapter_AcceptFamily  extends BaseAdapter implements onHttpPostCallback{
    private final String TAG = "推播設定";
    private JSONArray ElementsData ;   //資料
    private int[] resids;
    private LayoutInflater inflater;    //加載layout
    private int indentionBase;          //item缩排
    private final Context _context;
    String fromtosign = "";
    String StringMe = "";
    String acceptCommand = "";
    String denyCommand = "";
    int acceptAlertMsg, denyAlertMsg;

    @Override
    public void onComplete(String response) {

    }

    @Override
    public void onFail(String err) {

    }

    //優化Listview 避免重新加載
    //這邊宣告你會動到的Item元件
    static class ViewHolder{
        TextView text_acceptfamilyname;     // 親屬姓名及帳號
        TextView text_acceptfamilyaction_accept;   // 親屬設定動作
        TextView text_acceptfamilyaction_deny;   // 親屬設定動作
        TextView text_acceptfamilystatus;   // 親屬設定狀態
        LinearLayout layout_listview_acceptfamily_1, layout_listview_acceptfamily_2;
        int fromtoid;
    }

    //初始化
    public ViewAdapter_AcceptFamily(JSONArray data, LayoutInflater inflater, Context context){
        this.ElementsData = data;
        this.inflater = inflater;
        this.resids = resids;
        indentionBase = 100;
        this._context = context;
        StringMe = _context.getResources().getString(R.string.me);
    }

    //取得數量
    @Override
    public int getCount() {
        return ElementsData.length();
    }
    //取得Item

    @Override
    public JSONObject getItem(int position) {
        try {
            return ElementsData.getJSONObject(position);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
    //此範例沒有特別設計ID所以隨便回傳一個值
    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewAdapter_AcceptFamily.ViewHolder holder;
        //當ListView被拖拉時會不斷觸發getView，為了避免重複加載必須加上這個判斷
        if (convertView == null) {
            holder = new ViewAdapter_AcceptFamily.ViewHolder();
            convertView = inflater.inflate(R.layout.listview_acceptfamily, null);
            holder.text_acceptfamilyname = (TextView) convertView.findViewById(R.id.text_acceptfamilyname);
            holder.text_acceptfamilyaction_accept = (TextView) convertView.findViewById(R.id.text_acceptfamilyaction_accept);
            holder.text_acceptfamilyaction_deny = (TextView) convertView.findViewById(R.id.text_acceptfamilyaction_deny);
            holder.text_acceptfamilystatus = (TextView) convertView.findViewById(R.id.text_acceptfamilystatus);
            holder.layout_listview_acceptfamily_1 = (LinearLayout) convertView.findViewById(R.id.layout_listview_acceptfamily_1);
            holder.layout_listview_acceptfamily_2 = (LinearLayout) convertView.findViewById(R.id.layout_listview_acceptfamily_2);
            convertView.setTag(holder);
        } else {
            holder = (ViewAdapter_AcceptFamily.ViewHolder) convertView.getTag();
        }

        final JSONObject measuredata = getItem(position);

        try {
            if (measuredata.getString("fromto").equals("from")){        // 對方是from  我是接收者to
                fromtosign = StringMe +  " <<-- ";
            } else {
                fromtosign = StringMe +  " -->> ";
            }
            if (measuredata.getString("client").equals("none")){        // 對象為 一般會員   帳號【姓名】
                holder.text_acceptfamilyname.setText(fromtosign + measuredata.getString("account")  + " 【" +  measuredata.getString("name") + "】");
            } else {      // 對象為診所   診所【姓名】
                holder.text_acceptfamilyname.setText(fromtosign + measuredata.getString("clinic")  + " 【" +  measuredata.getString("name") + "】");
            }
            holder.text_acceptfamilystatus.setText(statusString(measuredata.getInt("status")));
            holder.text_acceptfamilyaction_accept.setText(acceptString(measuredata.getInt("status"), measuredata.getString("fromto")));
            holder.text_acceptfamilyaction_deny.setText(denyString(measuredata.getInt("status"), measuredata.getString("fromto")));
            if (position%2 == 0) {
                Log.e(TAG, "position = " + position + " 底灰");
                holder.layout_listview_acceptfamily_1.setBackgroundColor(0xDDDDDD);
                holder.layout_listview_acceptfamily_2.setBackgroundColor(0xDDDDDD);
            } else {
                Log.e(TAG, "position = " + position + " 底白");
                holder.layout_listview_acceptfamily_1.setBackgroundColor(0xFFFFFF);
                holder.layout_listview_acceptfamily_2.setBackgroundColor(0xFFFFFF);
            }
            holder.fromtoid = measuredata.getInt("id");
            // 接受按鍵
            holder.text_acceptfamilyaction_accept.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view) {
                    Log.e(TAG, "acceptCommand = " + acceptCommand);
                    if (acceptCommand.equals("")) return;       //  沒有指令就退出
                    AlertDialog.Builder builder = new AlertDialog.Builder(_context);
                    builder.setMessage(acceptAlertMsg)
                            .setTitle(R.string.dialog_systemcomment)
                            .setPositiveButton(R.string.dialog_Confirm, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    JSONObject sqljson = new JSONObject();
                                    try {
                                        sqljson.put("command", acceptCommand);
                                        sqljson.put("id", measuredata.getInt("id"));
                                        sqljson.put("date", Utility.getDateTimeNow());
                                        sqljson.put("tokenID", measuredata.getString("token"));
                                        sqljson.put("title", _context.getResources().getString(R.string.msg_fcm_title));
                                        switch (acceptCommand){
                                            case "RequestPushAgain":
                                                sqljson.put("message", SingleUser.getInstance().getUsername() +  _context.getResources().getString(R.string.msg_fcm_requestpushagain));
                                                break;
                                            case "AcceptPushRequest":
                                                sqljson.put("message", measuredata.getString("name") +  _context.getResources().getString(R.string.msg_fcm_acceptpushrequest));
                                                break;
                                            case "DenyPushRequest":
                                                sqljson.put("message", measuredata.getString("name") +  _context.getResources().getString(R.string.msg_fcm_denypushrequest));
                                                break;

                                        }
                                        sqljson.put("istopic", false);
                                        HttpPost httpPost = new HttpPost(ViewAdapter_AcceptFamily.this);
                                        httpPost.startPost(_context, GlobalVariables.http_url,  sqljson.toString());
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            })
                            .setNegativeButton(R.string.dialog_Cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    // Do nothing
                                }
                            });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            });

            holder.text_acceptfamilyaction_deny.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(_context);
                    builder.setMessage("res")
                            .setTitle(R.string.dialog_systemcomment)
                            .setPositiveButton(R.string.dialog_OK, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                }
                            });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return convertView;
    }

    private String statusString(int status){
        String statusstr = "";
        switch (status){
            case 0: statusstr = "等待授權中...";break;
            case 1: statusstr = "已取得授權";break;
            case 2: statusstr = "已拒絕授權";break;
            case 3: statusstr = "已中斷推播";break;
        }
        return statusstr;
    }

    private String acceptString (int status, String fromto){
        String acceptstr = "";
        acceptCommand = "";
        if (fromto.equals("from")){        // 對方是from  我是接收者to 也是申請者
            switch (status){
                case 0: case 2:         // 申請者在等待授權或已拒絕授權時  提出再次申請
                    acceptstr = _context.getResources().getString(R.string.requestpushagain);       // 再次申請
                    acceptCommand="RequestPushAgain";
                    acceptAlertMsg = R.string.dialog_msg_requestpushagain;
                    break;
                case 1:             // 已經同意授權後，提出中斷推播
                    acceptstr = _context.getResources().getString(R.string.disconnectpush);         // 終止推播
                    acceptCommand = "DisconnectPush";
                    acceptAlertMsg = R.string.dialog_msg_disconnectpush;
                    break;
                case 3:             // 已經中斷推播後，重新推播
                    acceptstr = _context.getResources().getString(R.string.reconnectpush);         // 終止推播
                    acceptCommand = "ReconnectPush";
                    acceptAlertMsg = R.string.dialog_msg_reconnectpush;
                    break;                // 中斷推播
            }
        }  else {        // 對方是申請者
            switch(status){
                case 0:        // 同意請者在等待授權時
                    acceptstr = _context.getResources().getString(R.string.acceptpushrequest);
                    acceptCommand="AcceptPushRequest";
                    acceptAlertMsg = R.string.dialog_msg_acceptpushrequest;
                    break;
                case 1:     // 已經接受對方的要求後，中斷推播
                    acceptstr = _context.getResources().getString(R.string.disconnectpush);
                    acceptCommand = "DisconnectPush";
                    acceptAlertMsg = R.string.dialog_msg_disconnectpush;
                    break;
            }
        }
        return acceptstr;
    }

    private String denyString (int status, String fromto){
        String denystr = "";
        if (fromto.equals("from")){        // 對方是from  我是接收者to 也是申請者
            switch (status){
                case 0: case 2:        // 申請者在等待授權或已拒絕授權時  放棄申請
                    denystr = _context.getResources().getString(R.string.requestpushabord);     //  放棄申請
                    denyCommand = "RequestPushAbord";
                    denyAlertMsg = R.string.dialog_msg_requestpushabord;
                    break;  // 放棄申請授權
            }
        }  else {
            switch(status){        // 對方是申請者
                case 0:
                    denystr = _context.getResources().getString(R.string.denypushrequest);
                    denyCommand = "DenyPushRequest";
                    denyAlertMsg = R.string.dialog_msg_denypushrequest;
                    break;     // 拒絕對方的要求
            }
        }
        return denystr;
    }


    private void showAlert(int res){
        showAlert(_context.getResources().getString(res));
    }

    private void showAlert(String res) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(_context);
        builder.setMessage(res)
                .setTitle(R.string.dialog_systemcomment)
                .setPositiveButton(R.string.dialog_OK, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
        androidx.appcompat.app.AlertDialog dialog = builder.create();
        dialog.show();
    }
}
