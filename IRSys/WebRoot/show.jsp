<%@ page language="java" import="java.util.*" pageEncoding="utf-8"%>
<%
request.setCharacterEncoding("utf-8");
response.setCharacterEncoding("utf-8");
String path = request.getContextPath();
String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";
String imagePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+"/";
%>

<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>result</title>
<link href="../css/bootstrap.min.css" rel="stylesheet">
<link href="../css/landing-page.css" rel="stylesheet">
<link href="../font-awesome/css/font-awesome.min.css" rel="stylesheet" type="text/css">
<link href="../http://fonts.googleapis.com/css?family=Lato:300,400,700,300italic,400italic,700italic" rel="stylesheet" type="text/css">

<style type="text/css">
<!--

#Layer1 {
    position:relative;
    z-index:1;
}
#Layer2 {
    position:relative;
    left:29px;
    width:648px;
    height:602px;
    z-index:2;
}
#Layer3 {
    position:relative;
    left:28px;
    top:697px;
    width:652px;
    height:67px;
    z-index:3;
}
#Searchinput {
	margin:100px;
}
.title{
   font-family: "Lato","Helvetica Neue",Helvetica,Arial,sans-serif;
   text-shadow: 2px 2px 3px rgba(0,0,0,0.6);
}
.highlight{
	color:#dd4b39;
}

-->
</style>
</head>

<script src="http://libs.baidu.com/jquery/2.1.4/jquery.min.js"></script>
<script>



</script>

<body>
<%
    String currentQuery=(String) request.getAttribute("currentQuery");
	if (currentQuery== null)
		currentQuery= "";
	String currentAuthor=(String) request.getAttribute("currentAuthor");
	if (currentAuthor == null)
		currentAuthor = "";
	String currentPublisher=(String) request.getAttribute("currentPublisher");
	if (currentPublisher == null)
		currentPublisher = "";
	String currentStartyear=(String) request.getAttribute("currentStartyear");
	if (currentStartyear == null)
		currentStartyear = "";
	String currentEndyear=(String) request.getAttribute("currentEndyear");
	if (currentEndyear == null)
		currentEndyear = "";
    int currentPage=(Integer) request.getAttribute("currentPage");
%>

<div class = "col-lg-12" style = "margin-top:10px;"></div>
<div class = "col-lg-2 title"><h3>IRSys</h3></div>
<div style="color:#00FF00;margin-top:20px;margin-left:-10px;" class="Searchinput col-lg-4">
    <form id="form1" name="form1" method="get" action="IRServer">
      <div class="input-group">
               <input name="query" type="text" class="form-control" value="<%=currentQuery%>">
               <span class="input-group-btn">
                  <input class="btn btn-default" type="submit" name = "Submit" value="搜索"></input>
               </span>
      </div><!-- /input-group -->
       <div class="col-lg-6"  style="padding: 10px 0px 10px">
        <div class="input-group">
        		<span class="input-group-addon">作者</span>
         <input name="author" type="text" class="form-control" placeholder="" value="<%=currentAuthor%>">
        </div>
       </div>
       <div class="col-lg-6"  style="padding: 10px 0px 10px">
        <div class="input-group">
        		<span class="input-group-addon">出版单位</span>
         <input name="publisher" type="text" class="form-control" placeholder="" value="<%=currentPublisher%>">
        </div>
       </div>
       
       <div class="col-lg-6"  style="padding: 10px 0px 10px">
        <div class="input-group">
        		<span class="input-group-addon">最早年份</span>
         <input name="startyear" type="text" class="form-control" placeholder="1901" value="<%=currentStartyear%>">
        </div>
       </div>
       <div class="col-lg-6"  style="padding: 10px 0px 10px">
        <div class="input-group">
        		<span class="input-group-addon">最晚年份</span>
         <input name="endyear" type="text" class="form-control" placeholder="2020" value="<%=currentEndyear%>">
        </div>
       </div>
    </form>
</div>
<div class = "col-lg-12" style = "margin-bottom:10px;"></div>
<div class = "col-lg-12" style = "height:40px; background-color:#E3E3E3;"></div>
<div class = "col-lg-12" style = "margin-bottom:10px;"></div>

