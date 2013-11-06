<%@page import="org.pa.entity.BookCategory"%>
<%@page import="java.util.List"%>
<jsp:include page="/jspf/pageHeader.jspf" />

<% List<BookCategory> list = (List<BookCategory>) request.getAttribute("list");

    String appPath = request.getServletContext().getContextPath();
%>
<table class="pure-table" style="letter-spacing:normal;">
    <thead>
        <tr>

            <th>Book</th>
            <th>Category</th>
            <th></th>
               <th></th>

        </tr>
    </thead>

    <tbody>

    <div><a href="<%= appPath%>/book-cat/add.htm">Add New Book Category</a></div>
    <% for (BookCategory bookCat : list) {%>
    <tr>

        <td><%= bookCat.getBookId().getTitle() %></td>
        <td><%= bookCat.getCategoryId().getTitle() %></td>
    
        
        <td><a href="<%= appPath%>/book-cat/edit.htm?id=<%= bookCat.getId()%>">Edit</a></td>
        <td><a href="<%= appPath%>/book-cat/delete.htm?id=<%= bookCat.getId()%>">Delete</a></td>


    </tr>
    <%  }%>
</tbody>
</table>

<%@include  file="../pageFooter" %>
