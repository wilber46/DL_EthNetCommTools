package win.wilber.commassistant.activity;

import android.app.Activity;
import java.util.LinkedList;
import java.util.List;

public class ExitApplication {
    public static ExitApplication instance = null;
    List<Activity> list = new LinkedList();

    public static ExitApplication getInstance() {
        if (instance == null) {
            instance = new ExitApplication();
        }
        return instance;
    }

    public void addActivity(Activity arg) {
        this.list.add(arg);
    }

    public void Exit() {
        for (Activity arg : this.list) {
            arg.finish();
        }
    }
}
