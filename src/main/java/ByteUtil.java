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
      ret[i] = bytes.get(i).byteValue();
    }

    return ret;
  }

}
