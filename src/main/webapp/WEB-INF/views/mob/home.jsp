<!DOCTYPE html>
<%@ page pageEncoding="UTF-8" %>
<%
        String appPath = request.getServletContext().getContextPath();
   %>
<html >
<head>
    <title>BookReview</title>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
     <link href="<%=  appPath %>/stylesheets/normalize.css" type="text/css" rel="stylesheet"/>
        <link href="<%=  appPath %>/stylesheets/responsive.css" type="text/css" rel="stylesheet"/>
        <link href="<%=  appPath %>/stylesheets/pure-min.css" type="text/css" rel="stylesheet"/>
        <link href="<%=  appPath %>/stylesheets/app-custom.css" type="text/css" rel="stylesheet"/>
        <link href="<%=  appPath %>/favicon.png" rel="shortcut icon"/>
</head>
<body>
        <header id="header-container" class="header" style="text-align:center;">
            <h4 id="menu-header"></h4>
            <div class="pure-menu pure-menu-open pure-menu-horizontal" id="entity-menu">
                <ul id="entity-menu-details">
                </ul>
            </div>
        </header>

<div id="report" class="yui3-nomalized push-down">
    <div class="pure-menu pure-menu-open">
        <ul id="report-details">

        </ul>
    </div>

</div>

<footer id="footer-menu" class="footer pure-menu pure-menu-open pure-menu-horizontal push-up">
    <ul>
        <li><a id="author-list-menu-item" href="#">Authors</a></li>
        <li><a id="book-list-menu-item" href="#">Books</a></li>
        <li><a id="category-list-menu-item" href="#">Categories</a></li>
        <li><a id="review-list-menu-item" href="#">Reviews</a></li>
    </ul>
</footer>

    <script type="text/javascript">var CLOSURE_NO_DEPS = true;</script>
    <script type="text/javascript" src="<%=  appPath %>/js/books_cljs.js"></script>
    <script type="text/javascript">
        goog.require('book_review.main');
    </script>
</body>
</html>
