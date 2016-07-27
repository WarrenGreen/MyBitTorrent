import com.google.common.base.Throwables;
import com.google.common.primitives.Longs;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * BEncoding.class
 *
 * Utility class to encode/decode .torrent files
 *
 * @author wsgreen
 */
public class BEncoding {
  private static final Byte NUMBER_START = 'i';
  private static final Byte DICTIONARY_START = 'd';
  private static final Byte LIST_START = 'l';
  private static final Byte END = 'e';
  private static final Byte DELIM = ':';

  private static final String UTF8 = "UTF-8";

  public static byte[] encode(Object data) {
    if (data instanceof Map) {
      return encodeMap(checkedCastMap(data));
    } else if (data instanceof List) {
      return encodeList(checkedCastList(data));
    } else if (data instanceof Long) {
      return encodeNumber(checkedCastLong(data));
    } else if (data instanceof byte[]) {
      return encodeByteArray(checkedCastByteArray(data));
    }

    return null;
  }

  private static byte[] encodeMap(Map<String, Object> data) {
    List<Byte> bytes = new LinkedList<>();
    bytes.add(DICTIONARY_START);
    for (String key : data.keySet()) {
      addBytes(bytes, encodeString(key));
      addBytes(bytes, encode(data.get(key)));
    }
    bytes.add(END);

    return ByteUtil.toPrimitiveArray(bytes);
  }

  private static byte[] encodeList(List<Object> data) {
    List<Byte> bytes = new LinkedList<>();
    bytes.add(LIST_START);
    for (Object o : data) {
      addBytes(bytes, encode(o));
    }
    bytes.add(END);
    return ByteUtil.toPrimitiveArray(bytes);
  }

  private static byte[] encodeNumber(Long data) {
    List<Byte> bytes = new LinkedList<>();
    bytes.add(NUMBER_START);
    addBytes(bytes, Longs.toByteArray(data));
    bytes.add(END);
    return ByteUtil.toPrimitiveArray(bytes);
  }

  private static byte[] encodeByteArray(byte[] data) {
    byte[] length = ByteBuffer.allocate(4).putInt(data.length).array();
    byte[] bytes = new byte[data.length + length.length + 1]; //size of length + size of ':' + size of data
    int index = 0;
    for (; index < length.length; index++) {
      bytes[index] = length[index];
    }
    bytes[index++] = DELIM;
    for (int i = 0; index < bytes.length && i < data.length; index++, i++) {
      bytes[index] = data[i];
    }

    return bytes;
  }

  private static byte[] encodeString(String data) {
    String encoded = String.format("%d:%s", data.length(), data);
    return encoded.getBytes();
  }

  public static Map<String, Object> decode(byte[] data) {
    if (Byte.compare(data[0], DICTIONARY_START) != 0 || Byte.compare(data[data.length - 1], END) != 0) { //God I wish this was C++ and I could just trim this array in O(1) space/time...
      throw new InvalidBEncodingFormatException();
    }

    LinkedList<Byte> bytes = new LinkedList<>();
    for (byte b : data) {
      bytes.add(b);
    }

    return checkedCastMap(decodeObject(bytes));
  }

  private static Object decodeObject(LinkedList<Byte> data) {
    if (data.getFirst().compareTo(DICTIONARY_START) == 0) {
      return decodeMap(data);
    } else if (data.getFirst().compareTo(LIST_START) == 0) {
      return decodeList(data);
    } else if (data.getFirst().compareTo(NUMBER_START) == 0) {
      return decodeNumber(data);
    } else {
      return decodeByteArray(data);
    }
  }

  private static Map<String, Object> decodeMap(LinkedList<Byte> data) {
    data.removeFirst();
    Map<String, Object> map = new HashMap<>();
    while (data.getFirst().compareTo(END) != 0) {
      String key = decodeString(data); // decodeString as key
      Object value = decodeObject(data); // add as value to map
      map.put(key, value);
    }
    data.removeFirst();

    return map;
  }

  private static String decodeString(LinkedList<Byte> data) {
    byte[] byteArray = decodeByteArray(data);
    try {
      return new String(byteArray, UTF8);
    } catch (UnsupportedEncodingException e) {
      throw Throwables.propagate(e);
    }
  }

  private static Long decodeNumber(LinkedList<Byte> data) {
    List<Byte> number = new ArrayList<>();
    data.removeFirst();
    while (data.getFirst().compareTo(END) != 0) {
      number.add(data.getFirst());
      data.removeFirst();
    }
    data.removeFirst();

    // Back-fill 0's
    byte[] formattedNumber = new byte[Long.BYTES];
    Arrays.fill(formattedNumber, (byte) 0);
    for (int i = number.size() - formattedNumber.length, j = 0; i < formattedNumber.length; i++, j++) {
      formattedNumber[i] = number.get(j);
    }

    return Longs.fromByteArray(formattedNumber);
  }

  private static List<Object> decodeList(LinkedList<Byte> data) {
    List<Object> list = new ArrayList<>();
    data.removeFirst();
    while (data.getFirst().compareTo(END) != 0) {
      decodeObject(data);
    }
    data.removeFirst();

    return list;
  }

  private static byte[] decodeByteArray(LinkedList<Byte> data) {
    List<Byte> arrayLength = new ArrayList<>();
    while (data.getFirst().compareTo(DELIM) != 0) {
      arrayLength.add(data.getFirst());
      data.removeFirst();
    }
    data.removeFirst();

    int len;
    try {
      len = Integer.parseInt(new String(ByteUtil.toPrimitiveArray(arrayLength), UTF8)); // Yikes
    } catch (UnsupportedEncodingException e) {
      throw Throwables.propagate(e);
    }

    List<Byte> byteArray = new ArrayList<>();
    for (int i = 0; i < len; i++) {
      byteArray.add(data.getFirst());
      data.removeFirst();
    }

    return ByteUtil.toPrimitiveArray(byteArray);
  }

  private static Map<String, Object> checkedCastMap(Object o) {
    if (o instanceof Map) {
      return (Map<String, Object>) o;
    }
    return null;
  }

  private static List<Object> checkedCastList(Object o) {
    if (o instanceof List) {
      return (List<Object>) o;
    }
    return null;
  }

  private static Long checkedCastLong(Object o) {
    if (o instanceof Long) {
      return (Long) o;
    }
    return null;
  }

  private static byte[] checkedCastByteArray(Object o) {
    if (o instanceof byte[]) {
      return (byte[]) o;
    }
    return null;
  }

  private static void addBytes(List<Byte> bytes, byte[] toAdd) {
    for (int i = 0; i < toAdd.length; i++) {
      bytes.add(toAdd[i]);
    }
  }

  public static void main(String[] args) {
    Map<String, Object> d = new HashMap<>();
    d.put("announce", 32768l);
    byte[] bytes = encode(d);
    System.out.println(new String(bytes));

    byte[] b = "d8:announcei32768ee".getBytes();
    Map<String, Object> decoded = decode(bytes);
    for (String s : decoded.keySet())
      System.out.println(s + ": " + decoded.get(s));


  }

}
