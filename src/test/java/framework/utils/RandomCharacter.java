package framework.utils;

import framework.utils.Log;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.log4j.Logger;

public class RandomCharacter {
	protected  static Logger logger;

	//Get random alphabetic characters
	public static String getRandomAlphaString(int length)  {
	    try {
            String charset = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
            charset = RandomStringUtils.random(length, charset.toCharArray());
            return charset;
        }catch(Exception ex) {
            Log.error(ex.getMessage());
        }
        return null;
	}
	
	//Get random numeric characters
	public static String getRandomNumericString(int length)  {
	    try {
            String charset = "1234567890";
            charset = RandomStringUtils.random(length, charset.toCharArray());
            return charset;
        }catch(Exception ex){
	        Log.error(ex.getMessage());
        }
        return null;
	}

    //Get random numeric characters except 0
    public static String getRandomNumericStringExceptZero(int length)  {
        try {
            String charset = "123456789";
            charset = RandomStringUtils.random(length, charset.toCharArray());
            return charset;
        }catch(Exception ex){
            Log.error(ex.getMessage());
        }
        return null;
    }
	
	//Get random alphanumeric characters
	public static String getRandomAlphaNumericString(int length)  {
	    try {
            String charset = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
            charset = RandomStringUtils.random(length, charset.toCharArray());
            return charset;
        }catch(Exception ex){
	        Log.error(ex.getMessage());
        }
        return null;
	}
}
