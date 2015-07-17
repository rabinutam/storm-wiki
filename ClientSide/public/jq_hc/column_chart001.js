$(function () {
    $(document).ready(function () {
        Highcharts.setOptions({
            global: {
                useUTC: false
            }
        });

        $('#chart-container-001').highcharts({
            chart: {
				/* type options: column, bar, spline, area, line */
                type: 'column',
                animation: Highcharts.svg, // don't animate in old IE
                marginRight: 10,
                events: {
                    load: function () {

                        // set up the updating of the chart upon listening 'wiki:data' 
                        var series0 = this.series[0];
                        var series1 = this.series[1];
                        var series2 = this.series[2];
                        var series3 = this.series[3];
                        var series4 = this.series[4];
						var socket = io();
						socket.on('wiki:data', function(data){
							console.log('client::showing data')
							console.log(data)
                            series0.addPoint([data.time, data.en], true, true);
                            series1.addPoint([data.time, data.fr], true, true);
                            series2.addPoint([data.time, data.es], true, true);
                            series3.addPoint([data.time, data.de], true, true);
                            series4.addPoint([data.time, data.ru], true, true);
						});

						/*
                        // set up the updating of the chart each second
                        setInterval(function () {
                            var x = (new Date()).getTime(), // current time
                                y = Math.random();
                            series.addPoint([x, y], true, true);
                        }, 2000);
						*/
                    }
                }
            },
            title: {
                text: 'Wiki Recent Changes Count'
            },
			subtitle: {
				text: 'by Language'
			},
            xAxis: {
                title: {
                    text: 'Time'
                },
                type: 'datetime',
                tickPixelInterval: 500
            },
            yAxis: {
                title: {
                    text: 'Count'
                },
                plotLines: [{
                    value: 0,
                    width: 1,
                    color: '#808080'
                }]
            },
            tooltip: {
                formatter: function () {
                    return '<b>' + this.series.name + '</b><br/>' +
                        Highcharts.dateFormat('%Y-%m-%d %H:%M:%S', this.x) + '<br/>' +
                        Highcharts.numberFormat(this.y, 2);
                }
            },
            legend: {
                enabled: true
            },
            exporting: {
                enabled: true
            },
            series: [
			{
                name: 'english',
                data: (function () {
                    // generate an array of random data
                    var data = [],
                        time = (new Date()).getTime(),
                        i;

                    for (i = -9; i <= 0; i += 1) {
                        data.push({
                            x: time + i * 1000,
                            //y: Math.random()
                            y: 0.1
                        });
                    }
                    return data;
                }())
            },
			{
                name: 'french',
                data: (function () {
                    // generate an array of random data
                    var data = [],
                        time = (new Date()).getTime(),
                        i;

                    for (i = -9; i <= 0; i += 1) {
                        data.push({
                            x: time + i * 1000,
                            y: 0.1
                        });
                    }
                    return data;
                }())
            },
			{
                name: 'spanish',
                data: (function () {
                    // generate an array of random data
                    var data = [],
                        time = (new Date()).getTime(),
                        i;

                    for (i = -9; i <= 0; i += 1) {
                        data.push({
                            x: time + i * 1000,
                            y: 0.1
                        });
                    }
                    return data;
                }())
            },
			{
                name: 'German',
                data: (function () {
                    // generate an array of random data
                    var data = [],
                        time = (new Date()).getTime(),
                        i;

                    for (i = -9; i <= 0; i += 1) {
                        data.push({
                            x: time + i * 1000,
                            y: 0.1
                        });
                    }
                    return data;
                }())
            },
			{
                name: 'Russian',
                data: (function () {
                    // generate an array of random data
                    var data = [],
                        time = (new Date()).getTime(),
                        i;

                    for (i = -9; i <= 0; i += 1) {
                        data.push({
                            x: time + i * 1000,
                            y: 0.1
                        });
                    }
                    return data;
                }())
            }
			]
        });
    });
});
