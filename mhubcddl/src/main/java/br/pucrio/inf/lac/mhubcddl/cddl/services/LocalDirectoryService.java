package br.pucrio.inf.lac.mhubcddl.cddl.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import br.pucrio.inf.lac.mhubcddl.cddl.util.Provider;

/**
 * Created by lcmuniz on 05/03/17.
 */
public class LocalDirectoryService extends Service  {

    private static final String TAG = LocalDirectoryService.class.getSimpleName();

    private LocalDirectory localDirectory;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        localDirectory = Provider.getLocalDirectory();

        return START_STICKY;

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
