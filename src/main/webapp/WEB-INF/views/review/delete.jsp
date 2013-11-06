<%-- 
    Document   : addCategory
    Created on : Aug 17, 2013, 11:21:52 AM
    Author     : lorinpa public-action.org
--%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<jsp:include page="/jspf/pageHeader.jspf" />

        <h1>Edit  Review</h1>
         <c:if test="${not empty message}"><div class="message green">${message}</div></c:if>
        <form:form modelAttribute="review">
            <form:errors path="*" cssClass="errorBox" />
    <table class="pure-table">
        <tr>
            <td>Rating (Stars) :</td>
            <td><form:input disabled="true" path="stars" /></td>
            <td><form:errors path="stars" /></td>
        </tr>
        <tr>
            <td>Book :</td>
            <td><form:input disabled="true" path="bookId.title" /></td>
            <td><form:errors path="bookId.title" /></td>
        </tr>
          <tr>
            <td>Details :</td>
            <td><form:textarea disabled="true"  path="body" /></td>
            <td><form:errors path="body" /></td>
        </tr>
        <tr>
            <td colspan="2">
                <input type="submit" value="Confirm Delete" />
            </td>
        </tr>
    </table>
</form:form>
    
<%@include  file="../pageFooter" %>