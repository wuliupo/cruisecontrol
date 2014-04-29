<%@page contentType="text/html; charset=utf-8" errorPage="/error.jsp" import="org.w3c.dom.*,javax.xml.parsers.*,java.io.*" %>
<!DOCTYPE html>
<html>
<head>
<title>Maven Modules Cost Time</title>
<link rel="stylesheet" type="text/css" href="./jquery.jqplot.css" />
<style>
.action {float:right; margin-right: 20px; z-index:1000px;}
.chart {clear:both;}
.jqplot-cursor2-tooltip {z-index: 2000; font-size:14px; font-weight:bold; background-color:#FFF; padding: 5px 10px; border-radius: 10px; border: 1px solid #888}
</style>
</head>
<body>
<script type="text/javascript" src="./jquery.min.js"></script>
<script type="text/javascript" src="./jquery.jqplot.js"></script>
<script type="text/javascript" src="./jqplot.dateAxisRenderer.js"></script>
<script type="text/javascript" src="./jqplot.logAxisRenderer.js"></script>
<script type="text/javascript" src="./jqplot.canvasTextRenderer.js"></script>
<script type="text/javascript" src="./jqplot.canvasAxisLabelRenderer.js"></script>
<script type="text/javascript" src="./jqplot.canvasAxisTickRenderer.js"></script>
<script type="text/javascript" src="./jqplot.categoryAxisRenderer.js"></script>
<script type="text/javascript" src="./jqplot.barRenderer.js"></script>
<script type="text/javascript" src="./jqplot.cursor2.js"></script>
<script type="text/javascript" src="./jqplot.pointLabels.js"></script>

<script type="text/javascript">
$(document).ready(function(){
var logs = [
<%
String log = request.getParameter("log");
try {
	File file = new File("cache" + log + "-detail.txt");
	if(file.exists()){
		BufferedReader bReader = new BufferedReader(new FileReader(file));
		String line = null;
		StringBuffer buffer = new StringBuffer();
		while ((line = bReader.readLine()) != null) {
			buffer.append(line);
		}
		out.print(buffer.toString().trim());
	} else {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document doc = db.parse(log + ".xml");

		Node node = doc.getLastChild();     //cruisecontrol
		NodeList nl = node.getChildNodes();

		node = nl.item(nl.getLength() - 2);     //build
		nl = node.getChildNodes();

		node = nl.item(nl.getLength() - 2);     //mavengoal
		nl = node.getChildNodes();

		String content;
		StringBuffer buffer = new StringBuffer();
		for (int i=nl.getLength() - 1; i >= 0; i--){
			node = nl.item(i);
			if ("message".equals(node.getNodeName())) {
				content = node.getTextContent();
				if (content.contains("SUCCESS [")) {
					buffer.append("'" + content + "',");
				}
			}
		}
		content = buffer.toString().trim();
		out.println(content);

		file.getParentFile().mkdirs();
		FileWriter fw = new FileWriter(file);
		PrintWriter pw = new PrintWriter(fw);
		pw.println(content);
		fw.close();
	}
} catch (Exception e) {
	e.printStackTrace();
}
%>
];

function format(){
	var len=logs.length, log, index, i, name, time, min, data=[];
	for(i=0;i<len;i++){
		log=logs[i];
		index=log.indexOf(' ..');
		if(index<0)index=log.indexOf(' SUCCESS');
		name=log.substring(7, index);
		time=log.substring(log.indexOf('SUCCESS [') + 9, log.indexOf('s]'));
		index=time.indexOf(':');
		min=0;
		if(index>0){
			min=time.substring(0, index);
			time=time.substring(index+1);
			index=time.indexOf(':');
			if(index>0){
				min=min*60+time.substring(0, index);
				time=time.substring(index+1);
			}
		}
		time=min*60+time;
		time=Math.floor(time*10)/10;
		data.push([name, time]);
		console.log(name + ' \t\t\t\t' + time)
	}
	return data;
}

$.jqplot.config.enablePlugins = true;
var isReal=false, data=false, canvas=$('#canvas');
$('#Real').click(function(){
	if(isReal) return;
	isReal=true;
	show($.jqplot.dateAxisRenderer);
}).trigger('click');
$('#Relative').click(function(){
	if(!isReal) return;
	isReal=false;
	show($.jqplot.LogAxisRenderer);
});

function show(isReal){
	if(!data)data=format();
    canvas.empty().jqplot([data], {
		animate: !$.jqplot.use_excanvas,
        title:'Maven Module Cost Time (Seconds)',
        seriesDefaults:{
            renderer: $.jqplot.BarRenderer,
			pointLabels: { show: true, location: 'e', edgeTolerance: -10 },
			shadowAngle: 135,
			showMarker: true,
            rendererOptions: {
                varyBarColor: true,
				smooth: true,
                animation: {
                    show: true
                }
            }
        },
        axes: {
            xaxis:{
				label: 'Maven Module',
                renderer: $.jqplot.CategoryAxisRenderer,
                rendererOptions:{
                    tickRenderer:$.jqplot.CanvasAxisTickRenderer
                },
				tickOptions:{ 
                    fontSize:'11px',
                    fontFamily:'Arial',
                    angle:90
                }
            },
			yaxis: {
				renderer: isReal,
				pad: 1,
				label: 'Cost Time',
				tickOptions:{
					suffix: 's'
				}
			}
        },
        grid: {
            drawBorder: true,
            shadow: true,
            background: "#FEFEFE"
        }
    });
}
});
</script>
<div class="action"><button id="Real">Real</button><button id="Relative">Relative</button></div>
<div id="canvas" class="chart" style="height:1000px;width:100%"></div>