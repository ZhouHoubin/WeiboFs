package z.hobin.weibofs;

import android.app.Application;

import z.hobin.weibofs.data.Caches;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Caches.cacheDir = getCacheDir();
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                System.out.println(e.getMessage());
            }
        });
        //Caches.get().clear();
    }
}
