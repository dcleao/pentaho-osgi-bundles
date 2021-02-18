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

import org.hitachivantara.security.web.api.model.cors.CorsConfiguration;
import org.hitachivantara.security.web.api.model.cors.CorsRequestSetConfiguration;
import org.junit.Test;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

public class CorsConfigurationSourceAdapterTest {

  private CorsConfiguration createConfigurationMock() {
    CorsConfiguration configuration = mock( CorsConfiguration.class );

    when( configuration.isEnabled() ).thenReturn( true );

    CorsRequestSetConfiguration rootSetConfig = mock( CorsRequestSetConfiguration.class );
    when( rootSetConfig.isEnabled() ).thenReturn( true );
    when( rootSetConfig.isAbstract() ).thenReturn( false );
    when( rootSetConfig.isEnabledEffective() ).thenReturn( true );

    when( configuration.getRootConfiguration() ).thenReturn( rootSetConfig );

    CorsRequestSetConfiguration requestSetConfig = mock( CorsRequestSetConfiguration.class );
    when( requestSetConfig.isEnabled() ).thenReturn( true );
    when( requestSetConfig.isAbstract() ).thenReturn( false );
    when( requestSetConfig.isEnabledEffective() ).thenReturn( true );

    when( configuration.getRequestConfiguration( any( HttpServletRequest.class ) ) )
      .thenReturn( requestSetConfig );

    return configuration;
  }

  @Nonnull
  private static Set<String> createSet( String... elements ) {
    return new LinkedHashSet<>( Arrays.asList( elements ) );
  }

  @Nonnull
  private static List<String> createList( String... elements ) {
    return Arrays.asList( elements );
  }

  @Test
  public void testAbstractEnabledConfigIsConvertedToNull() {
    CorsConfiguration configuration = createConfigurationMock();

    HttpServletRequest requestMock = mock( HttpServletRequest.class );
    CorsRequestSetConfiguration requestConfig = configuration
      .getRequestConfiguration( requestMock );

    when( requestConfig.isAbstract() ).thenReturn( true );
    when( requestConfig.isEnabledEffective() ).thenReturn( false );

    CorsConfigurationSourceAdapter adapter = new CorsConfigurationSourceAdapter( configuration );

    assertNull( adapter.getCorsConfiguration( requestMock ) );
  }

  @Test
  public void testConcreteDisabledConfigIsConvertedToNull() {
    CorsConfiguration configuration = createConfigurationMock();

    HttpServletRequest requestMock = mock( HttpServletRequest.class );
    CorsRequestSetConfiguration requestConfig = configuration
      .getRequestConfiguration( requestMock );

    when( requestConfig.isEnabled() ).thenReturn( false );
    when( requestConfig.isEnabledEffective() ).thenReturn( false );

    CorsConfigurationSourceAdapter adapter = new CorsConfigurationSourceAdapter( configuration );

    assertNull( adapter.getCorsConfiguration( requestMock ) );
  }

  @Test
  public void testAllNullPropsAreCopied() {

    CorsConfiguration configuration = createConfigurationMock();
    CorsConfigurationSourceAdapter adapter = new CorsConfigurationSourceAdapter( configuration );

    HttpServletRequest requestMock = mock( HttpServletRequest.class );
    CorsRequestSetConfiguration requestConfig = configuration
      .getRequestConfiguration( requestMock );

    when( requestConfig.getAllowedOrigins() ).thenReturn( null );
    when( requestConfig.getAllowedHeaders() ).thenReturn( null );
    when( requestConfig.getAllowedMethods() ).thenReturn( null );
    when( requestConfig.getExposedHeaders() ).thenReturn( null );
    when( requestConfig.getMaxAge() ).thenReturn( null );
    when( requestConfig.getAllowCredentials() ).thenReturn( null );

    org.springframework.web.cors.CorsConfiguration springConfig = adapter.getCorsConfiguration( requestMock );

    assertNotNull( springConfig );

    assertNull( springConfig.getAllowedOrigins() );
    assertNull( springConfig.getAllowedHeaders() );
    assertNull( springConfig.getAllowedMethods() );
    assertNull( springConfig.getExposedHeaders() );
    assertNull( springConfig.getMaxAge() );
    assertNull( springConfig.getAllowCredentials() );
  }

