$(function () {

    // Prepare demo data
	
	// Top 12 Countries by number of Internet Users
	var countries = {br: 'Brazil', cn: 'China', eg: 'Egypt',
					fr: 'France', de: 'Germany', in: 'India',
					jp: 'Japan', mx: 'Mexico', ng: 'Nigeria',
					ru: 'Russia', gb: 'United Kingdom', us: 'United States'
					};
	/*
	var data = [];
	for (co in countries) {
		data.push({'hc-key': co, value: 0});
	};
	*/
	var data = [];


    // Initiate the chart
    $('#container-geo-000').highcharts('Map', {
		chart: {
                // animation: Highcharts.svg, // don't animate in old IE
                // marginRight: 10,
                events: {
                    load: function () {

                        // set up the updating of the chart upon listening 'wiki:data' 
						// Refer: https://en.wikipedia.org/wiki/ISO_3166-2
                        var series0 = this.series[0];
                        var socket = io();
                        socket.on('wiki:data1', function(data){
                            console.log('client::showing data')
                            console.log(data)
							series0.data = []; //not sure if it periodically flushes
							
							for (co in countries) {
								if (countries[co] in data) {
									console.log(co + ' , ' + data[countries[co]]);
									series0.addPoint({'hc-key': co, value: data[countries[co]]});
								};
							};

                        });

                    }
                }
            },

        title: {
            text: 'Wiki Anonymous Users Count'
        },

        subtitle: {
            text: 'by Country'
        },

        mapNavigation: {
            enabled: true,
			enableMouseWheelZoom: false,
            buttonOptions: {
                verticalAlign: 'bottom'
            }
        },

        colorAxis: {
            min: 0
        },

        series: [{
            data: data,
            // mapData: Highcharts.maps['custom/north-america-no-central'],
            // mapData: Highcharts.maps['custom/world-highres'],
            mapData: Highcharts.maps['custom/world'],
            joinBy: 'hc-key',
            allAreas: true,
            name: 'New Users Count',
            states: {
                hover: {
                    color: '#BADA55'
                }
            },
            dataLabels: {
                enabled: true,
                format: '{point.name}'
            }
        }]
    });
});
