<%-- 
    Document   : addCategory
    Created on : Aug 17, 2013, 11:21:52 AM
    Author     : lorinpa public-action.org
--%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<jsp:include page="/jspf/pageHeader.jspf" />

        <h1>Add  Book Category</h1>
         <c:if test="${not empty message}"><div class="message green">${message}</div></c:if>
        <form:form modelAttribute="bookCat">
            <form:errors path="*" cssClass="errorBox" />
    <table class="pure-table">
        <tr>
            <td>Book :</td>
            <td><form:select path="bookId.id"  items= "${books}" /></td>
            <td><form:errors path="bookId.id" /></td>
        </tr>
        <tr>
            <td>Category :</td>
            <td><form:select path="categoryId.id" items="${categories}"/></td>
            <td><form:errors path="categoryId.id" /></td>
        </tr>
        
        <tr>
            <td colspan="2">
                <input type="submit" value="Save Changes" />
            </td>
        </tr>
    </table>
</form:form>
    
<%@include  file="../pageFooter" %>