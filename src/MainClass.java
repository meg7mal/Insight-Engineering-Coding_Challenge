import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import org.json.simple.parser.ParseException;
import com.google.common.collect.HashMultimap;
import java.time.*;


public class MainClass {
	
	
	public static LocalDateTime oldestTweet;
	public static LocalDateTime newestTweet;
	public static double running_hashtag_total;
	public static double running_degree_total;
	public static double running_mean;
	
	
	public static HashMultimap<LocalDateTime,GraphEdge>edgesCreatedAtMap=HashMultimap.create();
	public static HashMultimap<String,String>hashtagGraph=HashMultimap.create();
	public static HashMap<String,Integer>degreeMap=new HashMap<String,Integer>();
	public static HashMap<GraphEdge, LocalDateTime>edgeLatestTimestamp = new HashMap<GraphEdge, LocalDateTime>();
	
	public static void main(String[] args) throws IOException, ParseException, java.text.ParseException{
		
		FileWriter fw = new FileWriter(args[1]); // ./tweet_output/output.txt
		BufferedWriter bw = new BufferedWriter(fw);
	    BufferedReader br = null;
	    PrintWriter out = new PrintWriter(bw);
			try {
				br = new BufferedReader(new FileReader(args[0])); // ./tweet_input/tweets.txt
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
	  	 String line;
	  	// try {	
	  		 	while((line=br.readLine())!=null)
	  		 		{  
	  		 		 // try
	  		 		  
	  		 		 // {
	  		 			    //check for valid tweet (contains time or hashtags)
	  		 		
			  		 	    //for every line add a variable number of map entries
			  		 	    LocalDateTime created_at=ReadAndCleanTweet.getCreatedAt(line);
			  		 	    
			  		 	    
			  		 	    
			  		 	    String []hashTags=ReadAndCleanTweet.getHashTags(line);
			  		 	    
			  		 	    if(created_at!=null && hashTags.length>1)
			  		 	    { 
			  		 		//first edge 
			  		 	    if(edgesCreatedAtMap.size()==0)
			  		 		{
			  		 			oldestTweet=created_at;
			  		 			newestTweet=created_at;
			  		 			//running_hashtag_total=2;
			  		 			//running_degree_total=2;
			  		 			insertIntoDateEdgeGraph(created_at,hashTags);
			  		 			
			  		 			
			  		 		}
			  		 	    else //not empty map
			  		 	    { 
			  		 	    
				  		 	   if(created_at.isBefore(newestTweet)) //out of order
				  		 	   {
				  		 		   //tweet arrives out of order. Can insert only if it is less than a minute older.
				  		 		   long diff = Duration.between(created_at, newestTweet).getSeconds();
				  		 		   if(diff<=61)
				  		 			insertIntoDateEdgeGraph(created_at,hashTags);
				  		 			   
				  		 		   if(created_at.isBefore(oldestTweet))
				  		 				   oldestTweet=created_at;
				  		 			   
				  		 		   
				  		 		   
				  		 	   }
			  		 	    	
				  		 	   else //created_at>=newestTweet
				  		 	   {
				  		 		   
					  		 		long diff = Duration.between(newestTweet,created_at).getSeconds();
					  		 		if (diff==0)
					  		 			insertIntoDateEdgeGraph(created_at,hashTags);
					  		 		else 
					  		 		{
					  		 			if(diff>60)
					  		 			{
					  		 				
					  		 				edgesCreatedAtMap=HashMultimap.create();
					  		 				hashtagGraph=HashMultimap.create();
					  		 				degreeMap=null;
					  		 				degreeMap=new HashMap<String,Integer>();
					  		 				running_degree_total=0;
					  		 				running_hashtag_total=0;
					  		 				running_mean=0;
					  		 				oldestTweet=created_at;
						  		 			newestTweet=created_at;
					  		 				insertIntoDateEdgeGraph(created_at,hashTags);
					  		 			}
					  		 			else
					  		 			{   
					  		 				newestTweet=created_at;
					  		 				evictOldNodes();
					  		 				insertIntoDateEdgeGraph(created_at,hashTags);
					  		 			}
					  		 		}
				  		 		
				  		 	   }
			  		 	    
			  		 	    
			  		 			
			  		 		
				  		 	
			  		 				  
			  		 			 
			  		 	    }
			  		 	 if(running_hashtag_total>0)
			  		 	 		 running_mean= running_degree_total/running_hashtag_total;
			  		 	 out.println(String.format("%.2f", running_mean));
	  		 		     }
			  		 	    
			  		 	 else if(created_at!=null && hashTags.length<=1)  //only 0 or 1 hashtags in tweet. We do nothing for tweets that have {"limit": etc
			  		 	{  
//			  		 		if(running_hashtag_total!=0)
//			  		 		{	
//			  		 		running_mean=running_degree_total/running_hashtag_total;
//					    	out.println("Running mean "+running_mean);
//			  		 		}
			  		 		out.println(String.format("%.2f", running_mean)); 
			  		 	}   
	  		 		  
	  		 		  //}
	  		 		 /* catch (NullPointerException e) //for abnormal tweets
	  		 		  {
	  		 			 out.println("Abnormal tweet");
	  		 			 continue;
	  		 		  }*/
	  		 		 
	  		 		  
	  		 		}
	  		 	
	  	 	//} /*catch (Exception e) {
				// TODO Auto-generated catch block
				//out.println(e +"handle this");
			//}*/
	  	 
	      
	  		 	out.close();	
	  		 	
	  		 	
	
} // main
	

	
public static void insertIntoDateEdgeGraph(LocalDateTime created_at, String []hashTags) throws ParseException, java.text.ParseException{
	// Get hashtag array and add to map
	
	
		for (int x=0; x<hashTags.length;x++)
 		 {
 			 for( int y=x+1; y<hashTags.length; y++)
 			 {
 				Map.Entry<LocalDateTime, GraphEdge> edgeAddedAt=ReadAndCleanTweet.edgeAddedAt(created_at,hashTags[x],hashTags[y]);
 				edgesCreatedAtMap.put(edgeAddedAt.getKey(),edgeAddedAt.getValue());
 				//Record the timestamp for the edge. Make sure to update the latest timestamp for a hashtag pair or edge if it is already in the graph.
 				if(!edgeLatestTimestamp.containsKey(edgeAddedAt.getValue()) || 
 					(edgeLatestTimestamp.containsKey(edgeAddedAt.getValue())&&!created_at.isBefore(edgeLatestTimestamp.get(edgeAddedAt.getValue()))))
 					
 					edgeLatestTimestamp.put(edgeAddedAt.getValue(),edgeAddedAt.getKey()); //edge (value) is now the key and timestamp (key) is now the value in this map.
 				
 				insertIntoHashTagGraph(hashTags[x],hashTags[y]);
 			 }
 		 }
		
	
}



public static void insertIntoHashTagGraph(String v1, String v2)
{
	
	if(!(hashtagGraph.containsEntry(v1, v2)))
	{
		hashtagGraph.put(v1, v2);
		Integer new_degree_of_v1=hashtagGraph.get(v1).size();  //  degreeMap.get(v1)+1;
		degreeMap.put(v1,new_degree_of_v1);
		running_degree_total+=1;
	}
	
	if(!(hashtagGraph.containsEntry(v2, v1)))
	{
		hashtagGraph.put(v2, v1);
		Integer new_degree_of_v2=hashtagGraph.get(v2).size();
		degreeMap.put(v2,new_degree_of_v2);
		running_degree_total+=1;
	}
	
	running_hashtag_total=degreeMap.size();
	
	
	
	
}

public static void evictOldNodes()
{
   
	LocalDateTime oldestCopy=oldestTweet;
	LocalDateTime newestCopy=newestTweet;
	
	LocalDateTime latestExpired= newestCopy.minusSeconds(61);
	
	LocalDateTime oldestPossibleAllowedTweet=latestExpired.plusSeconds(1); 
	
	long delta = Duration.between(oldestPossibleAllowedTweet, newestTweet).getSeconds();
	
	for(long x=0; x<=delta; x++)
	{
		if(edgesCreatedAtMap.containsKey(oldestPossibleAllowedTweet.plusSeconds(x))!=false)
		{
			oldestTweet=oldestPossibleAllowedTweet.minusSeconds(x);
			break;
		}
	}
	
	long diff=Duration.between(oldestCopy, latestExpired).getSeconds();
	
	
		
	
	for(long i=0; i<=diff; i++)
	{
		//get edges
		 LocalDateTime currentDateTime=oldestCopy.plusSeconds(i);
		 if(edgesCreatedAtMap.get(currentDateTime)!=null)
		 {	 
			 int number_of_edges=edgesCreatedAtMap.get(currentDateTime).size();
			 if (number_of_edges>0)
			 {
				 GraphEdge [] edges_to_delete= edgesCreatedAtMap.get(oldestCopy.plusSeconds(i)).toArray(new GraphEdge[number_of_edges-1]);
				 for (int x=0; x<edges_to_delete.length; x++)
				 {
					 String v1=edges_to_delete[x].nodes[0];
					 String v2=edges_to_delete[x].nodes[1];
					 
					 //remove edges for an expired timestamp. If an edge was added later again in a non-expired tweet, do not remove it.
					 LocalDateTime latest_timestamp_of_edge=edgeLatestTimestamp.get(edges_to_delete[x]);
					 if(oldestCopy.plusSeconds(i).isEqual(latest_timestamp_of_edge))
					 {
						 hashtagGraph.remove(v1, v2);
						 hashtagGraph.remove(v2, v1);
						 
						 
						 //update degrees
						 Integer new_degree_of_v1=hashtagGraph.get(v1).size();//degreeMap.get(v1)-1;  //  
						 running_degree_total-=1;
						 if(new_degree_of_v1==0)
						 {
							 degreeMap.remove(v1);
						 }
						 else
						 {  
							 degreeMap.put(v1,new_degree_of_v1);
						 }
						 
						 Integer new_degree_of_v2=hashtagGraph.get(v1).size();//degreeMap.get(v2)-1;
						 running_degree_total-=1;
						 if(new_degree_of_v2==0)
						 {
							 degreeMap.remove(v2);
						 }
						 else
						 {
							 degreeMap.put(v2,new_degree_of_v2);
						 }
						 running_hashtag_total=degreeMap.size();
					 }
					 
					 
				 
				 }
			   
			 }
			//remove edges from edgesCreatedAtMap
			 
			 edgesCreatedAtMap.removeAll(currentDateTime);
			 if(edgesCreatedAtMap.size()==0)
				 System.out.println(newestTweet+" "+oldestTweet);
		 }
  }
}



}
