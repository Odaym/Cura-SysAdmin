package com.cura.main;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.cura.R;
import com.cura.classes.Server;
import com.cura.classes.TitleFont_Customizer;
import com.cura.classes.TypefacedTextView;
import com.cura.database.DBHelper;
import com.cura.gridview.Item;
import com.flurry.android.FlurryAgent;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;

import java.util.List;

public class Cells_Adapter extends com.cura.gridview.BaseDynamicGridAdapter {

    private LayoutInflater inflater;
    private Context context;
    private ServersViewHolder holder;
    private DBHelper dbHelper;
    private String serverPropertiesChangedString = "com.cura.serverPropertiesChanged";
    private String serverSettingsClickedString = "com.cura.serverSettingsClicked";

    public Cells_Adapter(Context context, List<Item> totalItems, int columnCount) {
        super(context, totalItems, columnCount);
        this.context = context;

        dbHelper = new DBHelper(context);
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        if (convertView == null) {
            inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.cells_shape, parent, false);

            holder = new ServersViewHolder();

            holder.cellFrame = (ImageView) convertView.findViewById(R.id.cellFrame);
            holder.cellGearSettings = (ImageView) convertView.findViewById(R.id.cellGearSettings);
            holder.usernameSubtitle = (TypefacedTextView) convertView
                    .findViewById(R.id.usernameSubtitle);
            holder.domainSubtitle = (TypefacedTextView) convertView
                    .findViewById(R.id.domainSubtitle);
            convertView.setTag(holder);
        } else {
            holder = (ServersViewHolder) convertView.getTag();
        }

        getItem(position).setOrder(position);

        if (getItem(position).getUsername().compareTo("root") == 0)
            holder.cellFrame.setBackgroundResource(R.drawable.server_root);
        else
            holder.cellFrame.setBackgroundResource(getItem(position).getDrawable());

        holder.usernameSubtitle.setText(TitleFont_Customizer.makeStringIntoTitle(context, getItem(position).getUsername()));
        holder.domainSubtitle.setText(TitleFont_Customizer.makeStringIntoTitle(context, getItem(position).getDomain()));

//        if (holder.usernameSubtitle.getText().toString().length() > 16) {
//            int lengthDif = holder.usernameSubtitle.getText().length() - 10;
//            Animation mAnimation = new TranslateAnimation(0f, -(17f * lengthDif),
//                    0.0f, 0.0f);
//            mAnimation.setDuration(1000);
//            mAnimation.setRepeatCount(2);
//            mAnimation.setRepeatMode(Animation.REVERSE);
//            holder.usernameSubtitle.setAnimation(mAnimation);
//        }
//
//        if (holder.domainSubtitle.getText().toString().length() > 16) {
//            int lengthDif = holder.domainSubtitle.getText().length() - 10;
//            Animation mAnimation = new TranslateAnimation(0f, -(17f * lengthDif),
//                    0.0f, 0.0f);
//            mAnimation.setDuration(1000);
//            mAnimation.setRepeatCount(2);
//            mAnimation.setRepeatMode(Animation.REVERSE);
//            holder.domainSubtitle.setAnimation(mAnimation);
//        }

        holder.cellGearSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View dialogLayout = inflater.inflate(R.layout.settings_popup_dialog, parent, false);

                final Dialog dialog = new Dialog(context);
                dialog.setTitle(TitleFont_Customizer.makeStringIntoTitle(context, getItem(position).getUsername() + "@" + getItem(position).getDomain()));
                dialog.setContentView(dialogLayout);

                Button deleteServerBTN, clearServerKeysBTN, editServerBTN;

                deleteServerBTN = (Button) dialogLayout.findViewById(R.id.deleteServerBTN);
                clearServerKeysBTN = (Button) dialogLayout.findViewById(R.id.clearServerKeysBTN);
                editServerBTN = (Button) dialogLayout.findViewById(R.id.editServerBTN);

                deleteServerBTN.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.cancel();
                        EasyTracker.getInstance(context).send(
                                MapBuilder.createEvent("Accounts_List", "button", "Delete Server",
                                        null).build());
                        FlurryAgent.logEvent("Login_Delete_Server");
                        dbHelper.deleteServer(getItem(position).getId());
                        List<Server> servers = dbHelper.getAllServers();
                        if (servers.isEmpty()) {
                            ((Activity) context).finish();
                            Intent serverPropertiesChanged = new Intent();
                            serverPropertiesChanged.setAction(serverPropertiesChangedString);
                            context.sendBroadcast(serverPropertiesChanged);
                        } else {
                            remove(getItem(position));
                            notifyDataSetChanged();
                        }
                    }
                });

                clearServerKeysBTN.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.cancel();

                        EasyTracker.getInstance(context).send(
                                MapBuilder.createEvent("Select_Server_Activity", "button", "Clear Server Key",
                                        null).build());
                        FlurryAgent.logEvent("Login_Clear_Server_Key");
                        dbHelper.clearServerKeys(getItem(position).getId());
                        Intent serverPropertiesChanged = new Intent();
                        serverPropertiesChanged.setAction(serverPropertiesChangedString);
                        context.sendBroadcast(serverPropertiesChanged);
                        new AlertDialog.Builder(context)
                                .setTitle(
                                        TitleFont_Customizer.makeStringIntoTitle(
                                                context,
                                                context.getResources().getString(R.string.success)))
                                .setMessage(
                                        TitleFont_Customizer.makeStringIntoTitle(
                                                context,
                                                context.getResources().getString(R.string.clearKeyResult)))
                                .setPositiveButton(
                                        TitleFont_Customizer.makeStringIntoTitle(
                                                context,
                                                context.getResources().getString(R.string.ok)),
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int arg1) {
                                                dialog.dismiss();
                                            }
                                        }).show();
                    }
                });

                editServerBTN.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.cancel();

                        Intent serverSettingsClicked = new Intent();
                        serverSettingsClicked.setAction(serverSettingsClickedString);
                        serverSettingsClicked.putExtra("serverPosition", position);
                        context.sendBroadcast(serverSettingsClicked);
                    }
                });

                dialog.show();
            }
        });

        return convertView;
    }

    static class ServersViewHolder {
        private ImageView cellGearSettings;
        private ImageView cellFrame;
        private TypefacedTextView usernameSubtitle;
        private TypefacedTextView domainSubtitle;
    }

}