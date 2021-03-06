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
 * Copyright (c) 2019-2021 Hitachi Vantara. All rights reserved.
 */

package org.hitachivantara.security.web.impl.client.csrf.jaxrsv1;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandler;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import org.hitachivantara.security.web.impl.client.csrf.jaxrsv1.util.SessionCookiesFilter;
import org.hitachivantara.security.web.impl.client.csrf.jaxrsv1.util.internal.CsrfUtil;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.MultivaluedMap;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CsrfTokenServiceClientTest {

  private static URI TEST_SERVICE_URI;
  private static final String TEST_CSRF_TOKEN = "test-token-value";
  private static final String TEST_CSRF_HEADER = "test-token-header";
  private static final String TEST_CSRF_PARAMETER = "test-token-param";

  static {
    try {
      TEST_SERVICE_URI = new URI( "http://corp.com:8080/pentaho/api/csrf/service" );
    } catch ( URISyntaxException e ) {
      e.printStackTrace();
    }
  }

  private Client mockClient;
  private WebResource mockWebResource;
  private ClientResponse mockResponse;

  @Before
  public void setUp() {
    mockClient = mock( Client.class );
    mockWebResource = mock( WebResource.class );
    mockResponse = mock( ClientResponse.class );

    // ---

    ClientHandler handler = mock( SessionCookiesFilter.class );
    when( mockClient.getHeadHandler() ).thenReturn( handler );

    when( mockClient.resource( any( URI.class ) ) ).thenReturn( mockWebResource );
    when( mockWebResource.get( eq( ClientResponse.class ) ) ).thenReturn( mockResponse );

    // ---

    when( mockResponse.getStatus() ).thenReturn( 204 );
    when( mockResponse.getHeaders() ).thenReturn( createResponseHeadersMap() );
  }

  private MultivaluedMap<String, String> createResponseHeadersMap() {

    MultivaluedMap<String, String> responseHeadersMap = new MultivaluedMapImpl();

    responseHeadersMap.put( CsrfUtil.RESPONSE_HEADER_HEADER, Collections.singletonList( TEST_CSRF_HEADER ) );
    responseHeadersMap.put( CsrfUtil.RESPONSE_HEADER_PARAM, Collections.singletonList( TEST_CSRF_PARAMETER ) );
    responseHeadersMap.put( CsrfUtil.RESPONSE_HEADER_TOKEN, Collections.singletonList( TEST_CSRF_TOKEN ) );

    return responseHeadersMap;
  }

  @Test
  public void testGetTokenWhenResponseIsNot204() {

    when( mockResponse.getStatus() ).thenReturn( 210 );

    CsrfTokenServiceClient csrfTokenClient = new CsrfTokenServiceClient( TEST_SERVICE_URI, mockClient );

    CsrfToken token = csrfTokenClient.getToken();

    assertNull( token );
  }

  @Test
  public void testGetTokenCallsCsrfService() {

    // Bail out asap.
    when( mockResponse.getStatus() ).thenReturn( 210 );

    CsrfTokenServiceClient csrfTokenClient = new CsrfTokenServiceClient( TEST_SERVICE_URI, mockClient );

    csrfTokenClient.getToken();

    verify( mockClient, times( 1 ) )
      .resource( eq( TEST_SERVICE_URI ) );

    verify( mockWebResource, times( 1 ) )
      .get( eq( ClientResponse.class ) );
  }

  // region 204 with Token Present
  @Test
  public void testGetTokenWhenResponseIs204AndTokenIsPresent() {

    when( mockResponse.getStatus() ).thenReturn( 204 );

    CsrfTokenServiceClient csrfTokenClient = new CsrfTokenServiceClient( TEST_SERVICE_URI, mockClient );

    CsrfToken token = csrfTokenClient.getToken();

    assertNotNull( token );

    assertEquals( token.getHeader(), TEST_CSRF_HEADER );
    assertEquals( token.getParameter(), TEST_CSRF_PARAMETER );
    assertEquals( token.getToken(), TEST_CSRF_TOKEN );
  }
  // endregion

  // region 204 with Null or Empty Token
  @Test
  public void testGetTokenWhenResponseIs204AndTokenIsNotPresent() {

    when( mockResponse.getStatus() ).thenReturn( 204 );
    when( mockResponse.getHeaders() ).thenReturn( new MultivaluedMapImpl() );

    CsrfTokenServiceClient csrfTokenClient = new CsrfTokenServiceClient( TEST_SERVICE_URI, mockClient );

    CsrfToken token = csrfTokenClient.getToken();

    assertNull( token );
  }

  @Test
  public void testGetTokenWhenResponseIs204AndTokenIsEmpty() {

    when( mockResponse.getStatus() ).thenReturn( 204 );

    MultivaluedMap<String, String> headers = new MultivaluedMapImpl();
    headers.put( CsrfUtil.RESPONSE_HEADER_TOKEN, Collections.singletonList( "" ) );
    when( mockResponse.getHeaders() ).thenReturn( headers );

    CsrfTokenServiceClient csrfTokenClient = new CsrfTokenServiceClient( TEST_SERVICE_URI, mockClient );

    CsrfToken token = csrfTokenClient.getToken();

    assertNull( token );
  }
  // endregion
}
