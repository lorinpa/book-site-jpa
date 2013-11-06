<%@page import="org.pa.entity.Author"%>
<%@page import="java.util.List"%>
<jsp:include page="/jspf/pageHeader.jspf" />

<% List<Author> list = (List<Author>) request.getAttribute("list");

    String appPath = request.getServletContext().getContextPath();
%>
<table class="pure-table" style="letter-spacing:normal;">
    <thead>
        <tr>

            <th>First Name</th>
            <th>Last Name</th>
            <th></th>
               <th></th>

        </tr>
    </thead>

    <tbody>

    <div><a href="<%= appPath%>/author/add.htm">Add New Author</a></div>
    <% for (Author author : list) {%>
    <tr>

       
        <td><%= author.getLastName() %></td>
        <td><%= author.getFirstName() %></td>
        
        <td><a href="<%= appPath%>/author/edit.htm?id=<%= author.getId()%>">Edit</a></td>
        <td><a href="<%= appPath%>/author/delete.htm?id=<%= author.getId()%>">Delete</a></td>


    </tr>
    <%  }%>
</tbody>
</table>

<%@include  file="../pageFooter" %>
