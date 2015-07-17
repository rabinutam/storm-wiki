package emse.rts.summer_2015.wiki_changes;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import backtype.storm.Config;
import backtype.storm.Constants;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseBasicBolt;
import backtype.storm.tuple.Tuple;

import com.lambdaworks.redis.RedisClient;
import com.lambdaworks.redis.RedisConnection;

@SuppressWarnings("serial")
public class WikiChangePublishBolt extends BaseBasicBolt {

	// place holder to keep the connection to redis
	transient RedisConnection<String, String> redis;

	// Count map
	Map<String, Integer> countMap;

	// Count map for countries
	Map<String, Integer> countryCountMap;

	private boolean isTickTuple(Tuple tuple) {
		String sourceComponent = tuple.getSourceComponent();
		String sourceStreamId = tuple.getSourceStreamId();

		return sourceComponent.equals(Constants.SYSTEM_COMPONENT_ID)
				&& sourceStreamId.equals(Constants.SYSTEM_TICK_STREAM_ID);
	}

	@Override
	public Map<String, Object> getComponentConfiguration() {
		Config conf = new Config();
		conf.put(Config.TOPOLOGY_TICK_TUPLE_FREQ_SECS, 5);
		return conf;
	}

	@Override
	public void prepare(Map stormConf, TopologyContext context) {
		// instantiate a redis connection
		RedisClient client = new RedisClient("localhost", 6379);

		// initiate the actual connection
		redis = client.connect();

		// Initiate the map
		countMap = new HashMap<String, Integer>();

		// Initialize country count map
		countryCountMap = new HashMap<String, Integer>();
	}

	@Override
	public void execute(Tuple tuple, BasicOutputCollector outputCollector) {
		if (isTickTuple(tuple)) {
//			String output = "";

			// Get iterator to count map
			Iterator<String> itr = countMap.keySet().iterator();

			// Collect the counts accumulated for 10 secs
			JSONObject jsonChanges = new JSONObject();
			
			while (itr.hasNext()) {
				String servername = itr.next();
//				output += servername + " : " + countMap.get(servername) + ",";
				try {
					jsonChanges.put(servername, countMap.get(servername));
				} catch (JSONException e) {
					e.printStackTrace();
				}
				itr.remove();
			}

			// publish to channel wikichangestopology
			redis.publish("wikichangestopology", jsonChanges.toString());
//			redis.publish("wikichangestopology",
//					"***********************************************************");

			// Get iterator to count map
			Iterator<String> itrCountry = countryCountMap.keySet().iterator();

			// Collect the counts accumulated for 10 secs
			JSONObject jsonCountries = new JSONObject();
			
			while (itrCountry.hasNext()) {
				String countryname = itrCountry.next();
				try {
					jsonCountries.put(countryname, countryCountMap.get(countryname));
				} catch (JSONException e) {
					e.printStackTrace();
				}
//				countryOutput += countryname + " : "
//						+ countryCountMap.get(countryname) + ",";
				itrCountry.remove();
			}

			// publish to channel wikichangespercountry
			if (jsonCountries.length() > 0) {
				redis.publish("wikichangespercountry", jsonCountries.toString());
//				redis.publish("wikichangespercountry",
//						"--------------------------------------------------------------");
			}
		} else {
			System.out.println("Source Component - "
					+ tuple.getSourceComponent());

			if (tuple.getSourceComponent().equalsIgnoreCase(
					"wikichangescountbolt")) {
				String serverName = tuple.getStringByField("servername");
				Integer updatecount = tuple.getIntegerByField("updatecount");

				// Accumulate the update count for 10 sec
				countMap.put(serverName, updatecount);

			} else if (tuple.getSourceComponent().equalsIgnoreCase(
					"geolocationcountbolt")) {

				String countryName = tuple.getStringByField("countryname");
				Integer count = tuple.getIntegerByField("count");

				// Accumulate the update count for 10 sec
				countryCountMap.put(countryName, count);
			}
		}
	}

	@Override
	public void cleanup() {
		redis.close();
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		// No need to declare any output
	}

}
