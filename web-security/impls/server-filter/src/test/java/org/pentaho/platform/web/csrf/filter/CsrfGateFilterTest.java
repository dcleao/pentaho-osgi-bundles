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
 * Copyright (c) 2019 Hitachi Vantara. All rights reserved.
 */
package org.pentaho.platform.web.csrf.filter;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.pentaho.web.security.csrf.CsrfConfiguration;
import org.pentaho.web.security.csrf.CsrfConfigurationProvider;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockFilterConfig;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.util.matcher.RequestMatcher;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith( PowerMockRunner.class )
@PrepareForTest( { CsrfUtil.class } )
public class CsrfGateFilterTest {

  private CsrfGateFilter csrfGateFilter;
  @Mock private CsrfTokenRepository csrfTokenRepository;
  @Mock private CsrfConfigurationProvider csrfProtectionDefinitionProvider;
  private FilterConfig filterConfig;

  @Before
  public void setUp() {
    csrfGateFilter = new CsrfGateFilter( csrfTokenRepository, csrfProtectionDefinitionProvider );
    filterConfig = new MockFilterConfig();
  }

  @Test
  public void testConstructor() {
    assertNotNull( csrfGateFilter.getInnerCsrfFilter() );
    // No way to test that csrfTokenRepository was the received repository.
  }

  @Test
  public void testDestroy() {

    // Coverage. No way to test that innerCsrfFilter.destroy() was called.

    csrfGateFilter.destroy();
  }

  @Test
  public void testSetAccessDeniedHandler() {

    // Coverage. No way to test that innerCsrfFilter.setAccessDeniedHandler( . ) was called.

    csrfGateFilter.setAccessDeniedHandler( Mockito.mock ( AccessDeniedHandler.class ) );
  }

  @Test
  public void testInitWithCsrfProtectionDisabled() throws Exception {

    when( csrfProtectionDefinitionProvider.getProtectionDefinitions() ).thenReturn( Collections.emptyList() );

    assertFalse( csrfGateFilter.getInitialized() );

    csrfGateFilter.init( filterConfig );

    assertEquals( filterConfig, csrfGateFilter.getInnerCsrfFilter().getFilterConfig() );

    assertTrue( csrfGateFilter.getInitialized() );
    assertFalse( csrfGateFilter.getIsCsrfProtectionEnabled() );

    verify( csrfProtectionDefinitionProvider ).addCsrfConfigurationListener( any( Runnable.class ) );
  }

  @Test
  public void testInitWithCsrfProtectionEnabledButWithoutCsrfProtectionDefinitions() throws Exception {

    when( csrfProtectionDefinitionProvider.getProtectionDefinitions() ).thenReturn( Collections.singletonList(
      new CsrfConfiguration()
    ) );

    PowerMockito.mockStatic( CsrfUtil.class );
    when( CsrfUtil.buildCsrfRequestMatcher( any() ) ).thenReturn( null );

    assertFalse( csrfGateFilter.getInitialized() );

    csrfGateFilter.init( filterConfig );

    assertEquals( filterConfig, csrfGateFilter.getInnerCsrfFilter().getFilterConfig() );

    assertTrue( csrfGateFilter.getInitialized() );
    assertFalse( csrfGateFilter.getIsCsrfProtectionEnabled() );
  }

  @Test
  public void testInitWithCsrfProtectionEnabledAndWithCsrfProtectionDefinitions() throws Exception {

    when( csrfProtectionDefinitionProvider.getProtectionDefinitions() ).thenReturn( Collections.singletonList(
      new CsrfConfiguration()
    ) );

    RequestMatcher requestMatcher = Mockito.mock( RequestMatcher.class );
    PowerMockito.mockStatic( CsrfUtil.class );
    when( CsrfUtil.buildCsrfRequestMatcher( any() ) ).thenReturn( requestMatcher );

    assertFalse( csrfGateFilter.getInitialized() );

    csrfGateFilter.init( filterConfig );

    assertEquals( filterConfig, csrfGateFilter.getInnerCsrfFilter().getFilterConfig() );

    assertTrue( csrfGateFilter.getInitialized() );
    assertTrue( csrfGateFilter.getIsCsrfProtectionEnabled() );
  }

  @Test
  public void testPluginManagerReload() throws Exception {

    // Setup
    ArgumentCaptor<Runnable> listenerCapture = ArgumentCaptor.forClass( Runnable.class );

    doNothing().when( csrfProtectionDefinitionProvider ).addCsrfConfigurationListener( listenerCapture.capture() );

    when( csrfProtectionDefinitionProvider.getProtectionDefinitions() ).thenReturn( Collections.singletonList(
      new CsrfConfiguration()
    ) );

    RequestMatcher requestMatcher = Mockito.mock( RequestMatcher.class );
    PowerMockito.mockStatic( CsrfUtil.class );
    when( CsrfUtil.buildCsrfRequestMatcher( any() ) ).thenReturn( requestMatcher );

    csrfGateFilter.init( filterConfig );

    verify( csrfProtectionDefinitionProvider, times( 1 ) )
      .getProtectionDefinitions();

    assertTrue( csrfGateFilter.getInitialized() );
    assertTrue( csrfGateFilter.getIsCsrfProtectionEnabled() );

    // Test Simulate reload.
    listenerCapture.getValue().onConfigurationChanged();

    assertTrue( csrfGateFilter.getInitialized() );
    assertTrue( csrfGateFilter.getIsCsrfProtectionEnabled() );

    verify( csrfProtectionDefinitionProvider, times( 2 ) )
      .getProtectionDefinitions();
  }

