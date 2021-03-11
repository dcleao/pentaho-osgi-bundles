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
import org.mockito.stubbing.Stubber;

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
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
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
  private static URI TEST_PROTECTED_URI;

  private static final String[] TEST_CSRF_TOKENS = new String[] { "test-token-value", "test-token-value-2" };
  private static final String[] TEST_CSRF_HEADERS = new String[] { "test-token-header", "test-token-header-2" };
  private static final String[] TEST_CSRF_PARAMETERS = new String[] { "test-token-param", "test-token-param-2" };

  static {
    try {
      TEST_SERVICE_URI = new URI( "http://corp.com:8080/pentaho/api/csrf/service" );
      TEST_PROTECTED_URI = new URI( "http://example.org/test/1" );
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

  private ClientRequest createClientRequestMock( @Nonnull URI uri ) {

    ClientRequest requestMock = mock( ClientRequest.class );
    when( requestMock.getURI() ).thenReturn( uri );
    when( requestMock.getProperties() ).thenReturn( createRequestPropertiesMapAB() );
    when( requestMock.getHeaders() ).thenReturn( new StringKeyObjectValueIgnoreCaseMultivaluedMap() );

    return requestMock;
  }

  private ClientRequest createTokenClientRequestMock() {

    ClientRequest tokenRequestMock = mock( ClientRequest.class );
    when( tokenRequestMock.getProperties() ).thenReturn( createRequestPropertiesMapC() );

    return tokenRequestMock;
  }

  // Stub CsrfTokenFilter#newClientRequest( URI uri, String method ) : ClientRequest
  private void stubNewTokenClientRequest( @Nonnull CsrfTokenFilter filterSpy,
                                          @Nonnull ClientRequest firstRequest,
                                          ClientRequest... additionalRequests ) {
    Stubber stubber = doReturn( firstRequest );

    for ( ClientRequest additionalRequest : additionalRequests ) {
      stubber = stubber.doReturn( additionalRequest );
    }

    stubber
      .when( filterSpy )
      .newClientRequest( any( URI.class ), eq( HttpMethod.GET ) );
  }

  // Stub CsrfTokenFilter#handleNext( ClientRequest request ) : ClientResponse
  private void stubHandleNextResponse( @Nonnull CsrfTokenFilter filterSpy,
                                       @Nonnull ClientResponse firstResponse,
                                       ClientResponse... additionalResponses ) {

    Stubber stubber = doReturn( firstResponse );

    for ( ClientResponse additionalResponse : additionalResponses ) {
      stubber = stubber.doReturn( additionalResponse );
    }

    stubber
      .when( filterSpy )
      .handleNext( any( ClientRequest.class ) );
  }

  private ClientResponse createTokenResponseOk( int tokenIndex ) {
    ClientResponse tokenResponseMock = mock( ClientResponse.class );
    when( tokenResponseMock.getStatus() ).thenReturn( 204 );

    MultivaluedMap<String, String> responseHeadersMap = new MultivaluedMapImpl();
    responseHeadersMap
      .put( CsrfUtil.RESPONSE_HEADER_HEADER, Collections.singletonList( TEST_CSRF_HEADERS[ tokenIndex ] ) );
    responseHeadersMap
      .put( CsrfUtil.RESPONSE_HEADER_PARAM, Collections.singletonList( TEST_CSRF_PARAMETERS[ tokenIndex ] ) );
    responseHeadersMap
      .put( CsrfUtil.RESPONSE_HEADER_TOKEN, Collections.singletonList( TEST_CSRF_TOKENS[ tokenIndex ] ) );

    when( tokenResponseMock.getHeaders() ).thenReturn( responseHeadersMap );

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

  private void checkTokenIsPresentInRequest( @Nonnull ClientRequest request, int tokenIndex ) {

    MultivaluedMap<String, Object> headersMap = request.getHeaders();

    assertNotNull( headersMap );

    List<Object> csrfHeaders = headersMap.get( TEST_CSRF_HEADERS[ tokenIndex ] );

    assertNotNull( csrfHeaders );
    assertEquals( 1, csrfHeaders.size() );

    assertEquals( TEST_CSRF_TOKENS[ tokenIndex ], csrfHeaders.get( 0 ) );
  }

  @Test
  public void testATokenIsFetchedAndThenUsed() {

    ClientRequest requestMock = createClientRequestMock( TEST_PROTECTED_URI );
    ClientRequest tokenRequestMock = createTokenClientRequestMock();

    CsrfTokenFilter filterSpy = spy( new CsrfTokenFilter( TEST_SERVICE_URI ) );

    stubNewTokenClientRequest( filterSpy, tokenRequestMock );

    stubHandleNextResponse( filterSpy, createTokenResponseOk( 0 ), createActualResponseOk() );

    ClientResponse response = filterSpy.handle( requestMock );

    assertNotNull( response );
    assertEquals( 200, response.getStatus() );

    ArgumentCaptor<ClientRequest> requestsCaptor = ArgumentCaptor.forClass( ClientRequest.class );

    // One call for the token request, the other for the actual request.
    verify( filterSpy, times( 2 ) )
      .handleNext( requestsCaptor.capture() );

    List<ClientRequest> clientRequests = requestsCaptor.getAllValues();

    assertSame( tokenRequestMock, clientRequests.get( 0 ) );
    assertSame( requestMock, clientRequests.get( 1 ) );

    // Check the token header is present in the request.
    checkTokenIsPresentInRequest( requestMock, 0 );
  }

  @Test
  public void testTokenRequestAlsoContainsAllPropertiesOfActualRequest() {

    ClientRequest requestMock = createClientRequestMock( TEST_PROTECTED_URI );
    ClientRequest tokenRequestMock = createTokenClientRequestMock();

    CsrfTokenFilter filterSpy = spy( new CsrfTokenFilter( TEST_SERVICE_URI ) );

    stubNewTokenClientRequest( filterSpy, tokenRequestMock );
    stubHandleNextResponse( filterSpy, createTokenResponseOk( 0 ), createActualResponseOk() );

    filterSpy.handle( requestMock );

    Map<String, Object> props = tokenRequestMock.getProperties();
    assertEquals( 3, props.size() );
    assertTrue( props.containsKey( "A" ) );
    assertTrue( props.containsKey( "B" ) );
    assertTrue( props.containsKey( "C" ) );
  }

  @Test
  public void testATokenIsFetchedAndItsResponseReturnedIfFailed() {

    ClientRequest requestMock = createClientRequestMock( TEST_PROTECTED_URI );
    ClientResponse tokenResponseFailed = createTokenResponseFailed();

    CsrfTokenFilter filterSpy = spy( new CsrfTokenFilter( TEST_SERVICE_URI ) );
    stubNewTokenClientRequest( filterSpy, createTokenClientRequestMock() );
    stubHandleNextResponse( filterSpy, tokenResponseFailed );

    ClientResponse response = filterSpy.handle( requestMock );

    assertEquals( tokenResponseFailed, response );
  }

  @Test
  public void testATokenIsNotReusedEvenIfRightAfterAndForTheSameURL() {

    ClientRequest actualRequest1Mock = createClientRequestMock( TEST_PROTECTED_URI );
    ClientRequest actualRequest2Mock = createClientRequestMock( TEST_PROTECTED_URI );
    ClientRequest tokenRequest1Mock = createTokenClientRequestMock();
    ClientRequest tokenRequest2Mock = createTokenClientRequestMock();

    CsrfTokenFilter filterSpy = spy( new CsrfTokenFilter( TEST_SERVICE_URI ) );

    stubNewTokenClientRequest( filterSpy, tokenRequest1Mock, tokenRequest2Mock );

    stubHandleNextResponse(
      filterSpy,
      createTokenResponseOk( 0 ),
      createActualResponseOk(),
      createTokenResponseOk( 1 ),
      createActualResponseOk() );

    // ----

    ClientResponse response1 = filterSpy.handle( actualRequest1Mock );

    ClientResponse response2 = filterSpy.handle( actualRequest2Mock );

    // ----

    assertNotNull( response1 );
    assertEquals( 200, response1.getStatus() );
    assertNotNull( response2 );
    assertEquals( 200, response2.getStatus() );

    ArgumentCaptor<ClientRequest> requestsCaptor = ArgumentCaptor.forClass( ClientRequest.class );

    verify( filterSpy, times( 4 ) )
      .handleNext( requestsCaptor.capture() );

    List<ClientRequest> clientRequests = requestsCaptor.getAllValues();

    assertSame( tokenRequest1Mock, clientRequests.get( 0 ) );
    assertSame( actualRequest1Mock, clientRequests.get( 1 ) );
    checkTokenIsPresentInRequest( actualRequest1Mock, 0 );

    assertSame( tokenRequest2Mock, clientRequests.get( 2 ) );
    assertSame( actualRequest2Mock, clientRequests.get( 3 ) );
    checkTokenIsPresentInRequest( actualRequest2Mock, 1 );
  }
}
