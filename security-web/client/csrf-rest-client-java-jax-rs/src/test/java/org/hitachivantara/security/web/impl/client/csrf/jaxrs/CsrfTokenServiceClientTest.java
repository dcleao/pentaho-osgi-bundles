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

package org.hitachivantara.security.web.impl.client.csrf.jaxrs;

import org.hitachivantara.security.web.impl.client.csrf.jaxrs.util.SessionCookiesFilter;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.net.URISyntaxException;

import static org.hitachivantara.security.web.impl.client.csrf.jaxrs.CsrfTokenServiceClient.QUERY_PARAM_URL;
import static org.hitachivantara.security.web.impl.client.csrf.jaxrs.CsrfTokenServiceClient.RESPONSE_HEADER_HEADER;
import static org.hitachivantara.security.web.impl.client.csrf.jaxrs.CsrfTokenServiceClient.RESPONSE_HEADER_PARAM;
import static org.hitachivantara.security.web.impl.client.csrf.jaxrs.CsrfTokenServiceClient.RESPONSE_HEADER_TOKEN;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CsrfTokenServiceClientTest {

  private static URI TEST_SERVICE_URI;
  private static URI TEST_PROTECTED_SERVICE_URI;
  private static final String TEST_CSRF_TOKEN = "test-token-value";
  private static final String TEST_CSRF_HEADER = "test-token-header";
  private static final String TEST_CSRF_PARAMETER = "test-token-param";

  static {
    try {
      TEST_SERVICE_URI = new URI( "http://corp.com:8080/pentaho/api/csrf/service" );
      TEST_PROTECTED_SERVICE_URI = new URI( "http://mydomain.com:8080/pentaho/moneyTransfer" );
    } catch ( URISyntaxException e ) {
      e.printStackTrace();
    }
  }

  private Client mockClient;
  private WebTarget mockWebTarget;
  private Configuration mockConfiguration;
  private Invocation.Builder mockInvocationBuilder;
  private Response mockResponse;

  @Before
  public void setUp() {
    mockClient = mock( Client.class );
    mockConfiguration = mock( Configuration.class );
    mockWebTarget = mock( WebTarget.class );
    mockInvocationBuilder = mock( Invocation.Builder.class );
    mockResponse = mock( Response.class );

    // ---
    when( mockConfiguration.isRegistered( SessionCookiesFilter.class ) ).thenReturn( true );
    when( mockClient.getConfiguration() ).thenReturn( mockConfiguration );

    when( mockClient.target( any( URI.class ) ) ).thenReturn( mockWebTarget );
    when( mockWebTarget.queryParam( anyString(), anyObject() ) ).thenReturn( mockWebTarget );
    when( mockWebTarget.request() ).thenReturn( mockInvocationBuilder );
    when( mockInvocationBuilder.get() ).thenReturn( mockResponse );

    // ---

    when( mockResponse.getStatus() ).thenReturn( 204 );

    when( mockResponse.getHeaderString( eq( RESPONSE_HEADER_HEADER ) ) )
      .thenReturn( TEST_CSRF_HEADER );
    when( mockResponse.getHeaderString( eq( RESPONSE_HEADER_PARAM ) ) )
      .thenReturn( TEST_CSRF_PARAMETER );
    when( mockResponse.getHeaderString( eq( RESPONSE_HEADER_TOKEN ) ) )
      .thenReturn( TEST_CSRF_TOKEN );
  }

  @Test
  public void testGetTokenWhenResponseIsNot200Or204() {

    when( mockResponse.getStatus() ).thenReturn( 210 );

    CsrfTokenServiceClient csrfTokenClient = new CsrfTokenServiceClient( TEST_SERVICE_URI, mockClient );

    CsrfToken token = csrfTokenClient.getToken( TEST_PROTECTED_SERVICE_URI );

    assertNull( token );
  }

  @Test
  public void testGetTokenCallsCsrfService() {

    // Bail out asap.
    when( mockResponse.getStatus() ).thenReturn( 210 );

    CsrfTokenServiceClient csrfTokenClient = new CsrfTokenServiceClient( TEST_SERVICE_URI, mockClient );

    csrfTokenClient.getToken( TEST_PROTECTED_SERVICE_URI );

    verify( mockClient, times( 1 ) )
      .target( eq( TEST_SERVICE_URI ) );

    verify( mockWebTarget, times( 1 ) )
      .queryParam( eq( QUERY_PARAM_URL ), eq( TEST_PROTECTED_SERVICE_URI ) );

    verify( mockWebTarget, times( 1 ) )
      .request();

    verify( mockInvocationBuilder, times( 1 ) )
      .get();
  }

  // region 200/204 with Token Present
  @Test
  public void testGetTokenWhenResponseIs200AndTokenIsPresent() {

    when( mockResponse.getStatus() ).thenReturn( 200 );

    CsrfTokenServiceClient csrfTokenClient = new CsrfTokenServiceClient( TEST_SERVICE_URI, mockClient );

    CsrfToken token = csrfTokenClient.getToken( TEST_PROTECTED_SERVICE_URI );

    assertNotNull( token );

    assertEquals( token.getHeader(), TEST_CSRF_HEADER );
    assertEquals( token.getParameter(), TEST_CSRF_PARAMETER );
    assertEquals( token.getToken(), TEST_CSRF_TOKEN );
  }

  @Test
  public void testGetTokenWhenResponseIs204AndTokenIsPresent() {

    when( mockResponse.getStatus() ).thenReturn( 204 );

    CsrfTokenServiceClient csrfTokenClient = new CsrfTokenServiceClient( TEST_SERVICE_URI, mockClient );

    CsrfToken token = csrfTokenClient.getToken( TEST_PROTECTED_SERVICE_URI );

    assertNotNull( token );

    assertEquals( token.getHeader(), TEST_CSRF_HEADER );
    assertEquals( token.getParameter(), TEST_CSRF_PARAMETER );
    assertEquals( token.getToken(), TEST_CSRF_TOKEN );
  }
  // endregion

  // region 200/204 with Null or Empty Token
  @Test
  public void testGetTokenWhenResponseIs200AndTokenIsNotPresent() {

    when( mockResponse.getStatus() ).thenReturn( 200 );
    when( mockResponse.getHeaderString( eq( RESPONSE_HEADER_TOKEN ) ) )
      .thenReturn( null );

    CsrfTokenServiceClient csrfTokenClient = new CsrfTokenServiceClient( TEST_SERVICE_URI, mockClient );

    CsrfToken token = csrfTokenClient.getToken( TEST_PROTECTED_SERVICE_URI );

    assertNull( token );
  }

  @Test
  public void testGetTokenWhenResponseIs200AndTokenIsEmpty() {

    when( mockResponse.getStatus() ).thenReturn( 200 );
    when( mockResponse.getHeaderString( eq( RESPONSE_HEADER_TOKEN ) ) )
      .thenReturn( "" );

    CsrfTokenServiceClient csrfTokenClient = new CsrfTokenServiceClient( TEST_SERVICE_URI, mockClient );

    CsrfToken token = csrfTokenClient.getToken( TEST_PROTECTED_SERVICE_URI );

    assertNull( token );
  }

  @Test
  public void testGetTokenWhenResponseIs204AndTokenIsNotPresent() {

    when( mockResponse.getStatus() ).thenReturn( 204 );
    when( mockResponse.getHeaderString( eq( RESPONSE_HEADER_TOKEN ) ) )
      .thenReturn( null );

    CsrfTokenServiceClient csrfTokenClient = new CsrfTokenServiceClient( TEST_SERVICE_URI, mockClient );

    CsrfToken token = csrfTokenClient.getToken( TEST_PROTECTED_SERVICE_URI );

    assertNull( token );
  }

  @Test
  public void testGetTokenWhenResponseIs204AndTokenIsEmpty() {

    when( mockResponse.getStatus() ).thenReturn( 204 );
    when( mockResponse.getHeaderString( eq( RESPONSE_HEADER_TOKEN ) ) )
      .thenReturn( "" );

    CsrfTokenServiceClient csrfTokenClient = new CsrfTokenServiceClient( TEST_SERVICE_URI, mockClient );

    CsrfToken token = csrfTokenClient.getToken( TEST_PROTECTED_SERVICE_URI );

    assertNull( token );
  }
  // endregion
}
