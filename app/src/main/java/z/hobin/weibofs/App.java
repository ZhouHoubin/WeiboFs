package z.hobin.weibofs;

import android.app.Application;

import z.hobin.weibofs.data.Caches;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Caches.cacheDir = getCacheDir();
        Caches.get().clear();
    }
}
