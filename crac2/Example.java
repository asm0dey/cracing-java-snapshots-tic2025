import java.util.stream.IntStream;

public class Example {
    public static void main(String args[]) throws InterruptedException {
        // This is a part of the saved state
        long startTime = System.currentTimeMillis();
        for (int counter : IntStream.range(1, 10000).toArray()) {
            Thread.sleep(1000);
            long currentTime = System.currentTimeMillis();
            System.out.println("Counter: " + counter + "(passed " + (currentTime - startTime) + " ms)");
            startTime = currentTime;
        }
    }
}
