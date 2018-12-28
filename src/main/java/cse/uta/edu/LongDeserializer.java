package cse.uta.edu;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

public class LongDeserializer implements JsonDeserializer<Long> {

	@Override
	public Long deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

		/* Note: don't use json.getAsString(), Accumulable[4]'s value filed is array, will cause execption*/
		String valueString =json.toString();
		
		boolean valueIsNumric = true;
		
		/** TODO: Parse if value is array other than Long
		Current implementation returns -1 for simplisity
		 */		
		try {
			Long.parseLong(valueString);
		} catch (NumberFormatException e) {
			valueIsNumric = false; 
		}
		
		if(valueIsNumric) {
			return Long.valueOf(valueString);
		} else {
			return Long.valueOf(-1);
		}
	}

}
