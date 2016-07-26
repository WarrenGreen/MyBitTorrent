import com.google.common.base.Throwables;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by wsgreen on 7/18/16.
 */
public class BEncoding {
  private static final Byte NUMBER_START = "i".getBytes()[0];
  private static final Byte DICTIONARY_START = "d".getBytes()[0];
  private static final Byte LIST_START = "l".getBytes()[0];
  private static final Byte END = "e".getBytes()[0];
  private static final Byte DELIM = ":".getBytes()[0];

  private static final String UTF8 = "UTF-8";

  private static final int MAX_LONG_BYTE_ARRAY_SIZE = 8;

  public static byte[] encode(Map<String, Object> data) {
    List<Byte> byteList = new LinkedList<>();
    for (String key : data.keySet()) {
      byte[] b = key.getBytes();
      for (int i = 0; i < b.length; i++) {
        byteList.add(b[i]);
      }


    }


    return new byte[0];
  }

  public static Map<String, Object> decode(byte[] val) {
    if (Byte.compare(val[0], DICTIONARY_START) != 0 || Byte.compare(val[val.length - 1], END) != 0) { //God I wish this was C++ and I could just trim this array in O(1) space/time...
      throw new InvalidBEncodingFormatException();
    }

    LinkedList<Byte> data = new LinkedList<>();
    for (byte b : val) {
      data.add(b);
    }

    return (Map<String, Object>) decodeObject(data);
  }

  private static Object decodeObject(LinkedList<Byte> data) {
    if (data.getFirst().compareTo(DICTIONARY_START) == 0) {
      return decodeMap(data);
    } else if (data.getFirst().compareTo(NUMBER_START) == 0) {
      return decodeNumber(data);
    } else if (data.getFirst().compareTo(LIST_START) == 0) {
      return decodeList(data);
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

    try {
      return Long.parseLong(new String(ByteUtil.toPrimitiveArray(number), UTF8)); // Yikes
    } catch (UnsupportedEncodingException e) {
      throw Throwables.propagate(e);
    }
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
    while (data.getFirst().compareTo(DELIM.byteValue()) != 0) {
      arrayLength.add(data.getFirst());
      data.removeFirst();
    }
    data.removeFirst();

    int len = -1;
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

  private static boolean isNumeric(byte s) {

    if (s >= 0 && s <= 9) {
      return true;
    } else {
      return false;
    }
  }

  public static void main(String[] args) {
    byte[] b = "d8:announcei32768ee".getBytes();
    Map<String, Object> decoded = decode(b);
    for (String s : decoded.keySet())
      System.out.println(s);
  }

}
