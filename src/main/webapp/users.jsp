<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.8/dist/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-sRIl4kxILFvY47J16cr9ZwB07vP4J8+LH7qKQnuqkuIAvNWLzeN8tE5YBujZqJLB" crossorigin="anonymous">
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.8/dist/js/bootstrap.bundle.min.js" integrity="sha384-FKyoEForCGlyvwx9Hj09JcYn3nv7wiPVlz7YYwJrWVcXK/BmnVDxM+D2scQbITxI" crossorigin="anonymous"></script>
    <title>Listado de productos</title>
</head>
<body>
<div class="container">
 <c:if test="${username.present}">
     <a class="btn btn-primary my-2" href="${pageContext.request.contextPath}/usuarios/form">crear [+]</a>
 </c:if>
 <table class="table table-hover table-striped">
     <tr>
         <th>id</th>
         <th>username</th>
         <th>email</th>
             <c:if test="${username.present}">
             <th>editar</th>
             <th>eliminar</th>
             </c:if>
     </tr>
     <c:forEach items="${usuarios}" var="u">
         <tr>
             <td>${u.id}</td>
             <td>${u.username}</td>
             <td>${u.email}</td>
             <c:if test="${username.present}">
                 <td><a class="btn btn-sm btn-success" href="${pageContext.request.contextPath}/usuarios/form?id=${u.id}">editar</a></td>
                 <td><a class="btn btn-sm btn-danger" onclick="return confirm('esta seguro que desea eliminar?');"
                        href="${pageContext.request.contextPath}/usuarios/eliminar?id=${u.id}">eliminar</a></td>
                 </c:if>
         </tr>
     </c:forEach>
 </table>
</div>
</body>
</html>