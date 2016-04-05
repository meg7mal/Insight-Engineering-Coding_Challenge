import org.json.simple.JSONObject;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.AbstractMap;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import org.json.simple.JSONArray;
import org.json.simple.parser.ParseException;
import org.json.simple.parser.JSONParser;



public class ReadAndCleanTweet {
	
	public static JSONParser parser = new JSONParser();
	

 
 public static LocalDateTime getTwitterDate(String date) throws ParseException, java.text.ParseException{

	  final String TWITTER="EEE MMM dd HH:mm:ss ZZZZZ yyyy";
	  SimpleDateFormat sf = new SimpleDateFormat(TWITTER);
	  sf.setLenient(true);
	  Date d= sf.parse(date);
	  Instant now = d.toInstant();
      ZoneId currentZone = ZoneId.systemDefault();
		
      LocalDateTime localDateTime = LocalDateTime.ofInstant(now,currentZone);
      return localDateTime;
	  }

 public static LocalDateTime getCreatedAt(String wholeTweet) throws ParseException, java.text.ParseException
 {
	 JSONObject json = (JSONObject) parser.parse(wholeTweet);
	 if(json.get("created_at")!=null)
	 {	 	 
		 LocalDateTime tweetDate= getTwitterDate(json.get("created_at").toString());
		 return tweetDate;
	 }
	 return null;
 }
 
 public static String [] getHashTags (String wholeTweet) throws ParseException
 {
	 JSONObject json = (JSONObject) parser.parse(wholeTweet);
		 
		 
		 
		 
		 JSONObject tweetEntities = (JSONObject) json.get("entities");
		 if(tweetEntities!=null)
		 {
		 JSONArray hashtags = (JSONArray)tweetEntities.get("hashtags");
		 HashSet<String> uniqueHashtags = new HashSet<String>();
		 
			 for (int x = 0 ; x < hashtags.size(); x++) {
					JSONObject obj = (JSONObject) hashtags.get(x);
					//System.out.println(obj.get("text"));
					uniqueHashtags.add(obj.get("text").toString());
				}
			 String [] temp = uniqueHashtags.toArray(new String[uniqueHashtags.size()]);
			 return temp;
		 }
		 return null;
		 
 }
 
 public static Map.Entry<LocalDateTime, GraphEdge> edgeAddedAt (LocalDateTime created_at, String hashtag1, String hashTag2) throws ParseException, java.text.ParseException
 {
	 
	 
	 
		
	 GraphEdge edge= new GraphEdge(hashtag1, hashTag2);
		
	 Map.Entry<LocalDateTime, GraphEdge> edgeCreatedAt = new AbstractMap.SimpleEntry<LocalDateTime, GraphEdge>(created_at, edge);
	 return edgeCreatedAt;	     
				
		
 }
 
}
