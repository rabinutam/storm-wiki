package emse.rts.summer_2015.wiki_changes;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.StormSubmitter;
import backtype.storm.topology.TopologyBuilder;
import backtype.storm.tuple.Fields;

public class WikiRecentChangesTopology {

	public static void main(String[] args) throws Exception {
		// create the topology
		TopologyBuilder builder = new TopologyBuilder();

		// attach the word spout to the topology - parallelism of 10
		builder.setSpout("wikichanges_spout", new RecentChangesSpout(), 10);

		// Attach the wiki change parse bolt
		builder.setBolt("wikichangescountbolt", new WikiChangesCountBolt(), 10)
				.fieldsGrouping("wikichanges_spout", "serverstream",
						new Fields("servername"));

		// Attach geolocation count bolt
		builder.setBolt("geolocationcountbolt", new GeoLocationCountBolt(), 1)
				.fieldsGrouping("wikichanges_spout", "ipstream",
						new Fields("ipaddress"));

		// Attach the persistor bolt to wikichanges count as well as geolocation
		builder.setBolt("wikichangepublishbolt", new WikiChangePublishBolt())
				.globalGrouping("wikichangescountbolt")
				.globalGrouping("geolocationcountbolt");

		// create the default config object
		Config conf = new Config();

		// set the config in debugging mode
		conf.setDebug(true);

		if (args != null && args.length > 0) {

			// run it in a live cluster

			// set the number of workers for running all spout and bolt tasks
			conf.setNumWorkers(3);

			// create the topology and submit with config
			StormSubmitter.submitTopology(args[0], conf,
					builder.createTopology());

		} else {

			// create the local cluster instance
			LocalCluster cluster = new LocalCluster();

			// submit the topology to the local cluster
			cluster.submitTopology("wiki_recent_changes", conf,
					builder.createTopology());

			// let the topology run for 20 seconds. note topologies never
			// terminate!
//			Thread.sleep(20000);
//
//			// kill the topology
//			cluster.killTopology("wiki_recent_changes");
//
//			// we are done, so shutdown the local cluster
//			cluster.shutdown();
		}

	}

}
