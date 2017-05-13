<%@ page import="java.util.*,java.sql.*"%>
<%@page import="com.google.gson.Gson"%>
<%@page import="com.google.gson.JsonObject"%>
<%@ page import="javax.servlet.http.*,javax.servlet.*" %>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<jsp:include page="./WEB-INF/pages/template.jsp">
	<jsp:param value="Elastic DB Project" name="pageTitle"/>
	<jsp:param value="<div id='chartContainer1' style='height: 150px; width: 100%;'></div>
                        <div id='chartContainer2' style='height: 150px; width: 100%;'></div>
                        <div id='chartContainer3' style='height: 150px; width: 100%;'></div>
                        <div id='chartContainer4' style='height: 150px; width: 100%;'></div>" name="content"/> 	
</jsp:include>
<script type="text/javascript">
 $(function () {
<%	Gson gsonObj = new Gson();
	String dataPoints1 = "";
        String dataPoints2 = "";
        String dataPoints3 = "";
        String dataPoints4 = "";
	Map<Object,Object> map = new HashMap<Object,Object>();
	List<Map<Object,Object>> list1 = new ArrayList<Map<Object,Object>>();
	List<Map<Object,Object>> list2 = new ArrayList<Map<Object,Object>>();
	List<Map<Object,Object>> list3 = new ArrayList<Map<Object,Object>>();
	List<Map<Object,Object>> list4 = new ArrayList<Map<Object,Object>>();
	
	try{
		Class.forName("com.mysql.jdbc.Driver"); 
		Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/canvasjs_db", "root", "TigerBit!2016");
		Statement statement = connection.createStatement();
		Double xVal, uVal, rVal, wVal, mVal;
		
		ResultSet resultSet = statement.executeQuery("select * from datapoints");
		ResultSetMetaData rsmd = resultSet.getMetaData();
		
		while(resultSet.next()){
			xVal = resultSet.getDouble("x");
			uVal = resultSet.getDouble("u");
			rVal = resultSet.getDouble("r");
			wVal = resultSet.getDouble("w");
			mVal = resultSet.getDouble("m");
			map = new HashMap<Object,Object>();	map.put("x", xVal);	map.put("y", uVal); list1.add(map);
			map = new HashMap<Object,Object>();	map.put("x", xVal);	map.put("y", rVal); list2.add(map);
			map = new HashMap<Object,Object>();	map.put("x", xVal);	map.put("y", wVal); list3.add(map);
			map = new HashMap<Object,Object>();	map.put("x", xVal);	map.put("y", mVal); list4.add(map);
			dataPoints1 = gsonObj.toJson(list1);
			dataPoints2 = gsonObj.toJson(list2);
			dataPoints3 = gsonObj.toJson(list3);
			dataPoints4 = gsonObj.toJson(list4);
		}
		connection.close();
	}
	catch(SQLException e){
		out.println("<div class='alert alert-danger' style='margin:1%;'>Could not connect to the database. Please check if you have mySQL Connector installed on the machine - if not, try installing the same. Please refer to Instruction.txt</div>");
		dataPoints1 = null;
	}
  if(dataPoints1 != null){%>
                   var chart = new CanvasJS.Chart("chartContainer1", {
                   theme: "theme2",
                   zoomEnabled: true,
                   animationEnabled: true,
                   title: {
                        text: "Workload"
                   },
                   axisX: {
                        interval: 1,
                        minimum: 0,
                   },
                   axisY: {
                        title: "#UserSessions",
                        includeZero: false,
                        suffix: "",
                        lineColor: "#469EAD"
                   },
                   data: [
                   {
                       type: "line",

                       dataPoints: <%out.print(dataPoints1);%>
                   }
                   ]
                   });
                   chart.render();

                   chart = new CanvasJS.Chart("chartContainer2", {
                   theme: "theme2",
                   zoomEnabled: true,
                   animationEnabled: true,
                   title: {
                        text: "Read response time (ms)"
                   },
                   axisX: {
                        interval: 1,
                        minimum: 0,
                   },
                   axisY: {
               		title: "Read",
               		includeZero: false,
               		suffix: "",
               		lineColor: "#469EAD"
           	   },
                   data: [
           	   {
               		type: "line",
               		dataPoints: <%out.print(dataPoints2);%>
                   }
                   ]
                   });
                   chart.render();

                   chart = new CanvasJS.Chart("chartContainer3", {
                   theme: "theme2",
                   zoomEnabled: true,
                   animationEnabled: true,
                   title: {
                        text: "Write response time (ms)"
                   },
                   axisX: {
                        interval: 1,
                        minimum: 0,
                   },
                   axisY: {
                        title: "Write",
                        includeZero: false,
                        suffix: "",
                        lineColor: "#469EAD"
                   },
                   data: [
                   {
                        type: "line",
                        dataPoints: <%out.print(dataPoints3);%>
                   }
                   ]
                   });
                   chart.render();

                   chart = new CanvasJS.Chart("chartContainer4", {
                   theme: "theme2",
                   zoomEnabled: true,
                   animationEnabled: true,
                   title: {
                        text: "Resouce usage (#nodes)"
                   },
                   axisX: {
                        interval: 1,
                        minimum: 0,
                   },
                   axisY: {
                        title: "#Nodes",
                        includeZero: false,
                        suffix: "",
                        lineColor: "#469EAD"
                   },
                   data: [
                   {
                       type: "line",

                       dataPoints: <%out.print(dataPoints4);%>
                   }
                   ]
                   });
                   chart.render();
  <%} %>
 });
 var timeout = setTimeout("location.reload(true);",10000);
  function resetTimeout() {
    clearTimeout(timeout);
    timeout = setTimeout("location.reload(true);",10000);
  }
</script>
