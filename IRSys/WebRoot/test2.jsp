<%@ page language="java" import="java.util.*" pageEncoding="utf-8"%>

<%
String path = request.getContextPath();
String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";
%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
  <head>
    <base href="<%=basePath%>">
    
    <title>My JSP 'test.jsp' starting page</title>
    
    <meta http-equiv="pragma" content="no-cache">
    <meta http-equiv="cache-control" content="no-cache">
    <meta http-equiv="expires" content="0">    
    <meta http-equiv="keywords" content="keyword1,keyword2,keyword3">
    <meta http-equiv="description" content="This is my page">
    <!--
    <link rel="stylesheet" type="text/css" href="styles.css">
    -->

  </head>
  <script src="http://libs.baidu.com/jquery/2.1.4/jquery.min.js"></script>
  <script type="text/javascript">

  $(document).ready(function(){
    $("#btn1").click(function(){
      $("#tex1").text("Hello world!");
      $("#texts").text("success!");
    });
    $("#btn2").click(function () {
        var obj = $.ajax({
            url: "http://suggestion.baidu.com/su?wd=黄予&zxmode=1&json=1&p=3&"
		
      })
        });
  });
  </script>
      
  <body>
<p id= "tex1">This is a paragraph.</p>
<button id="btn1">改变所有 p 元素的文本内容</button>
<button id="btn2">改变所有 p 元素的文本内容</button>
<p id="texts"></p>
  </body>
</html>
