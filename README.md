**Insight Engineering Coding Challenge**

I have chosen Java 8 (jdk1.8.0_77) because it provides, among other things, a simple 
library to deal with dates and times called java.time (based on Joda time).

I have also chosen to use two useful libraries from .jar files (need to be included in classpath)
* guava-19.0.jar : Primarily for the class com.google.common.collect.HashMultimap which provides a convenient Java Collection
				   to store our global data structures.
* json.simple.1.1.1.jar : Primarily for the simple JSON parser provided which we can use to extract the "created_at", "entities"
                          "hashtags" fields from the input file for processing.

**Files**

* ./src/ReadAndCleanTweet.java
* ./src/GraphEdge.java
* ./src/MainClass.java  - Get average degree of all hashtags and write to graph.

**Running the project**
Please make sure the JAVA_HOME and PATH variables are set.
run.sh contains both the compiling (javac) and executing (java) commands.
If the compilation fails using the script, please execute both lines of the script separately from
the command line.

In order to make the tests in run_tests.sh pass, the class files and jar files were placed in the src folder.

						  
**Global Data Structures**


* HashMultimap (LocalDateTime,GraphEdge) edgesCreatedAtMap - A map of the "created_at" timestamp to every unique hashtag edge (pair of hashtags)
														   added at that timestamp (could be shared by multiple tweets).
														   key="created_at" from JSON tweet value=All edges from all tweets added at this instatnt.
														   
* HashMultimap (String,String) hashtagGraph -  The hashtag graph is implemented as a HashMultimap. The key is a hashtag and the value will be a set of all the hashtags it is connected to in the graph. 
										     
                         Ex: key=h1 value=[h2,h3,h4]
										     However interally, the pairs are stored as:
										     * key=h1 value=h2
										     * key=h1 value=h3
										     * key=h1 value=h4
										   
										     This allows us to insert one edge (hashtag pair) at a time into the graph rather than have to collect
										     all connected hashtags a particular hashtag is connected to in a set.

* HashMap (String,Integer) degreeMap        -  This is a map of a hashtag to its degree count. By querying the size of this map, we also get the
										     running_hashtag_total.

* oldestTweet - The timestamp of the oldest tweet
* newestTweet - The timestamp of the newest tweet which the current tweet's "created_at" will be compared to.
* running_hashtag_total - This is the total number of hashtag nodes in the graph at any given time.
* running_degree_total - This is the totatl of the degrees of all nodes in the graph at any given time.
* double running_mean - running_degree_total/running_hashtag_total when running_hashtag_total>0.

**Optimizations**

We use the dynamic programming technique to keep track of the degrees of every hashtag as edeges are added or removed. 

We also keep a running_degree_total and increment it or decrement it only when a hashtag's degree is updated.
Similary, we keep a running_hashtag_total.

We also keep a running_mean which is given by running_degree_total/running_hashtag_total.

This avoids having to keep counting the number of hashtags in the graph and all their connections to get the ** average degree (running_mean) **
as the graph grows.

We also optimize the eviction algorithm.

When a new tweet arrives and eviction becomes necessary, we just count forward by 1 second from oldest to the latest or newest expired timestamp.

This allows us to use a HashMultimap to store timestamps and edges without having to maintain the order for tweets arriving out of order.

By counting forward from oldest to latest or newest expired timestamp, we make sure no obsolete hashtag edges are in the hashtag graph.

In the worst case, most timestamps **in betweeen* oldest and the latest or newest expired timestamp will not have entries. But this will result in
at most 57 wasted lookups. 

Suppose the newest timestamp in our map is t.

Suppose a tweet arrives at t+61.

The oldest timestamp allowed to remain in the graph is t+61-60=t+1 which doesn't exist. 

Hence all maps,graphs and counters are cleared in this case. This ensures that only tweets in a 60 second sliding window are recorded.

This means the oldest timestamp will never be BEFORE t-60.

Let's look at the worst case.

The newest timestamp is t and the oldest timestamp is t-60.

Now suppose a tweet arrives at t+60.

The oldest timestamp allowed to remain in the graph is t+60-60=t which does exist.

The latest timestamp considered to be expired therefore t-1.

We will have to count up from t-60 to t-1 and do t-1-(t-60)=59 lookups.

However since t-60 and t are guaranteed to exist, AT MOST 59-2=57 wasted lookups will happen for every eviction, ie O(57)

Since we are using a HashMultimap, 57 failed lookups will be extremeley fast. This is a good trade-off for not keeping the timestamp edge map sorted by timestamp during insertion which would be O(log n) where n is the number of tweets.

**Pseudocode**

