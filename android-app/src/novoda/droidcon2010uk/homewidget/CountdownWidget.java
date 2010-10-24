package novoda.droidcon2010uk.homewidget;

import novoda.droidcon2010uk.ui.HomeActivity;
import novoda.droidcon2010uk.util.UIUtils;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import android.widget.RemoteViews;

import novoda.droidconuk2010.R;

public class CountdownWidget extends AppWidgetProvider {

	private static boolean countDownRequired = true;
	private static PendingIntent refreshIntent;
	private static final String TAG = CountdownWidget.class.getSimpleName();

	@Override
	public void onUpdate(final Context context, final AppWidgetManager manager,
			final int[] widgetIds) {
		context.startService(new Intent(context, Updater.class));
	}

	@Override
	public void onEnabled(final Context context) {
		final Intent intent = new Intent(context, Updater.class);
		refreshIntent = PendingIntent.getService(context, 0, intent, 0);
		((AlarmManager) context.getSystemService(Context.ALARM_SERVICE))
				.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock
						.elapsedRealtime(), 1000 * 60, refreshIntent);
		
//		updateScore(context);
	}

	@Override
	public void onDisabled(final Context context) {
		((AlarmManager) context.getSystemService(Context.ALARM_SERVICE))
				.cancel(refreshIntent);
	}

	/**
	 * Updates the counter
	 */

	private static void updateScore(final Context context) {
		final RemoteViews views = new RemoteViews(context.getPackageName(),
				R.layout.widget_countdown);

		int remainingSec = (int) Math
				.max(0, (UIUtils.CONFERENCE_START_MILLIS - System
						.currentTimeMillis()) / 1000);

		if(remainingSec ==0){
			countDownRequired = false;
		}else{
			
			final int secs = remainingSec % 86400;
			final int days = remainingSec / 86400;
			final String str = context.getResources().getQuantityString(
					R.plurals.now_playing_countdown, days, days,
					formatRemainingTime(secs));
			views.setTextViewText(R.id.widget_title, str);
			
			final Intent appIntent = new Intent(context, HomeActivity.class);
			PendingIntent appStartPI = PendingIntent.getActivity( context, 0, appIntent, 0 );
			views.setOnClickPendingIntent(R.id.countdown_widget, appStartPI);
			
			final ComponentName widget = new ComponentName(context,
					CountdownWidget.class);
			AppWidgetManager.getInstance(context).updateAppWidget(widget, views);
		}
		
	}

	/**
	 * Formats a time in the form "HH:MM"
	 *
	 * @param elapsedSeconds
	 *            the time in seconds.
	 */
	private static String formatRemainingTime(long elapsedSeconds) {
		long hours = 0;
		long minutes = 0;

		if (elapsedSeconds >= 3600) {
			hours = elapsedSeconds / 3600;
			elapsedSeconds -= hours * 3600;
		}
		if (elapsedSeconds >= 60) {
			minutes = elapsedSeconds / 60;
			elapsedSeconds -= minutes * 60;
		}

		StringBuilder result = new StringBuilder(5);
		if(hours < 10) {
			result.append('0');
		}
		result.append(hours);
		result.append(':');
		if(minutes < 10) {
			result.append('0');
		}
		result.append(minutes);
		return result.toString();
	}

	public static class Updater extends Service {

		@Override
		public void onStart(final Intent intent, final int startId) {
			updateScore(this);
			
			if(!countDownRequired){
				final long nowTime = System.currentTimeMillis();
				final int timeSinceConfEnd = (int) Math.max(0, (UIUtils.CONFERENCE_END_MILLIS - nowTime) / 1000);

				if(timeSinceConfEnd > 0){
					setWidgetTxt(this, getResources().getString(R.string.widget_title_text_during_conf), getResources().getString(R.string.widget_subtitle_text_during_conf));
					setAlarmDaily(this);
				}else{
					setWidgetTxt(this, getResources().getString(R.string.widget_title_text_after_conf), getResources().getString(R.string.widget_subtitle_text_after_conf));
				}
			}
			
			stopSelf();
		}

		private void setWidgetTxt(Context ctx, String title, String subtitle) {
			final RemoteViews views = new RemoteViews(ctx.getPackageName(),
					R.layout.widget_countdown);
			views.setTextViewText(R.id.widget_title, title);
			views.setTextViewText(R.id.widget_subtitle, subtitle);
			
			final Intent appIntent = new Intent(ctx, HomeActivity.class);
			PendingIntent appStartPI = PendingIntent.getActivity( ctx, 0, appIntent, 0 );
			views.setOnClickPendingIntent(R.id.countdown_widget, appStartPI);
			
			final ComponentName widget = new ComponentName(ctx,
					CountdownWidget.class);
			AppWidgetManager.getInstance(ctx).updateAppWidget(widget, views);
		}

		private void setAlarmDaily(Context ctx) {
			final Intent intent = new Intent(ctx, Updater.class);
			refreshIntent = PendingIntent.getService(ctx, 0, intent, 0);
			((AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE))
					.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock
							.elapsedRealtime(), 1000 * 60 * 1440, refreshIntent);
		}

		@Override
		public IBinder onBind(final Intent intent) {
			return null;
		}
	}

}
