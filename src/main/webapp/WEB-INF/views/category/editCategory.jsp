<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<jsp:include page="/jspf/pageHeader.jspf" />

        <h1>Edit  Category</h1>
         <c:if test="${not empty message}"><div class="message green">${message}</div></c:if>
        <form:form modelAttribute="category">
            <form:errors path="*" cssClass="errorBox" />
    <table>
        <tr>
            <td>Category Value:</td>
            <td><form:input path="title" /></td>
            <td><form:errors path="title" /></td>
        </tr>
        <tr>
            <td colspan="2">
                <input type="submit" value="Save Changes" />
            </td>
        </tr>
    </table>
</form:form>
 <%@include  file="../pageFooter" %>
    