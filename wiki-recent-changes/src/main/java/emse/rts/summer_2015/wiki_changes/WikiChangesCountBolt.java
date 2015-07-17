package emse.rts.summer_2015.wiki_changes;

import java.util.HashMap;
import java.util.Map;

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
public class WikiChangesCountBolt extends BaseBasicBolt {

	// Map to maintain count of each server name
	private Map<String, Integer> countServerName;

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
	public void prepare(Map connfig, TopologyContext context) {
		countServerName = new HashMap<String, Integer>();
	}

	@Override
	public void execute(Tuple tuple, BasicOutputCollector outputCollector) {
		if (isTickTuple(tuple)) {
			countServerName.clear();
		} else {
			String server_name = tuple.getStringByField("servername");

			// Add the server name to the count map
			if (countServerName.get(server_name) == null)
				countServerName.put(server_name, 1);
			else
				countServerName.put(server_name,
						(countServerName.get(server_name) + 1));

			// Emit the output
			outputCollector.emit(new Values(server_name, countServerName
					.get(server_name)));
		}
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields("servername", "updatecount"));
	}
}
