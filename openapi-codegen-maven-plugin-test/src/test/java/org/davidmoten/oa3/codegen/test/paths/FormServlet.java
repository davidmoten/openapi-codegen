package org.davidmoten.oa3.codegen.test.paths;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class FormServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (req.getParameter("name") == null) {
            throw new IllegalArgumentException("name cannot be null");
        }
        if (req.getParameter("count") == null) {
            throw new IllegalArgumentException("count cannot be null");
        }
        resp.setStatus(200);
        // must do this other wise get ISO-88951
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json");
        resp.getWriter().write("{}");
    }

}
