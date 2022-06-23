import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;

public class Affine {
  private static final int MOD = 128;

  public static void main(String[] args) {
    switch (args[0]) {
      case "encrypt":
        encrypt(args[1], args[2], Integer.valueOf(args[3]), Integer.valueOf(args[4]));
        break;
      case "decrypt":
        decrypt(args[1], args[2], Integer.valueOf(args[3]), Integer.valueOf(args[4]));
        break;
      case "decipher":
        decipher(args[1], args[2], args[3]);
        break;
      default:
        break;
    }
  }

  private static int[] findGCD(final int a, final int b) {
    if (b == 0) {
      return new int[] {a, 1, 0};
    }

    int[] gcd = findGCD(b, a % b);
    int d = gcd[0];
    int s = gcd[2];
    int t = gcd[1] - (a / b) * gcd[2];
    return new int[] {d, s, t};
  }


  public static void encrypt(String input, String output, int a, int b) {
    try (InputStream inputStream = new FileInputStream(input);
        OutputStream outputStream = new FileOutputStream(new File(output))) {
      byte[] bytes = inputStream.readAllBytes();

      for (int i = 0; i < bytes.length; i++) {
        outputStream.write(((a * bytes[i]) + b) % MOD);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }



  public static void decrypt(String input, String output, int a, int b) {
    int[] gcdValues = findGCD(a, MOD);
    int gcd = gcdValues[0];
    int modInverse = gcdValues[1];

    if (gcd != 1) {
      System.out.print("The key pair is invalid, please choose another pair.");
      return;
    }

    try {
      InputStream inputStream = new FileInputStream(input);
      OutputStream outputStream = new FileOutputStream(new File(output));
      byte[] bytes = inputStream.readAllBytes();
      inputStream.close();

      for (int i = 0; i < bytes.length; i++) {
        outputStream.write(((bytes[i] - b) * ((modInverse + MOD) % MOD)) % MOD);
      }
      outputStream.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static void decipher(String input, String output, String dictionary) {
    try {
      InputStream inputStream = new FileInputStream(input);
      InputStream dictionaryStream = new FileInputStream(dictionary);
      OutputStream outputStream = new FileOutputStream(new File(output));

      byte[] inputBytes = inputStream.readAllBytes();
      byte[] dictionaryBytes = dictionaryStream.readAllBytes();
      dictionaryStream.close();
      inputStream.close();

      String dictionaryString = new String(dictionaryBytes);
      String[] dictionaryWords = dictionaryString.split("\n");

      Set<String> dictionarySet = new HashSet<String>();
      for (int i = 0; i < dictionaryWords.length; i++) {
        dictionarySet.add(dictionaryWords[i].toLowerCase());
      }

      KeyPair bestKeyPair = new KeyPair(-1, -1, null);
      for (int a = 0; a < MOD; a++) {
        int[] gcdArr = findGCD(a, MOD);
        int gcd = gcdArr[0];
        int modularInv = gcdArr[1];

        if (gcd == 1) {
          for (int b = 0; b < MOD; b++) {
            byte[] decryptedBytes = new byte[inputBytes.length];

            for (int i = 0; i < decryptedBytes.length; i++) {
              decryptedBytes[i] = (byte) (((inputBytes[i] - b) * ((modularInv + MOD) % MOD)) % MOD);
            }

            String decrypted = new String(decryptedBytes).toLowerCase();
            String decryptedPunct = decrypted.toString().replaceAll("\\p{Punct}", "");
            String[] words = decryptedPunct.toString().split(" ");
            KeyPair currentKeyPair = new KeyPair(a, b, decryptedBytes);


            for (int i = 0; i < words.length; i++) {
              if (dictionarySet.contains(words[i])) {
                currentKeyPair.count();
              }
            }

            if (currentKeyPair.compareTo(bestKeyPair) == 1) {
              bestKeyPair = currentKeyPair;
            }
          }
        }
      }
      outputStream.write(bestKeyPair.getDecryptedBytes());
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}


class KeyPair implements Comparable<KeyPair> {
  private int a;
  private int b;
  private int count;
  private byte[] decryptedBytes;

  public KeyPair(final int a, final int b, byte[] decryptedBytes) {
    this.a = a;
    this.b = b;
    this.count = 0;
    this.decryptedBytes = decryptedBytes;
  }

  public byte[] getDecryptedBytes() {
    return decryptedBytes;
  }

  public void count() {
    count++;
  }

  public int getA() {
    return a;
  }

  public int getB() {
    return b;
  }

  @Override
  public int compareTo(KeyPair other) {
    return Integer.compare(this.count, other.count);
  }
}
