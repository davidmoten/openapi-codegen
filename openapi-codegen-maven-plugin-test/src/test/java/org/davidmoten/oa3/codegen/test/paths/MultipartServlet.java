package org.davidmoten.oa3.codegen.test.paths;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.davidmoten.oa3.codegen.test.paths.schema.Point;
import org.davidmoten.oa3.codegen.util.Util;

import com.fasterxml.jackson.databind.ObjectMapper;

public class MultipartServlet extends HttpServlet {

    private static final long serialVersionUID = -8484071730022631939L;

    private static final ObjectMapper MAPPER = Globals.config().mapper();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        ServletFileUpload upload = new ServletFileUpload();

        // Parse the request
        try {
            FileItemIterator iter = upload.getItemIterator(req);
            int count = 0;
            while (iter.hasNext()) {
                FileItemStream item = iter.next();
                String name = item.getFieldName();
                System.out.println(name);
                if (name.equals("point")) {
                    Point point = MAPPER.readValue(item.openStream(), Point.class);
                    System.out.println(point);
                    count++;
                } else if (name.equals("description")) {
                    String description = MAPPER.readTree(item.openStream()).asText();
                    System.out.println(description);
                    count++;
                } else if (name.equals("document")) {
                    try (InputStream in = item.openStream()) {
                        byte[] b = Util.read(in);
                        if (!Arrays.equals(new byte[] {1,2,3}, b)) {
                            throw new IllegalStateException();
                        }
                        System.out.println("document bytes count="+ b.length);
                    }
                    count++;
                }
            } 
            if (count != 3) {
                throw new IllegalStateException("wrong count");
            }
        } catch (FileUploadException e) {
            throw new RuntimeException(e);
        }

        resp.setStatus(200);
        // must do this other wise get ISO-88951
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json");
        resp.getWriter().write("{}");
    }

}