  @Test
  public void testDoFilterWhenInitializedAndCsrfProtectionDisabled() throws Exception {

    // Setup
    when( csrfProtectionDefinitionProvider.getProtectionDefinitions() ).thenReturn( Collections.emptyList() );

    csrfGateFilter.init( filterConfig );

    assertTrue( csrfGateFilter.getInitialized() );
    assertFalse( csrfGateFilter.getIsCsrfProtectionEnabled() );

    // Test
    HttpServletRequest mockRequest = Mockito.mock( HttpServletRequest.class );
    HttpServletResponse mockResponse = Mockito.mock( HttpServletResponse.class );
    FilterChain filterChain = new MockFilterChain();

    csrfGateFilter.doFilter( mockRequest, mockResponse, filterChain );
  }

  @Test
  public void testDoFilterWhenInitializedAndCsrfProtectionEnabled() throws Exception {

    // Setup
    when( csrfProtectionDefinitionProvider.getProtectionDefinitions() ).thenReturn( Collections.singletonList(
      new CsrfConfiguration()
    ) );

    RequestMatcher requestMatcher = Mockito.mock( RequestMatcher.class );
    when( requestMatcher.matches( any() ) ).thenReturn( false );

    PowerMockito.mockStatic( CsrfUtil.class );
    when( CsrfUtil.buildCsrfRequestMatcher( any() ) ).thenReturn( requestMatcher );

    HttpServletRequest mockRequest = Mockito.mock( HttpServletRequest.class );
    HttpServletResponse mockResponse = Mockito.mock( HttpServletResponse.class );
    FilterChain filterChain = new MockFilterChain();

    CsrfToken mockCsrfToken = mock( CsrfToken.class );
    when( mockCsrfToken.getParameterName() ).thenReturn( "_token_" );
    when( csrfTokenRepository.loadToken( mockRequest ) ).thenReturn( mockCsrfToken );

    csrfGateFilter.init( filterConfig );

    verify( csrfProtectionDefinitionProvider, times( 1 ) )
      .getProtectionDefinitions();

    assertTrue( csrfGateFilter.getInitialized() );
    assertTrue( csrfGateFilter.getIsCsrfProtectionEnabled() );

    // Test
    csrfGateFilter.doFilter( mockRequest, mockResponse, filterChain );

    verify( csrfProtectionDefinitionProvider, times( 1 ) )
      .getProtectionDefinitions();

    assertTrue( csrfGateFilter.getInitialized() );
    assertTrue( csrfGateFilter.getIsCsrfProtectionEnabled() );
  }

  @Test
  public void testDoFilterWhileReloadAndCsrfProtectionEnabled() throws Exception {

    // Setup
    when( csrfProtectionDefinitionProvider.getProtectionDefinitions() ).thenReturn( Collections.singletonList(
      new CsrfConfiguration()
    ) );

    RequestMatcher requestMatcher = Mockito.mock( RequestMatcher.class );
    when( requestMatcher.matches( any() ) ).thenReturn( false );

    PowerMockito.mockStatic( CsrfUtil.class );
    when( CsrfUtil.buildCsrfRequestMatcher( any() ) ).thenReturn( requestMatcher );

    HttpServletRequest mockRequest = Mockito.mock( HttpServletRequest.class );
    HttpServletResponse mockResponse = Mockito.mock( HttpServletResponse.class );
    FilterChain filterChain = new MockFilterChain();

    CsrfToken mockCsrfToken = mock( CsrfToken.class );
    when( mockCsrfToken.getParameterName() ).thenReturn( "_token_" );
    when( csrfTokenRepository.loadToken( mockRequest ) ).thenReturn( mockCsrfToken );

    csrfGateFilter.init( filterConfig );

    verify( csrfProtectionDefinitionProvider, times( 1 ) )
      .getProtectionDefinitions();

    assertTrue( csrfGateFilter.getInitialized() );
    assertTrue( csrfGateFilter.getIsCsrfProtectionEnabled() );

    // Simulate to be within doInit(), in another thread...
    csrfGateFilter.setInitialized( false );

    // Test
    csrfGateFilter.doFilter( mockRequest, mockResponse, filterChain );

    verify( csrfProtectionDefinitionProvider, times( 2 ) )
      .getProtectionDefinitions();

    assertTrue( csrfGateFilter.getInitialized() );
    assertTrue( csrfGateFilter.getIsCsrfProtectionEnabled() );
  }
}
