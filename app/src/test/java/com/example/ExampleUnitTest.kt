package com.example

import com.example.statusping.util.HttpReasons
import com.example.statusping.util.deriveFaviconUrl
import com.example.statusping.util.normalizeTarget
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ExampleUnitTest {

  @Test
  fun testNormalizeTarget_specTable() {
    assertEquals("http://192.168.1.1:80/", normalizeTarget("192.168.1.1"))
    assertEquals("http://192.168.1.1:8080/", normalizeTarget("192.168.1.1:8080"))
    assertEquals("http://example.com:80/", normalizeTarget("example.com"))
    assertEquals("https://example.com:443/", normalizeTarget("https://example.com"))
    assertEquals("http://10.0.0.5:80/admin", normalizeTarget("http://10.0.0.5/admin"))
    assertEquals("http://[2001:db8::1]:80/", normalizeTarget("[2001:db8::1]"))
    assertNull(normalizeTarget("ftp://host"))
    assertNull(normalizeTarget("hello world"))
    assertNull(normalizeTarget(""))
    assertNull(normalizeTarget("   "))
  }

  @Test
  fun testDeriveFaviconUrl() {
    assertEquals("http://192.168.1.1:80/favicon.ico", deriveFaviconUrl("http://192.168.1.1:80/"))
    assertEquals("http://192.168.1.1:8080/favicon.ico", deriveFaviconUrl("http://192.168.1.1:8080/"))
    assertEquals("https://example.com:443/favicon.ico", deriveFaviconUrl("https://example.com:443/"))
    assertEquals("http://10.0.0.5:80/favicon.ico", deriveFaviconUrl("http://10.0.0.5:80/admin"))
    assertEquals("http://[2001:db8::1]:80/favicon.ico", deriveFaviconUrl("http://[2001:db8::1]:80/"))
  }

  @Test
  fun testHttpReasons() {
    assertEquals("OK", HttpReasons.getReasonPhrase(200, "OK"))
    assertEquals("Moved Permanently", HttpReasons.getReasonPhrase(301, null))
    assertEquals("Not Found", HttpReasons.getReasonPhrase(404, null))
    assertEquals("Internal Server Error", HttpReasons.getReasonPhrase(500, null))
    assertEquals("I'm a teapot", HttpReasons.getReasonPhrase(418, null))
    assertEquals("Custom Message", HttpReasons.getReasonPhrase(999, "Custom Message"))

    assertEquals("200 OK", HttpReasons.getFormattedStatus(200, "OK"))
    assertEquals("404 Not Found", HttpReasons.getFormattedStatus(404, "Not Found"))
    assertEquals("500 Internal Server Error", HttpReasons.getFormattedStatus(500, "Internal Server Error"))
    assertEquals("2xx Success", HttpReasons.getCategoryLabel(200))
    assertEquals("4xx Client Error", HttpReasons.getCategoryLabel(404))
    
    assert(HttpReasons.getDescription(200).contains("succeeded"))
    assert(HttpReasons.getDescription(404).contains("could not be found"))
  }
}
