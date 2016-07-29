import java.util.List;

/**
 *
 * ByteUtil.class
 *
 * Utility class for byte operations
 *
 * @author wsgreen
 */
public class ByteUtil {

  public static byte[] toPrimitiveArray(List<Byte> bytes) {
    byte[] ret = new byte[bytes.size()];

    for (int i = 0; i < bytes.size(); i++) {
      ret[i] = bytes.get(i);
    }

    return ret;
  }

  /**
   * Add given byte array to bytes list in place
   *
   * @param bytes list to be appended to
   * @param toAdd bytes to be added
   */
  public static void addBytes(List<Byte> bytes, byte[] toAdd) {
    for (int i = 0; i < toAdd.length; i++) {
      bytes.add(toAdd[i]);
    }
  }
}
