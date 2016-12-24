<%@ page language="java" import="java.util.*" pageEncoding="utf-8"%>
<%
request.setCharacterEncoding("utf-8");
System.out.println(request.getCharacterEncoding());
response.setCharacterEncoding("utf-8");
System.out.println(response.getCharacterEncoding());
String path = request.getContextPath();
String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";
System.out.println(path);
System.out.println(basePath);
%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html xmlns="http://www.w3.org/1999/xhtml">

<head>

    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <meta name="description" content="">
    <meta name="author" content="">

    <title>IRSys</title>

    <!-- Bootstrap Core CSS -->
    <link href="css/bootstrap.min.css" rel="stylesheet">

    <!-- Custom CSS -->
    <link href="css/landing-page.css" rel="stylesheet">

    <!-- Custom Fonts -->
    <link href="font-awesome/css/font-awesome.min.css" rel="stylesheet" type="text/css">
    <link href="http://fonts.googleapis.com/css?family=Lato:300,400,700,300italic,400italic,700italic" rel="stylesheet" type="text/css">

    <!-- HTML5 Shim and Respond.js IE8 support of HTML5 elements and media queries -->
    <!-- WARNING: Respond.js doesn't work if you view the page via file:// -->
    <!--[if lt IE 9]>
        <script src="https://oss.maxcdn.com/libs/html5shiv/3.7.0/html5shiv.js"></script>
        <script src="https://oss.maxcdn.com/libs/respond.js/1.4.2/respond.min.js"></script>
    <![endif]-->

</head>

<body>
    <!-- Header -->
    <a name="about"></a>
    <div class="intro-header">
        <div class="container">
            <div class="row">
                <div class="col-lg-12">
                    <div class="intro-message">
                        <h1>IRSys</h1>
                        <h3 style="margin-bottom:50px;">search CNKI</h3>

                       	<div id="Layer1" style="color:#000000;" class="col-lg-8 col-lg-offset-2">
						  <form id="form1" name="form1" method="get" action="servlet/IRServer">
						    <div class="input-group" style="padding: 10px 40px 10px;">
				               <input name="query" type="text" class="form-control" placeholder="清华大学">
				               <span class="input-group-btn">
				                  <input class="btn btn-default" type="submit" name="Submit" value="搜索">
				               </span>
				            </div><!-- /input-group -->

				            <div class="col-lg-6"  style="padding: 10px 40px 10px">
					             <div class="input-group">
					             		<span class="input-group-addon">作者</span>
							            <input name="author" type="text" class="form-control" placeholder="">
					             </div>
				             </div>
				             <div class="col-lg-6"  style="padding: 10px 40px 10px">
					             <div class="input-group">
					             		<span class="input-group-addon">出版单位</span>
							            <input name="publisher" type="text" class="form-control" placeholder="">
					             </div>
				             </div>

				             <div class="col-lg-6"  style="padding: 10px 40px 10px">
					             <div class="input-group">
					             		<span class="input-group-addon">最早年份</span>
							            <input name="startyear" type="text" class="form-control" placeholder="1901">
					             </div>
				             </div>
				             <div class="col-lg-6"  style="padding: 10px 40px 10px">
					             <div class="input-group">
					             		<span class="input-group-addon">最晚年份</span>
							            <input name="endyear" type="text" class="form-control" placeholder="2020">
					             </div>
				             </div>

						  </form>
						</div>
                    </div>
                </div>
            </div>
        </div>
        <div class="container">
            <div class="row">
                <div class="col-lg-12">
                    <p class="copyright small">Producer 施韶韵</p>
                </div>
            </div>
        </div>
    </div>


</body>

</html>



