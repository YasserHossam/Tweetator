package TwitterHelper;

import android.app.Application;
import android.content.Context;

/**
 * Created by yasser on 6/29/2015.
 */
public class App extends Application {

    private static Context mContext;

    public static Context getContext() {
        return mContext;
    }

    public static void setContext(Context mmContext) {
        mContext = mmContext;
    }

}
