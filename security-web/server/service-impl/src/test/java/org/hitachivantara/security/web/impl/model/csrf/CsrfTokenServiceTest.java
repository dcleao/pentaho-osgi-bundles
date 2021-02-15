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

package org.hitachivantara.security.web.impl.model.csrf;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.security.web.csrf.CsrfToken;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Response;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@RunWith( PowerMockRunner.class )
public class CsrfTokenServiceTest {

  private HttpServletRequest mockRequest;
  private CsrfTokenService csrfTokeResource;

  private static final String RESPONSE_HEADER_VALUE = "HEADER_NAME";
  private static final String RESPONSE_PARAM_VALUE = "PARAM_NAME";
  private static final String RESPONSE_TOKEN_VALUE = "TOKEN";

  @Before
  public void setUp() {
    mockRequest = mock( HttpServletRequest.class );
    csrfTokeResource = spy( new CsrfTokenService() );
  }

  private static CsrfToken createToken() {

    CsrfToken token = mock( CsrfToken.class );

    when( token.getHeaderName() ).thenReturn( RESPONSE_HEADER_VALUE );
    when( token.getParameterName() ).thenReturn( RESPONSE_PARAM_VALUE );
    when( token.getToken() ).thenReturn( RESPONSE_TOKEN_VALUE );

    return token;
  }

  public void testWhenNoCsrfTokenInRequest() {

    csrfTokeResource.request = mockRequest;

    Response response = csrfTokeResource.getToken( "url" );

    assertNull( response.getHeaderString( CsrfTokenService.RESPONSE_HEADER_HEADER ) );
    assertNull( response.getHeaderString( CsrfTokenService.RESPONSE_HEADER_PARAM ) );
    assertNull( response.getHeaderString( CsrfTokenService.RESPONSE_HEADER_TOKEN ) );
  }

  @Test
  public void testWhenCsrfTokenThenCsrfResponseHeaders() {

    CsrfToken token = createToken();
    when( mockRequest.getAttribute( CsrfTokenService.REQUEST_ATTRIBUTE_NAME ) ).thenReturn( token );

    csrfTokeResource.request = mockRequest;

    Response response = csrfTokeResource.getToken( "url" );

    assertEquals( RESPONSE_HEADER_VALUE, response.getHeaderString( CsrfTokenService.RESPONSE_HEADER_HEADER ) );
    assertEquals( RESPONSE_PARAM_VALUE, response.getHeaderString( CsrfTokenService.RESPONSE_HEADER_PARAM ) );
    assertEquals( RESPONSE_TOKEN_VALUE, response.getHeaderString( CsrfTokenService.RESPONSE_HEADER_TOKEN ) );
  }

  @Test
  public void testStatusCodeNoContent() {

    CsrfToken token = createToken();
    when( mockRequest.getAttribute( CsrfTokenService.REQUEST_ATTRIBUTE_NAME ) ).thenReturn( token );

    csrfTokeResource.request = mockRequest;

    Response response = csrfTokeResource.getToken( "url" );

    assertEquals( HttpServletResponse.SC_NO_CONTENT, response.getStatus() );
  }
}
