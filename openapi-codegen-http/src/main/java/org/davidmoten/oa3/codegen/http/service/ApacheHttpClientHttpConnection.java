package org.davidmoten.oa3.codegen.http.service;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Optional;
import java.util.function.Consumer;

import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;

public class ApacheHttpClientHttpConnection implements HttpConnection {

    private final HttpUriRequestBase request;

    public ApacheHttpClientHttpConnection(HttpUriRequestBase request) {
        this.request = request;
    }

    @Override
    public void header(String key, String value) {
        request.addHeader(key, value);
    }
    
    @Override
    public void output(Consumer<? super OutputStream> consumer, String contentType, Optional<String> contentEncoding, boolean chunked) {
    }

    @Override
    public Response response() throws IOException {
        return null;
    }


    @Override
    public void close() throws IOException {
        
    }
    
//    private static final class OutputStreamConsumerEntity extends AbstractHttpEntity {
//
//        protected OutputStreamConsumerEntity(ContentType contentType, String contentEncoding) {
//            super(contentType, contentEncoding);
//        }
//
//        @Override
//        public void writeTo(OutputStream outStream) throws IOException {
//        }
//
//        @Override
//        public InputStream getContent() throws IOException, UnsupportedOperationException {
//            // TODO Auto-generated method stub
//            return null;
//        }
//
//        @Override
//        public boolean isStreaming() {
//            // TODO Auto-generated method stub
//            return false;
//        }
//
//        @Override
//        public long getContentLength() {
//            // TODO Auto-generated method stub
//            return 0;
//        }
//
//        @Override
//        public void close() throws IOException {
//            // TODO Auto-generated method stub
//            
//        }
//        
//    }

}
