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

package org.hitachivantara.security.web.impl.model.cors;

import org.hitachivantara.security.web.api.model.cors.CorsRequestSetConfiguration;
import org.hitachivantara.security.web.api.model.matcher.RequestMatcher;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

public class AggregatedRequestSetCorsConfigurationTest {

  private AggregatedRequestSetCorsConfiguration configuration;

  @Before
  public void setUp() {
    configuration = new AggregatedRequestSetCorsConfiguration();
  }

  @Test
  public void testWhenJustConstructedIsDisabledAtRoot() {
    assertFalse( configuration.isEnabled() );
    assertFalse( configuration.getRootConfiguration().isEnabled() );
  }

  @Test
  public void testWhenJustConstructedIsDisabledForAnyRequest() {

    HttpServletRequest request = mock( HttpServletRequest.class );

    assertFalse( configuration.getRequestConfiguration( request ).isEnabled() );
  }

  // region Sample configuration
  private static final String ROOT_URL = "R";
  private static final String CHILD_URL_A = "R/A";
  private static final String CHILD_URL_B = "R/B";
  private static final String CHILD_URL_C = "R/C";
  private static final String GRAND_CHILD_URL_D = "R/C/D";
  private static final String GRAND_CHILD_URL_E = "R/C/E";
  private static final String GREAT_GRAND_CHILD_URL_F = "R/C/E/F";

  private static boolean urlMatches( @Nonnull String pattern, @Nonnull String url ) {
    // Basic "prefix" pattern
    return url.indexOf( pattern ) == 0;
  }

  @Nonnull
  private static RequestMatcher createRequestMatcher( @Nonnull String baseUrl ) {
    return request -> urlMatches( baseUrl, request.getRequestURI() );
  }

  @Nonnull
  private static CorsRequestSetConfigurationPojo createRequestConfig( @Nonnull String baseUrl ) {
    CorsRequestSetConfigurationPojo config = new CorsRequestSetConfigurationPojo( true );
    config.setRequestMatcher( createRequestMatcher( baseUrl ) );
    return config;
  }

  @Nonnull
  private static CorsRequestSetConfigurationPojo createRootRequestConfig() {
    CorsRequestSetConfigurationPojo rootConfig = createRequestConfig( ROOT_URL );
    rootConfig.setName( CorsRequestSetConfiguration.ROOT_NAME );
    return  rootConfig;
  }

  @Nonnull
  private static HttpServletRequest createRequest( @Nonnull String url ) {

    HttpServletRequest mockRequest = mock( HttpServletRequest.class );

    when( mockRequest.getRequestURI() ).thenReturn( url );

    return mockRequest;
  }

  @Nonnull
  private static Set<String> createSet( String... elements ) {
    return new HashSet<>( Arrays.asList( elements ) );
  }

  @Nonnull
  private static List<CorsRequestSetConfiguration> createList( CorsRequestSetConfiguration... elements ) {
    return Arrays.asList( elements );
  }

  @Nonnull
  private static List<CorsRequestSetConfiguration> createConfig() {
    List<CorsRequestSetConfiguration> list = new ArrayList<>();

    CorsRequestSetConfigurationPojo requestConfig = createRootRequestConfig();
    requestConfig.setAllowedOrigins( createSet( "o1", "o2" ) );
    list.add( requestConfig );

    requestConfig = createRequestConfig( CHILD_URL_A );
    list.add( requestConfig );

    requestConfig = createRequestConfig( CHILD_URL_B );
    requestConfig.setAllowedOrigins( new HashSet<>( Arrays.asList( "o3" )  ) );
    list.add( requestConfig );

    return list;
  }
  // endregion

  @Test
  public void testWhenSetWithNullConfigIsDisabledAtRoot() {
    configuration.setRequestSetConfigurations( null );
    assertFalse( configuration.isEnabled() );
    assertFalse( configuration.getRootConfiguration().isEnabled() );
  }

  @Test
  public void testWhenSetWithNullConfigIsDisabledForAnyRequest() {

    configuration.setRequestSetConfigurations( null );

    HttpServletRequest request = mock( HttpServletRequest.class );
    assertFalse( configuration.getRequestConfiguration( request ).isEnabled() );
  }

  @Test
  public void testWhenSetWithEmptyListConfigIsDisabledAtRoot() {
    configuration.setRequestSetConfigurations( new ArrayList<>() );
    assertFalse( configuration.isEnabled() );
    assertFalse( configuration.getRootConfiguration().isEnabled() );
  }

