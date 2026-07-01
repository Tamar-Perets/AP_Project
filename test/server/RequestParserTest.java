package server;

import org.junit.Test;

import server.RequestParser;
import server.RequestParser.RequestInfo;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;

public class RequestParserTest {

    // Helper method to create a BufferedReader from a free-text string
    private BufferedReader createReader(String httpRequest) {
        return new BufferedReader(new StringReader(httpRequest));
    }

    @Test
    public void testHappyPathGetRequest() throws IOException {
        String request = "GET /api/users/list?id=123&role=admin HTTP/1.1\r\n" +
                         "Host: localhost\r\n" +
                         "Accept: text/html\r\n" +
                         "\r\n";

        RequestInfo ri = RequestParser.parseRequest(createReader(request));

        assertNotNull(ri);
        assertEquals("GET", ri.getHttpCommand());

        // getUri should return the full URI, including query string
        assertEquals("/api/users/list?id=123&role=admin", ri.getUri());

        // uriSegments should be based only on the path, without parameters
        assertArrayEquals(new String[]{"api", "users", "list"}, ri.getUriSegments());

        assertEquals("123", ri.getParameters().get("id"));
        assertEquals("admin", ri.getParameters().get("role"));
        assertEquals(2, ri.getParameters().size());

        assertEquals(0, ri.getContent().length);
    }

    @Test
    public void testGetRequestWithoutParameters() throws IOException {
        String request = "GET /index.html HTTP/1.1\r\n" +
                         "\r\n";

        RequestInfo ri = RequestParser.parseRequest(createReader(request));

        assertNotNull(ri);
        assertEquals("GET", ri.getHttpCommand());
        assertEquals("/index.html", ri.getUri());
        assertTrue(ri.getParameters().isEmpty());
        assertArrayEquals(new String[]{"index.html"}, ri.getUriSegments());
        assertEquals(0, ri.getContent().length);
    }

    @Test
    public void testDirtySegmentsWithMultipleSlashes() throws IOException {
        String request = "GET //api///users// HTTP/1.1\r\n" +
                         "\r\n";

        RequestInfo ri = RequestParser.parseRequest(createReader(request));

        assertNotNull(ri);
        assertEquals("//api///users//", ri.getUri());
        assertArrayEquals(new String[]{"api", "users"}, ri.getUriSegments());
        assertEquals(0, ri.getContent().length);
    }

    @Test
    public void testDirtyParametersAndMultipleEquals() throws IOException {
        String request = "GET /search?valid=yes&&dirty=val=som&invalid HTTP/1.1\r\n" +
                         "\r\n";

        RequestInfo ri = RequestParser.parseRequest(createReader(request));

        assertNotNull(ri);
        assertEquals("/search?valid=yes&&dirty=val=som&invalid", ri.getUri());

        assertEquals("yes", ri.getParameters().get("valid"));

        // dirty=val=som has too many '=' signs, so it should be ignored
        assertNull(ri.getParameters().get("dirty"));

        // invalid has no '=', so it should be ignored
        assertNull(ri.getParameters().get("invalid"));

        assertEquals(1, ri.getParameters().size());
    }

    @Test
    public void testParameterWithEmptyValueIsAllowed() throws IOException {
        String request = "GET /api?name=&id=123 HTTP/1.1\r\n" +
                         "\r\n";

        RequestInfo ri = RequestParser.parseRequest(createReader(request));

        assertNotNull(ri);
        assertEquals("/api?name=&id=123", ri.getUri());

        assertTrue(ri.getParameters().containsKey("name"));
        assertEquals("", ri.getParameters().get("name"));

        assertEquals("123", ri.getParameters().get("id"));
        assertEquals(2, ri.getParameters().size());
    }

    @Test
    public void testMissingParameterKeysAreIgnored() throws IOException {
        String request = "GET /api?=hi&=&valid=yes HTTP/1.1\r\n" +
                         "\r\n";

        RequestInfo ri = RequestParser.parseRequest(createReader(request));

        assertNotNull(ri);

        // Parameters with empty key should be ignored
        assertFalse(ri.getParameters().containsKey(""));
        assertNull(ri.getParameters().get("hi"));

        assertEquals("yes", ri.getParameters().get("valid"));
        assertEquals(1, ri.getParameters().size());
    }

    @Test
    public void testPostWithContentImmediatelyAfterHeaders() throws IOException {
        String request = "POST /submit HTTP/1.1\r\n" +
                         "Host: localhost\r\n" +
                         "Content-Length: 12\r\n" +
                         "\r\n" +
                         "hello world!";

        RequestInfo ri = RequestParser.parseRequest(createReader(request));

        assertNotNull(ri);
        assertEquals("POST", ri.getHttpCommand());
        assertEquals("/submit", ri.getUri());
        assertArrayEquals(new String[]{"submit"}, ri.getUriSegments());

        String body = new String(ri.getContent(), StandardCharsets.UTF_8);
        assertEquals("hello world!", body);
        assertEquals(12, ri.getContent().length);
    }

