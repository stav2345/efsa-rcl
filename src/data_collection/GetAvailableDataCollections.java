package data_collection;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;

import app_config.PropertiesReader;

public class GetAvailableDataCollections {
	
	/**
	 * Get the available data collections codes starting from the
	 * starting year of the data collection defined in the configuration
	 * file.
	 * @return
	 */
	public static Collection<String> getCodes() {
		
		Collection<String> dcCodes = new ArrayList<>();
		
		// add test data collection
		dcCodes.add(PropertiesReader.getTestDataCollectionCode());
		
		// add all the data collections from the starting year
		// to today
		Calendar today = Calendar.getInstance();
		int currentYear = today.get(Calendar.YEAR);
		int startingYear = PropertiesReader.getDataCollectionStartingYear();
		
		// if other years are needed
		if (currentYear >= startingYear) {
			
			// add also the other years
			for (int i = currentYear; i >= startingYear; --i) {	
				dcCodes.add(PropertiesReader.getDataCollectionCode(String.valueOf(i)));
			}
		}
		
		return dcCodes;
	}
}
