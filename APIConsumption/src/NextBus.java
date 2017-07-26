import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class NextBus {
	
	static final int SECONDS_PER_MINUTE = 60;
	public static void main(String[] args) {
		
		if (args.length < 3) {
			System.out.println("Usage: java -jar APIConsumption.jar [BUS ROUTE] [BUS STOP NAME] [DIRECTION]");
			return;
		}
		
		NextBus bus = new NextBus();
		String direction = "";
		String dir = args[2].toLowerCase();
		if (dir.equals("south")) {//1 = South, 2 = East, 3 = West, 4 = North
			direction = "1";
		} else if (dir.equals("east")) {
			direction = "2";
		} else if (dir.equals("west")) {
			direction = "3";
		} else if (dir.equals("north")) {
			direction = "4";
		} else {
			System.out.println("Please inpur correct diretion(south,east,west or north)!");
			return;
		}
		
		try {
			bus.query(args[0], args[1], direction);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
	
	public void query(String route, String stop, String direction) throws ClientProtocolException, UnsupportedOperationException, IOException, ParserConfigurationException, SAXException, XPathExpressionException {
		
		String routeId = this.getRouteId(route);
		if (routeId == null) {
			throw new RuntimeException("please input correct route!\n"
					+ "It is ok as long as what you input is included in the full route name");
		}
		
		boolean bDirOk = checkDirection(routeId, direction);
		if (!bDirOk) {
			throw new RuntimeException("The direction is not available for this route!");
		}
		
		String timepointStopId = getTimepointStopId(routeId, direction, stop);
		if (timepointStopId == null) {
			throw new RuntimeException("please input correct stop!\n"
					+ "It is ok as long as what you input is included in the full stop name");
		}
		
		LocalDateTime departureTime = GetTimepointDepartures(routeId, direction, timepointStopId);
		if (departureTime == null) {
			throw new RuntimeException("Sorry,we cannot find any shift information for the given route, direction and stop");
		}
		Duration duration = Duration.between(LocalDateTime.now(), departureTime);
		long mins = duration.getSeconds() / SECONDS_PER_MINUTE;
		
		System.out.println(String.valueOf(mins) + " minutes");
	}
	
	private String getRouteId(String route) throws ClientProtocolException, IOException, ParserConfigurationException, UnsupportedOperationException, SAXException {
		
		HttpClient client = new DefaultHttpClient();
		String url = "http://svc.metrotransit.org/NexTrip/Routes";
		HttpGet req = new HttpGet(url);
		HttpResponse response = client.execute(req);
		
		
		DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = builderFactory.newDocumentBuilder();
		Document document = builder.parse(response.getEntity().getContent());
		
		NodeList nodes = document.getDocumentElement().getChildNodes();
		NodeList nodes2;
		String nodeName, routeDesp = null, routId = null;
		int numOfQryNodes = 0; 
		for (int i = 0; i < nodes.getLength(); i++) {
			
			numOfQryNodes = 0;
			routeDesp = null;
			routId = null;
			
			nodes2 = nodes.item(i).getChildNodes();
			for (int j = 0; j < nodes2.getLength(); j++) {
				nodeName = nodes2.item(j).getNodeName();
				if (nodeName.equals("Description")) {
					routeDesp = nodes2.item(j).getTextContent().toLowerCase();
					numOfQryNodes++;
				} else if (nodeName.equals("Route")) {
					routId = nodes2.item(j).getTextContent();
					numOfQryNodes++;
				}
				if (numOfQryNodes == 2)
					break;
			}
			
			if (routeDesp != null && routeDesp.indexOf(route.toLowerCase()) != -1) {
				return routId;
			}
		}
		return null;
	}

	private boolean checkDirection(String routeId, String direction) throws ClientProtocolException, IOException, ParserConfigurationException, UnsupportedOperationException, SAXException, XPathExpressionException {
		
		HttpClient client = new DefaultHttpClient();
		String url = "http://svc.metrotransit.org/NexTrip/Directions/" + routeId;
		HttpGet req = new HttpGet(url);
		HttpResponse response = client.execute(req);
		DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = builderFactory.newDocumentBuilder();
		
		Document document = builder.parse(response.getEntity().getContent());
		
		XPathFactory pathFactory = XPathFactory.newInstance();
		XPath path = pathFactory.newXPath();
		XPathExpression expr = path.compile("/ArrayOfTextValuePair/TextValuePair/Value");
		NodeList valueNodes = (NodeList)expr.evaluate(document, XPathConstants.NODESET);
		for (int i = 0; i < valueNodes.getLength(); i++) {
			if (valueNodes.item(i).getTextContent().equals(direction)) {
				return true;
			}
		}
		return false;
	}
	
	private String getTimepointStopId(String routeId, String direction, String stop) throws ClientProtocolException, IOException, ParserConfigurationException, UnsupportedOperationException, SAXException {
		
		HttpClient client = new DefaultHttpClient();
		String url = "http://svc.metrotransit.org/NexTrip/Stops" +
				"/" + routeId + "/" + direction;
		HttpGet req = new HttpGet(url);
		HttpResponse response = client.execute(req);
		DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = builderFactory.newDocumentBuilder();
		Document document = builder.parse(response.getEntity().getContent());
		NodeList nodes = document.getDocumentElement().getChildNodes();
		NodeList nodes2;
		String nodeName, stopDesp = null, stopId = null;
		int numOfQryNodes = 0;
		for (int i = 0; i < nodes.getLength(); i++) {
			
			numOfQryNodes = 0;
			stopDesp = null;
			stopId = null;
			
			nodes2 = nodes.item(i).getChildNodes();
			for (int j = 0; j < nodes2.getLength(); j++) {
				nodeName = nodes2.item(j).getNodeName();
				if (nodeName.equals("Text")) {
					stopDesp = nodes2.item(j).getTextContent().toLowerCase();
					numOfQryNodes++;
				} else if (nodeName.equals("Value")) {
					stopId = nodes2.item(j).getTextContent();
					numOfQryNodes++;
				}
				
				if (numOfQryNodes == 2)
					break;
			}
			
			if (stopDesp != null && stopDesp.indexOf(stop.toLowerCase()) != -1) {
				return stopId;
			}
		}
		return null;
	}
	
	private LocalDateTime GetTimepointDepartures(String routeId, String direction, String timePointStopId) throws ClientProtocolException, IOException, ParserConfigurationException, UnsupportedOperationException, SAXException, XPathExpressionException {
		HttpClient client = new DefaultHttpClient();
		String url = "http://svc.metrotransit.org/NexTrip" + ///{ROUTE}/{DIRECTION}/{STOP}
				"/" + routeId + "/" + direction + "/" + timePointStopId;
		HttpGet req = new HttpGet(url);
		HttpResponse response = client.execute(req);
		DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = builderFactory.newDocumentBuilder();
		Document document = builder.parse(response.getEntity().getContent());
		
		XPathFactory pathFactory = XPathFactory.newInstance();
		XPath path = pathFactory.newXPath();
		XPathExpression expr = path.compile("/ArrayOfNexTripDeparture/NexTripDeparture/DepartureTime");
		NodeList valueNodes = (NodeList)expr.evaluate(document, XPathConstants.NODESET);
		if (valueNodes.getLength() == 0) {
			return null;
		}
		LocalDateTime tMin = LocalDateTime.parse(valueNodes.item(0).getTextContent());
		LocalDateTime tempTime;
		for (int i = 1; i < valueNodes.getLength(); i++) {
			tempTime = LocalDateTime.parse(valueNodes.item(i).getTextContent());
			if (tempTime.compareTo(tMin) < 0) {
				tMin = tempTime;
			}
		}
		return tMin;
	}
}

