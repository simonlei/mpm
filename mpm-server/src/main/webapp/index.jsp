
<%@ taglib uri="http://www.smartclient.com/taglib" prefix="sc" %>
<%@ page language="java" pageEncoding="UTF-8"  isELIgnored="false"%>
<%
/*
    Illustrates another way to load the SmartGWT framework using the JSP tag 
    library.  The net result is almost identical to what you'll find in index.html, so you'd 
    normally choose one approach or the other, although the taglib does append a 
    'cache-busting' query string parameter to framework resources.  Refer to the 'SmartClient 
    JSP Tags' documentation topic for further discussion on the loadISC and loadDS tags, among 
    others.
    
    https://www.smartclient.com/smartclient-latest/isomorphic/system/reference/?id=group..jspTags
 */  
%>

<!doctype html>
<html>
  <head>
    <meta http-equiv="content-type" content="text/html; charset=UTF-8">
    
    <link type="text/css" rel="stylesheet" href="style.css">

    <!--                                           -->
    <!-- Any title is fine                         -->
    <!--                                           -->
    <title>PhotoManager</title>

    <!--                                           -->
    <!-- This script loads your compiled module.   -->
    <!-- If you add any GWT meta tags, they must   -->
    <!-- be added before this line.                -->
    <!--                                           -->
    <script src="app/app.nocache.js"></script>

    <!-- Load the SmartClient framework, including the commonly used optional Charts module  -->
    <sc:loadISC modulesDir="modules" includeModules="Charts" skin="Tahoe" />

  </head>

  <!--                                           -->
  <!-- The body can have arbitrary html, or      -->
  <!-- you can leave the body empty if you want  -->
  <!-- to create a completely dynamic UI.        -->
  <!--                                           -->
  <body style="overflow:hidden">

    <!-- Use something like the following to keep datasource loading separate (using e.g., the loadDS tag) -->
    <script src="./datasources.jsp" type="application/javascript"></script> 
    
    <!-- Or just load them inline if you prefer -->
    <!-- 
        <script src="app/sc/DataSourceLoader?dataSource=supplyItem"></script> 
    -->

    <!-- OPTIONAL: include this if you want history support -->
    <iframe src="javascript:''" id="__gwt_historyFrame" tabIndex='-1' style="position:absolute;width:0;height:0;border:0"></iframe>

    <!-- RECOMMENDED if your web app will not function without JavaScript enabled -->
    <noscript>
      <div style="width: 22em; position: absolute; left: 50%; margin-left: -11em; color: red; background-color: white; border: 1px solid red; padding: 4px; font-family: sans-serif">
        Your web browser must have JavaScript enabled
        in order for this application to display correctly.
      </div>
    </noscript>

  </body>
</html>