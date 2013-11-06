<%@page import="org.pa.entity.Book"%>
<%@page import="java.util.List"%>
<jsp:include page="/jspf/pageHeader.jspf" />

<% List<Book> list = (List<Book>) request.getAttribute("list");

    String appPath = request.getServletContext().getContextPath();
%>
<table class="pure-table" style="letter-spacing:normal;">
    <thead>
        <tr>

            <th>Title</th>
            <th>Author</th>
            <th></th>
               <th></th>

        </tr>
    </thead>

    <tbody>

    <div><a href="<%= appPath%>/book/add.htm">Add New Book</a></div>
    <% for (Book book : list) {%>
    <tr>

        <td><%= book.getTitle() %></td>
        <td><%= book.getAuthorId().getLastName() %></td>
        <td><%= book.getAuthorId().getFirstName() %></td>
        
        <td><a href="<%= appPath%>/book/edit.htm?id=<%= book.getId()%>">Edit</a></td>
        <td><a href="<%= appPath%>/book/delete.htm?id=<%= book.getId()%>">Delete</a></td>


    </tr>
    <%  }%>
</tbody>
</table>

<%@include  file="../pageFooter" %>