  @Test
  public void testWhenSetWithEmptyListConfigIsDisabledForAnyRequest() {

    configuration.setRequestSetConfigurations( new ArrayList<>() );

    HttpServletRequest request = mock( HttpServletRequest.class );
    assertFalse( configuration.getRequestConfiguration( request ).isEnabled() );
  }

  @Test
  public void testWhenSetWithNoRootListConfigIsDisabledAtRoot() {

    configuration.setRequestSetConfigurations(
      createList( createRequestConfig( "SOME_URL" ) ) );

    assertFalse( configuration.isEnabled() );
    assertFalse( configuration.getRootConfiguration().isEnabled() );
  }

  @Test
  public void testWhenSetWithARootListConfigIsEnabledAtRoot() {
    configuration.setRequestSetConfigurations(
      createList(
        createRequestConfig( CHILD_URL_A ),
        createRootRequestConfig() ) );

    assertTrue( configuration.isEnabled() );
    assertTrue( configuration.getRootConfiguration().isEnabled() );
  }

  @Test
  public void testChildWithUnspecifiedParentNameTurnsIntoChildOfRoot() {
    configuration.setRequestSetConfigurations(
      createList(
        createRequestConfig( CHILD_URL_A ),
        createRootRequestConfig() ) );

    CorsRequestSetConfiguration resultConfig = configuration.getRequestConfiguration( createRequest( CHILD_URL_A ) );

    assertNotNull( resultConfig );
    assertTrue( resultConfig.isEnabled() );
    assertEquals( CorsRequestSetConfiguration.ROOT_NAME, resultConfig.getParentName() );
  }

  @Test
  public void testRootConfigShouldBeThatReturnedByRootRequest() {
    configuration.setRequestSetConfigurations(
      createList( createRootRequestConfig(), createRequestConfig( CHILD_URL_A ) ) );

    CorsRequestSetConfiguration rootConfig1 = configuration.getRootConfiguration();
    CorsRequestSetConfiguration rootConfig2 = configuration.getRequestConfiguration( createRequest( ROOT_URL ) );

    assertSame( rootConfig1, rootConfig2 );
  }

  @Test
  public void testChildWithNoOptionsInheritsParentOptions() {
    CorsRequestSetConfigurationPojo rootConfig = createRootRequestConfig();
    rootConfig.setAllowedOrigins( createSet( "AO1", "AO2" ) );
    rootConfig.setAllowedHeaders( createSet( "AH1", "AH2" ) );
    rootConfig.setAllowedMethods( createSet( "AM1", "AM2" ) );
    rootConfig.setExposedHeaders( createSet( "EH1", "EH2" ) );
    rootConfig.setMaxAge( 10L );
    rootConfig.setAllowCredentials( true );

    configuration.setRequestSetConfigurations(
      createList(
        rootConfig,
        createRequestConfig( CHILD_URL_A ) ) );

    CorsRequestSetConfiguration resultConfig = configuration.getRequestConfiguration( createRequest( CHILD_URL_A ) );

    assertNotNull( resultConfig );
    assertEquals( createSet( "AO1", "AO2" ), resultConfig.getAllowedOrigins() );
    assertEquals( createSet( "AH1", "AH2" ), resultConfig.getAllowedHeaders() );
    assertEquals( createSet( "AM1", "AM2" ), resultConfig.getAllowedMethods() );
    assertEquals( createSet( "EH1", "EH2" ), resultConfig.getExposedHeaders() );
    assertEquals( new Long( 10L ), resultConfig.getMaxAge() );
    //noinspection UnnecessaryBoxing,BooleanConstructorCall
    assertEquals( new Boolean( true ), resultConfig.getAllowCredentials() );
  }

