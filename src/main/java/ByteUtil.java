import java.util.List;

/**
 * Created by wsgreen on 7/20/16.
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
