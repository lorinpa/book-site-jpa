<%@page import="org.pa.entity.Category"%>
<%@page import="java.util.List"%>
<jsp:include page="/jspf/pageHeader.jspf" />

<% List<Category> list = (List<Category>) request.getAttribute("list");

    String appPath = request.getServletContext().getContextPath();
%>
<table class="pure-table" style="letter-spacing:normal;">
    <thead>
        <tr>

            <th>Category</th>
            <th></th>
            <th></th>

        </tr>
    </thead>

    <tbody>

    <div><a href="<%= appPath%>/category/addCategory.htm">Add New Category</a></div>
    <% for (Category cat : list) {%>
    <tr>

        <td><%= cat.getTitle()%></td>
        <td><a href="<%= appPath%>/category/editCategory.htm?id=<%= cat.getId()%>">Edit</a></td>
        <td><a href="<%= appPath%>/category/deleteCategory.htm?id=<%= cat.getId()%>">Delete</a></td>


    </tr>
    <%  }%>
</tbody>
</table>

<%@include  file="../pageFooter" %>

