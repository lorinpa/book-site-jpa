<%-- 
    Document   : delete book
    Created on : Aug 17, 2013, 11:21:52 AM
    Author     : lorinpa public-action.org
--%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<jsp:include page="/jspf/pageHeader.jspf" />

        <h1>Delete  Book</h1>
         <c:if test="${not empty message}"><div class="message green">${message}</div></c:if>
        <form:form modelAttribute="book">
            <form:errors path="*" cssClass="errorBox" />
    <table class="pure-table">
        <tr>
            <td>Title :</td>
            <td><form:input disabled="true"   path="title" /></td>
            <td><form:errors path="title" /></td>
        </tr>
        <tr>
            <td>Author :</td>
            <td><%= request.getAttribute("authorName") %> </td>
            <td></td>
        </tr>
        
        <tr>
            <td colspan="2">
                <input type="submit" value="Confirm Delete" />
            </td>
        </tr>
    </table>
</form:form>
    
<%@include  file="../pageFooter" %>