  @Test
  public void testChildWithVectorOptionsAddsToInheritedParentOptions() {
    CorsRequestSetConfigurationPojo rootConfig = createRootRequestConfig();
    rootConfig.setAllowedOrigins( createSet( "AO1", "AO2" ) );
    rootConfig.setAllowedHeaders( createSet( "AH1", "AH2" ) );
    rootConfig.setAllowedMethods( createSet( "AM1", "AM2" ) );
    rootConfig.setExposedHeaders( createSet( "EH1", "EH2" ) );

    CorsRequestSetConfigurationPojo childConfig = createRequestConfig( CHILD_URL_A );
    childConfig.setAllowedOrigins( createSet( "AO3", "AO2" ) );
    childConfig.setAllowedHeaders( createSet( "AH3", "AH2" ) );
    childConfig.setAllowedMethods( createSet( "AM3", "AM2" ) );
    childConfig.setExposedHeaders( createSet( "EH3", "EH2" ) );

    configuration.setRequestSetConfigurations( createList( rootConfig, childConfig ) );

    CorsRequestSetConfiguration resultConfig = configuration.getRequestConfiguration( createRequest( CHILD_URL_A ) );

    assertNotNull( resultConfig );
    assertEquals( createSet( "AO1", "AO2", "AO3" ), resultConfig.getAllowedOrigins() );
    assertEquals( createSet( "AH1", "AH2", "AH3" ), resultConfig.getAllowedHeaders() );
    assertEquals( createSet( "AM1", "AM2", "AM3" ), resultConfig.getAllowedMethods() );
    assertEquals( createSet( "EH1", "EH2", "EH3" ), resultConfig.getExposedHeaders() );

    // Should not mutate root config!
    CorsRequestSetConfiguration resultRootConfig = configuration.getRequestConfiguration( createRequest( ROOT_URL ) );
    assertEquals( createSet( "AO1", "AO2" ), resultRootConfig.getAllowedOrigins() );
    assertEquals( createSet( "AH1", "AH2" ), resultRootConfig.getAllowedHeaders() );
    assertEquals( createSet( "AM1", "AM2" ), resultRootConfig.getAllowedMethods() );
    assertEquals( createSet( "EH1", "EH2" ), resultRootConfig.getExposedHeaders() );
  }

  @Test
  public void testChildWithScalarOptionsOverridesInheritedParentOptions() {
    CorsRequestSetConfigurationPojo rootConfig = createRootRequestConfig();
    rootConfig.setMaxAge( 10L );
    rootConfig.setAllowCredentials( true );

    CorsRequestSetConfigurationPojo childConfig = createRequestConfig( CHILD_URL_A );
    childConfig.setMaxAge( 20L );
    childConfig.setAllowCredentials( false );

    configuration.setRequestSetConfigurations( createList( rootConfig, childConfig ) );

    CorsRequestSetConfiguration resultConfig = configuration.getRequestConfiguration( createRequest( CHILD_URL_A ) );

    assertEquals( new Long( 20L ), resultConfig.getMaxAge() );
    //noinspection UnnecessaryBoxing,BooleanConstructorCall
    assertEquals( new Boolean( false ), resultConfig.getAllowCredentials() );
  }

  @Test
  public void testChildWithDuplicateNameIsDiscarded() {
    CorsRequestSetConfigurationPojo rootConfig = createRootRequestConfig();

    CorsRequestSetConfigurationPojo childConfigA = createRequestConfig( CHILD_URL_A );
    childConfigA.setName( "A" );
    childConfigA.setMaxAge( 20L );

    CorsRequestSetConfigurationPojo childConfigB = createRequestConfig( CHILD_URL_B );
    childConfigB.setName( "A" );
    childConfigB.setMaxAge( 30L );

    configuration.setRequestSetConfigurations( createList( rootConfig, childConfigA, childConfigB ) );

    CorsRequestSetConfiguration resultConfig = configuration.getRequestConfiguration( createRequest( CHILD_URL_A ) );

    assertEquals( "A", resultConfig.getName() );
    assertEquals( new Long( 20L ), resultConfig.getMaxAge() );

    resultConfig = configuration.getRequestConfiguration( createRequest( CHILD_URL_B ) );

    assertEquals( CorsRequestSetConfiguration.ROOT_NAME, resultConfig.getName() );
    assertNull( resultConfig.getMaxAge() );
  }

  @Test
  public void testDuplicateRootIsDiscarded() {
    CorsRequestSetConfigurationPojo rootConfig1 = createRootRequestConfig();
    rootConfig1.setMaxAge( 10L );

    CorsRequestSetConfigurationPojo rootConfig2 = createRootRequestConfig();
    rootConfig2.setMaxAge( 20L );

    configuration.setRequestSetConfigurations( createList( rootConfig1, rootConfig2 ) );

    CorsRequestSetConfiguration resultConfig = configuration.getRequestConfiguration( createRequest( ROOT_URL ) );

    assertEquals( new Long( 10L ), resultConfig.getMaxAge() );
  }

  @Test
  public void testChildWithParentWhichDoesNotExistIsDiscarded() {
    CorsRequestSetConfigurationPojo rootConfig = createRootRequestConfig();

    CorsRequestSetConfigurationPojo childConfigA = createRequestConfig( CHILD_URL_A );
    childConfigA.setParentName( "UNDEFINED_PARENT" );
    childConfigA.setMaxAge( 20L );

    configuration.setRequestSetConfigurations( createList( rootConfig, childConfigA ) );

    CorsRequestSetConfiguration resultConfig = configuration.getRequestConfiguration( createRequest( CHILD_URL_A ) );

    assertEquals( CorsRequestSetConfiguration.ROOT_NAME, resultConfig.getName() );
    assertNull( resultConfig.getMaxAge() );
  }

