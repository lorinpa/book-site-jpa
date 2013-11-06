<%@page import="org.pa.entity.Review"%>
<%@page import="java.util.List"%>
<jsp:include page="/jspf/pageHeader.jspf" />

<% List<Review> list = (List<Review>) request.getAttribute("list");

    String appPath = request.getServletContext().getContextPath();
%>
<table class="pure-table" style="letter-spacing:normal;">
    <thead>
        <tr>

            <th>Book</th>
            <th>Rating</th>
            <th></th>
               <th></th>

        </tr>
    </thead>

    <tbody>

    <div><a href="<%= appPath%>/review/add.htm">Add New Review</a></div>
    <% for (Review review : list) {%>
    <tr>

        <td><%= review.getBookId().getTitle() %></td>
        <td><%= review.getStars() %></td>
        
        <td><a href="<%= appPath%>/review/edit.htm?id=<%= review.getId()%>">Edit</a></td>
        <td><a href="<%= appPath%>/review/delete.htm?id=<%= review.getId()%>">Delete</a></td>


    </tr>
    <%  }%>
</tbody>
</table>

<%@include  file="../pageFooter" %>
