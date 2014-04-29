<%@page contentType="text/html; charset=utf-8" errorPage="/error.jsp" import="org.w3c.dom.*,javax.xml.parsers.*,java.io.*" %>
<%
String log = request.getParameter("log");
try {
	File file = new File("cache" + log + "-all.txt");
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
		String time = "";
		for (int i=nl.getLength() - 1; i >= 0; i--){
			node = nl.item(i);
			if ("message".equals(node.getNodeName())) {
				content = node.getTextContent();
				if (content.contains("[INFO] Total time: ")) {
					time = content.substring(content.indexOf(": ") + 2);
				}
			}
		}
		time = time.trim();
		out.print(time);
		
		file.getParentFile().mkdirs();
		FileWriter fw = new FileWriter(file);
		PrintWriter pw = new PrintWriter(fw);
		pw.println(time);
		fw.close();
		
	}
} catch (Exception e) {
	e.printStackTrace();
}
%>