  @Test
  public void testChildWithUndefinedParentIsDiscardedAlongWithAllDescendants() {
    CorsRequestSetConfigurationPojo rootConfig = createRootRequestConfig();

    CorsRequestSetConfigurationPojo childConfigC = createRequestConfig( CHILD_URL_C );
    childConfigC.setParentName( "UNDEFINED_PARENT" );
    childConfigC.setName( "C" );
    childConfigC.setMaxAge( 20L );

    CorsRequestSetConfigurationPojo grandChildConfigD = createRequestConfig( GRAND_CHILD_URL_D );
    grandChildConfigD.setParentName( "C" );
    grandChildConfigD.setMaxAge( 30L );

    CorsRequestSetConfigurationPojo grandChildConfigE = createRequestConfig( GRAND_CHILD_URL_E );
    grandChildConfigE.setParentName( "C" );
    grandChildConfigE.setMaxAge( 40L );

    configuration.setRequestSetConfigurations( createList( rootConfig, childConfigC, grandChildConfigD, grandChildConfigE ) );

    CorsRequestSetConfiguration resultConfig = configuration.getRequestConfiguration( createRequest( CHILD_URL_C ) );

    assertEquals( CorsRequestSetConfiguration.ROOT_NAME, resultConfig.getName() );
    assertNull( resultConfig.getMaxAge() );

    // ---

    resultConfig = configuration.getRequestConfiguration( createRequest( GRAND_CHILD_URL_D ) );
    assertEquals( CorsRequestSetConfiguration.ROOT_NAME, resultConfig.getName() );
    assertNull( resultConfig.getMaxAge() );

    // ---

    resultConfig = configuration.getRequestConfiguration( createRequest( GRAND_CHILD_URL_E ) );
    assertEquals( CorsRequestSetConfiguration.ROOT_NAME, resultConfig.getName() );
    assertNull( resultConfig.getMaxAge() );
  }

  @Test
  public void testChildWithCycleIsDiscardedAlongWithAllDescendants() {
    CorsRequestSetConfigurationPojo rootConfig = createRootRequestConfig();

    // C > D
    // C > E > C
    CorsRequestSetConfigurationPojo childConfigC = createRequestConfig( CHILD_URL_C );
    childConfigC.setParentName( "E" );
    childConfigC.setName( "C" );
    childConfigC.setMaxAge( 20L );

    CorsRequestSetConfigurationPojo grandChildConfigD = createRequestConfig( GRAND_CHILD_URL_D );
    grandChildConfigD.setParentName( "C" );
    grandChildConfigD.setMaxAge( 30L );

    CorsRequestSetConfigurationPojo grandChildConfigE = createRequestConfig( GRAND_CHILD_URL_E );
    grandChildConfigE.setParentName( "C" );
    grandChildConfigE.setName( "E" );
    grandChildConfigE.setMaxAge( 40L );

    configuration.setRequestSetConfigurations( createList( rootConfig, childConfigC, grandChildConfigD, grandChildConfigE ) );

    CorsRequestSetConfiguration resultConfig = configuration.getRequestConfiguration( createRequest( CHILD_URL_C ) );

    assertEquals( CorsRequestSetConfiguration.ROOT_NAME, resultConfig.getName() );
    assertNull( resultConfig.getMaxAge() );

    // ---

    resultConfig = configuration.getRequestConfiguration( createRequest( GRAND_CHILD_URL_D ) );
    assertEquals( CorsRequestSetConfiguration.ROOT_NAME, resultConfig.getName() );
    assertNull( resultConfig.getMaxAge() );

    // ---

    resultConfig = configuration.getRequestConfiguration( createRequest( GRAND_CHILD_URL_E ) );
    assertEquals( CorsRequestSetConfiguration.ROOT_NAME, resultConfig.getName() );
    assertNull( resultConfig.getMaxAge() );
  }

