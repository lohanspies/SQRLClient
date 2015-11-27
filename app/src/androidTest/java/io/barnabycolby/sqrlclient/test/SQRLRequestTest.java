package io.barnabycolby.sqrlclient.test;

import android.support.test.runner.AndroidJUnit4;
import org.junit.*;
import org.junit.runner.RunWith;
import static org.mockito.Mockito.*;
import android.net.Uri;
import java.net.HttpURLConnection;
import java.io.*;

import io.barnabycolby.sqrlclient.sqrl.*;

@RunWith(AndroidJUnit4.class)
public class SQRLRequestTest {
    private Uri uri;
    private SQRLUri sqrlUri;
    private SQRLIdentity sqrlIdentity;
    private SQRLRequest request;

    @Before
    public void setUp() throws Exception {
        // Create the SQRL URI
        uri = Uri.parse("sqrl://www.grc.com/sqrl?nut=P2Kr_4GB49GrwAF_kpDuJA&sfn=R1JD");
        sqrlUri = new SQRLUri(uri);
        sqrlIdentity = new SQRLIdentity();
        request = new SQRLRequest(sqrlUri, sqrlIdentity);
    }

    @Test
    public void createConnectionToTheCorrectURLAllowingIncomingAndOutgoingTraffic() throws Exception {
        HttpURLConnection connection = request.getConnection();
        String actual = connection.getURL().toExternalForm();
        String expected = "https://www.grc.com/sqrl?nut=P2Kr_4GB49GrwAF_kpDuJA&sfn=R1JD";
        Assert.assertEquals(expected, actual);

        Assert.assertTrue(connection.getDoInput());
        Assert.assertTrue(connection.getDoOutput());
    }

    @Test
    public void setCorrectMethodAndHeadersBasedOnURI() throws Exception {
        HttpURLConnection connection = request.getConnection();
        Assert.assertEquals("POST", connection.getRequestMethod());
        Assert.assertEquals(uri.getHost(), connection.getRequestProperty("Host"));
        Assert.assertEquals("SQRL/1", connection.getRequestProperty("User-Agent"));
        Assert.assertEquals("application/x-www-form-urlencoded", connection.getRequestProperty("Content-type"));
    }

    @Test
    public void correctlyGenerateQueryRequest() throws Exception {
        // First, we need to mock the connection object and the writer object
        HttpURLConnection connection = mock(HttpURLConnection.class);
        // We create a partial mock so that we can verify the final message (by calling to string)
        // without having to specify how the message should be constructed
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ByteArrayOutputStream spyOutputStream = spy(outputStream);
        when(connection.getOutputStream()).thenReturn(spyOutputStream);
        when(connection.getResponseCode()).thenReturn(200);
        
        // We also need to mock the SQRLIdentity object so that we know the keys that will be returned
        sqrlIdentity = mock(SQRLIdentity.class);
        String identityKey = "Jjl2OhUyP93M14-AQ3stYMaoZ2vq1BHfmAhxWjM1CuU";
        when(sqrlIdentity.getIdentityKey()).thenReturn(identityKey);
        // This is the expected client and server values
        String expectedClientValue = "dmVyPTENCmNtZD1xdWVyeQ0KaWRrPUpqbDJPaFV5UDkzTTE0LUFRM3N0WU1hb1oydnExQkhmbUFoeFdqTTFDdVUNCg";
        String expectedServerValue = "c3FybDovL3d3dy5ncmMuY29tL3Nxcmw_bnV0PVAyS3JfNEdCNDlHcndBRl9rcER1SkEmc2ZuPVIxSkQ";
        String expectedDataToSign = expectedClientValue + expectedServerValue;
        String signatureOfExpectedData = "je8rKDoBUnS0PdAyYNQQ-RpZ1YtI_bj4dTZCRnKDvTAcG1Vj_FQtPZlnKeajGFlZCJMH2JRWyBkRs5Y747drDw";
        when(sqrlIdentity.signUsingIdentityPrivateKey(expectedDataToSign)).thenReturn(signatureOfExpectedData);

        // Next, instantiate a SQRLRequest object with the mocked objects
        request = new SQRLRequest(sqrlUri, sqrlIdentity, connection);

        // Calculate what the expected data should be
        String expectedData = "client=" + expectedClientValue;
        expectedData += "&server=" + expectedServerValue;
        expectedData += "&ids=" + signatureOfExpectedData;

        // Ask the request object to send the data, and then verify it
        request.send();
        String dataSent = spyOutputStream.toString();
        Assert.assertEquals(expectedData, dataSent);
    }

    @Test
    public void sendShouldThrowExceptionWhenConnectionReturnsNon200() throws Exception {
        assertExceptionThrownWhenConnectionReturnsGivenCode(201);
        assertExceptionThrownWhenConnectionReturnsGivenCode(204);
        assertExceptionThrownWhenConnectionReturnsGivenCode(226);
        assertExceptionThrownWhenConnectionReturnsGivenCode(300);
        assertExceptionThrownWhenConnectionReturnsGivenCode(301);
        assertExceptionThrownWhenConnectionReturnsGivenCode(302);
        assertExceptionThrownWhenConnectionReturnsGivenCode(308);
        assertExceptionThrownWhenConnectionReturnsGivenCode(400);
        assertExceptionThrownWhenConnectionReturnsGivenCode(401);
        assertExceptionThrownWhenConnectionReturnsGivenCode(402);
        assertExceptionThrownWhenConnectionReturnsGivenCode(403);
        assertExceptionThrownWhenConnectionReturnsGivenCode(404);
        assertExceptionThrownWhenConnectionReturnsGivenCode(407);
        assertExceptionThrownWhenConnectionReturnsGivenCode(500);
        assertExceptionThrownWhenConnectionReturnsGivenCode(504);
    }

    @Test
    public void sendMessageWithoutExceptionWhenResponseCodeIs200() throws Exception {
        // Create the necessary mocks
        HttpURLConnection connectionMock = mock(HttpURLConnection.class);
        OutputStream mockOutputStream = mock(OutputStream.class);
        when(connectionMock.getOutputStream()).thenReturn(mockOutputStream);
        when(connectionMock.getResponseCode()).thenReturn(200);

        request = new SQRLRequest(sqrlUri, sqrlIdentity, connectionMock);
        request.send();
    }

    private void assertExceptionThrownWhenConnectionReturnsGivenCode(int responseCode) throws Exception {
        // Create the necessary mocks
        HttpURLConnection connectionMock = mock(HttpURLConnection.class);
        OutputStream mockOutputStream = mock(OutputStream.class);
        when(connectionMock.getOutputStream()).thenReturn(mockOutputStream);
        when(connectionMock.getResponseCode()).thenReturn(responseCode);

        request = new SQRLRequest(sqrlUri, sqrlIdentity, connectionMock);

        // Verify that send throws an exception
        try {
            request.send();
        } catch (IOException ex) {
            return;
        }
        Assert.fail("request.send() did not throw an exception for response code " + responseCode);
    }
}
