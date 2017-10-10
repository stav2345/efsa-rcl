package global_utils;

import java.text.SimpleDateFormat;

public class TimeUtils {

	/**
	 * Get a string which contains the today timestamp in format: yyyyMMdd-HHmmss
	 * @return
	 */
	public static String getTodayTimestamp() {
		final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-HHmmss");
		String todayTs = sdf.format(System.currentTimeMillis());
		return todayTs;
	}
}