  @Test
  public void testChildWithCycleToSelfIsDiscardedAlongWithAllDescendants() {
    CorsRequestSetConfigurationPojo rootConfig = createRootRequestConfig();

    // C > D
    // C > E > C
    CorsRequestSetConfigurationPojo childConfigC = createRequestConfig( CHILD_URL_C );
    childConfigC.setParentName( "C" );
    childConfigC.setName( "C" );
    childConfigC.setMaxAge( 20L );

    CorsRequestSetConfigurationPojo grandChildConfigD = createRequestConfig( GRAND_CHILD_URL_D );
    grandChildConfigD.setParentName( "C" );
    grandChildConfigD.setMaxAge( 30L );

    CorsRequestSetConfigurationPojo grandChildConfigE = createRequestConfig( GRAND_CHILD_URL_E );
    grandChildConfigE.setParentName( "C" );
    grandChildConfigE.setName( "E" );
    grandChildConfigE.setMaxAge( 40L );

    configuration.setRequestSetConfigurations( createList( rootConfig, childConfigC, grandChildConfigD, grandChildConfigE ) );

    CorsRequestSetConfiguration resultConfig = configuration.getRequestConfiguration( createRequest( CHILD_URL_C ) );

    assertEquals( CorsRequestSetConfiguration.ROOT_NAME, resultConfig.getName() );
    assertNull( resultConfig.getMaxAge() );

    // ---

    resultConfig = configuration.getRequestConfiguration( createRequest( GRAND_CHILD_URL_D ) );
    assertEquals( CorsRequestSetConfiguration.ROOT_NAME, resultConfig.getName() );
    assertNull( resultConfig.getMaxAge() );

    // ---

    resultConfig = configuration.getRequestConfiguration( createRequest( GRAND_CHILD_URL_E ) );
    assertEquals( CorsRequestSetConfiguration.ROOT_NAME, resultConfig.getName() );
    assertNull( resultConfig.getMaxAge() );
  }

  @Test
  public void testUntilGreatGrandChildWorks() {
    CorsRequestSetConfigurationPojo rootConfig = createRootRequestConfig();

    // Root > C > D
    // Root > C > E > F
    CorsRequestSetConfigurationPojo childConfigC = createRequestConfig( CHILD_URL_C );
    childConfigC.setName( "C" );
    childConfigC.setMaxAge( 20L );

    CorsRequestSetConfigurationPojo grandChildConfigD = createRequestConfig( GRAND_CHILD_URL_D );
    grandChildConfigD.setParentName( "C" );
    grandChildConfigD.setName( "D" );
    grandChildConfigD.setMaxAge( 30L );

    CorsRequestSetConfigurationPojo grandChildConfigE = createRequestConfig( GRAND_CHILD_URL_E );
    grandChildConfigE.setParentName( "C" );
    grandChildConfigE.setName( "E" );
    grandChildConfigE.setMaxAge( 40L );

    CorsRequestSetConfigurationPojo greatGrandChildConfigF = createRequestConfig( GREAT_GRAND_CHILD_URL_F );
    greatGrandChildConfigF.setParentName( "E" );
    greatGrandChildConfigF.setName( "F" );
    greatGrandChildConfigF.setMaxAge( 50L );

    configuration.setRequestSetConfigurations(
      createList( rootConfig, childConfigC, grandChildConfigD, grandChildConfigE, greatGrandChildConfigF ) );

    CorsRequestSetConfiguration resultConfig = configuration.getRequestConfiguration( createRequest( ROOT_URL ) );

    assertEquals( CorsRequestSetConfiguration.ROOT_NAME, resultConfig.getName() );
    assertNull( resultConfig.getMaxAge() );

    // ---

    resultConfig = configuration.getRequestConfiguration( createRequest( CHILD_URL_C ) );

    assertEquals( "C", resultConfig.getName() );
    assertEquals( new Long( 20L ), resultConfig.getMaxAge() );

    // ---

    resultConfig = configuration.getRequestConfiguration( createRequest( GRAND_CHILD_URL_D ) );
    assertEquals( "D", resultConfig.getName() );
    assertEquals( new Long( 30L ), resultConfig.getMaxAge() );

    // ---

    resultConfig = configuration.getRequestConfiguration( createRequest( GRAND_CHILD_URL_E ) );
    assertEquals( "E", resultConfig.getName() );
    assertEquals( new Long( 40L ), resultConfig.getMaxAge() );

    // ---

    resultConfig = configuration.getRequestConfiguration( createRequest( GREAT_GRAND_CHILD_URL_F ) );
    assertEquals( "F", resultConfig.getName() );
    assertEquals( new Long( 50L ), resultConfig.getMaxAge() );
  }

  @Test
  public void testRootConfigWithAParentNameResetsParentName() {
    CorsRequestSetConfigurationPojo rootConfig = createRootRequestConfig();
    rootConfig.setParentName( "Foo" );

    configuration.setRequestSetConfigurations( createList( rootConfig ) );

    CorsRequestSetConfiguration resultConfig = configuration.getRequestConfiguration( createRequest( ROOT_URL ) );

    assertEquals( CorsRequestSetConfiguration.ROOT_NAME, resultConfig.getName() );
    assertNull( resultConfig.getParentName() );
  }
}