    @Test
    public void testPostWithContentAfterExtraEmptyLine() throws IOException {
        String request = "POST /submit HTTP/1.1\r\n" +
                         "Host: localhost\r\n" +
                         "Content-Length: 12\r\n" +
                         "\r\n" +
                         "\r\n" +
                         "hello world!";

        RequestInfo ri = RequestParser.parseRequest(createReader(request));

        assertNotNull(ri);
        assertEquals("POST", ri.getHttpCommand());
        assertEquals("/submit", ri.getUri());

        String body = new String(ri.getContent(), StandardCharsets.UTF_8);
        assertEquals("hello world!", body);
        assertEquals(12, ri.getContent().length);
    }

    @Test
    public void testPostWithMetadataLineBeforeContent() throws IOException {
        String request = "POST /upload HTTP/1.1\r\n" +
                         "Host: localhost\r\n" +
                         "Content-Length: 5\r\n" +
                         "\r\n" +
                         "filename=\"hello_world.txt\"\r\n" +
                         "\r\n" +
                         "hello";

        RequestInfo ri = RequestParser.parseRequest(createReader(request));

        assertNotNull(ri);
        assertEquals("POST", ri.getHttpCommand());
        assertEquals("/upload", ri.getUri());

        // If your parser supports metadata lines, filename should be added to parameters
        assertEquals("\"hello_world.txt\"", ri.getParameters().get("filename"));

        String body = new String(ri.getContent(), StandardCharsets.UTF_8);
        assertEquals("hello", body);
        assertEquals(5, ri.getContent().length);
    }

    @Test
    public void testPostWithMultipleMetadataLinesBeforeContent() throws IOException {
        String request = "POST /upload HTTP/1.1\r\n" +
                         "Host: localhost\r\n" +
                         "Content-Length: 5\r\n" +
                         "\r\n" +
                         "filename=\"a.txt\"\r\n" +
                         "type=text\r\n" +
                         "\r\n" +
                         "hello";

        RequestInfo ri = RequestParser.parseRequest(createReader(request));

        assertNotNull(ri);
        assertEquals("POST", ri.getHttpCommand());
        assertEquals("/upload", ri.getUri());

        assertEquals("\"a.txt\"", ri.getParameters().get("filename"));
        assertEquals("text", ri.getParameters().get("type"));

        String body = new String(ri.getContent(), StandardCharsets.UTF_8);
        assertEquals("hello", body);
        assertEquals(5, ri.getContent().length);
    }

    @Test
    public void testPostWithLowerCaseContentLengthHeader() throws IOException {
        String request = "POST /submit HTTP/1.1\r\n" +
                         "host: localhost\r\n" +
                         "content-length: 5\r\n" +
                         "\r\n" +
                         "hello";

        RequestInfo ri = RequestParser.parseRequest(createReader(request));

        assertNotNull(ri);
        assertEquals("POST", ri.getHttpCommand());

        String body = new String(ri.getContent(), StandardCharsets.UTF_8);
        assertEquals("hello", body);
        assertEquals(5, ri.getContent().length);
    }

    @Test
    public void testPostWithContentButNoContentLengthHeaderShouldIgnoreBody() throws IOException {
        String request = "POST /submit HTTP/1.1\r\n" +
                         "Host: localhost\r\n" +
                         "\r\n" +
                         "some hidden data";

        RequestInfo ri = RequestParser.parseRequest(createReader(request));

        assertNotNull(ri);
        assertEquals("POST", ri.getHttpCommand());
        assertEquals("/submit", ri.getUri());

        // If there is no Content-Length, parser should not read content
        assertEquals(0, ri.getContent().length);
    }

    @Test
    public void testContentLengthZeroMeansEmptyContent() throws IOException {
        String request = "POST /submit HTTP/1.1\r\n" +
                         "Host: localhost\r\n" +
                         "Content-Length: 0\r\n" +
                         "\r\n";

        RequestInfo ri = RequestParser.parseRequest(createReader(request));

        assertNotNull(ri);
        assertEquals("POST", ri.getHttpCommand());
        assertEquals(0, ri.getContent().length);
    }

    @Test(expected = IOException.class)
    public void testMultipleQuestionMarksShouldThrowException() throws IOException {
        String request = "GET /api?id=1?name=test HTTP/1.1\r\n" +
                         "\r\n";

        RequestParser.parseRequest(createReader(request));
    }

    @Test(expected = IOException.class)
    public void testUnsupportedMethodShouldThrowException() throws IOException {
        String request = "PUT /api/resource HTTP/1.1\r\n" +
                         "\r\n";

        RequestParser.parseRequest(createReader(request));
    }

