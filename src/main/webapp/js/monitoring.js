var monitored_metric = "cpuTotal";
function getCurrValue(series) {
    $.ajax({
        type: "POST",
        url: "http://109.231.122.80:8888/JCatascopia-Web/restAPI/metrics/",
        data: {
            metrics: monitored_metric
            // metrics : "0d17b927149c4021a1e8362e7079ecbd:netBytesIN"
        },
        async: true,
        success: function (data) {
            if (data === undefined || data === null || data.metrics[0] === null)
                return;

            var x = (new Date()).getTime(); // current time
            try {
                series.addPoint([x, parseFloat(data.metrics[0].value)], true, true);
            } catch (e) {
                //   console.log("YO",e);
            }


        },
        error: function (xhr, textStatus, errorMessage) {
            alert(errorMessage);
        }
    });
}

function initMonitoringGraph() {
    try {
        $("div#monitoring_container").show();
        monitored_metric = $("select#available_monitoring_metrics").find('option:selected').val();
        Highcharts.setOptions({
            global: {
                useUTC: false
            }
        });

        Highcharts.chart('monitoring_container', {
            chart: {
                type: 'spline',
                animation: Highcharts.svg, // don't animate in old IE
                marginRight: 10,
                events: {
                    load: function () {
                        // set up the updating of the chart each second
                        var series = this.series[0];
                        setInterval(function () {
                            getCurrValue(series);
                        }, 2000);
                    }
                }
            },
            title: {
                text: 'Monitoring data: ' + monitored_metric
            },
            xAxis: {
                type: 'datetime',
                tickPixelInterval: 150
            },
            yAxis: {
                title: {
                    text: 'Value'
                },
                plotLines: [{
                    value: 0,
                    width: 3,
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
                enabled: false
            },
            exporting: {
                enabled: false
            },
            series: [{
                name: 'Random data',
                data: (function () {
                    // generate an array of random data
                    var data = [],
                        time = (new Date()).getTime(),
                        i;

                    for (i = -19; i <= 0; i += 1) {
                        data.push({
                            x: time + i * 1000,
                            y: 0
                        });
                    }
                    return data;
                }())
            }]
        });
    } catch (e) {
        console.log(e);
    }
}

function stopPod() {
}

function stopAllPods() {
}
