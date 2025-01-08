package ADG.Utils;

import com.google.gwt.user.client.Timer;

public class PollingService {
    private Timer timer;

    public void startPolling(int intervalMillis, Runnable action) {
        if (timer == null) {
            timer = new Timer() {
                @Override
                public void run() {
                    action.run();
                }
            };
            timer.scheduleRepeating(intervalMillis);
        }
    }

    public void stopPolling() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }
}
