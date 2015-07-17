package emse.rts.summer_2015.wiki_changes;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import backtype.storm.Config;
import backtype.storm.Constants;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseBasicBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;

@SuppressWarnings("serial")
public class GeoLocationCountBolt extends BaseBasicBolt {

	// Map to maintain count of each server name
	private Map<String, Integer> countCountryName;

	

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

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * backtype.storm.topology.IBasicBolt#execute(backtype.storm.tuple.Tuple,
	 * backtype.storm.topology.BasicOutputCollector)
	 */
	@Override
	public void execute(Tuple tuple, BasicOutputCollector collector) {
		if (isTickTuple(tuple)) {
			countCountryName.clear();
		} else {

			// Get IP address
			String ipAddress = tuple.getStringByField("ipaddress");

			// HTTP client
			CloseableHttpClient httpclient = HttpClients.createDefault();
			
			try {
				// Create a custom response handler
				ResponseHandler<String> responseHandler = new ResponseHandler<String>() {

					@Override
					public String handleResponse(final HttpResponse response)
							throws ClientProtocolException, IOException {
						int status = response.getStatusLine().getStatusCode();
						if (status >= 200 && status < 300) {
							HttpEntity entity = response.getEntity();
							return entity != null ? EntityUtils.toString(entity) : null;
						} else {
							throw new ClientProtocolException(
									"Unexpected response status: " + status);
						}
					}

				};
				
				HttpGet httpget = new HttpGet("http://freegeoip.net/json/"
						+ ipAddress);

				String responseBody = httpclient.execute(httpget,
						responseHandler);
				System.out.println("----------------------------------------");
				System.out.println(responseBody);

				// Parse JSON
				JSONObject json = new JSONObject(responseBody);
				String countryName = json.getString("country_name");

				// Add the country name to the count map
				if (countCountryName.get(countryName) == null)
					countCountryName.put(countryName, 1);
				else
					countCountryName.put(countryName,
							(countCountryName.get(countryName) + 1));

				// Emit tuple
				collector.emit(new Values(countryName, countCountryName
						.get(countryName)));

			} catch (Exception e) {
				e.printStackTrace();
			}finally {
				try {
					httpclient.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public void cleanup() {
	}

	@Override
	public void prepare(Map stormConf, TopologyContext context) {
		// Initialize country count map
		countCountryName = new HashMap<String, Integer>();
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields("countryname", "count"));
	}

}
