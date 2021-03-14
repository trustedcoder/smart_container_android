package com.mdx.smartcontainer.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.Html;

/**
 * Created by celestine on 07/12/2017.
 */

public class MyDialogBuilders {

    public static void displayPromptForFinish(final Activity activity, final String body) {
        final AlertDialog.Builder builder =  new AlertDialog.Builder(activity);
        builder.setMessage(Html.fromHtml(body))
                .setNeutralButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface d, int id) {
                                d.cancel();
                                activity.finish();
                            }
                        })
                .setCancelable(false);
        builder.create().show();
    }

    public static void displayPromptForError(final Activity activity, final String body) {
        final AlertDialog.Builder builder =  new AlertDialog.Builder(activity);
        builder.setMessage(Html.fromHtml(body))
                .setNeutralButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface d, int id) {
                                d.cancel();
                            }
                        })
                .setCancelable(false);
        builder.create().show();
    }
    public static void displayPromptForDialog1(final Activity activity, final String body, final AlertDialog i) {
        final AlertDialog.Builder builder =  new AlertDialog.Builder(activity);
        builder.setMessage(Html.fromHtml(body))
                .setNeutralButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface d, int id) {
                                d.cancel();
                                i.show();
                            }
                        })
                .setCancelable(false);
        builder.create().show();
    }

    public static void displayPromptForOnIntent(final Activity activity,final String body,final Intent i1,String option1, String option2) {
        final AlertDialog.Builder builder =  new AlertDialog.Builder(activity);
        builder.setMessage(Html.fromHtml(body))
                .setNegativeButton(option1,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface d, int id) {
                                d.cancel();

                            }
                        })
                .setPositiveButton(option2,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface d, int id) {
                                d.cancel();
                                activity.startActivity(i1);
                            }
                        })
                .setCancelable(false);
        builder.create().show();
    }
}