For every line in the input file tweets.txt
	
	Call methods `getCreatedAt` and `getHashTags` to get the timestamp and hashtags in a tweet.
	
	Check for tweet validity (Make sure we only process tweets that are formatted with the fields "created_at", "enitites" and "hashtags")
	
	**if valid**
			
			Check if graph is empty
			
			**if graph is empty** 
			
				oldestTweet=newestTweet=current timestamp
				
			**else**
			
			    **if current timestamp is **before** newestTweet, ie it is out of order
					
					**if newestTweet-current timestatmp<61 seconds (the out of order tweet is not more than a minute older than the newest timestamp)
					
						do an insertion
			
				
		
				**else** (current timestamp is **at** or **after** newest)
				
				    **if AT**
						
						do an insertion
					
					**else**
						
						**if more thatn a minute AFTER ** (arrival of the new tweet makes ALL others stale in this case)
						
							Clear all global data structures and re-initialzie running_mean, running_hashtag_total and running_degree_total
							The garbage collector will eventually de-allocate the memory used by the hashtagGraph and edgesCreatedAtMap.
							
						**else** (current timestatmp is less than a miunte older)
						
							newestTweet=current timestamp (update newest tweet)
							do an insertion
							do evictions if needed
							
	
	Finally, if the running_hashtag_count>0 update the running_mean and write to the file.
	
	else if tweet has less than 2 hashtags, write  the previous ruunning_mean to the file. 
	
	
	
	
**Insertion Pseudocode**

  * **function: insertIntoDateEdgeGraph**
  * **Parameters:  the current timestamp, the unique hashtags in the current tweet as an array**
  * **Return:void**

  For every unique pair of DIFFERENT hashtags 
	Insert the pair into the edgesCreatedAtMap
		If pair doesn't already exist in the graph, add the edge and timestamp to edgeLatestTimestamp
		else update the existing timestamp in edgeLatestTimestamp (if it HAS NOT arrived out of order)
	call insertIntoHashTagGraph
	
  * **function: insertIntoHashTagGraph**
  * **Parameters: A pair of hashtag strings representing a graph edge (v1,v2)**
  * **Return: void**

  If graph does NOT contain (v1,v2) already
	put the edge in hashtagGraph
	update the degree of v1 in degreeMap
	update running_degree_total by 1
	
  If graph does not contain (v2,v1) already
	put the edge in hashtagGraph
	update the degree of v2 in degreeMap
	update running_degree_total by 1	
	
  update running_hashtag_total if necessary from size of degreeMap
  
**Eviction Pseudocode**

* **function: evictOldNodes**
* **Parameters: none**
* **Return: void**
  
  latest expired timestamp = newestTweet - 61
  
  oldestTweetCopy = oldestTweet 
  
  oldestPossibleAllowedTweet = latest expired timestamp + 1 
  
delta= number of seconds between oldestPossibleAllowedTweet and newewstTweet
  
**for(i=0; i<=delta; i++)
	if oldestPossibleAllowedTweet+i exists in edgesCreatedAtMap
            
		oldestTweet=oldestPossibleAllowedTweet+i
        	
		break out of loop (This ensures the oldesTweet corresponds to the oldest allowed timestamp PRESENT rather than the oldest ALLOWED timestamp)

  end for
  
  diff = number of seconds between latest expired timestamp and oldestTweet
  
  **for(i=0; i<=diff; i increments by seconds)
	
		**For all edges in edgesCreatedAtMap for the timestamp at oldestTweet +i seconds
		
		if there is at least one edge (nodes[0],nodes[1])   (An edge is represented by wrapper class GraphEdge which has a 			"nodes" array of size 2. )
		
			if(timestamp at oldestTweet +i seconds is equal to the latest timestamp for the edge v1,v2) 
				hashtagGraph.remove(v1,v2)
				hashtagGraph.remove(v2,v1)  (We make sure that array [v1,v2] removes v2 from v1's connected hashtags 					and vice versa)
		
				
				update degrees of both v1 and v2 in degreeMap if needed.
			
				update running_degree_total accordingly.
			
			if v1's degree becomes 0, remove the entry from degree map. (If all the edges containing a hashtag are removed 			from hashtag graph, the vertex is autmatically deleted so there is no need to take care of it explicitly. However, 				since degreeMap is keeping track of degrees to avoid re-calculation by counting hashtags every time, we 				have to update it.)
			
			
			
			Similarly, if v2's degree becomes 0, remove the entry from degree map. 
			
			update running_hashtag_total accordingly
			
			
  
    	Delete entry for oldestTweetCopy +i second (if present)    
  
    

