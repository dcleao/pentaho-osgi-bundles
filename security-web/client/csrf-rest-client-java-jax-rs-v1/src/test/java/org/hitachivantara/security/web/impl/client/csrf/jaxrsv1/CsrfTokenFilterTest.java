/*!
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2021 Hitachi Vantara. All rights reserved.
 */

package org.hitachivantara.security.web.impl.client.csrf.jaxrsv1;

import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import com.sun.jersey.core.util.StringKeyObjectValueIgnoreCaseMultivaluedMap;
import org.hitachivantara.security.web.impl.client.csrf.jaxrsv1.util.internal.CsrfUtil;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import javax.annotation.Nonnull;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.MultivaluedMap;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CsrfTokenFilterTest {

  private static URI TEST_SERVICE_URI;
  private static URI TEST_1_URI;
  private static URI TEST_2_URI;
  private static URI TEST_3_URI;
  private static final String TEST_CSRF_TOKEN = "test-token-value";
  private static final String TEST_CSRF_HEADER = "test-token-header";
  private static final String TEST_CSRF_PARAMETER = "test-token-param";

  private static final String TEST_CSRF_TOKEN_2 = "test-token-value-2";
  private static final String TEST_CSRF_HEADER_2 = "test-token-header-2";
  private static final String TEST_CSRF_PARAMETER_2 = "test-token-param-2";

  static {
    try {
      TEST_SERVICE_URI = new URI( "http://corp.com:8080/pentaho/api/csrf/service" );
      TEST_1_URI = new URI( "http://example.org/test/1" );
      TEST_2_URI = new URI( "http://example.org/test/2" );
      TEST_3_URI = new URI( "http://example.org/test/3" );
    } catch ( URISyntaxException e ) {
      e.printStackTrace();
    }
  }

  private Map<String, Object> createRequestPropertiesMapAB() {

    Map<String, Object> props = new HashMap<>();
    props.put( "A", "A" );
    props.put( "B", "B" );

    return props;
  }

  private Map<String, Object> createRequestPropertiesMapC() {

    Map<String, Object> props = new HashMap<>();
    props.put( "C", "C" );

    return props;
  }

  private MultivaluedMap<String, String> createTokenOkResponseHeadersMap1() {

    MultivaluedMap<String, String> responseHeadersMap = new MultivaluedMapImpl();

    responseHeadersMap.put( CsrfUtil.RESPONSE_HEADER_HEADER, Collections.singletonList( TEST_CSRF_HEADER ) );
    responseHeadersMap.put( CsrfUtil.RESPONSE_HEADER_PARAM, Collections.singletonList( TEST_CSRF_PARAMETER ) );
    responseHeadersMap.put( CsrfUtil.RESPONSE_HEADER_TOKEN, Collections.singletonList( TEST_CSRF_TOKEN ) );

    return responseHeadersMap;
  }


  private MultivaluedMap<String, String> createTokenOkResponseHeadersMap2() {

    MultivaluedMap<String, String> responseHeadersMap = new MultivaluedMapImpl();

    responseHeadersMap.put( CsrfUtil.RESPONSE_HEADER_HEADER, Collections.singletonList( TEST_CSRF_HEADER_2 ) );
    responseHeadersMap.put( CsrfUtil.RESPONSE_HEADER_PARAM, Collections.singletonList( TEST_CSRF_PARAMETER_2 ) );
    responseHeadersMap.put( CsrfUtil.RESPONSE_HEADER_TOKEN, Collections.singletonList( TEST_CSRF_TOKEN_2 ) );

    return responseHeadersMap;
  }

  private ClientRequest createClientRequestMock() {

    ClientRequest requestMock = mock( ClientRequest.class );
    when( requestMock.getURI() )
      .thenReturn( TEST_1_URI );

    when( requestMock.getProperties() )
      .thenReturn( createRequestPropertiesMapAB() );

    ClientRequest requestClone1Mock = mock( ClientRequest.class );
    when( requestClone1Mock.getURI() ).thenReturn( TEST_2_URI );
    when( requestClone1Mock.getHeaders() ).thenReturn( new StringKeyObjectValueIgnoreCaseMultivaluedMap() );

    ClientRequest requestClone2Mock = mock( ClientRequest.class );
    when( requestClone2Mock.getURI() ).thenReturn( TEST_3_URI );
    when( requestClone2Mock.getHeaders() ).thenReturn( new StringKeyObjectValueIgnoreCaseMultivaluedMap() );

    when( requestMock.clone() )
      .thenReturn( requestClone1Mock )
      .thenReturn( requestClone2Mock );

    return requestMock;
  }

  private CsrfTokenFilter createTestSubjectSpyBasic() {

    CsrfTokenFilter filterSpy = spy( new CsrfTokenFilter( TEST_SERVICE_URI ) );

    // Stub CsrfTokenFilter#newClientRequest( URI uri, String method ) : ClientRequest
    ClientRequest tokenRequestMock = mock( ClientRequest.class );
    when( tokenRequestMock.getURI() ).thenReturn( TEST_SERVICE_URI );
    when( tokenRequestMock.getMethod() ).thenReturn( HttpMethod.GET );
    when( tokenRequestMock.getProperties() ).thenReturn( createRequestPropertiesMapC() );

    doReturn( tokenRequestMock )
      .when( filterSpy )
      .newClientRequest( eq( TEST_SERVICE_URI ), eq( HttpMethod.GET ) );

    return filterSpy;
  }

  private ClientResponse createTokenResponseOk1() {
    ClientResponse tokenResponseMock = mock( ClientResponse.class );
    when( tokenResponseMock.getStatus() ).thenReturn( 204 );
    when( tokenResponseMock.getHeaders() ).thenReturn( createTokenOkResponseHeadersMap1() );
    return tokenResponseMock;
  }

  private ClientResponse createTokenResponseOk2() {
    ClientResponse tokenResponseMock = mock( ClientResponse.class );
    when( tokenResponseMock.getStatus() ).thenReturn( 204 );
    when( tokenResponseMock.getHeaders() ).thenReturn( createTokenOkResponseHeadersMap2() );
    return tokenResponseMock;
  }

  private ClientResponse createTokenResponseFailed() {
    ClientResponse tokenResponseMock = mock( ClientResponse.class );
    when( tokenResponseMock.getStatus() ).thenReturn( 500 );
    return tokenResponseMock;
  }

  private ClientResponse createActualResponseOk() {
    ClientResponse actualResponseMock = mock( ClientResponse.class );
    when( actualResponseMock.getStatus() ).thenReturn( 200 );
    return actualResponseMock;
  }

  private ClientResponse createActualResponse403() {
    ClientResponse actualResponseMock = mock( ClientResponse.class );
    when( actualResponseMock.getStatus() ).thenReturn( 403 );
    return actualResponseMock;
  }

  private void checkToken1IsPresentInRequest( @Nonnull ClientRequest request ) {

    MultivaluedMap<String, Object> headersMap = request.getHeaders();

    assertNotNull( headersMap );

    List<Object> csrfHeaders = headersMap.get( TEST_CSRF_HEADER );

    assertNotNull( csrfHeaders );
    assertEquals( 1, csrfHeaders.size() );

    assertEquals( TEST_CSRF_TOKEN, csrfHeaders.get( 0 ) );
  }

  private void checkToken2IsPresentInRequest( @Nonnull ClientRequest request ) {

    MultivaluedMap<String, Object> headersMap = request.getHeaders();

    assertNotNull( headersMap );

    List<Object> csrfHeaders = headersMap.get( TEST_CSRF_HEADER_2 );

    assertNotNull( csrfHeaders );
    assertEquals( 1, csrfHeaders.size() );

    assertEquals( TEST_CSRF_TOKEN_2, csrfHeaders.get( 0 ) );
  }

  @Test
  public void testGetsATokenOnFirstUseAndUsesIt() {

    ClientRequest requestMock = createClientRequestMock();
    CsrfTokenFilter filterSpy = createTestSubjectSpyBasic();

    // Stub CsrfTokenFilter#handleNext( ClientRequest request ) : ClientResponse
    // These need to be done, in variables, before the `doReturn` calls, or otherwise
    // mockito loses its mind.
    ClientResponse tokenResponse = createTokenResponseOk1();
    ClientResponse actualResponseStatusOk = createActualResponseOk();
    doReturn( tokenResponse )
      .doReturn( actualResponseStatusOk )
      .when( filterSpy )
      .handleNext( any( ClientRequest.class ) );

    ClientResponse response = filterSpy.handle( requestMock );

    assertNotNull( response );
    assertEquals( 200, response.getStatus() );

    ArgumentCaptor<ClientRequest> requestsCaptor = ArgumentCaptor.forClass( ClientRequest.class );

    // One call for the token request, the other for the actual request.
    verify( filterSpy, times( 2 ) )
      .handleNext( requestsCaptor.capture() );

    List<ClientRequest> clientRequests = requestsCaptor.getAllValues();

    // Check the token request.
    ClientRequest tokenRequest = clientRequests.get( 0 );
    assertEquals( TEST_SERVICE_URI, tokenRequest.getURI() );

    // Check the actual request.
    ClientRequest actualRequestClone1 = clientRequests.get( 1 );
    assertEquals( TEST_2_URI, actualRequestClone1.getURI() );

    // Check the token header is present in the actual request clone.
    checkToken1IsPresentInRequest( actualRequestClone1 );
  }

  @Test
  public void testGetsATokenOnFirstUseAndReturnsItsFailedResponse() {

    ClientRequest requestMock = createClientRequestMock();
    CsrfTokenFilter filterSpy = createTestSubjectSpyBasic();

    // Stub CsrfTokenFilter#handleNext( ClientRequest request ) : ClientResponse
    // These need to be done, in variables, before the `doReturn` calls, or otherwise
    // mockito loses its mind.
    ClientResponse tokenResponse = createTokenResponseFailed();

    doReturn( tokenResponse )
      .when( filterSpy )
      .handleNext( any( ClientRequest.class ) );

    ClientResponse response = filterSpy.handle( requestMock );

    assertEquals( tokenResponse, response );
  }

  @Test
  public void testGetsATokenOnFirstUseAndUsesItAndReusesItOnSecondUse() {

    ClientRequest request1Mock = createClientRequestMock();
    ClientRequest request2Mock = createClientRequestMock();
    CsrfTokenFilter filterSpy = createTestSubjectSpyBasic();

    // Stub CsrfTokenFilter#handleNext( ClientRequest request ) : ClientResponse
    // These need to be done, in variables, before the `doReturn` calls, or otherwise
    // mockito loses its mind.
    ClientResponse tokenResponse = createTokenResponseOk1();
    ClientResponse actualResponse1StatusOk = createActualResponseOk();
    ClientResponse actualResponse2StatusOk = createActualResponseOk();

    doReturn( tokenResponse )
      .doReturn( actualResponse1StatusOk )
      .doReturn( actualResponse2StatusOk )
      .when( filterSpy )
      .handleNext( any( ClientRequest.class ) );

    // ----

    ClientResponse response1 = filterSpy.handle( request1Mock );
    ClientResponse response2 = filterSpy.handle( request2Mock );

    // ----

    assertNotNull( response2 );
    assertEquals( 200, response2.getStatus() );

    ArgumentCaptor<ClientRequest> requestsCaptor = ArgumentCaptor.forClass( ClientRequest.class );

    // One call for the token request, the other two for the two actual requests.
    verify( filterSpy, times( 3 ) )
      .handleNext( requestsCaptor.capture() );

    List<ClientRequest> clientRequests = requestsCaptor.getAllValues();

    // Check the token request.
    ClientRequest tokenRequest = clientRequests.get( 0 );
    assertEquals( TEST_SERVICE_URI, tokenRequest.getURI() );

    // Check the actual request (clone 1 of request 1).
    ClientRequest actualRequestClone1 = clientRequests.get( 1 );
    assertEquals( TEST_2_URI, actualRequestClone1.getURI() );

    // Check the actual request (clone 1 of request 2).
    ClientRequest actualRequestClone2 = clientRequests.get( 2 );
    assertEquals( TEST_2_URI, actualRequestClone2.getURI() );

    // Check the token header is present in the actual request clones.
    checkToken1IsPresentInRequest( actualRequestClone1 );
    checkToken1IsPresentInRequest( actualRequestClone2 );
  }

  @Test
  public void testGetsATokenOnFirstUseAndUsesItAndTriesToReuseItOnSecondUseButRenewsItDueTo403() {

    ClientRequest request1Mock = createClientRequestMock();
    ClientRequest request2Mock = createClientRequestMock();
    CsrfTokenFilter filterSpy = createTestSubjectSpyBasic();

    // Stub CsrfTokenFilter#handleNext( ClientRequest request ) : ClientResponse
    // These need to be done, in variables, before the `doReturn` calls, or otherwise
    // mockito loses its mind.
    ClientResponse tokenResponse1 = createTokenResponseOk1();
    ClientResponse actualResponse1StatusOk = createActualResponseOk();
    ClientResponse actualResponse2Status403 = createActualResponse403();
    ClientResponse tokenResponse2 = createTokenResponseOk2();

    doReturn( tokenResponse1 )
      .doReturn( actualResponse1StatusOk )
      .doReturn( actualResponse2Status403 )
      .doReturn( tokenResponse2 )
      .when( filterSpy )
      .handleNext( any( ClientRequest.class ) );

    // ----

    ClientResponse response1 = filterSpy.handle( request1Mock );
    ClientResponse response2 = filterSpy.handle( request2Mock );

    // ----

    assertEquals( tokenResponse2, response2 );

    // One call for the token request
    // another for the first actual request
    // another for the rejected second actual request
    // another for the second token request
    // another for the succeeded second actual request

    ArgumentCaptor<ClientRequest> requestsCaptor = ArgumentCaptor.forClass( ClientRequest.class );
    verify( filterSpy, times( 5 ) )
      .handleNext( requestsCaptor.capture() );

    List<ClientRequest> clientRequests = requestsCaptor.getAllValues();

    // Check the first token request.
    ClientRequest tokenRequest1 = clientRequests.get( 0 );
    assertEquals( TEST_SERVICE_URI, tokenRequest1.getURI() );

    // Check the actual request (clone 1 of request 1).
    ClientRequest actualRequestClone1 = clientRequests.get( 1 );
    assertEquals( TEST_2_URI, actualRequestClone1.getURI() );
    checkToken1IsPresentInRequest( actualRequestClone1 );

    // Check the rejected actual request (clone 1 of request 2).
    ClientRequest actualRequestClone2 = clientRequests.get( 2 );
    assertEquals( TEST_2_URI, actualRequestClone2.getURI() );
    checkToken1IsPresentInRequest( actualRequestClone2 );

    // Check the second token request.
    ClientRequest tokenRequest2 = clientRequests.get( 3 );
    assertEquals( TEST_SERVICE_URI, tokenRequest2.getURI() );

    // Check the succeeded actual request (clone 2 of request 2).
    ClientRequest actualRequestClone3 = clientRequests.get( 4 );
    assertEquals( TEST_3_URI, actualRequestClone3.getURI() );
    checkToken2IsPresentInRequest( actualRequestClone3 );
  }
}