    @Test(expected = IOException.class)
    public void testMalformedShortRequestShouldThrowException() throws IOException {
        String request = "GET \r\n" +
                         "\r\n";

        RequestParser.parseRequest(createReader(request));
    }

    @Test
    public void testNullReaderResultWhenRequestIsEmpty() throws IOException {
        String request = "";

        RequestInfo ri = RequestParser.parseRequest(createReader(request));

        assertNull(ri);
    }
    

	@Test(expected = IOException.class)
	public void testEmptyFirstLineShouldThrowException() throws IOException {
	    String request = "\r\n" +
	                     "Host: localhost\r\n" +
	                     "\r\n";
	
	    RequestParser.parseRequest(createReader(request));
	}
	

	@Test(expected = IOException.class)
	public void testBlankFirstLineShouldThrowException() throws IOException {
	    String request = "     \r\n" +
	                     "Host: localhost\r\n" +
	                     "\r\n";
	
	    RequestParser.parseRequest(createReader(request));
	}
	

	@Test
	public void testPostWithMultiLineContent() throws IOException {
	    String bodyText = "hello\r\nworld"; // length = 12
	
	    String request = "POST /submit HTTP/1.1\r\n" +
	                     "Host: localhost\r\n" +
	                     "Content-Length: 12\r\n" +
	                     "\r\n" +
	                     bodyText;
	
	    RequestInfo ri = RequestParser.parseRequest(createReader(request));
	
	    assertNotNull(ri);
	    assertEquals("POST", ri.getHttpCommand());
	    assertEquals("/submit", ri.getUri());
	
	    String body = new String(ri.getContent(), StandardCharsets.UTF_8);
	    assertEquals(bodyText, body);
	    assertEquals(12, ri.getContent().length);
	}
	

	@Test
	public void testPostWithMetadataAndMultiLineContent() throws IOException {
	    String bodyText = "hello\r\nworld"; // length = 12
	
	    String request = "POST /upload HTTP/1.1\r\n" +
	                     "Host: localhost\r\n" +
	                     "Content-Length: 12\r\n" +
	                     "\r\n" +
	                     "filename=\"a.txt\"\r\n" +
	                     "\r\n" +
	                     bodyText;
	
	    RequestInfo ri = RequestParser.parseRequest(createReader(request));
	
	    assertNotNull(ri);
	    assertEquals("POST", ri.getHttpCommand());
	    assertEquals("/upload", ri.getUri());
	
	    assertEquals("\"a.txt\"", ri.getParameters().get("filename"));
	
	    String body = new String(ri.getContent(), StandardCharsets.UTF_8);
	    assertEquals(bodyText, body);
	    assertEquals(12, ri.getContent().length);
	}
	
	@Test
	public void testContentLongerThanContentLengthReadsOnlyDeclaredLength() throws IOException {
	    String request = "POST /submit HTTP/1.1\r\n" +
	                     "Host: localhost\r\n" +
	                     "Content-Length: 5\r\n" +
	                     "\r\n" +
	                     "hello world";
	
	    RequestInfo ri = RequestParser.parseRequest(createReader(request));
	
	    assertNotNull(ri);
	
	    String body = new String(ri.getContent(), StandardCharsets.UTF_8);
	    assertEquals("hello", body);
	    assertEquals(5, ri.getContent().length);
	}
	
	@Test
	public void testContentWithoutTrailingNewline() throws IOException {
	    String request = "POST /submit HTTP/1.1\r\n" +
	                     "Content-Length: 5\r\n" +
	                     "\r\n" +
	                     "hello";
	
	    RequestInfo ri = RequestParser.parseRequest(createReader(request));
	
	    String body = new String(ri.getContent(), StandardCharsets.UTF_8);
	    assertEquals("hello", body);
	    assertEquals(5, ri.getContent().length);
	}

	@Test
	public void testContentWithTrailingNewline() throws IOException {
	    String request = "POST /submit HTTP/1.1\r\n" +
	                     "Content-Length: 6\r\n" +
	                     "\r\n" +
	                     "hello\n";
	
	    RequestInfo ri = RequestParser.parseRequest(createReader(request));
	
	    String body = new String(ri.getContent(), StandardCharsets.UTF_8);
	    assertEquals("hello\n", body);
	    assertEquals(6, ri.getContent().length);
	}

	@Test
	public void testMultiLineContentPreservesNewline() throws IOException {
	    String bodyText = "hello\nworld"; // length = 11
	
	    String request = "POST /submit HTTP/1.1\r\n" +
	                     "Content-Length: 11\r\n" +
	                     "\r\n" +
	                     bodyText;
	
	    RequestInfo ri = RequestParser.parseRequest(createReader(request));
	
	    String body = new String(ri.getContent(), StandardCharsets.UTF_8);
	    assertEquals(bodyText, body);
	    assertEquals(11, ri.getContent().length);
	}




}