import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.ProgressListener;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Logger;

public class Application extends AbstractHandler {
    Logger logger = Logger.getLogger("Application");

    ProgressListener progressListener = new ProgressListener() {
        private long megaBytes = -1;

        public void update(long pBytesRead, long pContentLength, int pItems) {
            long mBytes = pBytesRead / 1000000;
            if (megaBytes == mBytes) {
                return;
            }
            megaBytes = mBytes;
            write("We are currently reading item " + pItems);
            if (pContentLength == -1) {
                write("So far, " + pBytesRead + " bytes have been read.");
            } else {
                write("So far, " + pBytesRead + " of " + pContentLength + " bytes have been read.");
            }
        }
    };

    public void handle(String target,
                       Request baseRequest,
                       HttpServletRequest request,
                       HttpServletResponse response)
            throws IOException, ServletException {

        OutputStream out = response.getOutputStream();
        response.setHeader("Transfer-Encoding", "Chunked");

        write("Is multipart " + ServletFileUpload.isMultipartContent(request));

        ServletFileUpload upload = new ServletFileUpload();
        upload.setProgressListener(progressListener);

        try {
            FileItemIterator iter = upload.getItemIterator(request);

            write("About to read files");
            while (iter.hasNext()) {
                FileItemStream item = iter.next();
                String name = item.getFieldName();
                InputStream stream = item.openStream();
                write("Got a stream");
                if (item.isFormField()) {
                    write("its a form field");
                    write(out, "Form field " + name + " with value "
                            + Streams.asString(stream) + " detected.");
                } else {
                    write("its a file");
                    stream.close();
                    write("stream closed");
                    write(out, "File field " + name + " with file name "
                            + item.getName() + " detected.");
                }
                write("done with one");
            }
        } catch (FileUploadException e) {
            throw new RuntimeException(e);
        }

        response.setStatus(HttpServletResponse.SC_OK);
        baseRequest.setHandled(true);
        out.close();
    }

    private void write(OutputStream out, String message) throws IOException {
        out.write(message.getBytes());
        out.flush();
    }

    private void write(String message) {
        logger.info(message);
    }

    public static void main(String[] args) throws Exception {
        Server server = new Server(8080);
        server.setHandler(new Application());

        server.start();
        server.join();
    }
}