  @Test
  public void testAllVectorPropsAreCopied() {

    CorsConfiguration configuration = createConfigurationMock();
    CorsConfigurationSourceAdapter adapter = new CorsConfigurationSourceAdapter( configuration );

    HttpServletRequest requestMock = mock( HttpServletRequest.class );
    CorsRequestSetConfiguration requestConfig = configuration
      .getRequestConfiguration( requestMock );

    when( requestConfig.getAllowedOrigins() ).thenReturn( createSet( "AO1", "AO2" ) );
    when( requestConfig.getAllowedHeaders() ).thenReturn( createSet( "AH1", "AH2" ) );
    when( requestConfig.getAllowedMethods() ).thenReturn( createSet( "AM1", "AM2" ) );
    when( requestConfig.getExposedHeaders() ).thenReturn( createSet( "EH1", "EH2" ) );

    org.springframework.web.cors.CorsConfiguration springConfig = adapter.getCorsConfiguration( requestMock );

    assertEquals( createList( "AO1", "AO2" ), springConfig.getAllowedOrigins() );
    assertEquals( createList( "AH1", "AH2" ), springConfig.getAllowedHeaders() );
    assertEquals( createList( "AM1", "AM2" ), springConfig.getAllowedMethods() );
    assertEquals( createList( "EH1", "EH2" ), springConfig.getExposedHeaders() );
  }

  @SuppressWarnings( { "UnnecessaryBoxing", "BooleanConstructorCall" } ) @Test
  public void testAllScalarFieldsAreCopied() {

    CorsConfiguration configuration = createConfigurationMock();
    CorsConfigurationSourceAdapter adapter = new CorsConfigurationSourceAdapter( configuration );

    HttpServletRequest requestMock = mock( HttpServletRequest.class );
    CorsRequestSetConfiguration requestConfig = configuration
      .getRequestConfiguration( requestMock );

    when( requestConfig.getMaxAge() ).thenReturn( new Long( 10L ) );
    when( requestConfig.getAllowCredentials() ).thenReturn( new Boolean( true ) );

    org.springframework.web.cors.CorsConfiguration springConfig = adapter.getCorsConfiguration( requestMock );

    assertNotNull( springConfig );

    assertEquals( new Long( 10L ), springConfig.getMaxAge() );
    assertEquals( new Boolean( true ), springConfig.getAllowCredentials() );
  }

  @Test
  public void testSameRequestReturnsSameConfig() {

    CorsConfiguration configuration = createConfigurationMock();
    CorsConfigurationSourceAdapter adapter = new CorsConfigurationSourceAdapter( configuration );

    HttpServletRequest requestMock1 = mock( HttpServletRequest.class );
    CorsRequestSetConfiguration requestSetConfig1 = mock( CorsRequestSetConfiguration.class );
    when( requestSetConfig1.isEnabled() ).thenReturn( true );
    when( requestSetConfig1.isEnabledEffective() ).thenReturn( true );
    when( requestSetConfig1.getMaxAge() ).thenReturn( 10L );

    when( configuration.getRequestConfiguration( requestMock1 ) )
      .thenReturn( requestSetConfig1 );

    // ---

    HttpServletRequest requestMock2 = mock( HttpServletRequest.class );
    CorsRequestSetConfiguration requestSetConfig2 = mock( CorsRequestSetConfiguration.class );
    when( requestSetConfig2.isEnabled() ).thenReturn( true );
    when( requestSetConfig2.isEnabledEffective() ).thenReturn( true );
    when( requestSetConfig2.getMaxAge() ).thenReturn( 20L );

    when( configuration.getRequestConfiguration( requestMock2 ) )
      .thenReturn( requestSetConfig2 );

    // ---

    org.springframework.web.cors.CorsConfiguration springConfig1_1 = adapter.getCorsConfiguration( requestMock1 );

    assertNotNull( springConfig1_1 );

    assertEquals( new Long( 10L ), springConfig1_1.getMaxAge() );

    org.springframework.web.cors.CorsConfiguration springConfig2_1 = adapter.getCorsConfiguration( requestMock2 );

    assertNotNull( springConfig2_1 );

    assertEquals( new Long( 20L ), springConfig2_1.getMaxAge() );

    assertNotSame( springConfig1_1, springConfig2_1 );

    // ---

    org.springframework.web.cors.CorsConfiguration springConfig1_2 = adapter.getCorsConfiguration( requestMock1 );
    assertEquals( springConfig1_1, springConfig1_2 );

    org.springframework.web.cors.CorsConfiguration springConfig2_2 = adapter.getCorsConfiguration( requestMock2 );
    assertEquals( springConfig2_1, springConfig2_2 );
  }
}
