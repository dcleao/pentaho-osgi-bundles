/*!
 *
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

package org.hitachivantara.security.web.impl.model.spring;

import org.hitachivantara.security.web.api.model.csrf.CsrfConfiguration;
import org.junit.Test;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;

import javax.servlet.http.HttpServletRequest;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

public class CsrfRequiredRequestMatcherAdapterTest {

  private static final String PROTECTED_URL_A = "A";
  private static final String PROTECTED_URL_B = "B";
  private static final String UNPROTECTED_URL = "C";

  private static HttpServletRequest createMockRequest( String url ) {

    HttpServletRequest mockRequest = mock( HttpServletRequest.class );

    when( mockRequest.getRequestURI() ).thenReturn( url );

    return mockRequest;
  }

  private CsrfConfiguration getEnabledSampleConfig() {
    CsrfConfiguration mockConfig = PowerMockito.mock( CsrfConfiguration.class );

    when( mockConfig.isEnabled() ).thenReturn( true );
    when( mockConfig.isEnabled( any( HttpServletRequest.class ) ) )
      .then( (Answer<Boolean>) invocationOnMock -> {
        HttpServletRequest request = invocationOnMock.getArgumentAt( 0, HttpServletRequest.class );
        String uri = request.getRequestURI();
        return uri.equals( PROTECTED_URL_A ) || uri.equals( PROTECTED_URL_B );
      } );

    return mockConfig;
  }

  @Test
  public void testMatchesWhenRequestIsProtected() {

    CsrfConfiguration enabledConfig = getEnabledSampleConfig();

    CsrfRequiredRequestMatcherAdapter adapter = new CsrfRequiredRequestMatcherAdapter( enabledConfig );

    assertTrue( adapter.matches( createMockRequest( PROTECTED_URL_A ) ) );
  }

  @Test
  public void testDoesNotMatchWhenRequestIsNotProtected() {

    CsrfConfiguration enabledConfig = getEnabledSampleConfig();

    CsrfRequiredRequestMatcherAdapter adapter = new CsrfRequiredRequestMatcherAdapter( enabledConfig );

    assertFalse( adapter.matches( createMockRequest( UNPROTECTED_URL ) ) );
  }
}
