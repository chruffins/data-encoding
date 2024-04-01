public class DE2B {
    public static void main(String[] args) {
        for (int n = 0; n < 10; n = n + 1) {
            int c = n;
            for (int k = 0; k < 8; k = k + 1) {
                if ((c & 1) == 1) {
                    c = 0xEDB88320 ^ (c >>> 1);
                } else {
                    c = c >>> 1;
                }
            }
            System.out.println(Integer.toHexString(c));
        }
    }
}