<div class = "col-lg-12">
<div class = "col-lg-4 col-lg-offset-2" style = "font-size:20px;">
  <p>
      <%if(currentPage>1){ %>
          <a href="IRServer?query=<%=currentQuery%>&page=<%=currentPage-1%>&author=<%=currentAuthor%>&publisher=<%=currentPublisher%>&startyear=<%=currentStartyear%>&endyear=<%=currentEndyear%>
          ">上一页</a>
      <%}else{ %>
          <a>上一页</a>
      <%}; %>
      <%for (int i=Math.max(1,currentPage-5);i<currentPage;i++){%>
          <a href="IRServer?query=<%=currentQuery%>&page=<%=i%>&author=<%=currentAuthor%>&publisher=<%=currentPublisher%>&startyear=<%=currentStartyear%>&endyear=<%=currentEndyear%>
          "><%=i%></a>
      <%}; %>
      <strong><%=currentPage%></strong>
      <%for (int i=currentPage+1;i<=currentPage+5;i++){ %>
          <a href="IRServer?query=<%=currentQuery%>&page=<%=i%>&author=<%=currentAuthor%>&publisher=<%=currentPublisher%>&startyear=<%=currentStartyear%>&endyear=<%=currentEndyear%>
          "><%=i%></a>
      <%}; %>
      <a href="IRServer?query=<%=currentQuery%>&page=<%=currentPage+1%>&author=<%=currentAuthor%>&publisher=<%=currentPublisher%>&startyear=<%=currentStartyear%>&endyear=<%=currentEndyear%>
      ">下一页</a>
  </p>
</div>
</div>

<div class = "col-lg-12" style = "margin-bottom:40px;"></div>

<div style="top: 82px; height: 585px;">
  <div id="imagediv">
  <br>
  <div>
  <%
    String[] titles=(String[]) request.getAttribute("titles");
    String[] titles_en=(String[]) request.getAttribute("titles_en");
    String[] abstracts=(String[]) request.getAttribute("abstracts");
    String[] authors=(String[]) request.getAttribute("authors");
    String[] publishers=(String[]) request.getAttribute("publishers");
    String[] years=(String[]) request.getAttribute("years");
    if (titles != null && titles_en != null){
    	for(int i = 0; i<titles.length; i++){
        	if(titles[i].length() > 30) {
        		titles[i] = titles[i].substring(0, 30) + "...";
        	}
        }
        for(int i = 0; i<titles_en.length; i++){
        	if(titles_en[i].length() > 30) {
        		titles_en[i] = titles_en[i].substring(0, 30) + "...";
        	}
        }
    }
    if(titles!=null && titles.length>0){
        for(int i=0;i<titles.length;i++){%>
        <div class = "col-lg-12" style = "margin-bottom:30px;margin-left:30px;">
        <p class = "col-lg-7" style="font-size:25px; margin:-2px;color:#1a0dab;"><%=titles[i]%></p>
        <p class = "col-lg-7" style = "font-size:15px; margin: -2px;color:#006621;"><%=titles_en[i]%></p>
        <div class = "col-lg-7"><%=authors[i] + "  " + publishers[i] + "  " + years[i]%></div>
        <div class = "col-lg-7"><%=abstracts[i]%></div>
        </div><%};%>
    <%}else{ %>
       <!-- <p><tr><h3>no such result</h3></tr></p> -->
    <%}; %>
  </div>
  </div>

<div class = "col-lg-12">
<div class = "col-lg-4 col-lg-offset-2" style = "font-size:20px;">
  <p>
      <%if(currentPage>1){ %>
          <a href="IRServer?query=<%=currentQuery%>&page=<%=currentPage-1%>">上一页</a>
      <%}else{ %>
          <a>上一页</a>
      <%}; %>
      <%for (int i=Math.max(1,currentPage-5);i<currentPage;i++){%>
          <a href="IRServer?query=<%=currentQuery%>&page=<%=i%>"><%=i%></a>
      <%}; %>
      <strong><%=currentPage%></strong>
      <%for (int i=currentPage+1;i<=currentPage+5;i++){ %>
          <a href="IRServer?query=<%=currentQuery%>&page=<%=i%>"><%=i%></a>
      <%}; %>
      <a href="IRServer?query=<%=currentQuery%>&page=<%=currentPage+1%>">下一页</a>
  </p>
</div>
</div>

</div>

</div>
<div>
</div>
</body>
