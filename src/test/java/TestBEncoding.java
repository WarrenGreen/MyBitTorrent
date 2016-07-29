import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestBEncoding {

  @Test
  public void testDecodingEmptyMap() {
    String emptyMapEncoded = "de";
    Map<String, Object> decoded = BEncoding.decode(emptyMapEncoded.getBytes());
    Assert.assertNotNull(decoded);
    Assert.assertEquals(0, decoded.size());
  }

  @Test
  public void testDecodingEmptyList() {
    String emptyMapEncoded = "d4:listlee";
    Map<String, Object> decoded = BEncoding.decode(emptyMapEncoded.getBytes());
    Assert.assertNotNull(decoded);
    Assert.assertTrue(decoded.get("list") instanceof List);
  }

  @Test
  public void testDecodingNumber() {
    String emptyMapEncoded = "d6:numberi4ee";
    Map<String, Object> decoded = BEncoding.decode(emptyMapEncoded.getBytes());
    Assert.assertNotNull(decoded);
    Assert.assertTrue(decoded.get("number") instanceof Long);
    Assert.assertEquals(4l, decoded.get("number"));
  }

  @Test
  public void testDecodingString() {
    String emptyMapEncoded = "d6:string5:itis!e";
    Map<String, Object> decoded = BEncoding.decode(emptyMapEncoded.getBytes());
    Assert.assertNotNull(decoded);
    Assert.assertTrue(decoded.get("string") instanceof byte[]);
    Assert.assertArrayEquals("itis!".getBytes(), (byte[]) decoded.get("string"));
  }

  @Test
  public void testEncodingEmptyMap() {
    Map<String, Object> map = new HashMap<>();
    byte[] encoded = BEncoding.encode(map);
    Assert.assertEquals("de", new String(encoded));
  }

  @Test
  public void testEncodingEmptyList() {
    Map<String, Object> map = new HashMap<>();
    map.put("list", new ArrayList<>());
    byte[] encoded = BEncoding.encode(map);
    Assert.assertEquals("d4:listlee", new String(encoded));
  }

  @Test
  public void testEncodingNumber() {
    Map<String, Object> map = new HashMap<>();
    map.put("number", 4l);
    byte[] encoded = BEncoding.encode(map);
    //Assert.assertEquals("d6:numberi4ee", new String(encoded)); Figure out how to test this
  }

  @Test
  public void testEncodingString() {
    Map<String, Object> map = new HashMap<>();
    map.put("string", "itis!");
    byte[] encoded = BEncoding.encode(map);
    Assert.assertEquals("d6:string5:itis!e", new String(encoded));
  }
}
