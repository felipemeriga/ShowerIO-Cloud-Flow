package com.felipe.showeriocloud.Processes;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;

import com.espressif.iot.esptouch.EsptouchTask;
import com.espressif.iot.esptouch.IEsptouchResult;
import com.espressif.iot.esptouch.IEsptouchTask;
import com.espressif.iot.esptouch.task.__IEsptouchTask;
import com.felipe.showeriocloud.Activities.Fragments.SearchForDevicesFragment;
import com.felipe.showeriocloud.Utils.ServerCallbackObjects;

import java.lang.ref.WeakReference;
import java.util.List;

public class EsptouchAsyncTask4 extends AsyncTask<byte[], Void, List<IEsptouchResult>> {
    private WeakReference<SearchForDevicesFragment> mFragment;

    private static final String TAG = "EsptouchAsyncTask4";

    // without the lock, if the user tap confirm and cancel quickly enough,
    // the bug will arise. the reason is follows:
    // 0. task is starting created, but not finished
    // 1. the task is cancel for the task hasn't been created, it do nothing
    // 2. task is created
    // 3. Oops, the task should be cancelled, but it is running
    private final Object mLock = new Object();
    private ProgressDialog mProgressDialog;
    private AlertDialog mResultDialog;
    private IEsptouchTask mEsptouchTask;
    private ServerCallbackObjects serverCallbackObjects;

    EsptouchAsyncTask4(SearchForDevicesFragment fragment, ServerCallbackObjects serverCallbackObjects) {
        mFragment = new WeakReference<>(fragment);
        this.serverCallbackObjects = serverCallbackObjects;
    }

    void cancelEsptouch() {
        cancel(true);
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
        if (mResultDialog != null) {
            mResultDialog.dismiss();
        }
        if (mEsptouchTask != null) {
            mEsptouchTask.interrupt();
        }
    }

    @Override
    protected void onPreExecute() {
        Fragment fragment = mFragment.get();
        mProgressDialog = new ProgressDialog(fragment.getContext());
        mProgressDialog.setMessage("Estamos procurando seu dispositivo, isso pode levar alguns minutos...");
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                synchronized (mLock) {
                    if (__IEsptouchTask.DEBUG) {
                        Log.i(TAG, "progress dialog back pressed canceled");
                    }
                    if (mEsptouchTask != null) {
                        mEsptouchTask.interrupt();
                    }
                }
            }
        });
        mProgressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, fragment.getActivity().getText(android.R.string.cancel),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        synchronized (mLock) {
                            if (__IEsptouchTask.DEBUG) {
                                Log.i(TAG, "progress dialog cancel button canceled");
                            }
                            if (mEsptouchTask != null) {
                                mEsptouchTask.interrupt();
                            }
                        }
                    }
                });
        mProgressDialog.show();
    }

    @Override
    protected List<IEsptouchResult> doInBackground(byte[]... params) {
        SearchForDevicesFragment fragment = mFragment.get();
        int taskResultCount;
        synchronized (mLock) {
            byte[] apSsid = params[0];
            byte[] apBssid = params[1];
            byte[] apPassword = params[2];
            byte[] deviceCountData = params[3];
            byte[] broadcastData = params[4];
            taskResultCount = deviceCountData.length == 0 ? -1 : Integer.parseInt(new String(deviceCountData));
            Context context = fragment.getActivity().getApplicationContext();
            mEsptouchTask = new EsptouchTask(apSsid, apBssid, apPassword, context);
            mEsptouchTask.setPackageBroadcast(broadcastData[0] == 1);
            mEsptouchTask.setEsptouchListener(fragment.myListener);
        }
        return mEsptouchTask.executeForResults(taskResultCount);
    }

    @Override
    protected void onPostExecute(final List<IEsptouchResult> result) {
        final  SearchForDevicesFragment fragment = mFragment.get();
        mProgressDialog.dismiss();
        mResultDialog = new AlertDialog.Builder(fragment.getContext())
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (result.get(0).isSuc()) {
                            fragment.mApSsidTV.setVisibility(View.GONE);
                            fragment.mApPasswordET.setVisibility(View.GONE);
                            fragment.mConfirmBtn.setVisibility(View.GONE);
                            fragment.ssidLayout.setVisibility(View.GONE);
                            fragment.passwordLayout.setVisibility(View.GONE);
                            fragment.progressBar.setVisibility(View.VISIBLE);
                            fragment.findDevicesTV.setVisibility(View.VISIBLE);
                            serverCallbackObjects.onServerCallbackObject(true, "SUCCESS", (List<Object>) (List<?>) result);
                        }
                    }
                })
                .create();
        mResultDialog.setCanceledOnTouchOutside(false);
        if (result == null) {
            mResultDialog.setMessage("Nenhum dispositivo encontrado, verifique se foi instalado corretamente, e a senha inserida corretamente");
            mResultDialog.show();
            return;
        }

        IEsptouchResult firstResult = result.get(0);
        // check whether the task is cancelled and no results received
        if (!firstResult.isCancelled()) {
            int count = 0;
            // max results to be displayed, if it is more than maxDisplayCount,
            // just show the count of redundant ones
            final int maxDisplayCount = 5;
            // the task received some results including cancelled while
            // executing before receiving enough results
            if (firstResult.isSuc()) {
                StringBuilder sb = new StringBuilder();
                for (IEsptouchResult resultInList : result) {
                    sb.append("Esptouch success, bssid = ")
                            .append(resultInList.getBssid())
                            .append(", InetAddress = ")
                            .append(resultInList.getInetAddress().getHostAddress())
                            .append("\n");
                    count++;
                    if (count >= maxDisplayCount) {
                        break;
                    }
                }
                if (count < result.size()) {
                    sb.append("\nthere's ")
                            .append(result.size() - count)
                            .append(" more result(s) without showing\n");
                }
                mResultDialog.setMessage("Dispositivo conectado com sucesso!");
            } else {
                mResultDialog.setMessage("Nenhum dispositivo encontrado, verifique se foi instalado corretamente, e a senha inserida corretamente");
            }
            mResultDialog.show();
        }

        fragment.mTask = null;
    }
}