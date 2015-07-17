package emse.rts.summer_2015.wiki_changes;

import io.socket.IOAcknowledge;
import io.socket.IOCallback;
import io.socket.SocketIO;
import io.socket.SocketIOException;

import java.net.MalformedURLException;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;

import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichSpout;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;
import backtype.storm.utils.Utils;

@SuppressWarnings("serial")
public class RecentChangesSpout extends BaseRichSpout {

	SpoutOutputCollector _collector;
	LinkedBlockingQueue<String> queue = null;
	LinkedBlockingQueue<String> ipQueue = null;
	SocketIO socket;

	Pattern VALID_IPV4_PATTERN = null;
	Pattern VALID_IPV6_PATTERN = null;
	final String ipv4Pattern = "(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])";
	final String ipv6Pattern = "([0-9a-f]{1,4}:){7}([0-9a-f]){1,4}";

	@Override
	public void nextTuple() {
		String serverName = queue.poll();

		if (serverName == null || serverName.isEmpty()) {
			Utils.sleep(50);
		} else {
			_collector.emit("serverstream", new Values(serverName));
		}

		String ipAddress = ipQueue.poll();

		if (ipAddress == null || ipAddress.isEmpty()) {
			Utils.sleep(50);
		} else {
			_collector.emit("ipstream", new Values(ipAddress));
		}
	}

	@Override
	public void open(Map configMap, TopologyContext context,
			SpoutOutputCollector outputCollector) {

		this._collector = outputCollector;

		// Compile the regex pattern
		VALID_IPV4_PATTERN = Pattern.compile(ipv4Pattern,
				Pattern.CASE_INSENSITIVE);
		VALID_IPV6_PATTERN = Pattern.compile(ipv6Pattern,
				Pattern.CASE_INSENSITIVE);

		// Initiate the queue
		queue = new LinkedBlockingQueue<String>();
		ipQueue = new LinkedBlockingQueue<String>();

		try {
			socket = new SocketIO("http://stream.wikimedia.org/rc");
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Connect to the socket
		socket.connect(new IOCallback() {

			public void onMessage(JSONObject arg0, IOAcknowledge arg1) {

			}

			public void onMessage(String arg0, IOAcknowledge arg1) {

			}

			public void onError(SocketIOException arg0) {

			}

			public void onDisconnect() {

			}

			public void onConnect() {
				// System.out.println("Connected to WIKI socket");
				socket.emit("subscribe", "*");
			}

			public void on(String event, IOAcknowledge arg1, Object... arg2) {

				// Get JSON object from response
				JSONObject json = (JSONObject) arg2[0];

				// Get the value of "bot" field to make sure it's a change
				// made by human
				boolean isBotChange;
				try {
					isBotChange = json.getBoolean("bot");

					if (!isBotChange) {
						// Get the server name
						String serverName = json.getString("server_name");

						// Get the user
						String userName = json.getString("user");

						// Check if user name is "IP"
						if (isIPAddress(userName)) {
							ipQueue.offer(userName);
						}

						// Enqueue the server name
						queue.offer(serverName);
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
	}

	private boolean isIPAddress(String username) {

		Matcher m1 = VALID_IPV4_PATTERN.matcher(username);

		if (m1.matches()) {
			return true;
		}

		Matcher m2 = VALID_IPV6_PATTERN.matcher(username);
		return m2.matches();
	}

	@Override
	public void close() {
		if (socket != null && socket.isConnected()) {
			socket.disconnect();
		}
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declareStream("serverstream", new Fields("servername"));
		declarer.declareStream("ipstream", new Fields("ipaddress"));
	}
}
